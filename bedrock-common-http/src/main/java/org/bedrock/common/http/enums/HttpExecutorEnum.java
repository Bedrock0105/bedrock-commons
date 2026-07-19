package org.bedrock.common.http.enums;

import lombok.Getter;

/**
 * 执行器类型
 */
@Getter
public enum HttpExecutorEnum {
	OKHTTP("okhttp"),
	;

	private final String name;

	HttpExecutorEnum(String name) {
		this.name = name;
	}
}
