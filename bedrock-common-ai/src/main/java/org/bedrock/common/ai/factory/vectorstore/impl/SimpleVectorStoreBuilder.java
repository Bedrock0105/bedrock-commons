package org.bedrock.common.ai.factory.vectorstore.impl;

import lombok.extern.slf4j.Slf4j;
import org.bedrock.common.ai.enums.VectorStoreEnum;
import org.bedrock.common.ai.factory.vectorstore.VectorStoreBuilder;
import org.bedrock.common.ai.factory.vectorstore.VectorStorePersistenceRegistry;
import org.bedrock.common.ai.model.factory.vectorstore.VectorStoreCreateParam;
import org.bedrock.common.code.util.FileUtil;
import org.bedrock.common.code.util.StringUtil;
import org.bedrock.common.spi.autoservice.AutoService;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.util.Assert;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * 本地 SimpleVectorStore 构造器。
 * <p>
 * 基于 Spring AI {@link SimpleVectorStore}，向量数据保存在内存中，并定时持久化到本地 JSON 文件。
 * 适合开发测试或小规模场景，无需部署 Redis / Milvus / Elasticsearch。
 * </p>
 * <p>
 * 依赖说明：引入 {@code bedrock-common-ai} 即可使用，无需额外依赖。
 * 核心依赖为 {@code spring-ai-vector-store}（已随 bedrock-common-ai 传递引入）。
 * </p>
 * <p>
 * 参数映射关系：
 * <ul>
 *   <li>{@code id} → 持久化文件名（{@code {id}.json}）</li>
 *   <li>{@code databaseName} → 存储目录，未配置时默认 {@code ~/.bedrock/vector-store}</li>
 *   <li>{@code embeddingModel} → 文档向量化所用嵌入模型</li>
 *   <li>{@code metadataFieldTypes} → 当前不消费：内存按 Document metadata 过滤，无独立索引 schema</li>
 * </ul>
 * </p>
 */
@Slf4j
@AutoService(VectorStoreBuilder.class)
public class SimpleVectorStoreBuilder implements VectorStoreBuilder {

	private static final String DEFAULT_STORAGE_DIR = Paths
		.get(System.getProperty("user.home"), ".bedrock", "vector-store")
		.toString();

	private static final long PERSIST_INTERVAL_MS = Duration.ofMinutes(1).toMillis();

	private static final ScheduledExecutorService PERSIST_EXECUTOR = Executors.newSingleThreadScheduledExecutor(r -> {
		Thread thread = new Thread(r, "simple-vector-store-persist");
		thread.setDaemon(true);
		return thread;
	});

	static {
		Runtime.getRuntime().addShutdownHook(new Thread(VectorStorePersistenceRegistry::cleanupAll));
	}

	@Override
	public VectorStore createVectorStore(VectorStoreCreateParam param) {
		Assert.notNull(param.embeddingModel(), "embeddingModel cannot be null");
		Assert.hasText(param.id(), "id cannot be empty");

		SimpleVectorStore vectorStore = SimpleVectorStore.builder(param.embeddingModel()).build();
		File persistenceFile = resolvePersistenceFile(param);
		loadIfExists(vectorStore, persistenceFile);

		ScheduledFuture<?> persistTask = PERSIST_EXECUTOR.scheduleAtFixedRate(
			() -> saveQuietly(vectorStore, persistenceFile),
			PERSIST_INTERVAL_MS,
			PERSIST_INTERVAL_MS,
			TimeUnit.MILLISECONDS
		);
		VectorStorePersistenceRegistry.register(param.id(), () -> {
			persistTask.cancel(false);
			saveQuietly(vectorStore, persistenceFile);
		});
		return vectorStore;
	}

	/**
	 * 解析持久化文件路径。
	 */
	private File resolvePersistenceFile(VectorStoreCreateParam param) {
		String storageDir = StringUtil.isNotBlank(param.databaseName()) ? param.databaseName() : DEFAULT_STORAGE_DIR;
		FileUtil.mkdirs(storageDir);
		String safeId = param.id().replaceAll("[^a-zA-Z0-9._-]", "_");
		return Path.of(storageDir, safeId + ".json").toFile();
	}

	/**
	 * 启动时加载已有向量数据。
	 */
	private void loadIfExists(SimpleVectorStore vectorStore, File persistenceFile) {
		if (!persistenceFile.exists() || persistenceFile.length() <= 0) {
			return;
		}
		try {
			vectorStore.load(persistenceFile);
			log.info("本地向量库已加载: {}", persistenceFile.getAbsolutePath());
		}
		catch (Exception ex) {
			log.warn("本地向量库加载失败，将使用空向量库: {}", persistenceFile.getAbsolutePath(), ex);
		}
	}

	/**
	 * 静默持久化，避免定时任务异常中断。
	 */
	private void saveQuietly(SimpleVectorStore vectorStore, File persistenceFile) {
		try {
			File parent = persistenceFile.getParentFile();
			if (parent != null) {
				FileUtil.mkdirs(parent.getAbsolutePath());
			}
			vectorStore.save(persistenceFile);
		}
		catch (Exception ex) {
			log.warn("本地向量库持久化失败: {}", persistenceFile.getAbsolutePath(), ex);
		}
	}

	@Override
	public VectorStoreEnum getVectorStoreEnum() {
		return VectorStoreEnum.SIMPLE;
	}

}
