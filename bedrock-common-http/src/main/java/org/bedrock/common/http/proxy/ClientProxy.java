package org.bedrock.common.http.proxy;

import org.bedrock.common.code.util.StringUtil;
import org.bedrock.common.http.HttpConfiguration;
import org.bedrock.common.http.annotation.*;
import org.bedrock.common.http.converter.Converter;
import org.bedrock.common.http.converter.ParameterConverter;
import org.bedrock.common.http.enums.ProtocolVersion;
import org.bedrock.common.http.interceptor.Interceptor;
import org.bedrock.common.http.model.HttpProxyMeta;
import org.bedrock.common.http.model.RequestMeta;
import org.bedrock.common.http.ssl.SslContextProvider;
import org.bedrock.common.http.support.ClientMethod;
import org.bedrock.common.http.support.HttpSupport;

import java.io.Serial;
import java.io.Serializable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * JDK动态代理核心实现类，专用于处理接口默认方法（Default Method）和普通业务方法的调用
 * 核心能力：
 * 1. 跨版本（Java8/9+）获取接口默认方法的MethodHandle，解决模块化访问权限问题
 * 2. 缓存方法与调用器的映射，避免重复创建MethodHandle/ClientMethod，提升性能
 * 3. 过滤Object类通用方法（toString/equals等），避免死循环和业务逻辑干扰
 * 4. 适配普通业务方法执行：通过ClientMethod封装业务逻辑，统一方法调用入口
 *
 * @param <T> 被代理的目标接口类型
 */
public class ClientProxy<T> implements InvocationHandler, Serializable {

	@Serial
	private static final long serialVersionUID = 1L; // 序列化版本号，Serializable接口必需

	/**
	 * 全局配置对象（支撑业务方法执行的核心配置）
	 * 可配置项：接口调用超时、服务地址、序列化规则、拦截器链等，为PlainMethodInvoker提供执行参数
	 */
	private final HttpConfiguration config;

	/**
	 * 被代理的目标接口Class对象（如UserService.class）
	 * 约束：必须为接口类型，JDK动态代理仅支持接口代理
	 */
	private final Class<T> interfaceClass;

	/**
	 * 请求元数据对象，用于封装接口调用的必要信息（如接口地址、请求头）
	 */
	private final RequestMeta requestMeta = new RequestMeta();

	/**
	 * Java 9+ 新增的MethodHandles.privateLookupIn方法的反射引用
	 * 用于模块化环境下获取私有访问权限的MethodHandles.Lookup对象，解决Java9+访问权限收紧问题
	 */
	private static final Method privateLookupInMethod;

	/**
	 * Java 8中创建MethodHandles.Lookup时允许的访问模式掩码
	 * 包含：私有、受保护、包级、公有方法的访问权限，确保能访问接口默认方法
	 */
	private static final int ALLOWED_MODES = MethodHandles.Lookup.PRIVATE | MethodHandles.Lookup.PROTECTED |
		MethodHandles.Lookup.PACKAGE | MethodHandles.Lookup.PUBLIC;

	/**
	 * Java 8中MethodHandles.Lookup类的构造器反射引用（构造器为包私有，需反射实例化）
	 */
	private static final Constructor<MethodHandles.Lookup> lookupConstructor;

	/**
	 * 方法与调用器的缓存Map，核心作用：
	 * 1. 避免重复为同一个Method创建ClientMethodInvoker（DefaultMethodInvoker/PlainMethodInvoker）
	 * 2. 缓存MethodHandle（默认方法）和ClientMethod（普通方法），减少反射/初始化开销
	 * 3. 需传入线程安全实现（如ConcurrentHashMap），适配多线程调用场景
	 */
	private final Map<Method, ClientMethodInvoker> methodCache;

	/**
	 * 构造器：初始化代理的全局配置、目标接口和方法缓存
	 *
	 * @param config         全局配置对象（为普通业务方法提供执行配置，不能为空）
	 * @param interfaceClass 被代理的目标接口Class对象（不能为空，且必须是接口类型）
	 * @param methodCache    方法-调用器缓存Map，建议传入ConcurrentHashMap保证线程安全
	 */
	public ClientProxy(HttpConfiguration config, Class<T> interfaceClass, Map<Method, ClientMethodInvoker> methodCache) {
		this.config = config;
		this.interfaceClass = interfaceClass;
		this.methodCache = methodCache;
		analyticalBedrockClientClass();
		analyticalLogConfClass();
		analyticalHttpProxyClass();
	}

	/**
	 * 静态初始化块：类加载时完成版本适配的核心反射对象初始化
	 * 逻辑：
	 * 1. 先尝试获取Java 9+的privateLookupIn方法 → 存在则为Java9+环境
	 * 2. 若获取失败（Java8），则反射获取MethodHandles.Lookup的构造器并打破访问限制
	 * 特点：仅执行一次，避免运行时重复反射，提升性能
	 */
	static {
		// 步骤1：尝试获取Java 9+的privateLookupIn方法
		Method privateLookupIn;
		try {
			privateLookupIn = MethodHandles.class.getMethod(
				"privateLookupIn",
				Class.class,
				MethodHandles.Lookup.class
			);
		} catch (NoSuchMethodException e) {
			// 捕获异常→当前为Java8环境，赋值为null
			privateLookupIn = null;
		}
		privateLookupInMethod = privateLookupIn;

		// 步骤2：若为Java8环境，初始化MethodHandles.Lookup构造器
		Constructor<MethodHandles.Lookup> lookup = null;
		if (privateLookupInMethod == null) {
			// JDK 1.8 环境处理逻辑
			try {
				// 获取Lookup的包私有构造器：Lookup(Class<?> lookupClass, int allowedModes)
				lookup = MethodHandles.Lookup.class.getDeclaredConstructor(Class.class, int.class);
				// 打破访问限制，允许反射实例化包私有构造器
				lookup.setAccessible(true);
			} catch (NoSuchMethodException e) {
				// 极端异常：既无Java9+的privateLookupIn，也无Java8的Lookup构造器→环境不兼容
				throw new IllegalStateException(
					"There is neither 'privateLookupIn(Class, Lookup)' nor 'Lookup(Class, int)' method in java.lang.invoke.MethodHandles.",
					e
				);
			} catch (Exception e) {
				// 其他异常（如SecurityException）→构造器赋值为null，交给上层处理
				lookup = null;
			}
		}
		lookupConstructor = lookup;
	}

	/**
	 * InvocationHandler核心方法：代理实例的所有方法调用都会触发此方法
	 * 核心逻辑分支：
	 * 1. 处理Object类通用方法（toString/equals/hashCode等）→ 避免死循环和业务干扰
	 * 2. 处理接口方法（默认方法/普通业务方法）→ 从缓存获取对应调用器执行
	 *
	 * @param proxy  JVM动态生成的代理实例（实现了interfaceClass接口）
	 * @param method 当前被调用的方法对象（Object方法/接口默认方法/接口普通方法）
	 * @param args   方法调用的参数数组（无参则为null）
	 * @return 方法执行结果
	 * @throws Throwable 方法执行过程中抛出的任意异常
	 */
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		// 分支1：处理Object类声明的通用方法（避免走业务逻辑，防止递归死循环）
		if (Object.class.equals(method.getDeclaringClass())) {
			// 调用当前InvocationHandler实例的Object方法（而非代理实例，避免触发代理回调）
			return method.invoke(this, args);
		}
		// 分支2：处理接口方法（默认方法/普通业务方法）→ 从缓存获取调用器并执行
		return cachedInvoker(method)
			.invoke(proxy, method, args);
	}

	/**
	 * 获取方法对应的调用器（优先从缓存获取，无则创建）
	 * 核心逻辑：
	 * 1. 缓存命中→直接返回已有的ClientMethodInvoker
	 * 2. 缓存未命中→根据方法类型创建对应调用器并缓存：
	 * - 默认方法：创建DefaultMethodInvoker（基于MethodHandle执行）
	 * - 普通业务方法：创建PlainMethodInvoker（基于ClientMethod执行）
	 *
	 * @param method 当前要执行的方法对象
	 * @return 方法对应的调用器（默认方法→DefaultMethodInvoker；普通方法→PlainMethodInvoker）
	 * @throws Throwable 创建调用器过程中抛出的异常（反射/MethodHandle/ClientMethod初始化异常）
	 */
	private ClientMethodInvoker cachedInvoker(Method method) throws Throwable {
		try {
			// computeIfAbsent：原子性操作，多线程下避免重复创建调用器
			return methodCache.computeIfAbsent(method, m -> {
				// 分支1：接口默认方法→创建DefaultMethodInvoker
				if (method.isDefault()) {
					try {
						// 版本适配：Java8用lookup构造器，Java9+用privateLookupIn
						if (privateLookupInMethod == null) {
							return new DefaultMethodInvoker(getMethodHandleJava8(method));
						}
						return new DefaultMethodInvoker(getMethodHandleJava9(method));
					} catch (IllegalAccessException | InstantiationException | InvocationTargetException |
							 NoSuchMethodException e) {
						throw new RuntimeException(e);
					}
				}
				// 分支2：接口普通业务方法→创建PlainMethodInvoker
				// ClientMethod封装了方法执行的核心业务逻辑（如远程调用、参数解析等）
				return new PlainMethodInvoker(new ClientMethod(requestMeta, method, config));
			});
		} catch (RuntimeException re) {
			// 解包RuntimeException的根因，便于上层精准处理异常
			Throwable cause = re.getCause();
			throw cause == null ? re : cause;
		}
	}

	void analyticalBedrockClientClass() {
		requestMeta.setInterfaceClass(interfaceClass);
		BedrockClient annotation = interfaceClass.getAnnotation(BedrockClient.class);
		if (annotation == null) {
			return;
		}
		String url = annotation.url();
		if (StringUtil.isNotBlank(url)) {
			requestMeta.setUrl(HttpSupport.resolvePlaceholders(url));
		}
		String basePath = annotation.basePath();
		if (StringUtil.isNotBlank(basePath)) {
			requestMeta.setBasePath(HttpSupport.resolvePlaceholders(basePath));
		}
		/**
		 * 初始化拦截器信息
		 */
		Class<? extends Interceptor>[] interceptors = annotation.interceptors();
		for (Class<? extends Interceptor> interceptor : interceptors) {
			requestMeta.addInterceptor(config.getInterceptor(interceptor));
		}

		/**
		 * 初始化header信息
		 */
		Headers headers = annotation.headers();
		for (Header header : headers.value()) {
			requestMeta.addHeader(header.name(), HttpSupport.resolvePlaceholders(header.defaultValue()));
		}
		/**
		 * 添加参数转换器
		 */
		Class<? extends ParameterConverter>[] classes = annotation.parameterConverters();
		for (Class<? extends ParameterConverter> clazz : classes) {
			requestMeta.addParameterConverter(config.getParameterConverter(clazz));
		}
		/**
		 * 添加转换器
		 */
		Class<? extends Converter> converter = annotation.converter();
		if (converter != Converter.class) {
			requestMeta.setConverter(config.getConverter(converter));
		}
		/**
		 * 设置Content-Type
		 */
		String contentType = annotation.contentType();
		if (StringUtil.isNotBlank(contentType)) {
			requestMeta.setContentType(contentType);
		}
		/**
		 * 设置协议版本
		 */
		ProtocolVersion protocolVersion = annotation.protocolVersion();
		if (protocolVersion != ProtocolVersion.NONE) {
			requestMeta.setProtocolVersion(protocolVersion);
		}

		/**
		 * 禁用SSL验证
		 */
		String disableSslValidation = annotation.disableSslValidation();
		if (StringUtil.isNotBlank(disableSslValidation)) {
			requestMeta.enableSslValidation(Boolean.parseBoolean(disableSslValidation));
		}

		/**
		 * 设置SSLContext提供者
		 */
		Class<? extends SslContextProvider> sslContextProvider = annotation.sslContextProvider();
		if (sslContextProvider != SslContextProvider.class) {
			requestMeta.sslContextProvider(config.getSslContextProvider(sslContextProvider));
		}
	}

	void analyticalLogConfClass() {
		LogConf annotation = interfaceClass.getAnnotation(LogConf.class);
		if (annotation == null) {
			return;
		}
		requestMeta.setLog(HttpSupport.buildLog(annotation));
	}

	void analyticalHttpProxyClass() {
		HttpProxy annotation = interfaceClass.getAnnotation(HttpProxy.class);
		if (annotation == null) {
			return;
		}
		HttpProxyMeta httpProxyMeta = HttpProxyMeta.create(annotation.type(), annotation.host(), annotation.port());
		httpProxyMeta.setUsername(annotation.username());
		httpProxyMeta.setPassword(annotation.password());
		/**
		 * 初始化header信息
		 */
		Headers headers = annotation.headers();
		for (Header header : headers.value()) {
			httpProxyMeta.addHeader(header.name(), HttpSupport.resolvePlaceholders(header.defaultValue()));
		}
		requestMeta.httpProxyMeta(httpProxyMeta);
	}

	/**
	 * 方法调用器接口：定义统一的方法执行规范
	 * 适配JDK动态代理的invoke方法签名，实现不同类型方法（默认/普通）的执行逻辑统一
	 */
	interface ClientMethodInvoker {

		/**
		 * 执行目标方法
		 *
		 * @param proxy  代理实例（默认方法需绑定this引用；普通方法可忽略）
		 * @param method 目标方法对象（仅用于日志/监控，核心逻辑由调用器内部封装）
		 * @param args   方法调用的参数数组（无参则为null）
		 * @return 方法执行结果
		 * @throws Throwable 执行过程中的任意异常（业务异常/系统异常）
		 */
		Object invoke(Object proxy, Method method, Object[] args) throws Throwable;
	}

	/**
	 * Java 9+ 环境下获取接口默认方法的MethodHandle
	 * 核心解决：Java9模块化（JPMS）后，MethodHandles.Lookup的访问权限收紧问题
	 *
	 * @param method 目标默认方法对象
	 * @return 方法对应的MethodHandle（JVM优化的可执行方法句柄）
	 * @throws NoSuchMethodException     方法未找到（如方法名/参数类型不匹配）
	 * @throws IllegalAccessException    访问权限不足（如模块化未开放权限）
	 * @throws InvocationTargetException 反射调用privateLookupIn方法异常
	 */
	private MethodHandle getMethodHandleJava9(Method method)
		throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		// 获取方法所属的接口Class对象
		final Class<?> declaringClass = method.getDeclaringClass();
		// 1. 调用privateLookupIn获取具备私有访问权限的Lookup对象（突破模块化限制）
		// 2. 通过findSpecial查找接口默认方法的MethodHandle（专用于特殊方法：默认/私有方法）
		return ((MethodHandles.Lookup) privateLookupInMethod.invoke(
			null, // privateLookupIn是静态方法，第一个参数为null
			declaringClass,
			MethodHandles.lookup()
		)).findSpecial(
			declaringClass,          // 方法所属的接口类
			method.getName(),        // 目标默认方法名
			// 构建方法类型（返回值类型 + 参数类型列表）
			MethodType.methodType(method.getReturnType(), method.getParameterTypes()),
			declaringClass           // 接收者类（接口本身，默认方法的执行主体）
		);
	}

	/**
	 * Java 8 环境下获取接口默认方法的MethodHandle
	 * 核心逻辑：反射实例化包私有Lookup构造器，打破访问权限限制
	 *
	 * @param method 目标默认方法对象
	 * @return 方法对应的MethodHandle（JVM优化的可执行方法句柄）
	 * @throws IllegalAccessException    访问权限不足（如构造器未放开setAccessible）
	 * @throws InstantiationException    Lookup类实例化失败（如构造器参数错误）
	 * @throws InvocationTargetException 反射调用Lookup构造器异常
	 */
	private MethodHandle getMethodHandleJava8(Method method)
		throws IllegalAccessException, InstantiationException, InvocationTargetException {
		// 获取方法所属的接口Class对象
		final Class<?> declaringClass = method.getDeclaringClass();
		// 1. 实例化Lookup对象（指定目标接口 + 全量访问模式）
		// 2. unreflectSpecial：将反射Method转换为MethodHandle（专用于特殊方法）
		return lookupConstructor.newInstance(declaringClass, ALLOWED_MODES)
			.unreflectSpecial(method, declaringClass);
	}

	/**
	 * 默认方法调用器：实现ClientMethodInvoker，专门处理接口默认方法的高效执行
	 * 核心依赖MethodHandle（JVM层面的方法引用），相比反射执行效率提升3-5倍
	 *
	 * @param methodHandle 目标默认方法的MethodHandle（可执行的方法句柄，JVM预编译优化）
	 */
	private record DefaultMethodInvoker(MethodHandle methodHandle) implements ClientMethodInvoker {

		/**
		 * 执行接口默认方法
		 * 核心逻辑：绑定代理实例作为this引用 → 传入参数 → 执行方法句柄
		 *
		 * @param proxy  代理实例（默认方法需要this引用，绑定后才能执行）
		 * @param method 目标默认方法对象（仅日志/监控使用，无核心逻辑）
		 * @param args   方法调用参数数组（无参则为null）
		 * @return 方法执行结果
		 * @throws Throwable 执行过程中的任意异常（默认方法本身抛出的异常）
		 */
		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			// 1. bindTo(proxy)：将MethodHandle绑定到代理实例（补充this引用）
			// 2. invokeWithArguments(args)：传入参数执行方法，自动适配参数类型
			return methodHandle.bindTo(proxy).invokeWithArguments(args);
		}
	}

	/**
	 * 普通业务方法调用器：实现ClientMethodInvoker，处理接口普通方法的执行
	 * 核心依赖ClientMethod封装业务逻辑（如远程调用、参数校验、结果解析等）
	 *
	 * @param clientMethod 封装了普通业务方法执行逻辑的ClientMethod实例
	 *                     包含：接口信息、方法元数据、全局配置，是业务方法执行的核心载体
	 */
	private record PlainMethodInvoker(ClientMethod clientMethod) implements ClientMethodInvoker {

		/**
		 * 执行接口普通业务方法
		 * 核心逻辑：委托给ClientMethod.execute执行具体业务逻辑（解耦调用器与业务逻辑）
		 *
		 * @param proxy  代理实例（普通方法无需绑定this，仅兼容接口签名）
		 * @param method 目标业务方法对象（仅日志/监控使用）
		 * @param args   方法调用参数数组（无参则为null）
		 * @return 业务方法执行结果（如远程调用返回值、本地逻辑返回值）
		 * @throws Throwable 业务执行过程中的异常（如网络异常、参数校验异常）
		 */
		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			// 委托ClientMethod执行具体业务逻辑，解耦调用器与业务实现
			return clientMethod.execute(args);
		}
	}
}
