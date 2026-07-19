package org.bedrock.common.authentication.exception;

import org.bedrock.common.authentication.enums.ErrorEnums;

public class GrantTypeMissingException extends OAuth2Exception {

	public GrantTypeMissingException() {
		super(ErrorEnums.GRANT_TYPE_MISSING_EXCEPTION.getCode(), ErrorEnums.GRANT_TYPE_MISSING_EXCEPTION.getMessage());
	}

	public GrantTypeMissingException(Throwable cause) {
		super(ErrorEnums.GRANT_TYPE_MISSING_EXCEPTION.getCode(), ErrorEnums.GRANT_TYPE_MISSING_EXCEPTION.getMessage(), cause);
	}
}
