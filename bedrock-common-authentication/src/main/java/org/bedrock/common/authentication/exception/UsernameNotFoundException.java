package org.bedrock.common.authentication.exception;

import org.bedrock.common.authentication.enums.ErrorEnums;

/**
 * 用户不存在
 */
public class UsernameNotFoundException extends OAuth2Exception {

	public UsernameNotFoundException() {
		super(ErrorEnums.USER_NOT_FOUND.getCode(), ErrorEnums.USER_NOT_FOUND.getMessage());
	}

	public UsernameNotFoundException(Throwable cause) {
		super(ErrorEnums.USER_NOT_FOUND.getCode(), ErrorEnums.USER_NOT_FOUND.getMessage(), cause);
	}
}
