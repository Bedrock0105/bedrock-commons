package org.bedrock.common.crypto.algorithm;

import org.bedrock.common.crypto.exception.CryptoException;
import org.bedrock.common.crypto.props.CryptoProperties;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.util.Assert;

import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@link CryptoAlgorithm} 注册中心。
 * <p>
 * 构造时通过 JDK {@link ServiceLoader} 加载所有标注了 {@code @AutoService(CryptoAlgorithm.class)}
 * 的实现，并按 Class 缓存。容器单例就绪后调用各实现的 {@link CryptoAlgorithm#init(CryptoProperties)}。
 * </p>
 */
public class CryptoAlgorithmRegistry implements SmartInitializingSingleton {

	private final CryptoProperties properties;

	/**
	 * SPI 加载的算法实例缓存：key = 实现类，value = 实例
	 */
	private final Map<Class<? extends CryptoAlgorithm>, CryptoAlgorithm> spiCache = new ConcurrentHashMap<>();

	/**
	 * 构造并加载全部 SPI 实现。
	 *
	 * @param properties 加解密配置（用于后续 init）
	 */
	public CryptoAlgorithmRegistry(CryptoProperties properties) {
		this.properties = properties;
		ServiceLoader.load(CryptoAlgorithm.class).forEach(algorithm ->
			spiCache.put(algorithm.getClass(), algorithm));
	}

	/**
	 * 按实现类型获取算法实例。
	 *
	 * @param clazz 算法实现 Class（不可为接口 {@link CryptoAlgorithm} 本身）
	 * @return 算法实例；SPI 未注册时可能为 {@code null}
	 */
	public CryptoAlgorithm get(Class<? extends CryptoAlgorithm> clazz) {
		Assert.notNull(clazz, "CryptoAlgorithm class must not be null");
		if (clazz == CryptoAlgorithm.class || clazz.isInterface()) {
			throw new CryptoException("请指定具体的 CryptoAlgorithm 实现类，不能是接口本身: " + clazz.getName());
		}
		CryptoAlgorithm algorithm = spiCache.get(clazz);
		if (algorithm == null) {
			throw new CryptoException("未找到 CryptoAlgorithm 实现类: " + clazz.getName());
		}
		return algorithm;
	}

	/**
	 * 单例初始化完成后，向所有 SPI 算法注入配置。
	 */
	@Override
	public void afterSingletonsInstantiated() {
		spiCache.values().forEach(algorithm -> algorithm.init(properties));
	}
}
