package org.bedrock.common.http.converter;

import org.bedrock.common.http.HttpRequest;
import org.bedrock.common.http.HttpResponse;
import org.bedrock.common.http.model.ContentType;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import java.nio.charset.Charset;

/**
 * 参数转换器接口
 * 覆盖HTTP请求/响应的全场景参数转换，包含：
 * 1. 请求端转换：对象 → 不同Content-Type的请求体/参数
 * 2. 响应端转换：不同Content-Type的响应体 → 对象/流/文件
 */
public interface Converter {

	/**
	 * 对象转换为多值Map（适配x-www-form-urlencoded/form-data等多值场景）
	 *
	 * @param value   待转换的业务对象
	 * @param request HTTP请求上下文（含Content-Type、字符编码等）
	 */
	Map<String, Object> convertToMultiValueMap(Object value, HttpRequest request);

	/**
	 * 请求体转换（适配JSON/XML/纯文本等字符型Content-Type）
	 *
	 * @param value   待转换的业务对象
	 * @param request HTTP请求上下文（含Content-Type、字符编码等）
	 * @return 字符型请求体字符串（如JSON字符串、XML字符串）
	 */
	String convertToRequestString(Object value, HttpRequest request, ContentType contentType);

	/**
	 * 二进制请求体转换（适配form-data的文件、octet-stream等二进制场景）
	 *
	 * @param value   待转换的对象（可包含文件、二进制数组等）
	 * @param request HTTP请求上下文
	 * @return 二进制请求体字节数组
	 */
	byte[] convertToRequestBytes(Object value, HttpRequest request, ContentType contentType);

	/**
	 * 响应体转换为指定类型对象（适配JSON/XML等字符型响应）
	 *
	 * @param bytes    响应体字节数组
	 * @param response HTTP响应上下文（含Content-Type、字符编码等）
	 * @param type     目标类型
	 * @param <T>      泛型类型
	 * @return 转换后的业务对象
	 */
	<T> T convertToResponseObject(byte[] bytes, HttpResponse response, Type type);

	/**
	 * 获取转换时的默认字符编码（解决乱码问题）
	 *
	 * @param request/response 上下文（优先从上下文取编码，无则返回默认UTF-8）
	 * @return 字符编码
	 */
	default Charset getDefaultCharset(HttpRequest request) {
		return request.getCharset() != null ? request.getCharset() : StandardCharsets.UTF_8;
	}

	default Charset getDefaultCharset(HttpResponse response) {
		return response.getCharset() != null ? response.getCharset() : StandardCharsets.UTF_8;
	}
}

