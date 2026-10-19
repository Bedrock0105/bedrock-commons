package org.bedrock.common.code.util;

import org.springframework.lang.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

public abstract class CollectionUtil extends org.springframework.util.CollectionUtils {

	/**
	 * 判断集合是否非空。
	 */
	public static boolean isNotEmpty(@Nullable Collection<?> collection) {
		return !isEmpty(collection);
	}

	/**
	 * 判断 Map 是否非空。
	 */
	public static boolean isNotEmpty(@Nullable Map<?, ?> map) {
		return !isEmpty(map);
	}

	/**
	 * 判断数组是否为空。
	 */
	public static boolean isEmpty(@Nullable Object[] array) {
		return ObjectUtil.isEmpty(array);
	}

	/**
	 * 判断数组是否非空。
	 */
	public static boolean isNotEmpty(@Nullable Object[] array) {
		return ObjectUtil.isNotEmpty(array);
	}

	/**
	 * 按候选 key 依次查找，返回第一个非空白字符串，都没有时返回默认值。
	 */
	public static String getNonBlank(Map<String, String> map, String def, String... keys) {
		if (map == null || keys == null) {
			return def;
		}
		return Arrays.stream(keys)
			.map(map::get)
			.filter(StringUtil::isNotBlank)
			.findFirst()
			.orElse(def);
	}

	/**
	 * 按候选 key 依次查找，返回第一个非空值，都没有时返回默认值。
	 */
	public static Object getNonEmpty(Map<String, Object> map, Object def, String... keys) {
		if (map == null || keys == null) {
			return def;
		}
		return Arrays.stream(keys)
			.map(map::get)
			.filter(ObjectUtil::isNotEmpty)
			.findFirst()
			.orElse(def);
	}
}
