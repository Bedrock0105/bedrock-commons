package org.bedrock.common.http.model.param;

public record Param(String name, Object value, boolean encoded) {

	/**
	 * 构造默认需URL编码的参数
	 *
	 * @param name  参数名 参数名空的时候把value转map
	 * @param value 参数值
	 */
	public Param(String name, Object value) {
		this(name, value, true);
	}
}
