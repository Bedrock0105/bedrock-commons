package org.bedrock.common.http.proxy;

import org.bedrock.common.http.HttpConfiguration;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 客户端代理工厂类，封装JDK动态代理实例的创建逻辑
 * 核心职责：
 * 1. 管理目标接口的方法调用器缓存（复用ClientMethodInvoker，避免重复创建）
 * 2. 封装Proxy.newProxyInstance的调用细节，简化代理实例的创建流程
 * 3. 与ClientProxy配合，为目标接口生成具备默认方法执行能力的代理实例
 *
 * @param <T> 被代理的客户端接口类型（与ClientProxy的泛型一致）
 */
public class ClientProxyFactory<T> {

	/**
	 * 被代理的目标客户端接口Class对象（如UserClient.class）
	 * 要求：必须是接口类型（JDK动态代理仅支持接口代理）
	 */
	private final Class<T> clientInterface;

	/**
	 * 全局配置对象
	 */
	private final HttpConfiguration config;

	/**
	 * 方法与调用器的缓存Map，核心设计：
	 * 1. 采用ConcurrentHashMap保证多线程环境下的线程安全（代理实例可能被多线程调用）
	 * 2. 缓存全局复用：同一个工厂实例创建的所有代理实例共享此缓存，避免重复创建MethodHandle和ClientMethodInvoker
	 * 3. 键：目标接口的方法对象；值：方法对应的调用器（如DefaultMethodInvoker）
	 */
	private final Map<Method, ClientProxy.ClientMethodInvoker> methodCache = new ConcurrentHashMap<>();

	/**
	 * 构造器：初始化被代理的目标接口
	 *
	 * @param clientInterface 被代理的客户端接口Class对象（非空，且必须是接口）
	 * @throws IllegalArgumentException 若传入的Class不是接口，建议补充校验（可选）
	 */
	public ClientProxyFactory(HttpConfiguration config, Class<T> clientInterface) {
		if (!clientInterface.isInterface()) {
			throw new IllegalArgumentException("clientInterface must be an interface: " + clientInterface.getName());
		}
		this.config = config;
		this.clientInterface = clientInterface;
	}

	/**
	 * 创建目标接口的动态代理实例
	 * 核心流程：
	 * 1. 创建ClientProxy实例（绑定目标接口和全局方法缓存）
	 * 2. 调用Proxy.newProxyInstance生成JDK动态代理对象
	 * 3. 强制类型转换为目标接口类型并返回
	 *
	 * @return 目标接口的动态代理实例（实现了clientInterface，具备默认方法执行能力）
	 */
	@SuppressWarnings("unchecked") // 抑制类型转换警告：Proxy.newProxyInstance返回Object，强制转为T是安全的（因指定了clientInterface接口）
	protected T newInstance() {
		// 1. 创建InvocationHandler实现类实例（ClientProxy），绑定接口和缓存
		ClientProxy<T> proxy = new ClientProxy<>(config, clientInterface, methodCache);
		// 2. 生成动态代理实例：
		//    - 类加载器：复用目标接口的类加载器，保证类加载一致性
		//    - 实现的接口：仅包含目标接口，保证代理实例的类型纯净
		//    - InvocationHandler：绑定当前创建的ClientProxy实例
		return (T) Proxy.newProxyInstance(clientInterface.getClassLoader(), new Class[]{clientInterface}, proxy);
	}

}
