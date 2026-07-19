package org.bedrock.common.ai.factory.vectorstore.impl;

import io.milvus.client.MilvusServiceClient;
import io.milvus.param.ConnectParam;
import io.milvus.param.IndexType;
import io.milvus.param.MetricType;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.bedrock.common.ai.enums.VectorStoreEnum;
import org.bedrock.common.ai.factory.vectorstore.VectorStoreBuilder;
import org.bedrock.common.ai.model.factory.vectorstore.VectorStoreCreateParam;
import org.bedrock.common.code.util.StringUtil;
import org.bedrock.common.spi.autoservice.AutoService;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.milvus.MilvusVectorStore;
import org.springframework.util.Assert;

/**
 * Milvus 向量存储构造器。
 * <p>
 * 基于 Spring AI {@link MilvusVectorStore}，通过 gRPC 连接 Milvus 服务。
 * 通过 {@link AutoService} 自动注册到 {@link org.bedrock.common.ai.factory.vectorstore.DefaultVectorStoreFactoryImpl}。
 * </p>
 * <p>
 * 依赖说明：业务方需在 pom 中显式引入以下依赖后方可启用本构造器：
 * <pre>{@code
 * <dependency>
 *     <groupId>org.springframework.ai</groupId>
 *     <artifactId>spring-ai-milvus-store</artifactId>
 * </dependency>
 * }</pre>
 * 未引入时启动不会报错，仅跳过 Milvus 向量存储的注册。
 * </p>
 * <p>
 * 参数映射关系：
 * <ul>
 *   <li>{@code connectionParam} → Milvus 连接（host / port / username / password）</li>
 *   <li>{@code databaseName} → Milvus 数据库名（默认 default）</li>
 *   <li>{@code collectionName} → Milvus 集合名（默认 vector_store）</li>
 *   <li>{@code embeddingDimension} → 向量维度（未配置时默认 1536）</li>
 *   <li>{@code embeddingModel} → 文档向量化所用嵌入模型</li>
 * </ul>
 * </p>
 */
@Slf4j
@AutoService(VectorStoreBuilder.class)
public class MilvusVectorStoreBuilder implements VectorStoreBuilder {

	private static final String DEFAULT_DATABASE_NAME = "default";

	private static final String DEFAULT_COLLECTION_NAME = "vector_store";

	private static final int DEFAULT_EMBEDDING_DIMENSION = 1536;

	@Override
	@SneakyThrows
	public VectorStore createVectorStore(VectorStoreCreateParam param) {
		Assert.notNull(param.connectionParam(), "connectionParam cannot be null");
		VectorStoreCreateParam.VectorStoreConnectionParam connectionParam = param.connectionParam();
		String databaseName = StringUtil.isNotBlank(param.databaseName())
			? param.databaseName() : DEFAULT_DATABASE_NAME;
		String collectionName = StringUtil.isNotBlank(param.collectionName())
			? param.collectionName() : DEFAULT_COLLECTION_NAME;
		int embeddingDimension = param.embeddingDimension() > 0
			? param.embeddingDimension() : DEFAULT_EMBEDDING_DIMENSION;

		ConnectParam connectParam = ConnectParam.newBuilder()
			.withHost(connectionParam.host())
			.withPort(connectionParam.port())
			.withDatabaseName(databaseName)
			.withAuthorization(StringUtil.toStr(connectionParam.username(), ""),
				StringUtil.toStr(connectionParam.password(), ""))
			.build();
		MilvusServiceClient milvusClient = new MilvusServiceClient(connectParam);
		MilvusVectorStore vectorStore = MilvusVectorStore.builder(milvusClient, param.embeddingModel())
			.databaseName(databaseName)
			.collectionName(collectionName)
			.embeddingDimension(embeddingDimension)
			.indexType(IndexType.IVF_FLAT)
			.metricType(MetricType.COSINE)
			.initializeSchema(true)
			.build();
		vectorStore.afterPropertiesSet();

		return vectorStore;
	}

	@Override
	public VectorStoreEnum getVectorStoreEnum() {
		return VectorStoreEnum.MILVUS;
	}
}
