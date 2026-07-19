package org.bedrock.common.http.model;

import org.bedrock.common.code.constant.StringPool;
import org.bedrock.common.code.util.StringUtil;
import org.bedrock.common.code.util.UriUtil;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * URL 元数据封装类
 * <p>核心功能：解析/封装 URL 的各组成部分（scheme、host、port、path、query 等），支持路径格式化、Query 编码、URL 拼接与转换，提供链式调用 API</p>
 * <p>URL 标准结构参考 RFC 3986：[scheme:]//[userinfo@]host[:port][/basePath][/path][?query][#ref]</p>
 */
public class UrlMeta {

	/**
	 * URL 协议/方案（对应 URL.getProtocol()，如 http/https/ftp）
	 */
	private String scheme;

	/**
	 * 服务器主机名/IP（如 example.com、127.0.0.1）
	 */
	private String host;

	/**
	 * 端口号（URL 中显式指定的端口，无则为 -1；默认端口需通过 {@link #getEffectivePort()} 补充）
	 */
	private int port = -1;

	/**
	 * Query 参数（未编码的原始字符串，如 "user name=张三&age=25"）
	 */
	private String query;

	/**
	 * 基础路径（如 /api/v1，与 path 拼接为完整路径，需配合 {@link #formatPath(String)} 格式化）
	 */
	private String basePath;

	/**
	 * 业务路径（如 /user，与 basePath 拼接为完整路径，需配合 {@link #formatPath(String)} 格式化）
	 */
	private String path;

	/**
	 * 用户认证信息（URL 中 @ 前的部分，如 admin:123456，非安全场景慎用）
	 */
	private String userInfo;

	/**
	 * 锚点/片段（URL 中 # 后的部分，仅客户端解析，不随 HTTP 请求发送；参考 RFC 3986 的 fragment 定义）
	 */
	private String ref;

	/**
	 * 无参构造器：创建空的 URL 元数据对象，需通过链式调用设置属性
	 */
	public UrlMeta() {
	}

	/**
	 * 构造器：通过 {@link URL} 对象初始化 URL 元数据
	 *
	 * @param url 标准 URL 对象（非 null）
	 */
	public UrlMeta(URL url) {
		setUrl(url);
	}

	/**
	 * 构造器：通过 {@link UrlMeta} 对象初始化 URL 元数据
	 */
	public UrlMeta(UrlMeta original) {
		copyFrom(original);
	}

	/**
	 * 链式设置 URL 元数据：通过 {@link UrlMeta} 拷贝并初始化元数据
	 *
	 * @param original 源 URL 元数据对象（非 null）
	 * @return 当前 UrlMeta 实例（链式调用）
	 */
	public UrlMeta copyFrom(UrlMeta original) {
		this.scheme = original.scheme;
		this.host = original.host;
		this.port = original.port;
		this.query = original.query;
		this.basePath = original.basePath;
		this.path = original.path;
		this.userInfo = original.userInfo;
		this.ref = original.ref;
		return this;
	}

	public UrlMeta mergeFrom(UrlMeta original) {
		if (StringUtil.isBlank(this.scheme)) {
			this.scheme = original.scheme;
		}
		if (StringUtil.isBlank(this.host)) {
			this.host = original.host;
		}
		if (this.port < 0) {
			this.port = original.port;
		}
		if (StringUtil.isBlank(this.query)) {
			this.query = original.query;
		}
		if (StringUtil.isBlank(this.basePath)) {
			this.basePath = original.basePath;
		}
		if (StringUtil.isBlank(this.path)) {
			this.path = original.path;
		}
		if (StringUtil.isBlank(this.userInfo)) {
			this.userInfo = original.userInfo;
		}
		if (StringUtil.isBlank(this.ref)) {
			this.ref = original.ref;
		}
		return this;
	}

	/**
	 * 链式设置 URL：通过字符串解析为 URL 对象，初始化元数据
	 *
	 * @param urlStr URL 字符串（非 null/非空）
	 * @return 当前 UrlMeta 实例（链式调用）
	 * @throws IllegalArgumentException 当 URL 字符串为空或格式非法时抛出
	 */
	public UrlMeta setUrl(String urlStr) {
		if (urlStr == null || urlStr.isBlank()) {
			throw new IllegalArgumentException("URL string must not be null or blank");
		}
		try {
			return setUrl(new URL(urlStr));
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException("Invalid URL format: " + urlStr, e);
		}
	}

	/**
	 * 链式设置 URL：通过 {@link URL} 对象解析并填充所有元数据字段
	 *
	 * @param url 标准 URL 对象（非 null）
	 * @return 当前 UrlMeta 实例（链式调用）
	 */
	public UrlMeta setUrl(URL url) {
		this.scheme = url.getProtocol(); // scheme 对应 URL 的 protocol 字段（Java 命名习惯）
		this.host = url.getHost();
		this.port = url.getPort(); // 无显式端口返回 -1，需配合默认端口使用
		this.query = url.getQuery(); // 原始未编码的 Query 字符串
		this.path = url.getPath();
		this.userInfo = url.getUserInfo();
		this.ref = url.getRef(); // 锚点/片段
		this.basePath = "";
		return this;
	}

	/**
	 * 链式设置 URL 协议/方案
	 *
	 * @param scheme 协议名（如 http/https/ftp）
	 * @return 当前 UrlMeta 实例（链式调用）
	 */
	public UrlMeta setScheme(String scheme) {
		this.scheme = scheme;
		return this;
	}

	/**
	 * 链式设置基础路径（自动格式化路径）
	 *
	 * @param basePath 基础路径（如 /api/v1）
	 * @return 当前 UrlMeta 实例（链式调用）
	 */
	public UrlMeta setBasePath(String basePath) {
		basePath = formatPath(basePath);
		if (StringUtil.isBlank(basePath)) {
			return this;
		}
		this.basePath = basePath;
		return this;
	}

	/**
	 * 链式设置业务路径（自动格式化路径）
	 *
	 * @param path 业务路径（如 /user/list）
	 * @return 当前 UrlMeta 实例（链式调用）
	 */
	public UrlMeta setPath(String path) {
		path = formatPath(path);
		if (StringUtil.isBlank(path)) {
			return this;
		}
		this.path = path;
		return this;
	}

	/**
	 * 链式设置 Query 参数（原始未编码字符串）
	 *
	 * @param query Query 字符串（如 "user name=张三&age=25"）
	 * @return 当前 UrlMeta 实例（链式调用）
	 */
	public UrlMeta setQuery(String query) {
		this.query = query;
		return this;
	}

	/**
	 * 链式设置用户认证信息
	 *
	 * @param userInfo 用户信息（如 admin:123456）
	 * @return 当前 UrlMeta 实例（链式调用）
	 */
	public UrlMeta setUserInfo(String userInfo) {
		this.userInfo = userInfo;
		return this;
	}

	/**
	 * 链式设置锚点/片段
	 *
	 * @param ref 锚点字符串（如 section-1）
	 * @return 当前 UrlMeta 实例（链式调用）
	 */
	public UrlMeta setRef(String ref) {
		this.ref = ref;
		return this;
	}

	/**
	 * 链式设置端口号（仅当端口>0时生效）
	 *
	 * @param port 端口号（如 8080，<=0 时忽略）
	 * @return 当前 UrlMeta 实例（链式调用）
	 */
	public UrlMeta setPort(int port) {
		if (port <= 0) {
			return this;
		}
		this.port = port;
		return this;
	}

	/**
	 * 链式设置主机名/IP
	 *
	 * @param host 主机名（如 example.com）
	 * @return 当前 UrlMeta 实例（链式调用）
	 */
	public UrlMeta setHost(String host) {
		this.host = host;
		return this;
	}

	// ===================== 简化链式调用的语法糖方法（与setXxx等价） =====================

	/**
	 * 语法糖：链式设置 URL 协议/方案（同 {@link #setScheme(String)}）
	 *
	 * @param scheme 协议名
	 * @return 当前 UrlMeta 实例
	 */
	public UrlMeta scheme(String scheme) {
		return setScheme(scheme);
	}

	/**
	 * 语法糖：链式设置基础路径（同 {@link #setBasePath(String)}）
	 *
	 * @param basePath 基础路径
	 * @return 当前 UrlMeta 实例
	 */
	public UrlMeta basePath(String basePath) {
		return setBasePath(basePath);
	}

	/**
	 * 语法糖：链式设置业务路径（同 {@link #setPath(String)}）
	 *
	 * @param path 业务路径
	 * @return 当前 UrlMeta 实例
	 */
	public UrlMeta path(String path) {
		return setPath(path);
	}

	/**
	 * 语法糖：链式设置 Query 参数（同 {@link #setQuery(String)}）
	 *
	 * @param query Query 字符串
	 * @return 当前 UrlMeta 实例
	 */
	public UrlMeta query(String query) {
		return setQuery(query);
	}

	/**
	 * 语法糖：链式设置用户认证信息（同 {@link #setUserInfo(String)}）
	 *
	 * @param userInfo 用户信息
	 * @return 当前 UrlMeta 实例
	 */
	public UrlMeta userInfo(String userInfo) {
		return setUserInfo(userInfo);
	}

	/**
	 * 语法糖：链式设置锚点/片段（同 {@link #setRef(String)}）
	 *
	 * @param ref 锚点字符串
	 * @return 当前 UrlMeta 实例
	 */
	public UrlMeta ref(String ref) {
		return setRef(ref);
	}

	/**
	 * 语法糖：链式设置端口号（同 {@link #setPort(int)}）
	 *
	 * @param port 端口号
	 * @return 当前 UrlMeta 实例
	 */
	public UrlMeta port(int port) {
		return setPort(port);
	}

	/**
	 * 语法糖：链式设置主机名/IP（同 {@link #setHost(String)}）
	 *
	 * @param host 主机名
	 * @return 当前 UrlMeta 实例
	 */
	public UrlMeta host(String host) {
		return setHost(host);
	}

	// ===================== Getter 方法 =====================

	public String getScheme() {
		return scheme;
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	public String getQuery() {
		return query;
	}

	public String getBasePath() {
		return basePath;
	}

	public String getPath() {
		return path;
	}

	public String getUserInfo() {
		return userInfo;
	}

	public String getRef() {
		return ref;
	}

	/**
	 * 格式化路径：标准化路径格式，处理首尾斜杠、空路径等问题
	 * <p>规则：</p>
	 * <li>1. 空/空白路径返回空字符串</li>
	 * <li>2. 仅 "/" 时返回空字符串</li>
	 * <li>3. 路径开头补 "/"，结尾去除 "/"</li>
	 * <li>4. 去除路径首尾空格</li>
	 *
	 * @param path 原始路径（可为 null/空白）
	 * @return 格式化后的路径（如 "a/b/" → "/a/b"，"/" → ""）
	 */
	String formatPath(String path) {
		if (StringUtil.isBlank(path)) {
			return StringPool.EMPTY;
		}
		// 去除首尾空格，替换多斜杠为单斜杠
		path = path.trim().replaceAll("/+", "/");
		if ("/".equals(path)) {
			return StringPool.EMPTY; // 根路径返回 /，而非空，拼接更规范
		}
		if (!path.startsWith("/")) {
			path = "/" + path;
		}
		if (path.endsWith("/") && path.length() > 1) {
			path = path.substring(0, path.length() - 1);
		}
		return path;
	}

	/**
	 * 编码 Query 参数：对每个 key/value 分别 UTF-8 编码，避免分隔符（=&）失效
	 * <p>注意：该方法为内部辅助方法，存在边界场景限制，详见问题说明</p>
	 *
	 * @param query 原始 Query 字符串（如 "user name=张三&age=25"）
	 * @return 编码后的 Query 字符串（如 "user%20name=%E5%BC%A0%E4%B8%89&age=25"）
	 */
	String encodeQuery(String query) {
		if (StringUtil.isBlank(query)) {
			return StringPool.EMPTY;
		}
		List<Map.Entry<String, String>> paramList = new ArrayList<>();
		// 拆分&，处理空元素
		String[] paramPairs = query.split(StringPool.AMPERSAND);
		for (String pair : paramPairs) {
			if (pair.isBlank()) {
				continue;
			}
			// 仅拆分第一个=，避免value含=拆分错误
			String[] kv = pair.split(StringPool.EQUALS, 2);
			String key = kv.length >= 1 ? kv[0] : StringPool.EMPTY;
			String value = kv.length >= 2 ? kv[1] : StringPool.EMPTY;

			// 先解码原始参数，避免双重编码
			String decodedKey = UriUtil.decode(key);
			String decodedValue = UriUtil.decode(value);

			paramList.add(new AbstractMap.SimpleEntry<>(decodedKey, decodedValue));
		}

		StringBuilder sb = new StringBuilder();
		for (Map.Entry<String, String> entry : paramList) {
			if (!sb.isEmpty()) {
				sb.append("&");
			}
			String encodedKey = UriUtil.encode(entry.getKey());
			String encodedValue = UriUtil.encode(entry.getValue());
			sb.append(encodedKey).append("=").append(encodedValue);

		}
		return sb.toString();
	}

	/**
	 * 拼接 URL 字符串：按 URL 标准结构拼接所有字段，Query 自动编码
	 * <p>注意：未拼接 ref 字段，且未处理 scheme 为空的非法场景</p>
	 *
	 * @return 拼接后的 URL 字符串（如 https://admin@example.com:8080/api/v1/user?user%20name=%E5%BC%A0%E4%B8%89）
	 */
	@Override
	public String toString() {
		String url = buildUrlString();
		if (StringUtil.isNotBlank(ref)) {
			url = url.concat("#").concat(ref);
		}
		return url;
	}

	/**
	 * 构建基础URL字符串（不带锚点ref）
	 * <p>按URL标准结构拼接：scheme://[userinfo@]host[:port][basePath/path][?query]</p>
	 * <p>与 {@link #toString()} 的区别：该方法不包含锚点ref，toString() 包含锚点ref</p>
	 * <p>与 {@link #toURL()} 的区别：该方法返回字符串类型，toURL() 返回标准 URL 对象</p>
	 *
	 * @return 不带锚点的基础URL字符串（如 https://admin@example.com:8080/api/v1/user?user%20name=%E5%BC%A0%E4%B8%89）
	 */
	public String buildUrlString() {
		StringBuilder url = new StringBuilder();
		url.append(scheme).append("://");

		if (StringUtil.isNotBlank(userInfo)) {
			url.append(userInfo).append("@");
		}
		// 校验 host 非空
		if (StringUtil.isNotBlank(host)) {
			url.append(host);
		}
		if (port > 0) {
			url.append(":").append(port);
		}
		String fullPath = StringPool.EMPTY;
		if (StringUtil.isNotBlank(basePath)) {
			fullPath += basePath;
		}
		if (StringUtil.isNotBlank(path)) {
			if (!fullPath.endsWith("/") && !path.startsWith("/")) {
				fullPath += "/";
			}
			fullPath += path;
		}
		if (StringUtil.isNotBlank(fullPath)) {
			url.append(fullPath);
		}

		if (StringUtil.isNotBlank(query)) {
			String connector = fullPath.contains("?") ? "&" : "?";
			url.append(connector).append(encodeQuery(query));
		}
		return url.toString();
	}

	/**
	 * 转换为标准 {@link URL} 对象
	 *
	 * @return 标准 URL 对象
	 * @throws IllegalArgumentException 当拼接的 URL 字符串格式非法时抛出
	 */
	public URL toURL() {
		try {
			return new URL(toString());
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException("Invalid URL format: " + toString(), e);
		}
	}

	/**
	 * 判断是否SSL
	 *
	 */
	public boolean isSSL() {
		return "https".equalsIgnoreCase(scheme);
	}

	/**
	 * 补充：获取有效端口（显式端口 → 协议默认端口）
	 * <p>解决 port=-1 时无法直接使用的问题，适配 URL 标准</p>
	 *
	 * @return 有效端口号（如 https 协议 port=-1 时返回 443）
	 */
	public int getEffectivePort() {
		if (this.port > 0) {
			return this.port;
		}
		if (StringUtil.isBlank(this.scheme)) {
			return -1;
		}
		return switch (this.scheme.toLowerCase()) {
			case "http" -> 80;
			case "https" -> 443;
			case "ftp" -> 21;
			case "ssh" -> 22;
			default -> -1;
		};
	}

	@Override
	public UrlMeta clone() {
		return new UrlMeta(this);
	}
}
