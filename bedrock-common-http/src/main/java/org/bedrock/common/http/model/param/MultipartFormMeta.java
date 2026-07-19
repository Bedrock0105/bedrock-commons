package org.bedrock.common.http.model.param;

import lombok.Getter;
import org.bedrock.common.code.constant.StringPool;
import org.bedrock.common.code.util.ObjectUtil;
import org.bedrock.common.code.util.StringUtil;
import org.bedrock.common.http.HttpRequest;
import org.bedrock.common.http.enums.MediaTypeCategory;
import org.bedrock.common.http.model.ContentType;
import org.bedrock.common.http.support.HttpSupport;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 多部分表单（multipart/form-data）元数据容器
 * 管理普通参数与各类文件数据，支持多类型传入
 */
@Getter
public class MultipartFormMeta {

	/**
	 * 关联的HTTP请求实例
	 */
	private final HttpRequest request;

	/**
	 * 多部分表单参数列表
	 */
	private List<Param> params = new ArrayList<>();

	// ===================== 普通参数相关方法 =====================

	/**
	 * 添加单个普通表单参数
	 *
	 * @param name  参数名
	 * @param value 参数值
	 */
	public void add(String name, Object value) {
		add(name, value, ContentType.TEXT_PLAIN);
	}

	/**
	 * 添加带内容类型的普通表单参数
	 *
	 * @param name        参数名
	 * @param value       参数值
	 * @param contentType 内容类型
	 */
	public void add(String name, Object value, String contentType) {
		add(name, null, value, contentType);
	}

	/**
	 * 添加带内容类型的普通表单参数
	 *
	 * @param name        参数名
	 * @param value       参数值
	 * @param contentType 内容类型
	 */
	public void add(String name, String fileName, Object value, String contentType) {
		params.add(new Param(name, fileName, value, ContentType.create(contentType)));
	}

	// ===================== 字节数组相关方法 =====================

	/**
	 * 添加字节数组参数（默认二进制流类型）
	 *
	 * @param name  参数名
	 * @param bytes 字节数组数据
	 */
	public void add(String name, byte[] bytes) {
		add(name, bytes, ContentType.OCTET_STREAM);
	}

	/**
	 * 添加带文件名的字节数组参数（默认二进制流类型）
	 *
	 * @param name     参数名
	 * @param fileName 文件名
	 * @param bytes    字节数组数据
	 */
	public void add(String name, String fileName, byte[] bytes) {
		add(name, fileName, bytes, ContentType.OCTET_STREAM);
	}

	/**
	 * 添加带内容类型的字节数组参数
	 *
	 * @param name        参数名
	 * @param bytes       字节数组数据
	 * @param contentType 内容类型
	 */
	public void add(String name, byte[] bytes, String contentType) {
		add(name, null, bytes, contentType);
	}

	/**
	 * 添加带文件名和内容类型的字节数组参数
	 *
	 * @param name        参数名
	 * @param fileName    文件名
	 * @param bytes       字节数组数据
	 * @param contentType 内容类型
	 */
	public void add(String name, String fileName, byte[] bytes, String contentType) {
		params.add(new Param(name, fileName, bytes, ContentType.create(contentType)));
	}

	// ===================== 文件相关方法 =====================

	/**
	 * 添加文件参数（默认二进制流类型）
	 *
	 * @param name 参数名
	 * @param file 文件对象
	 */
	public void add(String name, File file) {
		add(name, file, ContentType.OCTET_STREAM);
	}

	/**
	 * 添加带文件名的文件参数（默认二进制流类型）
	 *
	 * @param name     参数名
	 * @param fileName 文件名
	 * @param file     文件对象
	 */
	public void add(String name, String fileName, File file) {
		add(name, fileName, file, ContentType.OCTET_STREAM);
	}

	/**
	 * 添加带内容类型的文件参数
	 *
	 * @param name        参数名
	 * @param file        文件对象
	 * @param contentType 内容类型
	 */
	public void add(String name, File file, String contentType) {
		add(name, null, file, contentType);
	}

	/**
	 * 添加带文件名和内容类型的文件参数
	 *
	 * @param name        参数名
	 * @param fileName    文件名
	 * @param file        文件对象
	 * @param contentType 内容类型
	 */
	public void add(String name, String fileName, File file, String contentType) {
		params.add(new Param(name, fileName, file, ContentType.create(contentType)));
	}

	// ===================== 输入流相关方法 =====================

	/**
	 * 添加输入流参数（默认二进制流类型）
	 *
	 * @param name        参数名
	 * @param inputStream 输入流对象
	 */
	public void add(String name, InputStream inputStream) {
		add(name, inputStream, ContentType.OCTET_STREAM);
	}

	/**
	 * 添加带文件名的输入流参数（默认二进制流类型）
	 *
	 * @param name        参数名
	 * @param fileName    文件名
	 * @param inputStream 输入流对象
	 */
	public void add(String name, String fileName, InputStream inputStream) {
		add(name, fileName, inputStream, ContentType.OCTET_STREAM);
	}

	/**
	 * 添加带内容类型的输入流参数
	 *
	 * @param name        参数名
	 * @param inputStream 输入流对象
	 * @param contentType 内容类型
	 */
	public void add(String name, InputStream inputStream, String contentType) {
		add(name, null, inputStream, contentType);
	}

	/**
	 * 添加带文件名和内容类型的输入流参数
	 *
	 * @param name        参数名
	 * @param fileName    文件名
	 * @param inputStream 输入流对象
	 * @param contentType 内容类型
	 */
	public void add(String name, String fileName, InputStream inputStream, String contentType) {
		params.add(new Param(name, fileName, inputStream, ContentType.create(contentType)));
	}

	// ===================== 通用方法 =====================

	/**
	 * 判断表单参数列表是否为空
	 *
	 * @return 空返回true，非空返回false
	 */
	public boolean isEmpty() {
		return params.isEmpty();
	}

	/**
	 * 构造参数元数据实例
	 *
	 * @param request 关联的HTTP请求实例
	 */
	public MultipartFormMeta(HttpRequest request) {
		this.request = request;
	}

	/**
	 * 获取关联的HTTP请求实例
	 *
	 * @return HttpRequest实例
	 */
	public HttpRequest request() {
		return request;
	}
	// ===================== 内部参数实体 =====================

	/**
	 * 多部分表单参数实体，含文件名与内容类型
	 */
	public record Param(String name, String fileName, Object value, ContentType contentType) {

	}

	@Override
	public String toString() {
		String boundary = request.getOrCreateBoundary();
		StringBuilder sb = new StringBuilder();

		for (MultipartFormMeta.Param param : params) {
			// 1. 拼接每个参数的分隔边界 + 固定请求头前缀
			sb.append("--").append(boundary).append(System.lineSeparator());

			// 2. 统一兜底处理参数的内容类型，复用逻辑
			ContentType contentType = param.contentType();
			contentType = ObjectUtil.isEmpty(contentType) ? ContentType.create(ContentType.FORM_URLENCODED) : contentType;
			MediaTypeCategory mediaTypeCategory = contentType.getMediaTypeCategory();
			mediaTypeCategory = MediaTypeCategory.UNKNOWN.equals(mediaTypeCategory) ? MediaTypeCategory.FORM_URLENCODED : mediaTypeCategory;

			// 3. 按媒体类型分发处理，核心逻辑完全不变
			if (MediaTypeCategory.FORM_URLENCODED.equals(mediaTypeCategory) || MediaTypeCategory.MULTIPART_FORM_DATA.equals(mediaTypeCategory)) {
				handleFormParam(sb, param, contentType);
			} else if (MediaTypeCategory.STRING_REQUEST_BODY.equals(mediaTypeCategory)) {
				handleStringBodyParam(sb, param, contentType);
			} else if (MediaTypeCategory.BINARY_DATA.equals(mediaTypeCategory)) {
				handleBinaryParam(sb, param, contentType);
			}
			// 每个参数末尾换行分隔
			sb.append(System.lineSeparator());
		}

		// 4. 拼接结束边界，补全multipart/form-data标准格式【必须加】，原代码缺失
		if (!sb.isEmpty()) {
			sb.append("--").append(boundary).append("--").append(System.lineSeparator());
		}
		return sb.toString();
	}

	/**
	 * 处理表单类型参数 - 复用通用逻辑
	 */
	private void handleFormParam(StringBuilder sb, MultipartFormMeta.Param param, ContentType contentType) {
		String name = param.name();
		if (StringUtil.isNotBlank(name)) {
			sb.append("Content-Disposition: form-data; name=\"")
				.append(param.name()).append("\"");
			if (StringUtil.isNotBlank(param.fileName())) {
				sb.append("; filename=\"").append(param.fileName()).append("\"");
			}
			sb.append(System.lineSeparator());
			String convertedValue = HttpSupport.paramConverter(request, name, param.value());
			convertedValue = StringUtil.isBlank(convertedValue) ? StringPool.EMPTY : convertedValue;
			appendContentTypeAndContent(sb, contentType, convertedValue);
			return;
		}
		// 处理结构化参数转Map批量拼接
		Map<String, Object> paramMap = request.getConverter().convertToMultiValueMap(param.value(), request);
		paramMap.forEach((k, v) -> {
			String valueStr = StringUtil.toStr(v, StringPool.EMPTY);
			sb.append("--").append(request.getOrCreateBoundary()).append(System.lineSeparator());
			sb.append("Content-Disposition: form-data; name=\"").append(k).append("\"").append(System.lineSeparator());
			appendContentTypeAndContent(sb, param.contentType(), valueStr);
			sb.append(System.lineSeparator());
		});
	}

	/**
	 * 处理字符串类型参数（JSON/XML等）- 复用通用逻辑
	 */
	private void handleStringBodyParam(StringBuilder sb, MultipartFormMeta.Param param, ContentType contentType) {
		sb.append("Content-Disposition: form-data; name=\"")
			.append(param.name()).append("\"");
		if (StringUtil.isNotBlank(param.fileName())) {
			sb.append("; filename=\"").append(param.fileName()).append("\"");
		}
		sb.append(System.lineSeparator());
		String body = request.getConverter().convertToRequestString(param.value(), request, contentType);
		body = StringUtil.isBlank(body) ? StringPool.EMPTY : body;
		appendContentTypeAndContent(sb, contentType, body);
	}

	/**
	 * 处理二进制类型参数（文件/字节数组）- 优化展示格式，避免乱码
	 */
	private void handleBinaryParam(StringBuilder sb, MultipartFormMeta.Param param, ContentType contentType) {
		sb.append("Content-Disposition: form-data; name=\"")
			.append(param.name()).append("\"");
		if (StringUtil.isNotBlank(param.fileName())) {
			sb.append("; filename=\"").append(param.fileName()).append("\"");
		}
		sb.append(System.lineSeparator());
		Object value = param.value();
		byte[] bytes = request.getConverter().convertToRequestBytes(value, request, contentType);
		// 拼接内容类型
		if (ObjectUtil.isNotEmpty(contentType)) {
			sb.append("Content-Type: ").append(contentType).append(System.lineSeparator());
		}
		// 拼接字节长度+友好的二进制展示（原数组toString无意义且乱码）
		sb.append("Content-Length: ").append(bytes.length).append(System.lineSeparator());
		sb.append("[二进制数据，字节长度: ").append(bytes.length).append("]");
	}

	/**
	 * 核心通用方法：拼接内容类型 + 内容长度 + 实际内容
	 * 消除90%的重复代码，统一处理逻辑，减少冗余
	 */
	private void appendContentTypeAndContent(StringBuilder sb, ContentType contentType, String content) {
		// 拼接Content-Type请求头
		if (ObjectUtil.isNotEmpty(contentType)) {
			sb.append("Content-Type: ").append(contentType).append(System.lineSeparator());
		}
		// 拼接Content-Length + 最终内容
		sb.append("Content-Length: ").append(content.getBytes(StandardCharsets.UTF_8).length).append(System.lineSeparator());
		sb.append(content);
	}
}
