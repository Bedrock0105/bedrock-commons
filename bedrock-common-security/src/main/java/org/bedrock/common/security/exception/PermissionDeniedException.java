package org.bedrock.common.security.exception;

import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import org.bedrock.common.security.enums.ErrorEnums;

import java.io.Serial;

/**
 * 权限异常父类
 */
@Getter
public class PermissionDeniedException extends RuntimeException {

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

	public PermissionDeniedException() {
		super(ErrorEnums.USER_NOT_PERMISSION.getMessage());
		this.code = ErrorEnums.USER_NOT_PERMISSION.getCode();
		this.message = ErrorEnums.USER_NOT_PERMISSION.getMessage();
	}

	public PermissionDeniedException(String message) {
		super(message);
		this.message = message;
		this.code = HttpServletResponse.SC_FORBIDDEN;
	}

	public PermissionDeniedException(Integer code, String message) {
		super(message);
		this.code = code;
		this.message = message;
	}

	public PermissionDeniedException(Integer code, String message, Throwable cause) {
		super(message, cause);
		this.code = code;
		this.message = message;
	}
}
