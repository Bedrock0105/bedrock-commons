package org.bedrock.common.excel.support;

import cn.idev.excel.metadata.GlobalConfiguration;
import cn.idev.excel.metadata.data.ReadCellData;
import cn.idev.excel.metadata.data.WriteCellData;
import cn.idev.excel.metadata.property.ExcelContentProperty;
import org.bedrock.common.code.constant.StringPool;
import org.bedrock.common.code.reflects.FieldMeta;
import org.bedrock.common.code.util.SpELParserUtil;
import org.bedrock.common.code.util.StringUtil;
import org.bedrock.common.excel.annotation.ExcelConverter;

import java.util.HashMap;
import java.util.Map;

/**
 * 解析@ExcelCoverter 注解
 */
public class ExcelConverterResolver implements HandlerExcelConverterResolver {

	@Override
	public boolean supportsParameter(FieldMeta fieldMeta) {
		return fieldMeta.hasFieldAnnotation(ExcelConverter.class);
	}

	@Override
	public Object convertToJavaData(FieldMeta fieldMeta, ReadCellData<?> cellData, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) throws Exception {
		ExcelConverter fieldAnnotation = fieldMeta.getFieldAnnotation(ExcelConverter.class);
		String converterData = fieldAnnotation.converterData();
		if (StringUtil.isNotBlank(converterData)) {
			return converterData(fieldAnnotation, true, cellData.getStringValue());
		}
		Map<String, Object> variables = new HashMap<>();
		variables.put("value", cellData.getStringValue());
		variables.put("readCallData", cellData);
		return SpELParserUtil.parse(fieldAnnotation.excelToJava(), variables, fieldMeta.getFieldType(), null);
	}

	@Override
	public WriteCellData<?> convertToExcelData(FieldMeta fieldMeta, Object value, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) throws Exception {
		ExcelConverter fieldAnnotation = fieldMeta.getFieldAnnotation(ExcelConverter.class);
		String converterData = fieldAnnotation.converterData();
		if (StringUtil.isNotBlank(converterData)) {
			return new WriteCellData<>(converterData(fieldAnnotation, false, value.toString()));
		}
		Map<String, Object> variables = new HashMap<>();
		variables.put("value", value);
		Object parse = SpELParserUtil.parse(fieldAnnotation.javaToExcel(), variables, null);
		if (parse instanceof WriteCellData) {
			return (WriteCellData<?>) parse;
		}
		if (parse == null) {
			return new WriteCellData<>(StringPool.EMPTY);
		}
		return new WriteCellData<>(parse.toString());
	}

	public String converterData(ExcelConverter excelConverter, boolean isRead, Object value) {
		String data = excelConverter.converterData();
		String[] split = data.split(StringPool.COMMA);
		for (String string : split) {
			if (StringUtil.isBlank(string) || !string.contains(StringPool.EQUALS)) {
				continue;
			}
			String[] splitTrim = StringUtil.split(string, StringPool.EQUALS);
			if (isRead && splitTrim[1].equals(value)) {
				return splitTrim[0];
			} else if (!isRead && splitTrim[0].equals(value)) {
				return splitTrim[1];
			}
		}
		return value.toString();
	}

}
