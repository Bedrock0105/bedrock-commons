package org.bedrock.common.code.util;

import org.bedrock.common.code.constant.StringPool;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;

import java.net.URLConnection;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 媒体类型 / MIME 类型工具类
 * <p>
 * 核心特性：
 * 1. 继承 Spring {@link MimeTypeUtils}，复用 MIME 解析、排序及内置 {@link MimeType} 常量；
 * 2. 根据文件名/扩展名解析 MIME Type（自定义映射 → Spring {@link MediaTypeFactory} → JDK {@link URLConnection}）；
 * 3. 提供图片、视频、音频、文本、应用等 MIME 类型判断；
 * 4. 支持通配符匹配（如 {@code image/*}）及运行时注册自定义扩展名映射；
 * 5. 所有方法均为静态方法，抽象类设计禁止实例化；空值友好，避免空指针。
 * </p>
 *
 * @see MimeType
 * @see MimeTypeUtils
 * @see MediaType
 * @see MediaTypeFactory
 */
public abstract class MediaTypeUtil extends MimeTypeUtils {

	// ==================== 常用 MIME 字符串常量 ====================

	/**
	 * 通用二进制流（未知类型时的兜底 MIME）
	 */
	public static final String APPLICATION_OCTET_STREAM_VALUE = "application/octet-stream";

	/**
	 * JSON
	 */
	public static final String APPLICATION_JSON_VALUE = "application/json";

	/**
	 * XML
	 */
	public static final String APPLICATION_XML_VALUE = "application/xml";

	/**
	 * PDF
	 */
	public static final String APPLICATION_PDF_VALUE = "application/pdf";

	/**
	 * ZIP
	 */
	public static final String APPLICATION_ZIP_VALUE = "application/zip";

	/**
	 * 纯文本
	 */
	public static final String TEXT_PLAIN_VALUE = "text/plain";

	/**
	 * HTML
	 */
	public static final String TEXT_HTML_VALUE = "text/html";

	/**
	 * CSS
	 */
	public static final String TEXT_CSS_VALUE = "text/css";

	/**
	 * JavaScript
	 */
	public static final String APPLICATION_JAVASCRIPT_VALUE = "application/javascript";

	/**
	 * JPEG 图片
	 */
	public static final String IMAGE_JPEG_VALUE = "image/jpeg";

	/**
	 * PNG 图片
	 */
	public static final String IMAGE_PNG_VALUE = "image/png";

	/**
	 * GIF 图片
	 */
	public static final String IMAGE_GIF_VALUE = "image/gif";

	/**
	 * WebP 图片
	 */
	public static final String IMAGE_WEBP_VALUE = "image/webp";

	/**
	 * SVG 图片
	 */
	public static final String IMAGE_SVG_VALUE = "image/svg+xml";

	/**
	 * MP4 视频
	 */
	public static final String VIDEO_MP4_VALUE = "video/mp4";

	/**
	 * MPEG 音频（MP3）
	 */
	public static final String AUDIO_MPEG_VALUE = "audio/mpeg";

	/**
	 * 表单 URL 编码
	 */
	public static final String APPLICATION_FORM_URLENCODED_VALUE = "application/x-www-form-urlencoded";

	/**
	 * 多部分表单（文件上传）
	 */
	public static final String MULTIPART_FORM_DATA_VALUE = "multipart/form-data";

	// ==================== 扩展名 → MIME 映射表 ====================

	/**
	 * 自定义扩展名与 MIME 映射（优先级最高，可运行时注册）
	 * <p>key 为小写扩展名（不含点），value 为 MIME 字符串</p>
	 */
	private static final Map<String, String> EXTENSION_MIME_MAP = new ConcurrentHashMap<>();

	static {
		// 文本 / 脚本
		EXTENSION_MIME_MAP.put("txt", TEXT_PLAIN_VALUE);
		EXTENSION_MIME_MAP.put("html", TEXT_HTML_VALUE);
		EXTENSION_MIME_MAP.put("htm", TEXT_HTML_VALUE);
		EXTENSION_MIME_MAP.put("css", TEXT_CSS_VALUE);
		EXTENSION_MIME_MAP.put("js", APPLICATION_JAVASCRIPT_VALUE);
		EXTENSION_MIME_MAP.put("mjs", APPLICATION_JAVASCRIPT_VALUE);
		EXTENSION_MIME_MAP.put("json", APPLICATION_JSON_VALUE);
		EXTENSION_MIME_MAP.put("xml", APPLICATION_XML_VALUE);
		EXTENSION_MIME_MAP.put("csv", "text/csv");
		EXTENSION_MIME_MAP.put("md", "text/markdown");
		EXTENSION_MIME_MAP.put("markdown", "text/markdown");
		EXTENSION_MIME_MAP.put("yaml", "application/x-yaml");
		EXTENSION_MIME_MAP.put("yml", "application/x-yaml");

		// 图片
		EXTENSION_MIME_MAP.put("jpg", IMAGE_JPEG_VALUE);
		EXTENSION_MIME_MAP.put("jpeg", IMAGE_JPEG_VALUE);
		EXTENSION_MIME_MAP.put("png", IMAGE_PNG_VALUE);
		EXTENSION_MIME_MAP.put("gif", IMAGE_GIF_VALUE);
		EXTENSION_MIME_MAP.put("bmp", "image/bmp");
		EXTENSION_MIME_MAP.put("webp", IMAGE_WEBP_VALUE);
		EXTENSION_MIME_MAP.put("svg", IMAGE_SVG_VALUE);
		EXTENSION_MIME_MAP.put("ico", "image/x-icon");
		EXTENSION_MIME_MAP.put("tif", "image/tiff");
		EXTENSION_MIME_MAP.put("tiff", "image/tiff");

		// 音频
		EXTENSION_MIME_MAP.put("mp3", AUDIO_MPEG_VALUE);
		EXTENSION_MIME_MAP.put("wav", "audio/wav");
		EXTENSION_MIME_MAP.put("flac", "audio/flac");
		EXTENSION_MIME_MAP.put("aac", "audio/aac");
		EXTENSION_MIME_MAP.put("ogg", "audio/ogg");
		EXTENSION_MIME_MAP.put("m4a", "audio/mp4");

		// 视频
		EXTENSION_MIME_MAP.put("mp4", VIDEO_MP4_VALUE);
		EXTENSION_MIME_MAP.put("webm", "video/webm");
		EXTENSION_MIME_MAP.put("avi", "video/x-msvideo");
		EXTENSION_MIME_MAP.put("mov", "video/quicktime");
		EXTENSION_MIME_MAP.put("mkv", "video/x-matroska");
		EXTENSION_MIME_MAP.put("flv", "video/x-flv");
		EXTENSION_MIME_MAP.put("wmv", "video/x-ms-wmv");

		// 文档 / 压缩包
		EXTENSION_MIME_MAP.put("pdf", APPLICATION_PDF_VALUE);
		EXTENSION_MIME_MAP.put("doc", "application/msword");
		EXTENSION_MIME_MAP.put("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
		EXTENSION_MIME_MAP.put("xls", "application/vnd.ms-excel");
		EXTENSION_MIME_MAP.put("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		EXTENSION_MIME_MAP.put("ppt", "application/vnd.ms-powerpoint");
		EXTENSION_MIME_MAP.put("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation");
		EXTENSION_MIME_MAP.put("zip", APPLICATION_ZIP_VALUE);
		EXTENSION_MIME_MAP.put("gz", "application/gzip");
		EXTENSION_MIME_MAP.put("tar", "application/x-tar");
		EXTENSION_MIME_MAP.put("rar", "application/x-rar-compressed");
		EXTENSION_MIME_MAP.put("7z", "application/x-7z-compressed");

		// 其他常见类型
		EXTENSION_MIME_MAP.put("apk", "application/vnd.android.package-archive");
		EXTENSION_MIME_MAP.put("wasm", "application/wasm");
		EXTENSION_MIME_MAP.put("woff", "font/woff");
		EXTENSION_MIME_MAP.put("woff2", "font/woff2");
		EXTENSION_MIME_MAP.put("ttf", "font/ttf");
		EXTENSION_MIME_MAP.put("otf", "font/otf");
	}

	// ==================== 注册 / 查询 MIME ====================

	/**
	 * 注册自定义扩展名与 MIME 映射（覆盖已有映射）
	 *
	 * @param extension 文件扩展名（可带或不带点，如 {@code png} / {@code .png}）
	 * @param mimeType  MIME 类型字符串（如 {@code image/png}）
	 * @example MediaTypeUtil.registerMimeType(" wgt ", " application / widget ");
	 */
	public static void registerMimeType(String extension, String mimeType) {
		if (StringUtil.isBlank(extension) || StringUtil.isBlank(mimeType)) {
			return;
		}
		EXTENSION_MIME_MAP.put(normalizeExtension(extension), mimeType.trim().toLowerCase(Locale.ROOT));
	}

	/**
	 * 根据文件名或路径获取 MIME Type
	 * <p>
	 * 解析顺序：自定义扩展名映射 → Spring {@link MediaTypeFactory} → JDK {@link URLConnection#guessContentTypeFromName}
	 * </p>
	 *
	 * @param fileName 文件名或路径（可为 null/空白）
	 * @return MIME 字符串；无法识别时返回 {@code null}
	 * @example // 返回 "image/png"
	 * MediaTypeUtil.getMimeType("avatar.png");
	 * // 返回 "application/pdf"
	 * MediaTypeUtil.getMimeType("/tmp/report.pdf");
	 */
	public static String getMimeType(String fileName) {
		if (StringUtil.isBlank(fileName)) {
			return null;
		}
		String pureName = FileUtil.getFileName(fileName);
		String extension = getLastExtension(pureName);
		if (StringUtil.isBlank(extension)) {
			return null;
		}

		// 1. 自定义映射（优先级最高）
		String mimeType = EXTENSION_MIME_MAP.get(extension);
		if (StringUtil.isNotBlank(mimeType)) {
			return mimeType;
		}

		// 2. Spring MediaTypeFactory（内置 mime.types）
		Optional<MediaType> mediaType = MediaTypeFactory.getMediaType(pureName);
		if (mediaType.isPresent()) {
			return mediaType.get().toString();
		}

		// 3. JDK FileNameMap / guessContentTypeFromName
		mimeType = URLConnection.getFileNameMap().getContentTypeFor(pureName);
		if (StringUtil.isBlank(mimeType)) {
			mimeType = URLConnection.guessContentTypeFromName(pureName);
		}
		return StringUtil.isBlank(mimeType) ? null : mimeType;
	}

	/**
	 * 根据文件名或路径获取 MIME Type（带默认值）
	 *
	 * @param fileName     文件名或路径（可为 null/空白）
	 * @param defaultValue 无法识别时返回的默认值
	 * @return MIME 字符串；无法识别时返回 defaultValue
	 * @example // 返回 "application/octet-stream"
	 * MediaTypeUtil.getMimeType("unknown.xyz", MediaTypeUtil.APPLICATION_OCTET_STREAM_VALUE);
	 */
	public static String getMimeType(String fileName, String defaultValue) {
		String mimeType = getMimeType(fileName);
		return mimeType != null ? mimeType : defaultValue;
	}

	/**
	 * 根据文件名或路径获取 Spring {@link MediaType}
	 *
	 * @param fileName 文件名或路径（可为 null/空白）
	 * @return MediaType；无法识别时返回 {@code null}
	 * @example MediaTypeUtil.getMediaType(" data.json "); // application/json
	 */
	public static MediaType getMediaType(String fileName) {
		String mimeType = getMimeType(fileName);
		if (StringUtil.isBlank(mimeType)) {
			return null;
		}
		try {
			return MediaType.parseMediaType(mimeType);
		} catch (Exception ex) {
			return null;
		}
	}

	/**
	 * 根据文件名或路径获取 Spring {@link MediaType}（带默认值）
	 *
	 * @param fileName     文件名或路径
	 * @param defaultValue 无法识别时返回的默认 MediaType
	 * @return MediaType
	 */
	public static MediaType getMediaType(String fileName, MediaType defaultValue) {
		MediaType mediaType = getMediaType(fileName);
		return mediaType != null ? mediaType : defaultValue;
	}

	/**
	 * 将 MIME 字符串解析为 {@link MimeType}
	 * <p>委托父类 {@link MimeTypeUtils#parseMimeType(String)}，解析失败返回 {@code null}</p>
	 *
	 * @param mimeType MIME 字符串（如 {@code text/plain;charset=UTF-8}）
	 * @return MimeType；空值或解析失败返回 {@code null}
	 */
	public static MimeType toMimeType(String mimeType) {
		if (StringUtil.isBlank(mimeType)) {
			return null;
		}
		try {
			return parseMimeType(mimeType.trim());
		} catch (Exception ex) {
			return null;
		}
	}

	/**
	 * 将 MIME 字符串解析为 {@link MediaType}
	 *
	 * @param mimeType MIME 字符串
	 * @return MediaType；空值或解析失败返回 {@code null}
	 */
	public static MediaType toMediaType(String mimeType) {
		if (StringUtil.isBlank(mimeType)) {
			return null;
		}
		try {
			return MediaType.parseMediaType(mimeType.trim());
		} catch (Exception ex) {
			return null;
		}
	}

	/**
	 * 根据 MIME Type 反查扩展名（取自定义映射中第一个匹配项）
	 *
	 * @param mimeType MIME 字符串（如 {@code image/jpeg}）
	 * @return 扩展名（不含点，如 {@code jpg}）；未找到返回空字符串
	 * @example // 返回 "jpg"（映射表中 jpeg 对应的第一个扩展名可能是 jpg）
	 * MediaTypeUtil.getExtensionByMimeType("image/jpeg");
	 */
	public static String getExtensionByMimeType(String mimeType) {
		if (StringUtil.isBlank(mimeType)) {
			return StringPool.EMPTY;
		}
		String normalized = normalizeMimeType(mimeType);
		for (Map.Entry<String, String> entry : EXTENSION_MIME_MAP.entrySet()) {
			if (normalized.equals(entry.getValue())) {
				return entry.getKey();
			}
		}
		return StringPool.EMPTY;
	}

	// ==================== MIME 类型判断 ====================

	/**
	 * 判断是否为图片类型（MIME 以 {@code image/} 开头）
	 * <p>参数可为 MIME 字符串或文件名：含 {@code /} 时按 MIME 判断，否则先解析文件 MIME</p>
	 *
	 * @param mimeTypeOrFileName MIME 或文件名
	 * @return true=图片类型
	 * @example MediaTypeUtil.isImage(" image / png "); // true
	 * MediaTypeUtil.isImage("photo.jpg");   // true
	 */
	public static boolean isImage(String mimeTypeOrFileName) {
		return startsWithType(mimeTypeOrFileName, "image/");
	}

	/**
	 * 判断是否为视频类型（MIME 以 {@code video/} 开头）
	 *
	 * @param mimeTypeOrFileName MIME 或文件名
	 * @return true=视频类型
	 */
	public static boolean isVideo(String mimeTypeOrFileName) {
		return startsWithType(mimeTypeOrFileName, "video/");
	}

	/**
	 * 判断是否为音频类型（MIME 以 {@code audio/} 开头）
	 *
	 * @param mimeTypeOrFileName MIME 或文件名
	 * @return true=音频类型
	 */
	public static boolean isAudio(String mimeTypeOrFileName) {
		return startsWithType(mimeTypeOrFileName, "audio/");
	}

	/**
	 * 判断是否为文本类型（MIME 以 {@code text/} 开头，或常见文本应用类型如 JSON/XML/JavaScript）
	 *
	 * @param mimeTypeOrFileName MIME 或文件名
	 * @return true=文本类型
	 */
	public static boolean isText(String mimeTypeOrFileName) {
		String mimeType = resolveMimeType(mimeTypeOrFileName);
		if (StringUtil.isBlank(mimeType)) {
			return false;
		}
		if (mimeType.startsWith("text/")) {
			return true;
		}
		// 常见以 application 声明、但内容为文本的类型
		return mimeType.equals(APPLICATION_JSON_VALUE)
			|| mimeType.equals(APPLICATION_XML_VALUE)
			|| mimeType.equals(APPLICATION_JAVASCRIPT_VALUE)
			|| mimeType.equals("application/x-javascript")
			|| mimeType.equals("application/x-yaml")
			|| mimeType.endsWith("+json")
			|| mimeType.endsWith("+xml");
	}

	/**
	 * 判断是否为应用类型（MIME 以 {@code application/} 开头）
	 *
	 * @param mimeTypeOrFileName MIME 或文件名
	 * @return true=应用类型
	 */
	public static boolean isApplication(String mimeTypeOrFileName) {
		return startsWithType(mimeTypeOrFileName, "application/");
	}

	/**
	 * 判断是否为字体类型（MIME 以 {@code font/} 开头）
	 *
	 * @param mimeTypeOrFileName MIME 或文件名
	 * @return true=字体类型
	 */
	public static boolean isFont(String mimeTypeOrFileName) {
		return startsWithType(mimeTypeOrFileName, "font/");
	}

	/**
	 * 判断 MIME 是否匹配给定模式（支持精确匹配与 {@code type/*} 通配）
	 *
	 * @param mimeType MIME 字符串（如 {@code image/png}）
	 * @param pattern  匹配模式（如 {@code image/png}、{@code image/*}
	 * @return true=匹配
	 * @example MediaTypeUtil.matches(" image / png ", " image / * "); // true
	 * MediaTypeUtil.matches("text/plain", "application/*"); // false
	 */
	public static boolean matches(String mimeType, String pattern) {
		if (StringUtil.isBlank(mimeType) || StringUtil.isBlank(pattern)) {
			return false;
		}
		String normalizedMime = normalizeMimeType(mimeType);
		String normalizedPattern = normalizeMimeType(pattern);
		if (normalizedMime.equals(normalizedPattern) || "*/*".equals(normalizedPattern)) {
			return true;
		}
		if (normalizedPattern.endsWith("/*")) {
			String mainType = normalizedPattern.substring(0, normalizedPattern.length() - 1);
			return normalizedMime.startsWith(mainType);
		}
		return false;
	}

	/**
	 * 判断两个 MIME 是否兼容（委托 Spring {@link MimeType#isCompatibleWith(MimeType)}）
	 *
	 * @param mimeType1 MIME 字符串 1
	 * @param mimeType2 MIME 字符串 2
	 * @return true=兼容（如 {@code text/plain} 与 {@code text/*}）
	 */
	public static boolean isCompatibleWith(String mimeType1, String mimeType2) {
		MimeType left = toMimeType(mimeType1);
		MimeType right = toMimeType(mimeType2);
		if (left == null || right == null) {
			return false;
		}
		return left.isCompatibleWith(right);
	}

	// ==================== 内部方法 ====================

	/**
	 * 解析参数为 MIME：含 {@code /} 视为 MIME，否则视为文件名再查 MIME
	 */
	private static String resolveMimeType(String mimeTypeOrFileName) {
		if (StringUtil.isBlank(mimeTypeOrFileName)) {
			return null;
		}
		String value = mimeTypeOrFileName.trim();
		if (value.contains(StringPool.SLASH)) {
			return normalizeMimeType(value);
		}
		String mimeType = getMimeType(value);
		return mimeType == null ? null : normalizeMimeType(mimeType);
	}

	/**
	 * 判断解析后的 MIME 是否以指定前缀开头
	 */
	private static boolean startsWithType(String mimeTypeOrFileName, String prefix) {
		String mimeType = resolveMimeType(mimeTypeOrFileName);
		return mimeType != null && mimeType.startsWith(prefix);
	}

	/**
	 * 规范化 MIME：转小写，并截掉参数部分（如 {@code ;charset=UTF-8}）
	 */
	private static String normalizeMimeType(String mimeType) {
		String value = mimeType.trim().toLowerCase(Locale.ROOT);
		int semicolon = value.indexOf(StringPool.SEMICOLON);
		if (semicolon > 0) {
			value = value.substring(0, semicolon).trim();
		}
		return value;
	}

	/**
	 * 规范化扩展名：去点、转小写
	 */
	private static String normalizeExtension(String extension) {
		String value = extension.trim().toLowerCase(Locale.ROOT);
		if (value.startsWith(StringPool.DOT)) {
			value = value.substring(1);
		}
		return value;
	}

	/**
	 * 取文件名最后一个扩展名（MIME 判断通常只关心末级后缀）
	 * <p>示例：{@code archive.tar.gz} → {@code gz}；{@code photo.PNG} → {@code png}</p>
	 */
	private static String getLastExtension(String fileName) {
		if (StringUtil.isBlank(fileName)) {
			return StringPool.EMPTY;
		}
		int lastDot = fileName.lastIndexOf(StringPool.DOT);
		if (lastDot < 0 || lastDot == fileName.length() - 1) {
			return StringPool.EMPTY;
		}
		String extension = fileName.substring(lastDot + 1);
		// 扩展名中不应再含路径分隔符
		if (extension.contains(StringPool.SLASH) || extension.contains(StringPool.BACK_SLASH)) {
			return StringPool.EMPTY;
		}
		return extension.toLowerCase(Locale.ROOT);
	}

}
