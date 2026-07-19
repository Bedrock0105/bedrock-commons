package org.bedrock.common.http.executor;

import lombok.extern.slf4j.Slf4j;
import org.bedrock.common.http.HttpRequest;

import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

import java.util.NoSuchElementException;

/**
 * HTTP执行器注册中心（单例模式）
 * <p>核心职责：</p>
 * <li>1. 注册/管理{@link HttpExecutorFactory}工厂实例，按名称映射</li>
 * <li>2. 根据HTTP请求指定的工厂名称，获取对应的{@link HttpExecutor}实例</li>
 * <li>3. 提供线程安全的注册/获取操作（基于ConcurrentHashMap实现）</li>
 */
@Slf4j
public class HttpExecutorRegistry {

	/**
	 * 线程安全的工厂注册表：key=工厂名称，value=HttpExecutorFactory实例
	 * <p>使用ConcurrentHashMap保证多线程下的注册/获取操作安全</p>
	 */
	private static final Map<String, HttpExecutorFactory> executorFactories = new ConcurrentHashMap<>();

	/**
	 * 根据HTTP请求获取对应的HttpExecutor实例
	 *
	 * @param request HTTP请求对象（包含工厂名称、配置等关键信息）
	 * @return 与请求匹配的HttpExecutor实例（非null）
	 * @throws IllegalArgumentException 当请求中的工厂名称为空/空白字符时抛出
	 * @throws NoSuchElementException   当指定名称的工厂未注册时抛出
	 * @throws IllegalStateException    当工厂创建HttpExecutor实例返回null时抛出
	 */
	public static HttpExecutor getHttpExecutor(HttpRequest request) {
		// 前置校验：请求对象非空
		if (request == null) {
			throw new IllegalArgumentException("HttpRequest must not be null.");
		}

		// 提取并校验工厂名称
		String httpExecutorName = request.getHttpExecutorName();
		if (httpExecutorName == null || httpExecutorName.isBlank()) {
			String errorMsg = "HttpExecutor factory name must not be null or blank in HttpRequest.";
			log.error(errorMsg);
			throw new IllegalArgumentException(errorMsg);
		}

		// 获取注册的工厂实例
		HttpExecutorFactory httpExecutorFactory = executorFactories.get(httpExecutorName);
		if (httpExecutorFactory == null) {
			String errorMsg = String.format("HttpExecutorFactory named '%s' is not registered. Registered factories: %s",
				httpExecutorName, executorFactories.keySet());
			log.error(errorMsg);
			throw new NoSuchElementException(errorMsg);
		}

		// 通过工厂获取HttpExecutor实例
		HttpExecutor executor = httpExecutorFactory.getInstance(request, request.getConfig());
		if (executor == null) {
			String errorMsg = String.format("HttpExecutorFactory named '%s' returned null when creating instance for request: %s",
				httpExecutorName, request);
			log.error(errorMsg);
			// 使用IllegalStateException表示“状态异常”（工厂本应创建实例但返回null）
			throw new IllegalStateException(errorMsg);
		}

		log.debug("Success to get HttpExecutor instance for factory name: {}", httpExecutorName);
		return executor;
	}

	/**
	 * 注册HttpExecutorFactory工厂实例
	 * <p>注：重复注册同一名称的工厂会覆盖原有实例</p>
	 *
	 * @param name    工厂名称（非空/非空白字符）
	 * @param factory 工厂实例（非null）
	 * @throws IllegalArgumentException 当名称为空/空白、工厂实例为null时抛出
	 */
	public static void registerHttpExecutorFactory(String name, HttpExecutorFactory factory) {
		// 校验工厂名称
		if (name == null || name.isBlank()) {
			String errorMsg = "HttpExecutorFactory register name must not be null or blank.";
			log.error(errorMsg);
			throw new IllegalArgumentException(errorMsg);
		}
		// 校验工厂实例
		if (factory == null) {
			String errorMsg = String.format("HttpExecutorFactory instance for name '%s' must not be null.", name);
			log.error(errorMsg);
			throw new IllegalArgumentException(errorMsg);
		}

		// 注册工厂（ConcurrentHashMap.put线程安全）
		executorFactories.put(name, factory);
		log.info("Success to register HttpExecutorFactory: {}", name);
	}

	// ===================== 可选扩展方法（增强易用性） =====================

	/**
	 * 检查指定名称的工厂是否已注册
	 *
	 * @param name 工厂名称
	 * @return true=已注册，false=未注册
	 */
	public static boolean isFactoryRegistered(String name) {
		return name != null && !name.isBlank() && executorFactories.containsKey(name);
	}

	/**
	 * 移除指定名称的工厂注册
	 *
	 * @param name 工厂名称
	 */
	public static void unregisterHttpExecutorFactory(String name) {
		if (name == null || name.isBlank()) {
			log.warn("Attempt to unregister HttpExecutorFactory with null/blank name, ignore.");
			return;
		}
		HttpExecutorFactory removed = executorFactories.remove(name);
		if (removed != null) {
			log.info("Success to unregister HttpExecutorFactory: {}", name);
		} else {
			log.warn("Attempt to unregister unregistered HttpExecutorFactory: {}", name);
		}
	}

	/**
	 * 静态初始化：自动注册所有实现HttpExecutorFactory的工厂类
	 */
	static {
		ServiceLoader.load(HttpExecutorFactory.class)
			.forEach(factory -> registerHttpExecutorFactory(factory.getName(), factory));
	}
}
