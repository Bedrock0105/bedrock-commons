package org.bedrock.common.http.proxy;

import lombok.Getter;
import org.bedrock.common.http.HttpConfiguration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 客户端代理注册中心（核心管理器）
 * 核心职责：
 * 1. 全局双层缓存管理：
 * - 一级缓存：客户端接口与代理实例的映射，避免重复创建代理对象
 * - 二级缓存：客户端接口与代理工厂的映射，避免重复创建工厂及工厂内的方法缓存
 * 2. 提供统一的代理实例获取入口，屏蔽工厂创建、代理生成的底层细节
 * 3. 预留Configuration配置扩展能力，支持后续通过配置定制代理行为（如超时、拦截规则等）
 * <p>
 * 设计特点：
 * - 线程安全：基于ConcurrentHashMap实现所有缓存，原子性操作（computeIfAbsent）避免多线程并发问题
 * - 双重懒加载：仅在首次获取某接口的代理实例时，才初始化对应的工厂和代理对象
 * - 全局复用：同一接口的工厂实例、代理实例均全局唯一，最大化复用缓存，降低性能损耗
 */
public class ClientRegistry {

	/**
	 * 全局配置对象
	 */
	private final HttpConfiguration config;

	/**
	 * 二级缓存：客户端接口与代理工厂的映射（核心工厂缓存）
	 * 1. 键：客户端接口Class对象（如UserClient.class，必须为接口类型）
	 * 2. 值：对应接口的ClientProxyFactory工厂实例
	 * 3. 线程安全：ConcurrentHashMap保证多线程下工厂创建/获取的原子性
	 * 4. 缓存策略：工厂实例全局唯一，避免重复创建工厂导致的方法缓存冗余
	 */
	@Getter
	private final Map<Class<?>, ClientProxyFactory<?>> clientProxyFactoryMap = new ConcurrentHashMap<>();

	/**
	 * 一级缓存：客户端接口与代理实例的映射（核心实例缓存）
	 * 1. 键：客户端接口Class对象（如UserClient.class）
	 * 2. 值：对应接口的动态代理实例（实现了目标接口，具备默认方法执行能力）
	 * 3. 线程安全：ConcurrentHashMap保证多线程下代理实例创建/获取的原子性
	 * 4. 缓存策略：代理实例全局唯一，避免重复生成代理对象带来的性能开销
	 */
	@Getter
	private final Map<Class<?>, Object> clientProxyMap = new ConcurrentHashMap<>();

	/**
	 * 构造器：初始化注册中心，绑定全局配置
	 *
	 * @param config 全局配置对象（预留扩展，当前暂未接入业务逻辑，可后续按需扩展）
	 */
	public ClientRegistry(HttpConfiguration config) {
		this.config = config;
	}

	/**
	 * 核心方法：根据客户端接口类型获取全局唯一的代理实例
	 * 双重懒加载逻辑（原子性操作，多线程安全）：
	 * 1. 一级缓存（clientProxyMap）检查：若已存在该接口的代理实例，直接返回
	 * 2. 一级缓存未命中：执行lambda创建代理实例，内部触发二级缓存逻辑：
	 * 2.1 二级缓存（clientProxyFactoryMap）检查：若已存在工厂实例，直接复用
	 * 2.2 二级缓存未命中：通过ClientProxyFactory::new创建新工厂并放入二级缓存
	 * 2.3 调用工厂的newInstance()生成代理实例
	 * 3. 将新创建的代理实例放入一级缓存，保证后续获取直接复用
	 *
	 * @param type 目标客户端接口的Class对象（约束：必须是接口类型，否则底层代理创建会抛出异常）
	 * @param <T>  目标接口的泛型类型
	 * @return 目标接口的全局唯一动态代理实例（具备接口默认方法执行能力）
	 * @throws IllegalArgumentException 若传入的type非接口类型（底层ClientProxyFactory/Proxy会触发校验）
	 * @throws ClassCastException       类型转换异常（理论上不会触发，因代理实例实现了目标接口）
	 */
	public <T> T getClient(Class<T> type) {
		if (!type.isInterface()) {
			throw new IllegalArgumentException("clientInterface must be an interface: " + type.getName());
		}
		return type.cast(clientProxyMap.computeIfAbsent(type, (_key) -> {
			// 二级缓存：原子性创建/获取工厂实例
			ClientProxyFactory<?> factory = clientProxyFactoryMap.computeIfAbsent(type, (_type) -> new ClientProxyFactory<>(config, _type));
			// 调用工厂生成代理实例，并强转为目标接口类型（type.cast保证类型安全）
			return type.cast(factory.newInstance());
		}));
	}

	/**
	 * 刷新工厂缓存，下次获取代理实例时，会重新创建工厂及代理实例
	 *
	 * @param type 目标客户端接口的Class对象
	 */
	public void refreshFactory(Class<?> type) {
		clientProxyFactoryMap.remove(type); // 清空工厂缓存，下次会重建
		clientProxyMap.remove(type); // 同步清空代理实例缓存
	}

}
