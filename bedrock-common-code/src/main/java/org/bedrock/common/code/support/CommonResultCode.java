package org.bedrock.common.code.support;

import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;

@Getter
public enum CommonResultCode implements ResultCode {

	SUCCESS(HttpServletResponse.SC_OK, "操作成功"),
	FAILURE(HttpServletResponse.SC_BAD_REQUEST, "操作失败"),
	ERROR(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "服务器异常"),
	;

	private final int code;

	private final String message;

	CommonResultCode(int code, String message) {
		this.code = code;
		this.message = message;
	}
}
