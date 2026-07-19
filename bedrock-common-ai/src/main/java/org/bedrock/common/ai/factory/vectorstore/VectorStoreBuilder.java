package org.bedrock.common.ai.factory.vectorstore;

import org.bedrock.common.ai.enums.VectorStoreEnum;
import org.bedrock.common.ai.model.factory.vectorstore.VectorStoreCreateParam;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.Ordered;

/**
 * 向量存储构造器。
 * <p>
 * 每种存储后端（Redis、Milvus、Elasticsearch 等）独立实现，负责根据连接参数创建对应的 {@link VectorStore} 实例。
 * 通过 {@link org.bedrock.common.spi.autoservice.AutoService} 注册后，
 * 由 {@link DefaultVectorStoreFactoryImpl} 在启动时通过 SPI 自动发现并装配。
 * </p>
 */
public interface VectorStoreBuilder extends Ordered {

	/**
	 * 根据参数创建当前存储类型专属的向量存储实例。
	 *
	 * @param param 向量存储构建参数（连接信息、索引名、集合前缀、嵌入模型等）
	 * @return VectorStore 实例
	 */
	VectorStore createVectorStore(VectorStoreCreateParam param);

	/**
	 * 获取当前构造器对应的向量存储类型枚举。
	 *
	 * @return VectorStoreEnum 存储类型标识
	 */
	VectorStoreEnum getVectorStoreEnum();

	@Override
	default int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE;
	}
}
