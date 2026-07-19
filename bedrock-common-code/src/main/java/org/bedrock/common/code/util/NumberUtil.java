package org.bedrock.common.code.util;

import org.bedrock.common.code.constant.StringPool;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public abstract class NumberUtil extends org.springframework.util.NumberUtils {

	/**
	 * 转换为Long列表
	 *
	 * @param val
	 * @return
	 */
	public static List<Long> toListLong(String val) {
		return toListLong(val, StringPool.COMMA);
	}

	/**
	 * 转换为Long列表
	 *
	 * @param val 要转换的值
	 * @param separator 分隔符
	 */
	public static List<Long> toListLong(String val, String separator) {
		if (!StringUtil.hasText(val)) {
			return Collections.emptyList();
		}
		return Arrays.stream(val.split(separator))
			.map(Long::parseLong)
			.toList();
	}

	/**
	 * 转换为long型
	 *
	 * @param val
	 * @return
	 */
	public static long toLong(Object val) {
		return toLong(val, 0);
	}

	/**
	 * 转换为long型
	 */
	public static long toLong(Object val, long defaultValue) {
		if (val == null) {
			return defaultValue;
		}
		try {
			return Long.parseLong(val.toString());
		} catch (Exception e) {
			return defaultValue;
		}
	}

	/**
	 * 转换为int型
	 */
	public static int toInt(Object val) {
		return toInt(val, 0);
	}

	/**
	 * 转换为int型
	 *
	 * @param val 值
	 */
	public static int toInt(Object val, int defaultValue) {
		if (val == null) {
			return defaultValue;
		}
		try {
			return Integer.parseInt(val.toString());
		} catch (Exception e) {
			return defaultValue;
		}
	}
}
