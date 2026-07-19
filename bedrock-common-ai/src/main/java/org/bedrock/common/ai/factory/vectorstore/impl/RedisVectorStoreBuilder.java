package org.bedrock.common.ai.factory.vectorstore.impl;

import lombok.extern.slf4j.Slf4j;
import org.bedrock.common.ai.enums.VectorStoreEnum;
import org.bedrock.common.ai.factory.vectorstore.VectorStoreBuilder;
import org.bedrock.common.ai.model.factory.vectorstore.VectorStoreCreateParam;
import org.bedrock.common.code.util.NumberUtil;
import org.bedrock.common.code.util.StringUtil;
import org.bedrock.common.spi.autoservice.AutoService;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.redis.RedisVectorStore;
import org.springframework.util.Assert;
import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisPooled;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Redis 向量存储构造器。
 * <p>
 * 基于 Spring AI {@link RedisVectorStore}，使用 Jedis 连接 Redis Stack（需 RediSearch + RedisJSON 模块）。
 * 通过 {@link AutoService} 自动注册到 {@link org.bedrock.common.ai.factory.vectorstore.DefaultVectorStoreFactoryImpl}。
 * </p>
 * <p>
 * 依赖说明：业务方需在 pom 中显式引入以下依赖后方可启用本构造器：
 * <pre>{@code
 * <dependency>
 *     <groupId>org.springframework.ai</groupId>
 *     <artifactId>spring-ai-redis-store</artifactId>
 * </dependency>
 * }</pre>
 * 未引入时启动不会报错，仅跳过 Redis 向量存储的注册。
 * </p>
 * <p>
 * 参数映射关系：
 * <ul>
 *   <li>{@code connectionParam} → Jedis 连接（host / port / username / password）</li>
 *   <li>{@code databaseName} → Redis 索引名（{@code indexName}，默认 spring-ai-index）</li>
 *   <li>{@code collectionName} → key 前缀（{@code prefix}）</li>
 *   <li>{@code embeddingModel} → 文档向量化所用嵌入模型</li>
 *   <li>{@code metadataFieldTypes} → 可过滤 metadata（按 Java 类型映射为 TAG / NUMERIC / TEXT）。
 *       Redis 建 RediSearch 索引时必须声明，否则按 knowledgeId 等过滤会报
 *       {@code Not allowed filter identifier name}；若索引已在未声明字段前创建，需删旧索引或换 indexName 重建。</li>
 * </ul>
 * </p>
 */
@Slf4j
@AutoService(VectorStoreBuilder.class)
public class RedisVectorStoreBuilder implements VectorStoreBuilder {

	/**
	 * 默认数据库索引名
	 */
	private static final String DEFAULT_DATABASE_INDEX_KEY = "database";

	@Override
	public VectorStore createVectorStore(VectorStoreCreateParam param) {
		Assert.notNull(param.connectionParam(), "connectionParam cannot be null");
		VectorStoreCreateParam.VectorStoreConnectionParam connectionParam = param.connectionParam();

		HostAndPort hostAndPort = new HostAndPort(connectionParam.host(), connectionParam.port());
		DefaultJedisClientConfig.Builder builder = DefaultJedisClientConfig.builder();

		if (StringUtil.isNotBlank(connectionParam.username())) {
			builder.user(connectionParam.username());
		}
		if (StringUtil.isNotBlank(connectionParam.password())) {
			builder.password(connectionParam.password());
		}
		Map<String, Object> params = param.params();
		if (params != null && params.containsKey(DEFAULT_DATABASE_INDEX_KEY)) {
			builder.database(NumberUtil.toInt(params.get(DEFAULT_DATABASE_INDEX_KEY), 0));
		}

		JedisPooled jedisPooled = new JedisPooled(hostAndPort, builder.build());
		RedisVectorStore.Builder storeBuilder = RedisVectorStore.builder(jedisPooled, param.embeddingModel())
			.indexName(param.databaseName())
			.prefix(param.collectionName())
			.initializeSchema(true);

		List<RedisVectorStore.MetadataField> metadataFields = toMetadataFields(param.metadataFieldTypes());
		// 空则不注册：与未传 metadataFieldTypes 时行为一致；非空则写入索引 schema 供 filterExpression 使用
		if (!metadataFields.isEmpty()) {
			storeBuilder.metadataFields(metadataFields);
		}

		RedisVectorStore vectorStore = storeBuilder.build();
		vectorStore.afterPropertiesSet();
		return vectorStore;
	}

	/**
	 * 按 Java 类型映射为 Redis MetadataField：String→tag，Number→numeric，其它→text。
	 */
	private List<RedisVectorStore.MetadataField> toMetadataFields(Map<String, Class<?>> metadataFieldTypes) {
		if (metadataFieldTypes == null || metadataFieldTypes.isEmpty()) {
			return List.of();
		}
		List<RedisVectorStore.MetadataField> fields = new ArrayList<>(metadataFieldTypes.size());
		for (Map.Entry<String, Class<?>> entry : metadataFieldTypes.entrySet()) {
			String name = entry.getKey();
			Class<?> type = entry.getValue();
			if (StringUtil.isBlank(name) || type == null) {
				continue;
			}
			if (CharSequence.class.isAssignableFrom(type)) {
				fields.add(RedisVectorStore.MetadataField.tag(name));
			} else if (Number.class.isAssignableFrom(type)) {
				fields.add(RedisVectorStore.MetadataField.numeric(name));
			} else {
				log.debug("Redis metadata field '{}' type {} mapped to TEXT", name, type.getName());
				fields.add(RedisVectorStore.MetadataField.text(name));
			}
		}
		return fields;
	}

	@Override
	public VectorStoreEnum getVectorStoreEnum() {
		return VectorStoreEnum.REDIS;
	}
}
