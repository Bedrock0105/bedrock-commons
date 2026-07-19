package org.bedrock.common.http.handler;

import org.bedrock.common.http.enums.MediaTypeCategory;
import org.bedrock.common.http.model.ContentType;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 媒体类型分类注册表（模仿 Spring MediaTypeRegistry）
 */
public class ContentTypeRegistry {

	// 注册表：Content-Type 字符串 → 媒体类型分类
	private static final Map<String, MediaTypeCategory> REGISTRY = new ConcurrentHashMap<>();

	// 通配符注册表：主类型/子类型通配符 → 媒体类型分类（如 image/* → BINARY_DATA）
	private static final Map<String, MediaTypeCategory> WILDCARD_REGISTRY = new ConcurrentHashMap<>();

	// 静态初始化：注册常用媒体类型
	static {
		// 注册普通表单类型
		REGISTRY.put(ContentType.FORM_URLENCODED, MediaTypeCategory.FORM_URLENCODED);
		// 注册文件上传表单类型
		REGISTRY.put(ContentType.MULTIPART, MediaTypeCategory.MULTIPART_FORM_DATA);
		REGISTRY.put("multipart/mixed", MediaTypeCategory.MULTIPART_FORM_DATA);
		REGISTRY.put("multipart/alternative", MediaTypeCategory.MULTIPART_FORM_DATA);
		REGISTRY.put("multipart/digest", MediaTypeCategory.MULTIPART_FORM_DATA);
		REGISTRY.put("multipart/parallel", MediaTypeCategory.MULTIPART_FORM_DATA);
		// 注册字符串类型（精确匹配）
		REGISTRY.put(ContentType.JSON, MediaTypeCategory.STRING_REQUEST_BODY);
		REGISTRY.put(ContentType.XML, MediaTypeCategory.STRING_REQUEST_BODY);
		REGISTRY.put(ContentType.TEXT_PLAIN, MediaTypeCategory.STRING_REQUEST_BODY);
		REGISTRY.put(ContentType.TEXT_HTML, MediaTypeCategory.STRING_REQUEST_BODY);
		REGISTRY.put("application/javascript", MediaTypeCategory.STRING_REQUEST_BODY);
		// 注册二进制类型（精确匹配）
		REGISTRY.put(ContentType.OCTET_STREAM, MediaTypeCategory.BINARY_DATA);
		REGISTRY.put("application/pdf", MediaTypeCategory.BINARY_DATA);
		// 注册通配符类型（批量匹配）
		WILDCARD_REGISTRY.put("text/*", MediaTypeCategory.STRING_REQUEST_BODY);
		WILDCARD_REGISTRY.put("image/*", MediaTypeCategory.BINARY_DATA);
		WILDCARD_REGISTRY.put("video/*", MediaTypeCategory.BINARY_DATA);
		WILDCARD_REGISTRY.put("audio/*", MediaTypeCategory.BINARY_DATA);
	}

	/**
	 * 注册自定义媒体类型
	 *
	 * @param contentType Content-Type 字符串（如 "application/zip"）
	 * @param category    媒体类型分类
	 */
	public static void register(String contentType, MediaTypeCategory category) {
		REGISTRY.put(contentType.toLowerCase(), category);
	}

	/**
	 * 注册自定义通配符类型
	 *
	 * @param wildcardPattern 通配符模式（如 "application/*"）
	 * @param category        媒体类型分类
	 */
	public static void registerWildcard(String wildcardPattern, MediaTypeCategory category) {
		WILDCARD_REGISTRY.put(wildcardPattern.toLowerCase(), category);
	}

	/**
	 * 根据 ContentType 对象获取分类
	 *
	 * @param contentType ContentType 实例
	 * @return MediaTypeCategory 分类
	 */
	public static MediaTypeCategory getCategory(ContentType contentType) {
		// 1. 先匹配精确类型
		String mediaType = contentType.getMediaType().toLowerCase();
		if (REGISTRY.containsKey(mediaType)) {
			return REGISTRY.get(mediaType);
		}
		// 2. 再匹配通配符类型
		for (Map.Entry<String, MediaTypeCategory> entry : WILDCARD_REGISTRY.entrySet()) {
			String pattern = entry.getKey();
			if (contentType.matches(pattern)) {
				return entry.getValue();
			}
		}
		// 3. 未知类型
		return MediaTypeCategory.UNKNOWN;
	}
}
