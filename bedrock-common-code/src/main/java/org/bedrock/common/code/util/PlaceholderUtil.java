package org.bedrock.common.code.util;

import org.bedrock.common.code.constant.StringPool;
import org.springframework.lang.NonNull;

import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * 占位符解析器
 */
public abstract class PlaceholderUtil {

	/**
	 * 默认前缀占位符
	 */
	public static final String DEFAULT_PLACEHOLDER_PREFIX = "${";

	/**
	 * 默认后缀占位符
	 */
	public static final String DEFAULT_PLACEHOLDER_SUFFIX = "}";

	/**
	 * 解析带有指定占位符的模板字符串，默认占位符为前缀：${  后缀：}<br/><br/>
	 * 如：template = category:${}:product:${}<br/>
	 * values = {"1", "2"}<br/>
	 * 返回 category:1:product:2<br/>
	 *
	 * @param content 要解析的带有占位符的模板字符串
	 * @param values  按照模板占位符索引位置设置对应的值
	 * @return {String}
	 */
	public static String resolve(@NonNull String prefix, @NonNull String suffix, String content, String... values) {
		if (content == null || values == null) {
			return content;
		}

		// 转义前缀和后缀中的特殊字符
		String escapedPrefix = Pattern.quote(prefix);
		String escapedSuffix = Pattern.quote(suffix);

		// 匹配占位符的正则表达式
		Pattern pattern = Pattern.compile(escapedPrefix + "\\s*" + escapedSuffix);
		Matcher matcher = pattern.matcher(content);

		StringBuilder result = new StringBuilder();
		int paramIndex = 0; // 跟踪参数数组的索引

		while (matcher.find()) {
			// 如果参数数组有足够的元素，则替换；否则保留原始占位符
			String replacement = (paramIndex < values.length)
				? values[paramIndex]
				: matcher.group(0);

			matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
			paramIndex++;
		}
		matcher.appendTail(result);
		return result.toString();
	}

	/**
	 * 解析带有指定占位符的模板字符串，默认占位符为前缀：${  后缀：}<br/><br/>
	 * 如：template = category:${}:product:${}<br/>
	 * values = {"1", "2"}<br/>
	 * 返回 category:1:product:2<br/>
	 *
	 * @param content 要解析的带有占位符的模板字符串
	 * @param values  按照模板占位符索引位置设置对应的值
	 * @return {String}
	 */
	public static String resolve(@NonNull String prefix, @NonNull String suffix, String content, Object[] values) {
		return resolve(prefix, suffix, content, Stream.of(values).map(String::valueOf).toArray(String[]::new));
	}

	/**
	 * 根据替换规则来替换指定模板中的占位符值
	 *
	 * @param content 要解析的字符串
	 * @param rule    解析规则回调
	 * @return {String}
	 */
	public static String resolveByRule(@NonNull String prefix, @NonNull String suffix, String content, Function<String, String> rule) {
		if (content == null || rule == null) {
			return content;
		}

		// 转义前缀和后缀中的特殊字符，确保正则表达式安全
		String escapedPrefix = Pattern.quote(prefix);
		String escapedSuffix = Pattern.quote(suffix);

		// 构建正则表达式：匹配前缀+任意字符(非贪婪)+后缀
		Pattern pattern = Pattern.compile(escapedPrefix + "(.*?)" + escapedSuffix);
		Matcher matcher = pattern.matcher(content);

		StringBuilder result = new StringBuilder();
		while (matcher.find()) {
			// 获取占位符名称（去掉前缀后缀）
			String placeholderName = matcher.group(1);
			// 从map中获取对应的值，不存在则保留原始占位符
			String replacement = rule.apply(placeholderName);
			// 替换当前匹配项
			matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
		}
		// 追加剩余部分
		matcher.appendTail(result);

		return result.toString();
	}

	/**
	 * 替换模板中占位符内容，占位符的内容即为map key对应的值，key为占位符中的内容。<br/><br/>
	 * 如：content = product:${id}:detail:${did}<br/>
	 * valueMap = id -> 1; pid -> 2<br/>
	 * 经过解析返回 product:1:detail:2<br/>
	 *
	 * @param content  模板内容
	 * @param valueMap 值映射
	 * @return 替换完成后的字符串
	 */
	public static String resolveByMap(@NonNull String prefix, @NonNull String suffix, String content, final Map<String, Object> valueMap) {
		return resolveByRule(prefix, suffix, content, placeholderValue -> {
			String[] splitTrim = StringUtil.split(placeholderValue, StringPool.DOT);
			/**
			 * 如果是1 没有点
			 */
			if (splitTrim.length == 1) {
				Object value = valueMap.get(placeholderValue);
				return value == null ? StringPool.EMPTY : value.toString();
			}
			/**
			 * 如果包含多层级嵌套
			 * 需要继续往下面找到最后一个
			 */
			Object value = valueMap.get(splitTrim[0]);
			for (int i = 1; i < splitTrim.length; i++) {
				if (!(value instanceof Map)) {
					return StringPool.EMPTY;
				}
				Object obj = ((Map) value).get(splitTrim[i]);
				if (obj == null) {
					return StringPool.EMPTY;
				}
				value = obj;
			}
			return value.toString();
		});
	}
}
