package org.bedrock.common.http.model.param;

import lombok.Getter;
import org.bedrock.common.http.HttpRequest;

/**
 * 二进制数据参数元数据类
 * 封装二进制类型参数的核心信息，支持文件、字节数组、输入流等二进制数据类型
 */
@Getter
public class BinaryDataParamMeta {

	/**
	 * 关联的HTTP请求实例
	 */
	private final HttpRequest request;

	/**
	 * 参数名称
	 */
	private String name;

	/**
	 * 二进制数据值（支持File、byte[]、InputStream等类型）
	 */
	private Object value;

	/**
	 * 核心设置方法（指定所有核心属性，其他重载方法最终调用该方法）
	 *
	 * @param name  参数名称
	 * @param value 二进制数据值
	 * @return 当前BinaryDataParamMeta实例（链式调用）
	 */
	public BinaryDataParamMeta set(String name, Object value) {
		this.name = name;
		this.value = value;
		return this;
	}

	/**
	 * 判断当前二进制参数是否为空
	 *
	 * @return true=参数为空（参数名称或数据值为null），false=参数非空
	 */
	public boolean isEmpty() {
		return value == null || name == null;
	}

	/**
	 * 构造参数元数据实例
	 *
	 * @param request 关联的HTTP请求实例
	 */
	public BinaryDataParamMeta(HttpRequest request) {
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

	@Override
	public String toString() {
		byte[] bytes = request.getConverter().convertToRequestBytes(value, request, request.contentType());
		return "[二进制数据，字节长度: " + bytes.length + "]";
	}
}
