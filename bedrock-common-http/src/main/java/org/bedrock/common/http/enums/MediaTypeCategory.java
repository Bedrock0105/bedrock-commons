package org.bedrock.common.http.enums;

/**
 * 媒体类型分类枚举（替代硬编码的类型判断）
 */
public enum MediaTypeCategory {
	STRING_REQUEST_BODY, // 字符串请求体类型
	FORM_URLENCODED,     // 普通表单类型
	MULTIPART_FORM_DATA, // 文件上传表单类型
	BINARY_DATA,         // 二进制数据类型
	UNKNOWN              // 未知类型
}
