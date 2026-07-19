package org.bedrock.common.code.util;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

public abstract class UriUtil {

	/**
	 * 合法 URL Scheme 正则（RFC 3986 规范）：
	 * ^[a-zA-Z][a-zA-Z0-9+-.]*://
	 * - ^[a-zA-Z]：scheme 以字母开头
	 * - [a-zA-Z0-9+-.]*：scheme 后续字符（可选）
	 * - ://：scheme 分隔符
	 */
	private static final Pattern URL_SCHEME_PATTERN = Pattern.compile("^[a-zA-Z][a-zA-Z0-9+-.]*://");

	/**
	 * 判断字符串是否为「携带协议的完整 URL」（非仅路径）
	 *
	 * @param str 待判断的字符串（可为 null/空白）
	 * @return true = 完整URL（含合法scheme://）；false = 仅path/空字符串/非法格式
	 */
	public static boolean isFullUrl(String str) {
		// 1. 空值/空白直接判定为非URL
		if (str == null || str.isBlank()) {
			return false;
		}
		// 2. 匹配 scheme:// 正则，判定为完整URL
		return URL_SCHEME_PATTERN.matcher(str).find();
	}

	/**
	 * 判断字符串是否为「仅路径（path）」（非完整URL）
	 *
	 * @param str 待判断的字符串（可为 null/空白）
	 * @return true = 仅path；false = 完整URL/空字符串
	 */
	public static boolean isOnlyPath(String str) {
		// 空值不算path
		if (str == null || str.isBlank()) {
			return false;
		}
		// 非完整URL → 判定为path
		return !isFullUrl(str);
	}

	/**
	 * URL 编码
	 *
	 * @param value 待编码的值
	 * @return 编码后的值
	 */
	public static String encode(String value) {
		return encode(value, StandardCharsets.UTF_8);
	}

	/**
	 * URL 编码
	 *
	 * @param value 待编码的值
	 * @return 编码后的值
	 */
	public static String encode(String value, Charset charset) {
		return URLEncoder.encode(value, charset);
	}

	/**
	 * URL 解码
	 *
	 * @param value 待解码的值
	 * @return 解码后的值
	 */
	public static String decode(String value) {
		return decode(value, StandardCharsets.UTF_8);
	}

	/**
	 * URL 解码
	 *
	 * @param value 待解码的值
	 * @return 解码后的值
	 */
	public static String decode(String value, Charset charset) {
		return URLDecoder.decode(value, charset);
	}
}
