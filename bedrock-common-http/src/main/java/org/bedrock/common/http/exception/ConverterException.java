package org.bedrock.common.http.exception;

import lombok.Getter;
import org.bedrock.common.code.support.ResultCode;

@Getter
public class ConverterException extends HttpException {

	public ConverterException(String message) {
		super(message);
	}

	public ConverterException(String message, Throwable cause) {
		super(message, cause);
	}

	public ConverterException(int code, String message) {
		super(code, message);
	}

	public ConverterException(ResultCode resultCode) {
		super(resultCode);
	}

	public ConverterException(ResultCode resultCode, Object... args) {
		super(resultCode, args);
	}

	public ConverterException(int code, String message, Throwable cause) {
		super(code, message, cause);
	}

	public ConverterException(ResultCode resultCode, Throwable cause) {
		super(resultCode, cause);
	}

	public ConverterException(ResultCode resultCode, Throwable cause, Object... args) {
		super(resultCode, cause, args);
	}
}
