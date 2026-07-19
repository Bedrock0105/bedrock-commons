package org.bedrock.common.http.model;

import lombok.Getter;
import org.bedrock.common.code.util.StringUtil;
import org.bedrock.common.http.enums.MediaTypeCategory;
import org.bedrock.common.http.handler.ContentTypeRegistry;

import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.nio.charset.StandardCharsets;

/**
 * HTTP Content-Type 描述类
 * <p>
 * 核心特性：
 * 1.  集成 Java {@link java.nio.charset.Charset} 类管理字符编码，避免无效编码字符串
 * 2.  提供静态常量快速引用常用Content-Type（如JSON、表单格式等）
 * 3.  支持字符串解析/手动构建，自动提取参数与字符编码
 * 4.  新增类型分类判断：表单类、字符串请求体类、文件上传表单类、二进制类
 * </p>
 *
 * @author 自定义作者
 * @date 自定义日期
 */
public class ContentType {

	// ==================== 公有静态常量（常用Content-Type字符串，直接复用） ====================

	/**
	 * 普通表单编码（application/x-www-form-urlencoded）
	 * <p>当form的action为GET时，浏览器用该编码将表单数据转为键值对字符串（name1=value1&name2=value2…）；POST请求默认也使用该编码</p>
	 */
	public static final String FORM_URLENCODED = "application/x-www-form-urlencoded";

	/**
	 * 文件上传编码（multipart/form-data）
	 * <p>浏览器会以表单控件为单位分割数据，为每个部分添加Content-Disposition头，并通过boundary分隔符区分不同字段（支持文件+普通文本字段）</p>
	 */
	public static final String MULTIPART = "multipart/form-data";

	/**
	 * JSON格式编码（application/json）
	 * <p>RESTful API常用请求/响应格式，数据以JSON结构化字符串传输</p>
	 */
	public static final String JSON = "application/json";

	/**
	 * XML格式编码（application/xml）
	 * <p>结构化数据传输格式，多用于传统SOAP接口</p>
	 */
	public static final String XML = "application/xml";

	/**
	 * 纯文本编码（text/plain）
	 * <p>无格式纯文本数据，不包含任何标记或结构化信息</p>
	 */
	public static final String TEXT_PLAIN = "text/plain";

	/**
	 * 文本XML编码（text/xml）
	 * <p>以纯文本形式传输的XML数据，区别于application/xml（应用层XML）</p>
	 */
	public static final String TEXT_XML = "text/xml";

	/**
	 * HTML文本编码（text/html）
	 * <p>HTML页面格式，浏览器可直接解析渲染</p>
	 */
	public static final String TEXT_HTML = "text/html";

	/**
	 * 通用二进制编码（application/octet-stream）
	 * <p>未知类型的二进制数据，多用于文件下载/上传的通用二进制流传输</p>
	 */
	public static final String OCTET_STREAM = "application/octet-stream";

	// ==================== 私有静态常量（正则表达式，内部解析使用） ====================

	/**
	 * Content-Type字符串解析正则（支持匹配主类型/子类型，及带引号的参数）
	 * 匹配格式：主类型/子类型; 参数1=值1; 参数2="值2"
	 */
	private static final Pattern CONTENT_TYPE_PATTERN = Pattern.compile(
		"^\\s*([^/;\\s]+)/([^;\\s]+)\\s*(;\\s*([^=;\\s]+)\\s*=\\s*(\"[^\"]*\"|[^;\\s]*))*\\s*$"
	);

	/**
	 * Content-Type参数匹配正则（用于提取键值对参数）
	 */
	private static final Pattern PARAM_PATTERN = Pattern.compile(
		"\\s*;\\s*([^=;\\s]+)\\s*=\\s*(\"[^\"]*\"|[^;\\s]*)"
	);

	// ==================== 成员变量（私有/公有，带Getter，按不可变优先级排序） ====================

	/**
	 * 主类型（如application、text、multipart、image）
	 * <p>不可变，通过构造方法初始化</p>
	 */
	@Getter
	private final String mainType;

	/**
	 * 子类型（如json、html、form-data、png）
	 * <p>不可变，通过构造方法初始化</p>
	 */
	@Getter
	private final String subType;

	/**
	 * 参数集合（LinkedHashMap保持插入顺序，排除charset参数，内部维护）
	 * <p>不可变引用，内部数据可通过参数方法修改</p>
	 */
	private final Map<String, String> parameters;

	/**
	 * 字符编码（Java Charset类型，单独管理，与参数集合同步）
	 */
	@Getter
	private Charset charset;

	/**
	 * 媒体类型（MediaTypeCategory枚举类型，内部维护）
	 */
	private String mediaType;

	// ==================== 构造方法（私有，通过静态工厂方法create()创建实例） ====================

	/**
	 * 全参构造器（指定主类型、子类型、参数集合）
	 * <p>自动从参数集合中提取charset字符串并转换为Charset对象，提取后移除参数集合中的charset项</p>
	 *
	 * @param mainType   主类型（非null）
	 * @param subType    子类型（非null）
	 * @param parameters 参数集合（非null）
	 */
	private ContentType(String mainType, String subType, Map<String, String> parameters) {
		this.mainType = Objects.requireNonNull(mainType, "主类型mainType不能为空");
		this.subType = Objects.requireNonNull(subType, "子类型subType不能为空");
		this.parameters = new LinkedHashMap<>(Objects.requireNonNull(parameters, "参数集合parameters不能为空"));

		// 提取并转换charset参数
		String charsetStr = this.parameters.get("charset");
		if (charsetStr != null && !charsetStr.isBlank()) {
			try {
				this.charset = Charset.forName(charsetStr.trim());
				this.parameters.remove("charset"); // 移除已提取的charset参数，避免重复存储
			} catch (IllegalArgumentException e) {
				this.charset = null; // 无效编码设为null
			}
		}
	}

	/**
	 * 简化构造器（指定主类型、子类型，无参数）
	 *
	 * @param mainType 主类型（非null）
	 * @param subType  子类型（非null）
	 */
	private ContentType(String mainType, String subType) {
		this(mainType, subType, new LinkedHashMap<>());
	}

	/**
	 * 字符串解析构造器（核心：自动解析完整Content-Type字符串为对象）
	 *
	 * @param contentTypeStr 完整的Content-Type字符串（如"application/json; charset=utf-8"，非null）
	 * @throws IllegalArgumentException 无效的Content-Type格式时抛出异常
	 */
	private ContentType(String contentTypeStr) {
		Objects.requireNonNull(contentTypeStr, "Content-Type字符串不能为空");
		this.parameters = new LinkedHashMap<>();

		// 第一步：匹配主类型与子类型
		Matcher mainMatcher = CONTENT_TYPE_PATTERN.matcher(contentTypeStr);
		if (!mainMatcher.matches()) {
			throw new IllegalArgumentException("无效的Content-Type格式：" + contentTypeStr);
		}

		// 提取并小写存储，保证一致性
		this.mainType = mainMatcher.group(1).trim().toLowerCase();
		this.subType = mainMatcher.group(2).trim().toLowerCase();

		// 第二步：提取所有参数并处理charset
		Matcher paramMatcher = PARAM_PATTERN.matcher(contentTypeStr);
		while (paramMatcher.find()) {
			String paramKey = paramMatcher.group(1).trim().toLowerCase();
			String paramValue = paramMatcher.group(2).trim();

			// 去除参数值的首尾引号
			if (paramValue.startsWith("\"") && paramValue.endsWith("\"")) {
				paramValue = paramValue.substring(1, paramValue.length() - 1);
			}

			// 单独处理charset参数，其余参数存入参数集合
			if ("charset".equals(paramKey) && !paramValue.isBlank()) {
				this.charset = Charset.forName(paramValue);
			} else {
				this.parameters.put(paramKey, paramValue);
			}
		}
	}

	// ==================== 静态工厂方法（公有，创建ContentType实例，替代直接new） ====================

	/**
	 * 从Content-Type字符串创建实例
	 *
	 * @param contentType 完整的Content-Type字符串（如"application/json; charset=utf-8"）
	 * @return ContentType实例
	 */
	public static ContentType create(String contentType) {
		if (StringUtil.isBlank(contentType)) {
			return null;
		}
		return new ContentType(contentType);
	}

	/**
	 * 从主类型+子类型创建实例（无参数）
	 *
	 * @param mainType 主类型（如"application"）
	 * @param subType  子类型（如"json"）
	 * @return ContentType实例
	 */
	public static ContentType create(String mainType, String subType) {
		return new ContentType(mainType, subType);
	}

	/**
	 * 从主类型+子类型+参数集合创建实例
	 *
	 * @param mainType   主类型（如"multipart"）
	 * @param subType    子类型（如"form-data"）
	 * @param parameters 参数集合（如boundary参数）
	 * @return ContentType实例
	 */
	public static ContentType create(String mainType, String subType, Map<String, String> parameters) {
		return new ContentType(mainType, subType, parameters);
	}

	// ==================== Charset 专属操作方法（核心：字符编码获取与设置） ====================

	/**
	 * 获取Charset对象，若为null则返回默认Charset
	 * <p>避免空指针异常，推荐优先使用该方法</p>
	 *
	 * @param defaultValue 默认Charset（如 {@link StandardCharsets#UTF_8}）
	 * @return 实际Charset对象或默认值
	 */
	public Charset getCharset(Charset defaultValue) {
		return this.charset == null ? defaultValue : this.charset;
	}

	/**
	 * 设置Charset对象（自动同步编码信息，支持链式调用）
	 *
	 * @param charset Java Charset对象（可为null，null表示移除编码配置）
	 * @return 当前ContentType实例（链式调用）
	 */
	public ContentType setCharset(Charset charset) {
		this.charset = charset;
		return this;
	}

	/**
	 * 重载：通过编码名字符串设置Charset
	 * <p>兼容字符串入参，无效编码会抛出IllegalArgumentException</p>
	 *
	 * @param charsetName 编码名（如"utf-8"、"GBK"，可为null或空字符串）
	 * @return 当前ContentType实例（链式调用）
	 * @throws IllegalArgumentException 无效编码名时抛出异常
	 */
	public ContentType setCharset(String charsetName) {
		if (charsetName == null || charsetName.isBlank()) {
			this.setCharset((Charset) null);
			return this;
		}
		Charset charset = Charset.forName(charsetName.trim());
		this.setCharset(charset);
		return this;
	}

	// ==================== 类型分类判断方法（核心：业务类型快速识别） ====================

	/**
	 * 通配符匹配（模仿 Spring MediaType）
	 * 支持：image/*、application/json 等格式匹配
	 *
	 * @param pattern 匹配模式（如 "image/*"）
	 * @return true=匹配，false=不匹配
	 */
	public boolean matches(String pattern) {
		if (pattern == null || pattern.isBlank()) {
			return false;
		}
		ContentType patternType = ContentType.create(pattern);
		// 主类型匹配（支持 * 通配符）
		boolean typeMatch = "*".equals(patternType.getMainType())
			|| this.mainType.equals(patternType.getMainType());
		// 子类型匹配（支持 * 通配符）
		boolean subTypeMatch = "*".equals(patternType.getSubType())
			|| this.subType.equals(patternType.getSubType());
		return typeMatch && subTypeMatch;
	}

	/**
	 * 获取媒体类型分类
	 */
	public MediaTypeCategory getMediaTypeCategory() {
		return ContentTypeRegistry.getCategory(this);
	}

	// ==================== 参数操作方法（内部参数管理，支持链式调用） ====================

	/**
	 * 添加/覆盖参数（若为charset参数，自动同步到Charset对象）
	 *
	 * @param key   参数名（非null）
	 * @param value 参数值（非null）
	 * @return 当前ContentType实例（链式调用）
	 */
	public ContentType addParameter(String key, String value) {
		Objects.requireNonNull(key, "参数名不能为空");
		Objects.requireNonNull(value, "参数值不能为空");
		String lowerKey = key.trim().toLowerCase();
		String trimValue = value.trim();

		// 单独处理charset参数，其余参数存入参数集合
		if ("charset".equals(lowerKey)) {
			this.charset = Charset.forName(trimValue);
		} else {
			this.parameters.put(lowerKey, trimValue);
		}
		return this;
	}

	/**
	 * 移除参数（若为charset参数，自动同步清空Charset对象）
	 *
	 * @param key 参数名（可为null，null时不执行操作）
	 * @return 当前ContentType实例（链式调用）
	 */
	public ContentType removeParameter(String key) {
		if (key == null) {
			return this;
		}
		String lowerKey = key.trim().toLowerCase();
		this.parameters.remove(lowerKey);

		// 移除charset参数时，同步清空Charset对象
		if ("charset".equals(lowerKey)) {
			this.charset = null;
		}
		return this;
	}

	/**
	 * 判断是否包含指定参数
	 *
	 * @param key 参数名（可为null，null时返回false）
	 * @return true=包含该参数，false=不包含该参数
	 */
	public boolean containsParameter(String key) {
		if (key == null) {
			return false;
		}
		return this.parameters.containsKey(key.trim().toLowerCase());
	}

	/**
	 * 获取指定参数值
	 *
	 * @param key 参数名（可为null，null时返回null）
	 * @return 参数值（不存在时返回null）
	 */
	public String getParameter(String key) {
		if (key == null) {
			return null;
		}
		return this.parameters.get(key.trim().toLowerCase());
	}

	// ==================== 辅助方法（工具性方法，增强易用性） ====================

	/**
	 * 获取完整的Media Type（主类型/子类型，如application/json）
	 *
	 * @return 媒体类型字符串
	 */
	public String getMediaType() {
		String mediaType = this.mediaType;
		if (StringUtil.isBlank(mediaType)) {
			mediaType = String.format("%s/%s", this.mainType, this.subType);
			this.mediaType = mediaType;
		}
		return mediaType;
	}

	/**
	 * 获取不可修改的参数集合（防止外部篡改内部数据）
	 *
	 * @return 不可变的Map<String, String>参数集合
	 */
	public Map<String, String> getParameters() {
		return Map.copyOf(parameters);
	}

	// ==================== 重写Object方法（规范对象比较与字符串输出） ====================

	/**
	 * 拼接为标准的Content-Type字符串（符合HTTP规范）
	 * <p>格式：主类型/子类型; 参数1=值1; 参数2="值2"; charset=utf-8</p>
	 *
	 * @return 标准Content-Type字符串
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(getMediaType());

		// 拼接普通参数
		for (Map.Entry<String, String> entry : this.parameters.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			// 参数值包含空格/分号时，用引号包裹
			if (value.contains(" ") || value.contains(";")) {
				sb.append("; ").append(key).append("=\"").append(value).append("\"");
			} else {
				sb.append("; ").append(key).append("=").append(value);
			}
		}
		// 拼接charset参数（单独处理，保证顺序在普通参数之后）
		if (this.charset != null) {
			sb.append("; charset=").append(this.charset.name());
		}
		return sb.toString();
	}

	/**
	 * 判断两个ContentType对象是否一致
	 * <p>忽略主类型/子类型的大小写，忽略参数顺序</p>
	 *
	 * @param o 对比对象
	 * @return true=一致，false=不一致
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ContentType that = (ContentType) o;
		return Objects.equals(mainType.toLowerCase(), that.mainType.toLowerCase())
			&& Objects.equals(subType.toLowerCase(), that.subType.toLowerCase())
			&& Objects.equals(parameters, that.parameters)
			&& Objects.equals(charset, that.charset);
	}

	/**
	 * 获取对象哈希值
	 *
	 * @return 哈希值
	 */
	@Override
	public int hashCode() {
		return Objects.hash(mainType.toLowerCase(), subType.toLowerCase(), parameters, charset);
	}
}
