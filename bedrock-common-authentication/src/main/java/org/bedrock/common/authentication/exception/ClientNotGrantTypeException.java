package org.bedrock.common.authentication.exception;

import org.bedrock.common.authentication.enums.ErrorEnums;

public class ClientNotGrantTypeException extends OAuth2Exception {

	public ClientNotGrantTypeException() {
		super(ErrorEnums.CLIENT_NOT_GRANT_TYPE_OAUTH2.getCode(), ErrorEnums.CLIENT_NOT_GRANT_TYPE_OAUTH2.getMessage());
	}

	public ClientNotGrantTypeException(Throwable cause) {
		super(ErrorEnums.CLIENT_NOT_GRANT_TYPE_OAUTH2.getCode(), ErrorEnums.CLIENT_NOT_GRANT_TYPE_OAUTH2.getMessage(), cause);
	}
}
