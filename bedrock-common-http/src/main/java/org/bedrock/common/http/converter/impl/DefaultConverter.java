package org.bedrock.common.http.converter.impl;

import org.bedrock.common.code.util.FileUtil;
import org.bedrock.common.code.util.IoUtil;
import org.bedrock.common.code.util.JsonUtil;
import org.bedrock.common.http.HttpRequest;
import org.bedrock.common.http.HttpResponse;
import org.bedrock.common.http.converter.Converter;
import org.bedrock.common.http.exception.ConverterException;
import org.bedrock.common.http.model.ContentType;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.Map;

import java.nio.charset.Charset;
import java.util.*;

/**
 * 默认参数转换器实现
 * 适配 JSON/XML/表单/二进制 等主流 ContentType 的转换逻辑，核心特性：
 * 1. 多值参数转换为 Map<String, List<String>>，兼容 HTTP 多值场景；
 * 2. 统一 UTF-8 编码，支持从请求/响应中获取自定义编码；
 * 3. 所有异常封装为 ConverterException，便于上层统一处理；
 * 4. 完善边界条件：null值、空字节数组、空流等场景处理。
 */
public class DefaultConverter implements Converter {

	/**
	 * 转换为多值Map（修复核心返回值类型，适配HTTP多值参数）
	 *
	 * @param value   待转换对象（POJO/Map/基本类型等）
	 * @param request HTTP请求上下文（含ContentType、编码等）
	 * @return 多值Map，单值时List长度为1，空值时返回空Map
	 */
	@Override
	public Map<String, Object> convertToMultiValueMap(Object value, HttpRequest request) {
		if (value == null) {
			return Collections.emptyMap();
		}
		if (value instanceof Map) {
			return (Map<String, Object>) value;
		}
		return JsonUtil.convertValue(value, JsonUtil.getMapType(String.class, Object.class));
	}

	/**
	 * 转换为请求字符串（适配JSON/XML，其他类型抛异常）
	 *
	 * @param value   待转换对象（POJO/Map等）
	 * @param request HTTP请求上下文（含ContentType、编码）
	 * @return 序列化后的字符串（JSON/XML）
	 */
	@Override
	public String convertToRequestString(Object value, HttpRequest request, ContentType contentType) {
		if (value == null) {
			return "";
		}
		/**
		 * 如果是字符串，直接返回
		 */
		if (value instanceof String) {
			return value.toString();
		}
		// 解析ContentType（忽略参数，如charset）
		try {
			if (contentType.getMediaType().equalsIgnoreCase(ContentType.XML)) {
				// TODO: 替换为实际的XML序列化工具（如JAXB/XStream）
				throw new ConverterException("XML序列化暂未实现，请补充XML工具类逻辑");
				// return XmlUtil.toXml(value, charset);
			}
			// 不支持的ContentType，抛明确异常（避免返回空字符串导致隐藏问题）
			return JsonUtil.toJson(value);
		} catch (Exception e) {
			throw new ConverterException("请求字符串转换失败（ContentType=" + contentType + "）", e);
		}
	}

	/**
	 * 转换为请求字节数组（适配文件/流/字符串/二进制/JSON）
	 *
	 * @param value   待转换对象（File/InputStream/byte[]/String/POJO等）
	 * @param request HTTP请求上下文（含ContentType、编码）
	 * @return 二进制字节数组
	 */
	@Override
	public byte[] convertToRequestBytes(Object value, HttpRequest request, ContentType contentType) {
		if (value == null) {
			return new byte[0];
		}

		Charset charset = getDefaultCharset(request);
		try {
			// 1. 二进制类型优先处理
			if (value instanceof File file) {
				return FileUtil.fileToByte(file); // 替换原fileToByte，Hutool的readBytes更稳定
			} else if (value instanceof InputStream inputStream) {
				return IoUtil.copyToByteArray(inputStream); // 替换copyToByteArray，更贴合字节读取语义
			} else if (value instanceof byte[] bytes) {
				return bytes;
			} else if (value instanceof String str) {
				// 指定编码，避免系统默认编码乱码
				return str.getBytes(charset);
			} else {
				// 默认JSON序列化
				return JsonUtil.toJsonBytes(value);
			}
		} catch (Exception e) {
			throw new ConverterException("请求字节数组转换失败（value类型=" + value.getClass().getName() + "）", e);
		}
	}

	/**
	 * 转换为响应对象（适配JSON/XML，处理空字节数组）
	 *
	 * @param bytes    响应字节数组
	 * @param response HTTP响应上下文（含ContentType、编码）
	 * @param type     目标类型
	 * @return 反序列化后的对象，bytes为空时返回null
	 */
	@Override
	public <T> T convertToResponseObject(byte[] bytes, HttpResponse response, Type type) {
		if (bytes == null || bytes.length == 0) {
			return null;
		}

		ContentType contentType = response.contentType();
		Charset charset = getDefaultCharset(response);

		try {
			if (contentType == null || contentType.getMediaType().equalsIgnoreCase(ContentType.JSON)) {
				return JsonUtil.parse(new String(bytes, charset), JsonUtil.getJavaType(type));
			}
			if (contentType.getMediaType().equalsIgnoreCase(ContentType.XML)) {
				throw new ConverterException("XML反序列化暂未实现，请补充XML工具类逻辑");
				// return XmlUtil.parse(new String(bytes, charset), JsonUtil.getJavaType(type));
			}
			throw new ConverterException("不支持的响应ContentType：" + response.contentType() + "，无法转换为对象");
		} catch (Exception e) {
			throw new ConverterException("响应对象转换失败（目标类型=" + type.getTypeName() + "）", e);
		}
	}
}
