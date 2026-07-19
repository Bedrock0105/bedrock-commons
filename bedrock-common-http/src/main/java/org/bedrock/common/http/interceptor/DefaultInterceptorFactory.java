package org.bedrock.common.http.interceptor;

import org.bedrock.common.code.util.BeanUtil;
import org.bedrock.common.code.util.SpringUtil;
import org.bedrock.common.http.HttpConfiguration;
import org.springframework.beans.factory.ObjectProvider;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.util.Assert;

/**
 * 默认拦截器工厂实现类（实现{@link InterceptorFactory}接口）
 * <p>核心职责：</p>
 * <li>1. 管理拦截器实例缓存，保证线程安全的拦截器获取</li>
 * <li>2. 通过JDK SPI机制加载默认拦截器列表，支持动态添加</li>
 * <li>3. 创建拦截器实例时优先从Spring容器获取（享受依赖注入），兜底手动实例化</li>
 * <li>4. 对默认拦截器列表提供只读访问，防止外部篡改</li>
 */
public class DefaultInterceptorFactory implements InterceptorFactory {

	/**
	 * HTTP配置对象：为拦截器创建提供全局配置依赖（如超时、重试策略等）
	 */
	private final HttpConfiguration config;

	/**
	 * 拦截器实例缓存：key=拦截器类类型，value=拦截器实例
	 * <p>使用ConcurrentHashMap保证多线程下获取/创建拦截器的线程安全，避免重复实例化</p>
	 */
	private final Map<Class<? extends Interceptor>, Interceptor> interceptors = new ConcurrentHashMap<>();

	/**
	 * 默认拦截器列表：通过SPI机制加载，支持动态添加
	 * <p>存储框架内置/扩展的默认拦截器，请求执行时会默认执行该列表中的拦截器</p>
	 */
	private final Set<Interceptor> defaultInterceptors = new HashSet<>();

	/**
	 * 构造器：初始化配置并通过SPI加载所有默认拦截器
	 *
	 * @param config HTTP全局配置对象（非null）
	 * @throws IllegalArgumentException 当config为null时抛出
	 */
	public DefaultInterceptorFactory(HttpConfiguration config) {
		// 前置校验：确保配置对象非空
		Assert.notNull(config, "HttpConfiguration must not be null for DefaultInterceptorFactory");
		this.config = config;
		// 通过JDK SPI机制加载所有Interceptor接口的实现类，加入默认拦截器列表
		ServiceLoader.load(Interceptor.class)
			.forEach(defaultInterceptors::add);
	}

	/**
	 * 获取指定类型的拦截器实例（缓存优先，不存在则创建）
	 * <p>核心逻辑：</p>
	 * <li>1. 先从缓存中获取，存在则直接返回</li>
	 * <li>2. 缓存不存在则调用{@link #createInterceptor(Class)}创建实例并加入缓存</li>
	 * <li>3. ConcurrentHashMap的computeIfAbsent保证原子性，避免多线程重复创建</li>
	 *
	 * @param clazz 拦截器类类型（非null，且为具体实现类）
	 * @return 拦截器实例（非null）
	 * @throws IllegalArgumentException 当clazz为null/抽象类/接口时抛出
	 * @throws RuntimeException         当拦截器创建失败时抛出（如实例化异常）
	 */
	@Override
	public Interceptor getInterceptor(Class<? extends Interceptor> clazz) {
		Assert.notNull(clazz, "Interceptor class must not be null");
		return interceptors.computeIfAbsent(clazz, this::createInterceptor);
	}

	/**
	 * 获取默认拦截器列表（只读）
	 * <p>返回不可修改的列表，防止外部代码篡改默认拦截器集合，保证框架行为稳定</p>
	 *
	 * @return 只读的默认拦截器列表（非null，可能为空）
	 */
	@Override
	public Set<Interceptor> getDefaultInterceptors() {
		return Collections.unmodifiableSet(defaultInterceptors);
	}

	/**
	 * 动态添加默认拦截器
	 * <p>支持在运行时扩展默认拦截器，添加后的拦截器会参与后续所有请求的拦截流程</p>
	 *
	 * @param interceptor 拦截器实例（非null）
	 * @throws IllegalArgumentException 当interceptor为null时抛出
	 */
	@Override
	public void addDefaultInterceptor(Interceptor interceptor) {
		Assert.notNull(interceptor, "Interceptor instance must not be null when adding to default list");
		defaultInterceptors.add(interceptor);
	}

	/**
	 * 私有方法：创建拦截器实例（核心逻辑）
	 * <p>创建优先级：</p>
	 * <li>1. 若Spring上下文存在，优先从Spring容器获取（享受依赖注入、生命周期管理）</li>
	 * <li>2. Spring容器中无该拦截器时，手动实例化（反射创建）</li>
	 *
	 * @param clazz 拦截器类类型（非null，具体实现类）
	 * @return 拦截器实例（非null）
	 * @throws RuntimeException 当手动实例化失败时抛出（如无合法构造器、构造器抛异常）
	 */
	private Interceptor createInterceptor(Class<? extends Interceptor> clazz) {
		// 1. 检查Spring上下文是否存在，存在则优先从容器获取
		if (SpringUtil.getApplicationContext() != null) {
			ObjectProvider<? extends Interceptor> beanProvider = SpringUtil.getApplicationContext().getBeanProvider(clazz);
			Interceptor springBean = beanProvider.getIfAvailable();
			if (springBean != null) {
				return springBean;
			}
		}
		// 2. Spring容器无该拦截器，兜底手动实例化（反射创建）
		return BeanUtil.instantiateClass(clazz);
	}
}
