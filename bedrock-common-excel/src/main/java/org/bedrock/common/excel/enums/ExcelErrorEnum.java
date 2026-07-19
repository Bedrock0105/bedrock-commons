package org.bedrock.common.excel.enums;

import lombok.Getter;
import org.bedrock.common.code.support.ResultCode;

@Getter
public enum ExcelErrorEnum implements ResultCode {
	EXCEL_CONVERTER_FOUND(6000, "Can not find 'Converter' support class %s %s."),
	EXCEL_CONVERTER_ERROR(6001, "Convert data: %s error."),
	EXCEL_CONVERTER_RETURN_NULL(6002, "Convert data: %s return is null or return type is null"),
	;

	private final int code;

	private final String message;

	ExcelErrorEnum(Integer code, String message) {
		this.code = code;
		this.message = message;
	}
}
