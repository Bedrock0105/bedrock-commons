package org.bedrock.common.authentication.exception;

import org.bedrock.common.authentication.enums.ErrorEnums;

public class InvalidGrantTypeException extends OAuth2Exception {

	public InvalidGrantTypeException() {
		super(ErrorEnums.INVALID_GRANT_TYPE_EXCEPTION.getCode(), ErrorEnums.INVALID_GRANT_TYPE_EXCEPTION.getMessage());
	}

	public InvalidGrantTypeException(Throwable cause) {
		super(ErrorEnums.INVALID_GRANT_TYPE_EXCEPTION.getCode(), ErrorEnums.INVALID_GRANT_TYPE_EXCEPTION.getMessage(), cause);
	}
}
