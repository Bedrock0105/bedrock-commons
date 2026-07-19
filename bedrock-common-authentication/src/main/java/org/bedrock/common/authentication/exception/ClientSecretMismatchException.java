package org.bedrock.common.authentication.exception;

import org.bedrock.common.authentication.enums.ErrorEnums;

public class ClientSecretMismatchException extends OAuth2Exception {

	public ClientSecretMismatchException() {
		super(ErrorEnums.CLIENT_SECRET_MISMATCH_EXCEPTION.getCode(), ErrorEnums.CLIENT_SECRET_MISMATCH_EXCEPTION.getMessage());
	}

	public ClientSecretMismatchException(Throwable cause) {
		super(ErrorEnums.CLIENT_SECRET_MISMATCH_EXCEPTION.getCode(), ErrorEnums.CLIENT_SECRET_MISMATCH_EXCEPTION.getMessage(), cause);
	}
}
