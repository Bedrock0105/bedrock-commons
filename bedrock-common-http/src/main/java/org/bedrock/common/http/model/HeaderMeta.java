package org.bedrock.common.http.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * header 数据模型
 */
@Getter
@Setter
@ToString
public class HeaderMeta {

	/**
	 * header 名称
	 */
	private String name;

	/**
	 * header 值
	 */
	private Object value;

	public HeaderMeta(String name, String value) {
		this.name = name;
		this.value = value;
	}

	public HeaderMeta(String name, Object value) {
		this.name = name;
		this.value = value;
	}

	public String getValue() {
		if (value != null) {
			return value.toString();
		}
		return null;
	}

	public Object getOriginalValue() {
		return value;
	}

	@Override
	public HeaderMeta clone() {
		return new HeaderMeta(name, value);
	}
}
