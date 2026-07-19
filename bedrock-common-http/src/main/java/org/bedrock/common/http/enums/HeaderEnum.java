package org.bedrock.common.http.enums;

/**
 * HTTP请求/响应头通用名称枚举类
 * 遵循HTTP标准头名称规范（首字母大写，连字符分隔），覆盖通用、代理、缓存、安全、跨域、追踪等全场景
 */
public enum HeaderEnum {

	// ===================== 通用基础头 =====================
	/**
	 * 认证信息（如Bearer Token、Basic Auth）
	 */
	AUTHORIZATION("Authorization"),
	/**
	 * 代理服务器认证信息（与Authorization类似，专用于代理）
	 */
	PROXY_AUTHORIZATION("Proxy-Authorization"),
	/**
	 * 报文创建的日期和时间（遵循RFC 1123格式，如Tue, 15 Nov 1994 08:12:31 GMT）
	 */
	DATE("Date"),
	/**
	 * 允许客户端和服务器指定与请求/响应连接有关的选项（如keep-alive、close）
	 */
	CONNECTION("Connection"),
	/**
	 * 发送端使用的MIME协议版本（如1.0）
	 */
	MIME_VERSION("MIME-Version"),
	/**
	 * 分块传输编码时，声明报文拖挂部分包含的首部集合
	 */
	TRAILER("Trailer"),
	/**
	 * 告知接收端报文采用的传输编码方式（如chunked、gzip）
	 */
	TRANSFER_ENCODING("Transfer-Encoding"),
	/**
	 * 告知接收端发送端希望升级使用的新版本/协议（如HTTP/2、WebSocket）
	 */
	UPGRADE("Upgrade"),
	/**
	 * 指定请求和响应遵循的缓存机制（如no-cache、max-age=3600、no-store）
	 */
	CACHE_CONTROL("Cache-Control"),
	/**
	 * 实现特定的缓存指令（常用Pragma: no-cache，等效于Cache-Control: no-cache）
	 */
	PRAGMA("Pragma"),
	/**
	 * 表示请求体/响应体的媒体类型（如application/json、multipart/form-data）
	 */
	CONTENT_TYPE("Content-Type"),
	/**
	 * 表示请求体/响应体的字节长度
	 */
	CONTENT_LENGTH("Content-Length"),
	/**
	 * 客户端能接受的响应内容类型（如text/html,application/json）
	 */
	ACCEPT("Accept"),
	/**
	 * 客户端标识（浏览器/客户端类型、版本等信息）
	 */
	USER_AGENT("User-Agent"),
	/**
	 * 请求的目标主机（域名+端口）
	 */
	HOST("Host"),

	// ===================== 代理相关 =====================
	/**
	 * 代理服务器返回的认证要求（响应头，告知客户端需提供代理认证）
	 */
	PROXY_AUTHENTICATE("Proxy-Authenticate"),
	/**
	 * 反向代理传递的原始请求主机（如Nginx转发时使用）
	 */
	X_FORWARDED_HOST("X-Forwarded-Host"),
	/**
	 * 反向代理传递的原始请求协议（http/https）
	 */
	X_FORWARDED_PROTO("X-Forwarded-Proto"),
	/**
	 * 反向代理传递的客户端真实IP（多个代理时为IP列表）
	 */
	X_FORWARDED_FOR("X-Forwarded-For"),
	/**
	 * 替代X-Forwarded-For，标准化的客户端IP传递头
	 */
	X_REAL_IP("X-Real-IP"),
	/**
	 * 显示报文经过的中间代理/网关节点（用于追踪请求链路）
	 */
	VIA("Via"),

	// ===================== 缓存相关 =====================
	/**
	 * 响应过期时间
	 */
	EXPIRES("Expires"),
	/**
	 * 资源最后修改时间
	 */
	LAST_MODIFIED("Last-Modified"),
	/**
	 * 资源的唯一标识（用于缓存验证，如W/"123456"）
	 */
	ETAG("ETag"),
	/**
	 * 缓存验证：仅当资源修改时返回（配合ETag/Last-Modified）
	 */
	IF_MODIFIED_SINCE("If-Modified-Since"),
	/**
	 * 缓存验证：仅当ETag不匹配时返回
	 */
	IF_NONE_MATCH("If-None-Match"),

	// ===================== 认证响应相关 =====================
	/**
	 * 服务端要求客户端提供认证信息（如Basic realm="example"）
	 */
	WWW_AUTHENTICATE("WWW-Authenticate"),
	/**
	 * 认证会话的刷新令牌（自定义业务头，高频使用）
	 */
	X_REFRESH_TOKEN("X-Refresh-Token"),
	/**
	 * Token过期时间（自定义业务头）
	 */
	X_TOKEN_EXPIRES("X-Token-Expires"),

	// ===================== 安全防护相关 =====================
	/**
	 * 防止XSS攻击（如1; mode=block）
	 */
	X_XSS_PROTECTION("X-XSS-Protection"),
	/**
	 * 防止点击劫持（如DENY、SAMEORIGIN）
	 */
	X_FRAME_OPTIONS("X-Frame-Options"),
	/**
	 * 禁止浏览器MIME类型嗅探
	 */
	X_CONTENT_TYPE_OPTIONS("X-Content-Type-Options"),
	/**
	 * 内容安全策略（限制资源加载来源，如default-src 'self'）
	 */
	CONTENT_SECURITY_POLICY("Content-Security-Policy"),
	/**
	 * 严格传输安全（强制HTTPS，max-age=31536000）
	 */
	STRICT_TRANSPORT_SECURITY("Strict-Transport-Security"),

	// ===================== 内容相关 =====================
	/**
	 * 响应体字符编码（如UTF-8、GBK）
	 */
	CONTENT_ENCODING("Content-Encoding"),
	/**
	 * 客户端支持的编码方式（如gzip, deflate, br）
	 */
	ACCEPT_ENCODING("Accept-Encoding"),
	/**
	 * 客户端支持的语言（如zh-CN,en-US;q=0.9）
	 */
	ACCEPT_LANGUAGE("Accept-Language"),
	/**
	 * 客户端浏览器可接受的字符编码集（如UTF-8,ISO-8859-1）
	 */
	ACCEPT_CHARSET("Accept-Charset"),
	/**
	 * 文件下载时的文件名（如attachment; filename="test.txt"）
	 */
	CONTENT_DISPOSITION("Content-Disposition"),
	/**
	 * 断点续传/范围请求（如bytes=0-1023）
	 */
	RANGE("Range"),
	/**
	 * 响应的内容范围（如bytes 0-1023/2048）
	 */
	CONTENT_RANGE("Content-Range"),

	// ===================== Cookie相关 =====================
	/**
	 * 客户端向服务端发送的Cookie
	 */
	COOKIE("Cookie"),
	/**
	 * 服务端向客户端设置的Cookie
	 */
	SET_COOKIE("Set-Cookie"),

	// ===================== 跨域（CORS）相关 =====================
	/**
	 * 请求来源页面的URL
	 */
	REFERER("Referer"),
	/**
	 * 跨域请求的源域名（CORS预检请求核心头，如https://example.com）
	 */
	ORIGIN("Origin"),
	/**
	 * 标识AJAX请求（如XMLHttpRequest）
	 */
	X_REQUESTED_WITH("X-Requested-With"),
	/**
	 * 跨域资源共享（CORS）- 允许的源
	 */
	ACCESS_CONTROL_ALLOW_ORIGIN("Access-Control-Allow-Origin"),
	/**
	 * 跨域资源共享（CORS）- 允许的请求方法
	 */
	ACCESS_CONTROL_ALLOW_METHODS("Access-Control-Allow-Methods"),
	/**
	 * 跨域资源共享（CORS）- 允许的请求头
	 */
	ACCESS_CONTROL_ALLOW_HEADERS("Access-Control-Allow-Headers"),
	/**
	 * 跨域资源共享（CORS）- 预检请求的缓存时长（秒）
	 */
	ACCESS_CONTROL_MAX_AGE("Access-Control-Max-Age"),
	/**
	 * 跨域资源共享（CORS）- 允许携带凭证（Cookie）
	 */
	ACCESS_CONTROL_ALLOW_CREDENTIALS("Access-Control-Allow-Credentials"),
	/**
	 * 跨域预检请求（OPTIONS）- 告知服务端实际请求的方法
	 */
	ACCESS_CONTROL_REQUEST_METHOD("Access-Control-Request-Method"),
	/**
	 * 跨域预检请求（OPTIONS）- 告知服务端实际请求的自定义头
	 */
	ACCESS_CONTROL_REQUEST_HEADERS("Access-Control-Request-Headers"),

	// ===================== 重定向/重试相关 =====================
	/**
	 * 重定向目标URL
	 */
	LOCATION("Location"),
	/**
	 * 重试延迟（秒，如5）
	 */
	RETRY_AFTER("Retry-After"),

	// ===================== 追踪/链路相关 =====================
	/**
	 * 分布式追踪 - Trace ID（OpenTelemetry/自定义链路追踪）
	 */
	X_TRACE_ID("X-Trace-Id"),
	/**
	 * 分布式追踪 - Span ID（OpenTelemetry/自定义链路追踪）
	 */
	X_SPAN_ID("X-Span-Id"),
	/**
	 * W3C标准分布式追踪头（如00-1234567890abcdef1234567890abcdef-1234567890abcdef-01）
	 */
	TRACEPARENT("traceparent"),
	/**
	 * W3C标准分布式追踪扩展头
	 */
	TRACESTATE("tracestate"),
	/**
	 * 请求ID（业务层面唯一标识单次请求）
	 */
	X_REQUEST_ID("X-Request-Id"),

	// ===================== 业务自定义高频头 =====================
	/**
	 * 租户ID（多租户系统标识）
	 */
	X_TENANT_ID("X-Tenant-Id"),
	/**
	 * 用户ID（业务层传递当前用户ID）
	 */
	X_USER_ID("X-User-Id"),
	/**
	 * 设备ID（移动端/前端设备标识）
	 */
	X_DEVICE_ID("X-Device-Id"),
	/**
	 * 接口版本号（如v1、v2）
	 */
	X_API_VERSION("X-Api-Version");

	// 私有字段：存储HTTP头的标准名称
	private final String value;

	/**
	 * 构造方法
	 *
	 * @param value HTTP头标准名称
	 */
	HeaderEnum(String value) {
		this.value = value;
	}

	/**
	 * 获取HTTP头的标准名称（对外暴露的核心方法）
	 *
	 * @return 如"Content-Type"、"Proxy-Authorization"
	 */
	public String getValue() {
		return value;
	}

	@Override
	public String toString() {
		return value;
	}

	/**
	 * 静态方法：判断指定头名称是否存在（忽略大小写）
	 *
	 * @param headerName 待判断的头名称（如"proxy-authorization"、"Proxy-Authorization"）
	 * @return true=存在，false=不存在
	 */
	public static boolean contains(String headerName) {
		if (headerName == null || headerName.trim().isEmpty()) {
			return false;
		}
		for (HeaderEnum header : values()) {
			if (header.getValue().equalsIgnoreCase(headerName.trim())) {
				return true;
			}
		}
		return false;
	}
}
