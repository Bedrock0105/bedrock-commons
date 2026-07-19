package org.bedrock.common.code.util;

import org.springframework.lang.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * ThreadLocal 工具类：提供线程局部变量的统一管理，避免内存泄漏
 * 核心：用 Map 存储多组线程局部变量，支持多类型、多key管理
 */
public abstract class ThreadLocalUtil {

	// 为什么用 Map？因为一个线程可能需要存储多个不同类型的局部变量（如用户信息、请求ID）
	private static final ThreadLocal<Map<String, Object>> THREAD_LOCAL = ThreadLocal.withInitial(HashMap::new);

	/**
	 * 多线程传递的时候要忽略的 key
	 */
	private static final Set<String> THREAD_LOCAL_KEYS_IGNORED_IN_TRANSMISSION  = new HashSet<>();

	/**
	 * @param key   变量标识（唯一，如 "loginUser"、"requestId"）
	 * @param value 线程私有变量（任意类型）
	 */
	public static <T> void set(String key, T value) {
		// 获取当前线程的 Map 容器，存入 key-value
		Map<String, Object> map = THREAD_LOCAL.get();
		map.put(key, value);
	}

	/**
	 * @param map 线程私有变量（key-value）
	 */
	public static <T> void set(Map<String, Object> map) {
		map.forEach(ThreadLocalUtil::set);
	}

	/**
	 * @param key          变量标识
	 * @param defaultValue 变量不存在时的默认值
	 * @return 线程私有变量（泛型类型，自动强转）
	 */
	@Nullable
	@SuppressWarnings("unchecked")
	public static <T> T get(String key, T defaultValue) {
		Map<String, Object> map = THREAD_LOCAL.get();
		// 从 Map 中获取，不存在则返回默认值
		return (T) map.getOrDefault(key, defaultValue);
	}

	/**
	 * 重载方法：变量不存在时返回 null
	 */
	@Nullable
	@SuppressWarnings("unchecked")
	public static <T> T get(String key) {
		return get(key, null);
	}

	/**
	 * 获取所有变量
	 */
	public static Map<String, Object> get() {
		return new HashMap<>(THREAD_LOCAL.get());
	}

	/**
	 * 获取变量，若不存在则执行回调函数（适合需要动态生成默认值的场景）
	 *
	 * @param key      变量标识
	 * @param supplier 回调函数（变量不存在时执行，生成返回值）
	 * @return 存在的变量值 或 回调函数的返回值
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getOrElse(String key, Supplier<T> supplier) {
		Map<String, Object> map = THREAD_LOCAL.get();
		if (map.containsKey(key)) {
			return (T) map.get(key);
		}
		// 变量不存在，执行回调函数获取值
		return supplier.get();
	}

	/**
	 * 获取变量，若不存在则创建并存储默认值（适合需要“懒加载”初始化的场景）
	 *
	 * @param key      变量标识
	 * @param supplier 回调函数（生成默认值并存储）
	 * @return 已存在的变量值 或 新创建的默认值（已存入ThreadLocal）
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getOrCreate(String key, Supplier<T> supplier) {
		Map<String, Object> map = THREAD_LOCAL.get();
		if (map.containsKey(key)) {
			return (T) map.get(key);
		}
		// 变量不存在，创建默认值并存储
		T value = supplier.get();
		map.put(key, value);
		return value;
	}

	/**
	 * 判断变量是否存在
	 *
	 * @param key 变量标识
	 * @return true=存在，false=不存在
	 */
	public static boolean has(String key) {
		Map<String, Object> map = THREAD_LOCAL.get();
		return map.containsKey(key);
	}

	/**
	 * 获取变量，若不存在则抛出异常（适合必须存在的场景，如登录用户信息）
	 *
	 * @param key     变量标识
	 * @param message 异常提示信息
	 * @return 变量值（一定非null）
	 * @throws IllegalArgumentException 变量不存在时抛出
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getOrThrow(String key, String message) {
		T value = get(key);
		if (value == null) {
			throw new IllegalArgumentException(message);
		}
		return value;
	}

	/**
	 * @param key 变量标识
	 */
	@Nullable
	@SuppressWarnings("unchecked")
	public static <T> T remove(String key) {
		return (T) THREAD_LOCAL.get().remove(key);
	}

	/**
	 * 添加多线程传递时忽略的 ThreadLocal key
	 *
	 * @param key 多线程传递时忽略的 ThreadLocal key
	 */
	public static void addIgnoredThreadLocalKeyForTransfer(String key) {
		THREAD_LOCAL_KEYS_IGNORED_IN_TRANSMISSION.add(key);
	}

	/**
	 * 获取多线程传递时忽略的 ThreadLocal key 集合
	 */
	public static Set<String> getIgnoredThreadLocalKeysForTransfer() {
		return THREAD_LOCAL_KEYS_IGNORED_IN_TRANSMISSION;
	}

	/**
	 * 注意：线程结束后会自动释放，但线程池中的线程复用会导致变量残留，需手动调用
	 */
	public static void clear() {
		THREAD_LOCAL.remove();
	}
}
