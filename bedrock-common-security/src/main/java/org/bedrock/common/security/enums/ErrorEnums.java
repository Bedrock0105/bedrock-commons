package org.bedrock.common.security.enums;

import lombok.Getter;

/**
 * 异常信息
 */
@Getter
public enum ErrorEnums {

	USER_NOT_LOGIN(401, "用户未登录"),
	USER_NOT_PERMISSION(403, "用户权限不够"),
	;

	private final Integer code;

	private final String message;

	ErrorEnums(Integer code, String message) {
		this.code = code;
		this.message = message;
	}
}
