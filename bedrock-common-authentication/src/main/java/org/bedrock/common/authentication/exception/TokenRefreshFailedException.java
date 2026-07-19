package org.bedrock.common.authentication.exception;

import org.bedrock.common.authentication.enums.ErrorEnums;

public class TokenRefreshFailedException extends OAuth2Exception {

	public TokenRefreshFailedException() {
		super(ErrorEnums.TOKEN_REFRESH_FAILED_EXCEPTION.getCode(), ErrorEnums.TOKEN_REFRESH_FAILED_EXCEPTION.getMessage());
	}

	public TokenRefreshFailedException(Throwable cause) {
		super(ErrorEnums.TOKEN_REFRESH_FAILED_EXCEPTION.getCode(), ErrorEnums.TOKEN_REFRESH_FAILED_EXCEPTION.getMessage(), cause);
	}
}
