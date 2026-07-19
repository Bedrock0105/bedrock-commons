package org.bedrock.common.authentication.exception;

import org.bedrock.common.authentication.enums.ErrorEnums;

/**
 * 密码错误
 */
public class PasswordMismatchException extends OAuth2Exception {

	public PasswordMismatchException() {
		super(ErrorEnums.USER_NOT_EXIST.getCode(), ErrorEnums.USER_NOT_EXIST.getMessage());
	}

	public PasswordMismatchException(Throwable cause) {
		super(ErrorEnums.USER_NOT_EXIST.getCode(), ErrorEnums.USER_NOT_EXIST.getMessage(), cause);
	}
}
