package org.bedrock.common.ai.factory.vectorstore;

import org.bedrock.common.ai.model.factory.vectorstore.VectorStoreCreateParam;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.function.Predicate;

/**
 * 向量存储工厂接口。
 * <p>
 * 统一管理 {@link VectorStore} 实例的创建、缓存获取与缓存销毁，
 * 内部按 {@link org.bedrock.common.ai.enums.VectorStoreEnum} 路由到对应 {@link VectorStoreBuilder} 实现，
 * 复用已有连接与索引配置，避免重复初始化。
 * </p>
 *
 * @see VectorStoreBuilder
 * @see DefaultVectorStoreFactoryImpl
 */
public interface VectorStoreFactory {

	/**
	 * 优先从缓存获取向量存储实例。
	 * <p>缓存不存在时，自动调用 {@link #createVectorStore(VectorStoreCreateParam)} 创建并放入缓存。</p>
	 *
	 * @param param 构建向量存储所需参数（含存储类型、连接信息、嵌入模型等）
	 * @return 向量存储实例
	 */
	VectorStore getVectorStore(VectorStoreCreateParam param);

	/**
	 * 全新创建向量存储实例（不读取缓存、不自动存入缓存）。
	 *
	 * @param param 构建向量存储所需参数
	 * @return 全新向量存储实例
	 */
	VectorStore createVectorStore(VectorStoreCreateParam param);

	/**
	 * 根据唯一标识移除缓存中的向量存储实例。
	 *
	 * @param id 向量存储唯一标识，与 {@link VectorStoreCreateParam#id()} 对应
	 * @return 被移除的实例；不存在时返回 {@code null}
	 */
	VectorStore removeVectorStore(String id);

	/**
	 * 移除key包含id的缓存
	 *
	 * @param id 向量存储唯一标识，与 {@link VectorStoreCreateParam#id()} 对应
	 */
	default void removeVectorStoreContains(String id) {
		removeVectorStore(s -> s.contains(id));
	}

	/**
	 * 批量清理指定标识下缓存实例。
	 */
	void removeVectorStore(Predicate<String> predicate);
}
