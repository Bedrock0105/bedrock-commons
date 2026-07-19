package org.bedrock.common.ai.factory.vectorstore.impl;

import lombok.SneakyThrows;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.bedrock.common.ai.enums.VectorStoreEnum;
import org.bedrock.common.ai.factory.vectorstore.VectorStoreBuilder;
import org.bedrock.common.ai.model.factory.vectorstore.VectorStoreCreateParam;
import org.bedrock.common.code.constant.StringPool;
import org.bedrock.common.code.util.StringUtil;
import org.bedrock.common.spi.autoservice.AutoService;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.elasticsearch.ElasticsearchVectorStore;
import org.springframework.ai.vectorstore.elasticsearch.ElasticsearchVectorStoreOptions;
import org.springframework.ai.vectorstore.elasticsearch.SimilarityFunction;
import org.springframework.util.Assert;

import java.net.URI;
import java.util.Arrays;

/**
 * Elasticsearch 向量存储构造器。
 * <p>
 * 基于 Spring AI {@link ElasticsearchVectorStore}，通过 REST 连接 Elasticsearch 8.x（dense_vector + kNN）。
 * 通过 {@link AutoService} 自动注册到 {@link org.bedrock.common.ai.factory.vectorstore.DefaultVectorStoreFactoryImpl}。
 * </p>
 * <p>
 * 依赖说明：业务方需在 pom 中显式引入以下依赖后方可启用本构造器：
 * <pre>{@code
 * <dependency>
 *     <groupId>org.springframework.ai</groupId>
 *     <artifactId>spring-ai-elasticsearch-store</artifactId>
 * </dependency>
 * }</pre>
 * 未引入时启动不会报错，仅跳过 Elasticsearch 向量存储的注册。
 * </p>
 * <p>
 * 参数映射关系：
 * <ul>
 *   <li>{@code connectionParam} → RestClient 连接（host / port / username / password）</li>
 *   <li>{@code databaseName} → 索引名（{@code indexName}，默认 spring-ai-document-index）</li>
 *   <li>{@code embeddingDimension} → 向量维度（未配置时默认 1536）</li>
 *   <li>{@code embeddingModel} → 文档向量化所用嵌入模型</li>
 *   <li>{@code metadataFieldTypes} → 当前不消费：ES 动态写入 metadata，过滤转为 metadata.xxx 查询，无需预声明</li>
 * </ul>
 * </p>
 *
 * @see <a href="https://docs.spring.io/spring-ai/reference/api/vectordbs/elasticsearch.html">Spring AI Elasticsearch</a>
 */
@AutoService(VectorStoreBuilder.class)
public class ElasticsearchVectorStoreBuilder implements VectorStoreBuilder {

	private static final String DEFAULT_INDEX_NAME = "spring-ai-document-index";



	private static final int DEFAULT_EMBEDDING_DIMENSION = 1536;

	private static final int DEFAULT_PORT = 9200;

	@Override
	@SneakyThrows
	public VectorStore createVectorStore(VectorStoreCreateParam param) {
		Assert.notNull(param.connectionParam(), "connectionParam cannot be null");
		VectorStoreCreateParam.VectorStoreConnectionParam connectionParam = param.connectionParam();

		RestClient restClient = buildRestClient(connectionParam);
		ElasticsearchVectorStoreOptions options = buildOptions(param);

		ElasticsearchVectorStore vectorStore = ElasticsearchVectorStore.builder(restClient, param.embeddingModel())
			.options(options)
			.initializeSchema(true)
			.build();
		vectorStore.afterPropertiesSet();
		return vectorStore;
	}

	private RestClient buildRestClient(VectorStoreCreateParam.VectorStoreConnectionParam connectionParam) {
		HttpHost[] httpHosts = resolveHttpHost(connectionParam);
		RestClientBuilder restClientBuilder = RestClient.builder(httpHosts);
		if (StringUtil.isNotBlank(connectionParam.username())) {
			BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
			credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(connectionParam.username(),
				StringUtil.toStr(connectionParam.password(), StringPool.EMPTY)));
			restClientBuilder.setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder
				.setDefaultCredentialsProvider(credentialsProvider));
		}
		return restClientBuilder.build();
	}

	private ElasticsearchVectorStoreOptions buildOptions(VectorStoreCreateParam param) {
		String indexName = StringUtil.isNotBlank(param.databaseName())
			? param.databaseName() : DEFAULT_INDEX_NAME;
		int embeddingDimension = param.embeddingDimension() > 0
			? param.embeddingDimension() : DEFAULT_EMBEDDING_DIMENSION;

		ElasticsearchVectorStoreOptions options = new ElasticsearchVectorStoreOptions();
		options.setIndexName(indexName);
		options.setDimensions(embeddingDimension);
		options.setSimilarity(SimilarityFunction.cosine);
		return options;
	}

	private HttpHost[] resolveHttpHost(VectorStoreCreateParam.VectorStoreConnectionParam connectionParam) {
		String host = connectionParam.host();
		int port = connectionParam.port() > 0 ? connectionParam.port() : DEFAULT_PORT;
		return Arrays.stream(StringUtil.split(host, StringPool.COMMA))
			.map(url -> {
				if (url.startsWith("http://") || url.startsWith("https://")) {
					URI uri = URI.create(url);
					String scheme = uri.getScheme();
					String hostname = uri.getHost();
					Assert.hasText(hostname, "Elasticsearch host cannot be parsed from URI: " + url);
					int uriPort = uri.getPort() > 0 ? uri.getPort() : port;
					return new HttpHost(hostname, uriPort, scheme);
				}
				String scheme = (port == 443 || port == 9243) ? "https" : "http";
				return new HttpHost(host, port, scheme);
			}).toArray(HttpHost[]::new);

	}

	@Override
	public VectorStoreEnum getVectorStoreEnum() {
		return VectorStoreEnum.ELASTICSEARCH;
	}

}
