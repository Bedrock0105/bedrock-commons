package org.bedrock.common.ai.model.factory.vectorstore;

import org.bedrock.common.ai.enums.VectorStoreEnum;
import org.springframework.ai.embedding.EmbeddingModel;

import java.util.Map;

/**
 * 向量存储创建参数。
 *
 * @param id                 业务侧唯一标识，用于工厂缓存 key
 * @param vectorStoreEnum    向量存储类型（Redis、Milvus、Elasticsearch 等）
 * @param embeddingModel     文档写入时使用的嵌入模型，需与检索侧保持一致
 * @param databaseName       逻辑库/索引名（Redis 映射为 indexName）
 * @param collectionName     集合/表名或 key 前缀（Redis 映射为 prefix）
 * @param embeddingDimension 向量维度，部分存储后端建索引时需要
 * @param connectionParam    存储连接信息（地址、端口、账号等）
 * @param params             扩展参数
 * @param metadataFieldTypes 可过滤 metadata：字段名 → Java 类型。
 *                           由各 {@link org.bedrock.common.ai.factory.vectorstore.VectorStoreBuilder} 自行映射为后端 schema；
 *                           目前仅 Redis 建索引时必须消费（否则 filter 报 Not allowed filter identifier）；
 *                           Milvus（JSON metadata）/ Elasticsearch / Simple 过滤不依赖预声明，可忽略本 Map。
 */
public record VectorStoreCreateParam(String id,
                                     VectorStoreEnum vectorStoreEnum,
                                     EmbeddingModel embeddingModel,
                                     String databaseName,
                                     String collectionName,
                                     Integer embeddingDimension,
                                     VectorStoreConnectionParam connectionParam,
                                     Map<String, Object> params,
                                     Map<String, Class<?>> metadataFieldTypes) {

	/**
	 * 兼容旧调用：无扩展参数、无可过滤 metadata。
	 */
	public VectorStoreCreateParam(String id, VectorStoreEnum vectorStoreEnum, EmbeddingModel embeddingModel,
	                              String databaseName, String collectionName, Integer embeddingDimension,
	                              VectorStoreConnectionParam connectionParam) {
		this(id, vectorStoreEnum, embeddingModel, databaseName, collectionName, embeddingDimension,
			connectionParam, Map.of(), Map.of());
	}

	/**
	 * 带扩展参数、无可过滤 metadata。
	 */
	public VectorStoreCreateParam(String id, VectorStoreEnum vectorStoreEnum, EmbeddingModel embeddingModel,
	                              String databaseName, String collectionName, Integer embeddingDimension,
	                              VectorStoreConnectionParam connectionParam, Map<String, Object> params) {
		this(id, vectorStoreEnum, embeddingModel, databaseName, collectionName, embeddingDimension,
			connectionParam, params, Map.of());
	}

	/**
	 * 向量存储连接参数。
	 *
	 * @param host     主机地址
	 * @param port     端口
	 * @param username 用户名，无认证时可传空
	 * @param password 密码，无认证时可传空
	 */
	public record VectorStoreConnectionParam(String host, int port, String username, String password) {

	}
}
