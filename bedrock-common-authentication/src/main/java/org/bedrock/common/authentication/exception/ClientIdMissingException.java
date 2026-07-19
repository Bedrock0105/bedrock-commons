package org.bedrock.common.authentication.exception;

import org.bedrock.common.authentication.enums.ErrorEnums;

public class ClientIdMissingException extends OAuth2Exception {

	public ClientIdMissingException() {
		super(ErrorEnums.CLIENT_ID_MISSING_EXCEPTION.getCode(), ErrorEnums.CLIENT_ID_MISSING_EXCEPTION.getMessage());
	}

	public ClientIdMissingException(Throwable cause) {
		super(ErrorEnums.CLIENT_ID_MISSING_EXCEPTION.getCode(), ErrorEnums.CLIENT_ID_MISSING_EXCEPTION.getMessage(), cause);
	}
}
