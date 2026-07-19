package org.bedrock.common.authentication.exception;

import org.bedrock.common.authentication.enums.ErrorEnums;

/**
 * 客户端没有找到异常
 */
public class ClientNotFoundOauth2Exception extends OAuth2Exception {

	public ClientNotFoundOauth2Exception() {
		super(ErrorEnums.CLIENT_NOT_FOUND_OAUTH2.getCode(), ErrorEnums.CLIENT_NOT_FOUND_OAUTH2.getMessage());
	}

	public ClientNotFoundOauth2Exception(Throwable cause) {
		super(ErrorEnums.CLIENT_NOT_FOUND_OAUTH2.getCode(), ErrorEnums.CLIENT_NOT_FOUND_OAUTH2.getMessage(), cause);
	}
}
