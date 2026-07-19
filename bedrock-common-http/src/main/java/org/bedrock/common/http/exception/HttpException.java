package org.bedrock.common.http.exception;

import lombok.Getter;
import org.bedrock.common.code.support.ResultCode;

/**
 * 业务异常
 */
@Getter
public class HttpException extends RuntimeException {

	private final int code;

	private final String message;

	/**
	 * 构造方法
	 *
	 * @param message 错误信息
	 */
	public HttpException(String message) {
		super(message);
		this.code = 500;
		this.message = message;
	}

	/**
	 * 构造方法
	 *
	 * @param message 错误信息
	 * @param cause   错误原因
	 */
	public HttpException(String message, Throwable cause) {
		super(message, cause);
		this.code = 500;
		this.message = message;
	}

	/**
	 * 构造方法
	 *
	 * @param code    错误码
	 * @param message 错误信息
	 */
	public HttpException(int code, String message) {
		super(message);
		this.code = code;
		this.message = message;
	}

	/**
	 * 构造方法
	 */
	public HttpException(ResultCode resultCode) {
		super(resultCode.getMessage());
		this.code = resultCode.getCode();
		this.message = resultCode.getMessage();
	}

	/**
	 * 构造方法
	 */
	public HttpException(ResultCode resultCode, Object... args) {
		super(resultCode.formatMessage(args));
		this.code = resultCode.getCode();
		this.message = resultCode.formatMessage(args);
	}

	/**
	 * 构造方法
	 *
	 * @param code    错误码
	 * @param message 错误信息
	 * @param cause   错误原因
	 */
	public HttpException(int code, String message, Throwable cause) {
		super(message, cause);
		this.code = code;
		this.message = message;
	}

	/**
	 * 构造方法
	 */
	public HttpException(ResultCode resultCode, Throwable cause) {
		super(resultCode.getMessage(), cause);
		this.code = resultCode.getCode();
		this.message = resultCode.getMessage();
	}

	/**
	 * 构造方法
	 */
	public HttpException(ResultCode resultCode, Throwable cause, Object... args) {
		super(resultCode.formatMessage(args), cause);
		this.code = resultCode.getCode();
		this.message = resultCode.formatMessage(args);
	}

}
