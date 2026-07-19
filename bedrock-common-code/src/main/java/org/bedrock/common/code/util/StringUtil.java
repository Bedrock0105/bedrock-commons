package org.bedrock.common.code.util;

import org.bedrock.common.code.constant.StringPool;
import org.springframework.lang.Contract;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 字符串工具类（扩展 Spring StringUtils）
 * <p>核心特性：
 * 1. 继承 {@link org.springframework.util.StringUtils}，复用其核心字符串处理能力；
 * 2. 扩展业务常用的字符串转换、格式化、解析、UUID 生成等功能；
 * 3. 所有方法均为静态方法，无需实例化（抽象类设计，禁止实例化）；
 * 4. 空值友好：所有方法均做空值兜底，避免空指针异常。
 * </p>
 *
 */
public abstract class StringUtil extends org.springframework.util.StringUtils {

	/**
	 * 将任意对象转换为字符串（空值兜底）
	 * <p>空值场景返回指定默认值，非空对象直接调用 {@link Object#toString()} 方法</p>
	 *
	 * @param obj        待转换的对象（可为 null）
	 * @return 转换后的字符串：obj 非空返回 obj.toString()，否则返回 defaultVal
	 * @example // 输出 "123"
	 * StringUtil.toStr(123, "");
	 * // 输出 "default"
	 * StringUtil.toStr(null, "default");
	 */
	public static String toStr(Object obj) {
		return toStr(obj, StringPool.EMPTY);
	}

	/**
	 * 将任意对象转换为字符串（空值兜底）
	 * <p>空值场景返回指定默认值，非空对象直接调用 {@link Object#toString()} 方法</p>
	 *
	 * @param obj        待转换的对象（可为 null）
	 * @param defaultVal 空值时返回的默认值（可为空字符串）
	 * @return 转换后的字符串：obj 非空返回 obj.toString()，否则返回 defaultVal
	 * @example // 输出 "123"
	 * StringUtil.toStr(123, "");
	 * // 输出 "default"
	 * StringUtil.toStr(null, "default");
	 */
	public static String toStr(Object obj, String defaultVal) {
		return obj == null ? defaultVal : obj.toString();
	}

	/**
	 * 判断字符串是否为空（空白字符串/空字符串/null 均判定为空）
	 * <p>等价于 !{@link #hasText(CharSequence)}，语义更贴合业务习惯</p>
	 *
	 * @param str 待判断的字符串（可为 null）
	 * @return true=空（null/"" / "   "），false=非空且包含有效字符
	 * @example // 返回 true
	 * StringUtil.isBlank(null);
	 * StringUtil.isBlank("   ");
	 * // 返回 false
	 * StringUtil.isBlank("abc");
	 */
	@Contract("null -> true")
	public static boolean isBlank(CharSequence str) {
		return !hasText(str);
	}

	/**
	 * 判断字符串是否不为空（空白字符串/空字符串/null 均判定为空）
	 * <p>等价于 {@link #hasText(CharSequence)}，语义更贴合业务习惯</p>
	 *
	 * @param str 待判断的字符串（可为 null）
	 * @return true=非空且包含有效字符，false=空（null/"" / "   "）
	 * @example // 返回 false
	 * StringUtil.isNotBlank(null);
	 * // 返回 true
	 * StringUtil.isNotBlank("  abc  ");
	 */
	@Contract("null -> false")
	public static boolean isNotBlank(CharSequence str) {
		return hasText(str);
	}

	/**
	 * 将分隔符为逗号的字符串拆分为字符串列表（默认分隔符：逗号）
	 * <p>空值/空白字符串返回空列表，拆分后自动过滤空字符串（仅基于 split 原生逻辑）</p>
	 *
	 * @param val 待拆分的字符串（如 "a,b,c"，可为 null/空白）
	 * @return 拆分后的字符串列表：空值返回 {@link Collections#emptyList()}，非空按逗号拆分
	 * @example // 返回 ["a", "b", "c"]
	 * StringUtil.toListStr("a,b,c");
	 * // 返回空列表
	 * StringUtil.toListStr("");
	 */
	public static List<String> toListStr(String val) {
		return toListStr(val, StringPool.COMMA);
	}

	/**
	 * 将指定分隔符的字符串拆分为字符串列表
	 * <p>空值/空白字符串返回空列表，拆分后自动过滤空字符串（仅基于 split 原生逻辑）</p>
	 *
	 * @param val       待拆分的字符串（可为 null/空白）
	 * @param separator 拆分分隔符（如 ","、"|" 等，不可为 null，建议使用 {@link StringPool} 常量）
	 * @return 拆分后的字符串列表：空值返回 {@link Collections#emptyList()}，非空按指定分隔符拆分
	 * @example // 返回 ["1", "2", "3"]
	 * StringUtil.toListStr("1|2|3", StringPool.VERTICAL_BAR);
	 */
	public static List<String> toListStr(String val, String separator) {
		if (!StringUtil.hasText(val)) {
			return Collections.emptyList();
		}
		return Arrays.stream(val.split(separator))
			.collect(Collectors.toList()); // 替换 toList() 为 collect(Collectors.toList())，兼容 Java 8
	}

	/**
	 * 解析索引型占位符模板字符串（默认占位符：{}）
	 * <p>按占位符索引顺序填充值，支持任意数量占位符，占位符数量多于值时，多余占位符保留原样</p>
	 *
	 * @param content 带索引占位符的模板字符串（如 "category:{}:product:{}"，可为 null/空白）
	 * @param values  按索引填充的值数组（可为 null，空数组时返回原模板）
	 * @return 解析后的字符串：空模板返回空字符串，值数组为空返回原模板
	 * @example // 返回 "category:1:product:2"
	 * StringUtil.format("category:{}:product:{}", "1", "2");
	 * // 返回 "category:{}:product:2"（占位符多于值）
	 * StringUtil.format("category:{}:product:{}", "2");
	 */
	public static String format(String content, String... values) {
		return PlaceholderUtil.resolve(StringPool.LEFT_BRACE, StringPool.RIGHT_BRACE, content, values);
	}

	/**
	 * 解析索引型占位符模板字符串（重载：List 类型值）
	 * <p>将 List 转换为数组后调用 {@link #format(String, String...)}，语义更贴合 List 入参场景</p>
	 *
	 * @param content 带索引占位符的模板字符串（可为 null/空白）
	 * @param values  按索引填充的值列表（可为 null/空列表）
	 * @return 解析后的字符串
	 * @example // 返回 "category:1:product:2"
	 * StringUtil.format("category:{}:product:{}", Arrays.asList("1", "2"));
	 */
	public static String format(String content, List<String> values) {
		return format(content, values.toArray(new String[0]));
	}

	/**
	 * 解析命名型占位符模板字符串（默认占位符：{}）
	 * <p>按占位符名称匹配值，如模板 "category:{name}:product:{pwd}" + Map {name:"a", pwd:"b"} → "category:a:product:b"</p>
	 *
	 * @param content 带命名占位符的模板字符串（可为 null/空白）
	 * @param values  占位符名称-值映射 Map（可为 null/空 Map）
	 * @return 解析后的字符串：空模板返回空字符串，Map 为空返回原模板
	 * @example Map<String, String> map = new HashMap<>();
	 * map.put("name", "张三");
	 * map.put("age", "25");
	 * // 返回 "user:{name}:age:25"（未匹配的占位符保留原样）
	 * StringUtil.format("user:{name}:age:{age}:sex:{sex}", map);
	 */
	public static String format(String content, Map<String, String> values) {
		return PlaceholderUtil.resolveByRule(StringPool.LEFT_BRACE, StringPool.RIGHT_BRACE, content, values::get);
	}

	/**
	 * 解析命名型占位符模板字符串（默认占位符：{}）
	 * <p>按占位符名称匹配值，如模板 "category:{name}:product:{pwd}" + Map {name:"a", pwd:"b"} → "category:a:product:b"</p>
	 *
	 * @param content 带命名占位符的模板字符串（可为 null/空白）
	 * @return 解析后的字符串：空模板返回空字符串，Map 为空返回原模板
	 * @example Map<String, String> map = new HashMap<>();
	 * map.put("name", "张三");
	 * map.put("age", "25");
	 * // 返回 "user:{name}:age:25"（未匹配的占位符保留原样）
	 * StringUtil.format("user:{name}:age:{age}:sex:{sex}", map);
	 */
	public static String format(String content, Function<String, String> function) {
		return PlaceholderUtil.resolveByRule(StringPool.LEFT_BRACE, StringPool.RIGHT_BRACE, content, function);
	}

	/**
	 * 解析 Spring 风格命名型占位符模板字符串（默认占位符：${}）
	 * <p>适配 Spring 标准占位符风格，如模板 "category:${name}:product:${pwd}" + Map {name:"a"} → "category:a:product:${pwd}"</p>
	 *
	 * @param content 带 Spring 风格占位符的模板字符串（可为 null/空白）
	 * @param values  占位符名称-值映射 Map（可为 null/空 Map）
	 * @return 解析后的字符串：空模板返回空字符串，Map 为空返回原模板
	 * @example Map<String, String> map = new HashMap<>();
	 * map.put("name", "李四");
	 * // 返回 "category:李四:product:${pwd}"
	 * StringUtil.resolve("category:${name}:product:${pwd}", map);
	 */
	public static String resolve(String content, Map<String, String> values) {
		return PlaceholderUtil.resolveByRule(PlaceholderUtil.DEFAULT_PLACEHOLDER_PREFIX, PlaceholderUtil.DEFAULT_PLACEHOLDER_SUFFIX, content, values::get);
	}

	/**
	 * 生成无横线的随机 UUID 字符串
	 * <p>基于 {@link ThreadLocalRandom} 生成高性能随机 UUID，自动移除 UUID 中的横线（-），保证唯一性</p>
	 *
	 * @return 32位无横线的 UUID 字符串（如 "550e8400e29b41d4a716446655440000"）
	 * @example // 输出类似 "550e8400e29b41d4a716446655440000" 的字符串
	 * StringUtil.randomUUID();
	 */
	public static String randomUUID() {
		ThreadLocalRandom random = ThreadLocalRandom.current();
		return new UUID(random.nextLong(), random.nextLong()).toString().replace(StringPool.DASH, StringPool.EMPTY);
	}
}
