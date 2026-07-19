package org.bedrock.common.authentication.enums;

import lombok.Getter;

/**
 * 异常信息
 */
@Getter
public enum ErrorEnums {

	USER_NOT_FOUND(10001, "用户不存在"),
	USER_NOT_ENABLED(10002, "用户未启用"),
	USER_NOT_EXIST(10003, "密码错误"),
	CLIENT_NOT_FOUND_OAUTH2(10004, "客户端不存在"),
	CLIENT_NOT_GRANT_TYPE_OAUTH2(10005, "授权类型不存在"),
	CLIENT_ID_MISSING_EXCEPTION(10006, "未携带客户端或者格式不正确"),
	CLIENT_SECRET_MISMATCH_EXCEPTION(10007, "客户端密钥不匹配"),
	TOKEN_REFRESH_FAILED_EXCEPTION(10008, "刷新TOKEN校验失败,已经过期或者类型不匹配"),
	GRANT_TYPE_MISSING_EXCEPTION(10009, "缺少授权类型"),
	INVALID_GRANT_TYPE_EXCEPTION(10010, "授权类型不匹配"),
	;

	private final Integer code;

	private final String message;

	ErrorEnums(Integer code, String message) {
		this.code = code;
		this.message = message;
	}
}
