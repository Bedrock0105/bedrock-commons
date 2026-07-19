package org.bedrock.common.code.reflects;

import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.stream.IntStream;

/**
 * 方法元数据描述类
 * <p>核心特性：</p>
 * <ul>
 *   <li>1. 基于原生 {@link Method} 封装，提供方法基础信息、注解、参数、返回值的一站式获取能力</li>
 *   <li>2. 懒加载机制：参数/返回值的 {@link MethodParameter}、所属类、方法名等采用 volatile 懒加载，提升性能</li>
 *   <li>3. 线程安全：成员变量仅懒加载初始化，无写操作（除初始化），天然线程安全</li>
 *   <li>4. 多构造器支持：支持直接传入 Method、通过「类+方法名」反射获取 Method、拷贝构造器三种方式</li>
 *   <li>5. 规范返回值：所有方法均做非空校验，避免 NPE（原生 Method 由构造器保证非 null）</li>
 *   <li>6. 注解解析：支持原生反射注解、Spring 合并注解（元注解/组合注解/@{@link org.springframework.core.annotation.AliasFor}）双模式</li>
 * </ul>
 *
 * @since 1.0.0
 */
public class MethodMeta {

	/**
	 * 原生Method对象（构造器保证非null，核心数据源）
	 */
	private final Method method;

	/**
	 * 方法所属类（懒加载，volatile保证多线程可见性）
	 */
	private volatile Class<?> declaringClass;

	/**
	 * 方法所有参数的 Spring 增强封装（{@link MethodParameter}），懒加载初始化
	 */
	private volatile Map<Integer, MethodParameter> parametersMap;

	/**
	 * 方法所有参数的 Class 类型数组（懒加载，volatile保证多线程可见性）
	 */
	private volatile Class<?>[] parameterTypes;

	/**
	 * 方法返回值的 Spring 增强封装（{@link MethodParameter}），索引固定为 -1，懒加载初始化
	 */
	private volatile MethodParameter returnType;

	/**
	 * 方法返回值类型（缓存），等价于 {@link Method#getReturnType()}；
	 * volatile 修饰保证多线程可见性
	 */
	private volatile Class<?> returnClassType;

	/**
	 * 方法泛型返回值类型（缓存），等价于 {@link Method#getGenericReturnType()}；
	 * volatile 修饰保证多线程可见性
	 */
	@Nullable
	private volatile Type genericReturnType;

	/**
	 * 方法名称（懒加载，避免重复调用 {@link Method#getName()}）
	 */
	private volatile String methodName;

	/**
	 * 方法参数数量（构造时初始化，避免重复调用 {@link Method#getParameterCount()}）
	 */
	private final int parameterCount;

	/**
	 * 方法上的所有注解（缓存），等价于 {@link Method#getAnnotations()}；
	 * volatile 修饰保证多线程可见性，null 表示未初始化
	 */
	@Nullable
	private volatile Annotation[] methodAnnotations;

	// ======================== 构造器 ========================

	/**
	 * 基础构造器：直接传入原生 {@link Method} 创建元数据
	 *
	 * @param method 原生Method对象，不可为null
	 * @throws IllegalArgumentException 如果 method 为 null，抛出非法参数异常
	 */
	public MethodMeta(Method method) {
		Assert.notNull(method, "Method must not be null");
		this.method = method;
		this.parameterCount = method.getParameterCount();
	}

	/**
	 * 反射构造器：通过「所属类 + 方法名」反射获取 Method 创建元数据
	 * <p>仅支持无参方法，若需匹配带参数的方法，请自行反射获取 Method 后使用 {@link #MethodMeta(Method)} 构造</p>
	 *
	 * @param declaringClass 方法所属的类，不可为null
	 * @param methodName     方法名称，不可为空字符串
	 * @throws IllegalArgumentException 1. 类/方法名为空；2. 指定类中无此方法时抛出
	 */
	public MethodMeta(Class<?> declaringClass, String methodName) {
		Assert.notNull(declaringClass, "declaringClass must not be null");
		Assert.hasText(methodName, "methodName must not be empty");
		Method method = ReflectionUtils.findMethod(declaringClass, methodName);
		if (method == null) {
			throw new IllegalArgumentException("No such method [" + methodName + "] in class [" + declaringClass.getName() + "]");
		}
		this.method = method;
		this.declaringClass = declaringClass; // 提前赋值，避免后续懒加载
		this.methodName = methodName; // 提前赋值，避免后续懒加载
		this.parameterCount = method.getParameterCount();
	}

	/**
	 * 拷贝构造器：基于已有 {@link MethodMeta} 实例创建新实例（复用已懒加载的缓存数据）
	 *
	 * @param original 原始 MethodMeta 实例，不可为null
	 * @throws IllegalArgumentException 如果 original 为 null，抛出非法参数异常
	 */
	public MethodMeta(MethodMeta original) {
		Assert.notNull(original, "Original MethodMeta must not be null");
		this.method = original.method;
		this.declaringClass = original.declaringClass;
		this.methodName = original.methodName;
		this.parameterCount = original.parameterCount;
		this.parametersMap = original.parametersMap; // 复用已初始化的参数列表
		this.returnType = original.returnType; // 复用已初始化的返回值
		this.parameterTypes = original.parameterTypes;
		this.returnClassType = original.returnClassType;
		this.genericReturnType = original.genericReturnType;
		this.methodAnnotations = original.methodAnnotations;
	}

	// ======================== 静态工厂方法 ========================

	/**
	 * 静态工厂方法：通过原生 {@link Method} 创建 {@link MethodMeta}
	 * <p>语义更友好，替代直接 new 构造器</p>
	 *
	 * @param method 原生Method对象，不可为null
	 * @return MethodMeta 实例
	 * @throws IllegalArgumentException 如果 method 为 null，抛出非法参数异常
	 */
	public static MethodMeta forMethod(Method method) {
		return new MethodMeta(method);
	}

	/**
	 * 静态工厂方法：通过「所属类 + 方法名」反射创建 {@link MethodMeta}
	 * <p>仅支持无参方法，语义更友好，替代直接 new 构造器</p>
	 *
	 * @param containingClass 方法所属的类，不可为null
	 * @param methodName      方法名称，不可为空字符串
	 * @return MethodMeta 实例
	 * @throws IllegalArgumentException 1. 类/方法名为空；2. 指定类中无此方法时抛出
	 */
	public static MethodMeta forMethod(Class<?> containingClass, String methodName) {
		return new MethodMeta(containingClass, methodName);
	}

	// ======================== 基础信息获取 ========================

	/**
	 * 获取原生 {@link Method} 对象
	 *
	 * @return 原生Method实例，非null
	 */
	public Method getMethod() {
		return method;
	}

	/**
	 * 获取方法所属的类（懒加载，首次调用初始化后缓存）
	 *
	 * @return 方法所属的 Class 实例，非null
	 */
	public Class<?> getDeclaringClass() {
		Class<?> declaringClass = this.declaringClass;
		if (declaringClass == null) {
			declaringClass = method.getDeclaringClass();
			this.declaringClass = declaringClass;
		}
		return declaringClass;
	}

	/**
	 * 获取方法名称（懒加载，首次调用初始化后缓存）
	 *
	 * @return 方法名称，非空字符串
	 */
	public String getMethodName() {
		String methodName = this.methodName;
		if (methodName == null) {
			methodName = method.getName();
			this.methodName = methodName;
		}
		return methodName;
	}

	/**
	 * 获取方法参数数量
	 *
	 * @return 参数数量，非负整数（无参返回0）
	 */
	public int getParameterCount() {
		return parameterCount;
	}

	/**
	 * 获取方法的修饰符（如 public/private/static/final 等）
	 *
	 * @return 修饰符的 int 常量，参考 {@link Modifier} 类的常量定义
	 * @see Modifier#PUBLIC
	 * @see Modifier#STATIC
	 * @see Modifier#FINAL
	 */
	public int getModifiers() {
		return method.getModifiers();
	}

	/**
	 * 判断方法是否为静态方法
	 *
	 * @return true = 静态方法（static 修饰），false = 实例方法
	 * @see Modifier#isStatic(int)
	 */
	public boolean isStatic() {
		return Modifier.isStatic(method.getModifiers());
	}

	/**
	 * 判断方法是否为最终方法（final 修饰，不可被重写）
	 *
	 * @return true = final 方法，false = 非 final 方法
	 * @see Modifier#isFinal(int)
	 */
	public boolean isFinal() {
		return Modifier.isFinal(method.getModifiers());
	}

	/**
	 * 判断方法是否为默认方法
	 *
	 * @return true = 默认方法，false = 非 默认方法
	 * @see Method#isDefault()
	 */
	public boolean isDefault() {
		return method.isDefault();
	}

	/**
	 * 获取方法的注解承载元素（即方法本身）
	 *
	 * @return 方法对应的 {@link AnnotatedElement} 实例，非null
	 */
	public AnnotatedElement getAnnotatedElement() {
		return this.method;
	}

	// ======================== 方法注解相关 ========================

	/**
	 * 判断方法上是否存在指定注解（原生反射逻辑，不处理合并注解/元注解）
	 *
	 * @param annotationType 注解类型，不可为null
	 * @return true=存在该注解，false=不存在
	 * @see AnnotatedElement#isAnnotationPresent(Class)
	 */
	public boolean hasMethodAnnotation(Class<? extends Annotation> annotationType) {
		return getAnnotatedElement().isAnnotationPresent(annotationType);
	}

	/**
	 * 获取方法上的指定注解实例（原生反射逻辑，不处理合并注解/元注解）
	 *
	 * @param annotationType 注解类型，不可为null
	 * @param <A>            注解泛型类型
	 * @return 注解实例（方法上无此注解时返回null）
	 * @see AnnotatedElement#getAnnotation(Class)
	 */
	public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
		return getAnnotatedElement().getAnnotation(annotationType);
	}

	/**
	 * 判断方法上是否存在指定合并注解（支持元注解、组合注解、@{@link org.springframework.core.annotation.AliasFor} 别名）
	 * <p>区别于 {@link #hasMethodAnnotation(Class)}：可解析注解上的元注解（如 @GetMapping 包含的 @RequestMapping）</p>
	 *
	 * @param annotationType 注解类型，不可为null
	 * @return true=存在（直接/元/组合注解），false=不存在
	 * @see AnnotatedElementUtils#isAnnotated(AnnotatedElement, Class)
	 */
	public boolean hasMergedAnnotation(Class<? extends Annotation> annotationType) {
		Assert.notNull(annotationType, "Annotation type must not be null");
		return AnnotatedElementUtils.isAnnotated(method, annotationType);
	}

	/**
	 * 获取方法上的指定合并注解实例（支持元注解、组合注解、@{@link org.springframework.core.annotation.AliasFor} 别名）
	 * <p>区别于 {@link #getAnnotation(Class)}：可解析注解上的元注解（如 @GetMapping 包含的 @RequestMapping）</p>
	 *
	 * @param annotationType 注解类型，不可为null
	 * @param <A>            注解泛型类型
	 * @return 合并后的注解实例（无则返回null）
	 * @see AnnotatedElementUtils#findMergedAnnotation(AnnotatedElement, Class)
	 */
	public <A extends Annotation> A getMergedAnnotation(Class<A> annotationType) {
		Assert.notNull(annotationType, "Annotation type must not be null");
		return AnnotatedElementUtils.findMergedAnnotation(method, annotationType);
	}

	/**
	 * 获取方法上的所有注解（原生反射，不含继承/合并注解）
	 * <p>优先返回缓存值，未初始化时通过 {@link Method#getAnnotations()} 解析并缓存；
	 * 双重检查锁定保证线程安全</p>
	 *
	 * @return 注解数组（无注解时返回空数组，非null）
	 */
	public Annotation[] getAnnotations() {
		Annotation[] methodAnnotations = this.methodAnnotations;
		if (methodAnnotations == null) {
			methodAnnotations = method.getAnnotations();
			this.methodAnnotations = methodAnnotations;
		}
		return methodAnnotations;
	}

	// ======================== 方法参数相关 ========================

	/**
	 * 获取方法所有参数的 Spring 增强封装（{@link MethodParameter}）
	 * <p>懒加载初始化，首次调用后缓存，后续调用直接返回缓存值</p>
	 *
	 * @return 参数的 MethodParameter 列表，非null（无参返回空列表）
	 */
	public Collection<MethodParameter> getParameters() {
		return getParametersMap().values();
	}

	/**
	 * 获取方法所有参数的 Spring 增强封装（{@link MethodParameter}）
	 * <p>懒加载初始化，首次调用后缓存，后续调用直接返回缓存值</p>
	 *
	 * @return 参数的 MethodParameter 列表，非null（无参返回空列表）
	 */
	public Map<Integer, MethodParameter> getParametersMap() {
		Map<Integer, MethodParameter> parametersMap = this.parametersMap;
		if (parametersMap == null) {
			parametersMap = IntStream.range(0, parameterCount)
				.mapToObj(index -> new MethodParameter(method, index))
				.collect(Collectors.toMap(MethodParameter::getParameterIndex, parameter -> parameter));
			this.parametersMap = parametersMap;
		}
		return parametersMap;
	}

	/**
	 * 获取方法所有参数的类型数组
	 *
	 * @return 参数类型数组，非null（无参返回空数组）
	 */
	public Class<?>[] getParameterTypes() {
		Class<?>[] parameterTypes = this.parameterTypes;
		if (parameterTypes == null) {
			parameterTypes = method.getParameterTypes();
			this.parameterTypes = parameterTypes;
		}
		return parameterTypes;
	}

	/**
	 * 获取携带指定注解的参数列表（基于 Spring {@link MethodParameter} 解析）
	 *
	 * @param annotationType 注解类型，可为null（为null时返回空列表）
	 * @return 带指定注解的参数列表，非null（无匹配参数返回空列表）
	 */
	public List<MethodParameter> getParametersWithAnnotation(Class<? extends Annotation> annotationType) {
		List<MethodParameter> result = new ArrayList<>();
		if (annotationType == null) {
			return result;
		}
		for (MethodParameter parameter : getParameters()) {
			if (parameter.hasParameterAnnotation(annotationType)) {
				result.add(parameter);
			}
		}
		return result;
	}

	// ======================== 返回值相关 ========================

	/**
	 * 获取方法返回值的 Spring 增强封装（{@link MethodParameter}）
	 * <p>懒加载初始化，首次调用后缓存，后续调用直接返回缓存值</p>
	 * <p>注意：返回值的 {@link MethodParameter} 索引固定为 -1，仅用于返回值元数据解析</p>
	 *
	 * @return 返回值的 MethodParameter 实例，非null
	 */
	public MethodParameter getReturnType() {
		MethodParameter returnType = this.returnType;
		if (returnType == null) {
			returnType = new MethodParameter(method, -1); // -1 固定为返回值索引
			this.returnType = returnType;
		}
		return returnType;
	}

	/**
	 * 获取方法返回值的类型
	 * <p>优先返回缓存值，未初始化时通过 {@link Method#getReturnType()} 解析并缓存；
	 * 双重检查锁定保证线程安全</p>
	 *
	 * @return 返回值的 Class 实例，非null（void 返回 {@link Void#TYPE}）
	 */
	public Class<?> getReturnClassType() {
		Class<?> returnClassType = this.returnClassType;
		if (returnClassType == null) {
			returnClassType = method.getReturnType();
			this.returnClassType = returnClassType;
		}
		return returnClassType;
	}

	/**
	 * 获取方法的泛型返回值类型
	 * <p>优先返回缓存值，未初始化时通过 {@link Method#getGenericReturnType()} 解析并缓存；
	 * 双重检查锁定保证线程安全</p>
	 *
	 * @return 泛型返回值 Type 对象，非null（无泛型时返回 {@link Class} 实例）
	 */
	public Type getGenericReturnType() {
		Type genericReturnType = this.genericReturnType;
		if (genericReturnType == null) {
			genericReturnType = method.getGenericReturnType();
			this.genericReturnType = genericReturnType;
		}
		return genericReturnType;
	}

	// ======================== 通用工具方法 ========================

	/**
	 * 获取方法的完整签名（格式：类全限定名#方法名(参数类型1简名,参数类型2简名)）
	 * <p>示例：com.xxx.service.UserService#getUserById(Long)</p>
	 *
	 * @return 方法完整签名，非空字符串
	 */
	public String getFullMethodSignature() {
		StringBuilder sb = new StringBuilder();
		sb.append(method.getDeclaringClass().getName())
			.append("#")
			.append(method.getName())
			.append("(");
		Class<?>[] paramTypes = method.getParameterTypes();
		for (int i = 0; i < paramTypes.length; i++) {
			sb.append(paramTypes[i].getSimpleName());
			if (i < paramTypes.length - 1) {
				sb.append(",");
			}
		}
		sb.append(")");
		return sb.toString();
	}

	/**
	 * 克隆当前 {@link MethodMeta} 实例（复用已缓存的懒加载数据）
	 * <p>重写 {@link Object#clone()}，返回当前类型，避免强制类型转换</p>
	 *
	 * @return 新的 MethodMeta 实例，与原实例内容完全一致
	 */
	@Override
	protected MethodMeta clone() {
		return new MethodMeta(this);
	}

	// ======================== 通用对象方法 ========================

	/**
	 * 基于原生 {@link Method} 实例判断相等性
	 *
	 * @param o 待比较对象
	 * @return true=相等（同一Method实例），false=不相等
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		MethodMeta methodMeta = (MethodMeta) o;
		return Objects.equals(method, methodMeta.method);
	}

	/**
	 * 基于原生 {@link Method} 实例生成哈希值
	 *
	 * @return 哈希值
	 */
	@Override
	public int hashCode() {
		return Objects.hash(method);
	}

	/**
	 * 方法元数据的字符串表示（包含完整方法签名）
	 *
	 * @return 格式化字符串，示例：MethodMeta{method=com.xxx.service.UserService#getUserById(Long)}
	 */
	@Override
	public String toString() {
		return "MethodMeta{" +
			"method=" + getFullMethodSignature() +
			'}';
	}
}
