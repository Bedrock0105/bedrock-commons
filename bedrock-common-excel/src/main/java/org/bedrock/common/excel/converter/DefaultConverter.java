package org.bedrock.common.excel.converter;

import cn.idev.excel.context.WriteContext;
import cn.idev.excel.converters.*;
import cn.idev.excel.enums.CellDataTypeEnum;
import cn.idev.excel.metadata.data.ReadCellData;
import cn.idev.excel.metadata.data.WriteCellData;
import cn.idev.excel.metadata.property.ExcelContentProperty;
import cn.idev.excel.support.ExcelTypeEnum;
import org.bedrock.common.code.constant.StringPool;
import org.bedrock.common.code.reflects.FieldMeta;
import org.bedrock.common.excel.support.HandlerExcelConverterResolver;
import org.bedrock.common.log.exception.ServiceException;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.bedrock.common.excel.enums.ExcelErrorEnum.*;

/**
 * 默认的转换器
 */
public class DefaultConverter implements Converter<Object> {

	private final List<HandlerExcelConverterResolver> convertToJavaData;


	private static final Map<FieldMeta, HandlerExcelConverterResolver> convertToJavaDataCache = new ConcurrentHashMap<>();


	@Override
	public Class<?> supportJavaTypeKey() {
		return Object.class;
	}

	@Override
	public CellDataTypeEnum supportExcelTypeKey() {
		return null;
	}

	@Override
	public Object convertToJavaData(ReadConverterContext<?> context) throws Exception {
		ExcelContentProperty contentProperty = context.getContentProperty();
		FieldMeta fieldMeta = FieldMeta.forField(contentProperty.getField());
		HandlerExcelConverterResolver convert = getConvert(fieldMeta);
		if (convert != null) {
			return convert.convertToJavaData(fieldMeta, context);
		}
		return convertToJavaData(fieldMeta, context);
	}

	@Override
	public WriteCellData<?> convertToExcelData(WriteConverterContext<Object> context) throws Exception {
		ExcelContentProperty contentProperty = context.getContentProperty();
		FieldMeta fieldMeta = FieldMeta.forField(contentProperty.getField());
		HandlerExcelConverterResolver convert = getConvert(fieldMeta);
		if (convert != null) {
			return convert.convertToExcelData(fieldMeta, context);
		}
		return convertToExcelData(fieldMeta, context);
	}

	public Object convertToJavaData(FieldMeta fieldMeta, ReadConverterContext<?> context) {
		ReadCellData<?> cellData = context.getReadCellData();
		boolean canNotConverterEmpty =
			cellData.getType() == CellDataTypeEnum.EMPTY;
		if (canNotConverterEmpty) {
			return null;
		}
		Converter<?> converter = context.getAnalysisContext()
			.currentReadHolder()
			.converterMap()
			.get(ConverterKeyBuild.buildKey(fieldMeta.getFieldType(), cellData.getType()));
		if (converter == null) {
			throw new ServiceException(EXCEL_CONVERTER_FOUND, fieldMeta.getFieldType().getSimpleName(), cellData.getType());
		}
		try {
			return converter.convertToJavaData(context);
		} catch (Exception e) {
			throw new ServiceException(EXCEL_CONVERTER_ERROR, e, cellData);
		}
	}

	public WriteCellData<?> convertToExcelData(FieldMeta fieldMeta, WriteConverterContext<Object> context) {
		WriteContext writeContext = context.getWriteContext();
		Converter<?> converter;
		CellDataTypeEnum targetCellDataType = null;
		if (writeContext.writeWorkbookHolder().getExcelType() == ExcelTypeEnum.CSV) {
			targetCellDataType = CellDataTypeEnum.STRING;
		}
		converter = writeContext
			.currentWriteHolder()
			.converterMap()
			.get(ConverterKeyBuild.buildKey(
				fieldMeta.getFieldType(),
				targetCellDataType));
		if (context.getValue() == null && !(converter instanceof NullableObjectConverter)) {
			return new WriteCellData<>(CellDataTypeEnum.EMPTY);
		}
		if (converter == null) {
			throw new ServiceException(EXCEL_CONVERTER_FOUND, fieldMeta.getFieldType().getSimpleName(), StringPool.EMPTY);

		}
		WriteCellData<?> cellData;
		try {
			cellData = ((Converter<Object>) converter)
				.convertToExcelData(context);
		} catch (Exception e) {
			throw new ServiceException(EXCEL_CONVERTER_ERROR, e, context.getValue());
		}
		if (cellData == null || cellData.getType() == null) {
			throw new ServiceException(EXCEL_CONVERTER_RETURN_NULL, context.getValue());
		}
		return cellData;
	}

	public HandlerExcelConverterResolver getConvert(FieldMeta fieldMeta) {
		return convertToJavaDataCache.computeIfAbsent(fieldMeta, (key) -> {
			for (HandlerExcelConverterResolver resolver : convertToJavaData) {
				if (resolver.supportsParameter(key)) {
					return resolver;
				}
			}
			return null;
		});
	}


	public DefaultConverter(List<HandlerExcelConverterResolver> convertToJavaData) {
		this.convertToJavaData = convertToJavaData;
		this.convertToJavaData.sort(Comparator.comparingInt(HandlerExcelConverterResolver::getOrder));
	}

	public static void clearCache() {
		convertToJavaDataCache.clear();
	}
}
