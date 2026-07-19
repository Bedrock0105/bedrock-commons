package org.bedrock.common.security.handler;

import lombok.extern.slf4j.Slf4j;
import org.bedrock.common.security.exception.NotLoginException;
import org.bedrock.common.security.exception.PermissionDeniedException;
import org.bedrock.common.code.api.R;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class SecurityExceptionHandler {

	@ExceptionHandler(value = PermissionDeniedException.class)
	public R<Void> handle(PermissionDeniedException e) {
		log.error("PermissionDeniedException: {}", e.getMessage(), e);
		return R.error(e.getCode(), e.getMessage());
	}

	@ExceptionHandler(value = NotLoginException.class)
	public R<Void> notLoginException(NotLoginException e) {
		log.error("NotLoginException: {}", e.getMessage(), e);
		return R.error(e.getCode(), e.getMessage());
	}
}
