package org.bedrock.common.ai.factory.vectorstore;

import lombok.extern.slf4j.Slf4j;
import org.bedrock.common.ai.enums.VectorStoreEnum;
import org.bedrock.common.ai.model.factory.vectorstore.VectorStoreCreateParam;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.SmartInitializingSingleton;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

/**
 * {@link VectorStoreFactory} 默认实现。
 * <p>
 * 维护 {@code id → VectorStore} 实例缓存，并通过 SPI 加载所有 {@link VectorStoreBuilder} 实现，
 * 按 {@link VectorStoreEnum} 路由到具体存储后端。
 * </p>
 */
@Slf4j
public class DefaultVectorStoreFactoryImpl implements VectorStoreFactory, SmartInitializingSingleton {

	/**
	 * 向量存储实例缓存，key 为业务侧唯一标识
	 */
	private final Map<String, VectorStore> vectorStoreCache = new ConcurrentHashMap<>();

	/**
	 * 向量存储构造器映射。
	 * key: 存储类型枚举；value: 对应后端的构造器实现
	 */
	private final Map<VectorStoreEnum, VectorStoreBuilder> vectorStoreBuilderMap = new EnumMap<>(VectorStoreEnum.class);

	@Override
	public VectorStore getVectorStore(VectorStoreCreateParam param) {
		return vectorStoreCache.computeIfAbsent(param.id(), _id -> createVectorStore(param));
	}

	@Override
	public VectorStore createVectorStore(VectorStoreCreateParam param) {
		VectorStoreBuilder builder = vectorStoreBuilderMap.get(param.vectorStoreEnum());
		if (builder != null) {
			return builder.createVectorStore(param);
		}
		log.error("向量存储类型[{}]暂不支持创建", param.vectorStoreEnum());
		throw new UnsupportedOperationException("向量存储类型[" + param.vectorStoreEnum() + "]暂不支持创建");
	}

	@Override
	public VectorStore removeVectorStore(String id) {
		VectorStorePersistenceRegistry.unregister(id);
		return vectorStoreCache.remove(id);
	}

	@Override
	public void removeVectorStore(Predicate<String> predicate) {
		vectorStoreCache.keySet().stream()
			.filter(predicate)
			.toList()
			.forEach(this::removeVectorStore);
	}

	/**
	 * 容器单例初始化完成后，通过 SPI 发现并注册所有可用的 {@link VectorStoreBuilder} 实现。
	 * <p>
	 * 部分构造器依赖可选的三方库（Redis / Milvus / Elasticsearch），未引入对应依赖时自动跳过，
	 * 不影响其他已就绪的向量存储后端注册。
	 * </p>
	 */
	@Override
	public void afterSingletonsInstantiated() {
		ServiceLoader.load(VectorStoreBuilder.class)
			.stream()
			.map(ServiceLoader.Provider::get)
			.sorted(Comparator.comparingInt(VectorStoreBuilder::getOrder))
			.forEach(builder -> {
				vectorStoreBuilderMap.put(builder.getVectorStoreEnum(), builder);
				log.info("已注册向量存储构造器: {}", builder.getVectorStoreEnum());
			});
	}
}
