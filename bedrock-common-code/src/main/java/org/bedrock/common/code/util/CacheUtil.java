package org.bedrock.common.code.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.Collection;
import java.util.concurrent.Callable;

/**
 * 缓存处理器
 */
@Slf4j
public abstract class CacheUtil {

	/**
	 * 缓存管理器
	 */
	private static CacheManager CACHE_MANAGER;

	public static CacheManager getCacheManager() {
		if (CACHE_MANAGER == null) {
			CACHE_MANAGER = SpringUtil.getBean(CacheManager.class);
		}
		return CACHE_MANAGER;
	}

	/**
	 * 获取指定名称的缓存实例
	 *
	 * @param cacheName 缓存名称
	 * @return 缓存实例，若不存在则返回 null
	 */
	public static Cache getCache(@NonNull String cacheName) {
		return getCacheManager().getCache(cacheName);
	}

	/**
	 * 向指定缓存中添加数据
	 *
	 * @param cacheName 缓存名称
	 * @param key       缓存键
	 * @param value     缓存值
	 */
	public static void put(@NonNull String cacheName, @NonNull Object key, @Nullable Object value) {
		getCache(cacheName).put(key, value);
	}

	/**
	 * 从指定缓存中获取数据
	 *
	 * @param cacheName 缓存名称
	 * @param key       缓存键
	 * @param <T>       数据类型
	 * @return 缓存值，若不存在则返回 null
	 */
	@Nullable
	public static <T> T get(@NonNull String cacheName, @NonNull Object key, @Nullable Class<T> type) {
		return getCache(cacheName).get(key, type);
	}

	/**
	 * 从指定缓存中获取数据
	 *
	 * @param cacheName 缓存名称
	 * @param key       缓存键
	 * @return 缓存值，若不存在则返回 null
	 */
	@Nullable
	public static Object get(@NonNull String cacheName, @NonNull Object key) {
		Cache.ValueWrapper valueWrapper = getCache(cacheName).get(key);
		return valueWrapper == null ? null : valueWrapper.get();
	}

	/**
	 * 从指定缓存中获取数据，若不存在则执行回调函数并缓存结果
	 *
	 * @param cacheName 缓存名称
	 * @param key       缓存键
	 * @param callable  回调函数（当缓存不存在时执行）
	 * @param <T>       数据类型
	 * @return 缓存值或回调函数返回值
	 */
	@Nullable
	@SuppressWarnings("unchecked")
	public static <T> T get(@NonNull String cacheName, @NonNull Object key, @NonNull Callable<T> callable) {
		Cache.ValueWrapper valueWrapper = getCache(cacheName).get(key);
		if (valueWrapper != null) {
			return (T) valueWrapper.get();
		}
		try {
			T result = callable.call();
			if (result == null) {
				return null;
			}
			put(cacheName, key, result);
			return result;
		} catch (Exception e) {
			log.error("缓存获取失败:{}", e.getMessage(), e);
			return null;
		}
	}

	/**
	 * 删除指定缓存中的数据
	 *
	 * @param cacheName 缓存名称
	 * @param key       缓存键
	 */
	public static void evict(@NonNull String cacheName, @NonNull Object key) {
		getCache(cacheName).evict(key);
	}

	/**
	 * 清空指定缓存中的所有数据
	 *
	 * @param cacheName 缓存名称
	 */
	public static void clear(@NonNull String cacheName) {
		getCache(cacheName).clear();
	}

	/**
	 * 检查指定缓存是否存在
	 *
	 * @param cacheName 缓存名称
	 * @return 存在返回 true，否则返回 false
	 */
	public static boolean exists(@NonNull String cacheName) {
		return getCache(cacheName) != null;
	}

	/**
	 * 获取所有的缓存名称
	 */
	public static Collection<String> getCacheNames() {
		return getCacheManager().getCacheNames();
	}

}
