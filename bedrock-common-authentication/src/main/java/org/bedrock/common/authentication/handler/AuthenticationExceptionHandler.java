package org.bedrock.common.authentication.handler;

import lombok.extern.slf4j.Slf4j;
import org.bedrock.common.authentication.exception.OAuth2Exception;
import org.bedrock.common.code.api.R;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class AuthenticationExceptionHandler {

	@ExceptionHandler(value = OAuth2Exception.class)
	public R<Void> handle(OAuth2Exception e) {
		log.error("OAuth2Exception: {}", e.getMessage(), e);
		return R.error(e.getCode(), e.getMessage());
	}
}
