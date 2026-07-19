package org.bedrock.common.ai.factory.vectorstore;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 向量存储实例清理注册表。
 * <p>
 * 部分向量存储（如本地 SimpleVectorStore）在创建时会启动定时持久化任务，
 * 当 {@link VectorStoreFactory#removeVectorStore(String)} 移除缓存实例时，需同步执行清理逻辑。
 * </p>
 */
public final class VectorStorePersistenceRegistry {

	private static final ConcurrentHashMap<String, Runnable> CLEANUP_REGISTRY = new ConcurrentHashMap<>();

	private VectorStorePersistenceRegistry() {
	}

	/**
	 * 注册向量存储实例清理回调。
	 *
	 * @param id      向量存储唯一标识
	 * @param cleanup 清理逻辑（如取消定时任务、落盘等）
	 */
	public static void register(String id, Runnable cleanup) {
		unregister(id);
		CLEANUP_REGISTRY.put(id, cleanup);
	}

	/**
	 * 移除并执行指定实例的清理回调。
	 *
	 * @param id 向量存储唯一标识
	 */
	public static void unregister(String id) {
		Runnable cleanup = CLEANUP_REGISTRY.remove(id);
		if (cleanup != null) {
			cleanup.run();
		}
	}

	/**
	 * 清理所有已注册实例，通常在 JVM 退出时调用。
	 */
	public static void cleanupAll() {
		CLEANUP_REGISTRY.keySet().forEach(VectorStorePersistenceRegistry::unregister);
	}

}
