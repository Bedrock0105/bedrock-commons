package org.bedrock.common.excel.support;

import cn.idev.excel.converters.ReadConverterContext;
import cn.idev.excel.converters.WriteConverterContext;
import cn.idev.excel.metadata.GlobalConfiguration;
import cn.idev.excel.metadata.data.ReadCellData;
import cn.idev.excel.metadata.data.WriteCellData;
import cn.idev.excel.metadata.property.ExcelContentProperty;
import org.bedrock.common.code.reflects.FieldMeta;
import org.springframework.core.Ordered;

/**
 * Excel数据双向转换器解析器接口
 * <p>
 * 定义Excel单元格数据与Java对象字段之间的转换契约，支持自定义字段级别的Excel读写转换逻辑。
 * 实现类需根据字段元信息判断是否支持转换，并实现具体的双向转换逻辑。
 * 继承{@link Ordered}接口，支持通过优先级控制多个转换器的执行顺序。
 */
public interface HandlerExcelConverterResolver extends Ordered {

	/**
	 * 判断当前转换器是否支持对指定字段参数进行Excel数据转换
	 * <p>
	 * 可根据字段的类型、注解、字段名等元信息判断是否支持转换
	 *
	 * @param fieldMeta 字段参数封装对象，包含字段元信息（字段类型、注解、Field实例等）
	 * @return true：支持当前字段的转换；false：不支持
	 */
	boolean supportsParameter(FieldMeta fieldMeta);

	/**
	 * Excel数据转Java对象数据（默认实现）
	 * <p>
	 * 从读取上下文{@link ReadConverterContext}中提取核心参数，委托给重载的抽象方法执行转换。
	 * 子类无需重写此方法，专注实现带完整参数的抽象方法即可。
	 *
	 * @param fieldMeta 字段参数封装对象（目标Java字段元信息）
	 * @param context        Excel读取转换上下文，包含单元格数据、全局配置等完整信息
	 * @return 转换后的Java对象（需匹配目标字段的类型）
	 * @throws Exception 转换过程中发生的异常（类型不匹配、格式解析失败等）
	 */
	default Object convertToJavaData(FieldMeta fieldMeta, ReadConverterContext<?> context) throws Exception {
		// 从上下文提取核心参数：单元格数据、Excel内容属性、全局配置
		return convertToJavaData(fieldMeta, context.getReadCellData(), context.getContentProperty(), context.getAnalysisContext().currentReadHolder().globalConfiguration());
	}

	/**
	 * Excel数据转Java对象数据（核心转换逻辑）
	 * <p>
	 * 抽象方法，子类必须实现：将Excel单元格读取到的数据转换为目标Java字段类型的数据
	 *
	 * @param fieldMeta      字段参数封装对象（目标Java字段元信息）
	 * @param cellData            Excel单元格读取数据，包含单元格原始值、数据类型等
	 * @param contentProperty     Excel内容属性
	 * @param globalConfiguration Excel配置
	 * @return 转换后的Java对象（需与目标字段类型兼容）
	 * @throws Exception 转换异常（如格式解析失败、类型转换失败等）
	 */
	Object convertToJavaData(FieldMeta fieldMeta, ReadCellData<?> cellData, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) throws Exception;

	/**
	 * Java对象数据转Excel数据（默认实现）
	 * <p>
	 * 从写入上下文{@link WriteConverterContext}中提取核心参数，委托给重载的抽象方法执行转换。
	 * 子类无需重写此方法，专注实现带完整参数的抽象方法即可。
	 *
	 * @param fieldMeta 字段参数封装对象（源Java字段元信息）
	 * @param context        Excel写入转换上下文，包含Java对象值、全局配置等完整信息
	 * @return 转换后的Excel单元格写入数据{@link WriteCellData}，包含单元格显示值、格式等
	 * @throws Exception 转换过程中发生的异常（类型不匹配、格式处理失败等）
	 */
	default WriteCellData<?> convertToExcelData(FieldMeta fieldMeta, WriteConverterContext<?> context) throws Exception {
		// 从上下文提取核心参数：Java对象值、Excel内容属性、全局配置
		return convertToExcelData(fieldMeta, context.getValue(), context.getContentProperty(), context.getWriteContext().currentWriteHolder().globalConfiguration());
	}

	/**
	 * Java对象数据转Excel数据（核心转换逻辑）
	 * <p>
	 * 抽象方法，子类必须实现：将Java字段的值转换为Excel单元格需要的写入数据
	 *
	 * @param fieldMeta      字段参数封装对象（源Java字段元信息）
	 * @param value               待转换的Java对象值（源字段的实际值）
	 * @param contentProperty     Excel内容属性，包含列索引、日期格式、小数位数等配置信息
	 * @param globalConfiguration Excel全局配置，包含默认格式、转换器等全局设置
	 * @return Excel单元格写入数据{@link WriteCellData}，包含单元格显示值、数据类型、格式等信息
	 * @throws Exception 转换异常（如格式处理失败、空值处理异常等）
	 */
	WriteCellData<?> convertToExcelData(FieldMeta fieldMeta, Object value, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) throws Exception;

	@Override
	default int getOrder() {
		return 0;
	}
}
