package org.bedrock.common.authentication.exception;

import org.bedrock.common.authentication.enums.ErrorEnums;

/**
 * 用户未启用
 */
public class UsernameNotEnabledException extends OAuth2Exception {

	public UsernameNotEnabledException() {
		super(ErrorEnums.USER_NOT_ENABLED.getCode(), ErrorEnums.USER_NOT_ENABLED.getMessage());
	}

	public UsernameNotEnabledException(Throwable cause) {
		super(ErrorEnums.USER_NOT_ENABLED.getCode(), ErrorEnums.USER_NOT_ENABLED.getMessage(), cause);
	}
}
