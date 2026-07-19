package org.bedrock.common.authentication.exception;

import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import org.bedrock.common.code.support.ResultCode;

import java.io.Serial;

@Getter
public class OAuth2Exception extends RuntimeException {

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

	public OAuth2Exception(String message) {
		super(message);
		this.message = message;
		this.code = HttpServletResponse.SC_UNAUTHORIZED;
	}

	public OAuth2Exception(Integer code, String message) {
		super(message);
		this.code = code;
		this.message = message;
	}

	public OAuth2Exception(ResultCode resultCode, Object... args) {
		super(resultCode.formatMessage(resultCode.getMessage(), args));
		this.code = resultCode.getCode();
		this.message = resultCode.formatMessage(resultCode.getMessage(), args);
	}

	public OAuth2Exception(Integer code, String message, Throwable cause) {
		super(message, cause);
		this.code = code;
		this.message = message;
	}
}
