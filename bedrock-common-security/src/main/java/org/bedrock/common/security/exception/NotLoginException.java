package org.bedrock.common.security.exception;

import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;

import java.io.Serial;

/**
 * 用户未登录异常
 */
@Getter
public class NotLoginException extends RuntimeException {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 错误码
	 */
	private final Integer code;

	/**
	 * 错误信息
	 */
	private final String message;

	public NotLoginException(String message) {
		super(message);
		this.message = message;
		this.code = HttpServletResponse.SC_UNAUTHORIZED;
	}

	public NotLoginException(Integer code, String message) {
		super(message);
		this.code = code;
		this.message = message;
	}

	public NotLoginException(Integer code, String message, Throwable cause) {
		super(message, cause);
		this.code = code;
		this.message = message;
	}
}
