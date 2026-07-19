package org.bedrock.common.http.ssl;

import lombok.Getter;
import org.bedrock.common.code.util.SpringUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.ObjectProvider;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SSL上下文提供者注册中心
 * <p>
 * 统一管理{SslContextProvider}接口的所有实现类，核心特性：
 * 1. 以实现类的Class对象作为唯一注册标识，天然保证唯一性，无重复注册风险；
 * 2. 支持注册、获取、移除实现类，采用懒加载机制创建实例，按需初始化；
 * 3. 优先从Spring容器中获取托管的实现类Bean，非Spring托管类则通过反射兜底实例化；
 * 4. 线程安全的容器存储，适配多线程业务场景，解耦实现类与调用方，支持动态切换SSL策略。
 */
@Getter
public class SslContextProviderRegistry {

	/**
	 * SSL上下文提供者容器，线程安全的并发Map
	 * key：SslContextProvider接口的实现类Class对象（唯一标识）
	 * value：对应的实现类实例对象
	 */
	private final Map<Class<? extends SslContextProvider>, SslContextProvider> providerMap = new ConcurrentHashMap<>();

	/**
	 * 注册SSL上下文实现类
	 *
	 * @param providerKey 实现类的Class对象，作为唯一注册标识
	 * @param provider    实现类的实例对象
	 */
	public void register(Class<? extends SslContextProvider> providerKey, SslContextProvider provider) {
		providerMap.put(providerKey, provider);
	}

	/**
	 * 根据实现类Class获取对应的SSL上下文提供者实例【核心方法，懒加载】
	 * <p>
	 * 逻辑：存在则直接返回已注册的实例，不存在则调用创建方法实例化并自动注册，再返回
	 *
	 * @param providerKey 实现类的Class对象
	 * @return 对应的SslContextProvider实现类实例，不会返回null
	 */
	public SslContextProvider getProvider(Class<? extends SslContextProvider> providerKey) {
		return providerMap.computeIfAbsent(providerKey, this::createProvider);
	}

	/**
	 * 私有化创建SSL上下文提供者实例【兜底创建逻辑】
	 * 优先级：Spring容器托管实例 > 反射实例化，保证Spring Bean的生命周期统一管理
	 *
	 * @param clazz 目标实现类的Class对象
	 * @return 实例化完成的SslContextProvider实现类对象
	 */
	private SslContextProvider createProvider(Class<? extends SslContextProvider> clazz) {
		// 1. 优先从Spring容器中获取Bean实例，适配Spring托管的实现类
		if (SpringUtil.getApplicationContext() != null) {
			ObjectProvider<? extends SslContextProvider> beanProvider = SpringUtil.getApplicationContext().getBeanProvider(clazz);
			SslContextProvider springBean = beanProvider.getIfAvailable();
			if (springBean != null) {
				return springBean;
			}
		}
		// 2. 容器中无实例，通过反射实例化非Spring托管的实现类
		return BeanUtils.instantiateClass(clazz);
	}

	/**
	 * 移除指定的SSL上下文实现类注册信息
	 *
	 * @param providerKey 实现类的Class对象
	 */
	public void unregister(Class<? extends SslContextProvider> providerKey) {
		providerMap.remove(providerKey);
	}
}
