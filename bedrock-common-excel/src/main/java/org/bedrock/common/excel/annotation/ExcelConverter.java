package org.bedrock.common.excel.annotation;

import java.lang.annotation.*;

/**
 * Excel字段双向转换注解
 * <p>
 * 用于标注Java实体类的字段，配置该字段与Excel单元格之间的转换规则，支持两种转换方式：
 * 1. 静态键值对映射（{@link #converterData()}）：优先级最高，直接通过key-value映射完成转换
 * 2. SpEL表达式（{@link #javaToExcel()} 和 {@link #excelToJava()}）：灵活处理复杂转换逻辑
 * 配合 {@link org.bedrock.common.excel.support.HandlerExcelConverterResolver} 接口使用，实现注解驱动的字段级Excel转换
 *
 * @see org.bedrock.common.excel.support.HandlerExcelConverterResolver 对应的转换器解析器接口
 */
@Target({ElementType.FIELD}) // 仅支持标注在Java类的字段上
@Retention(RetentionPolicy.RUNTIME) // 运行时保留，允许反射获取注解信息
@Documented // 生成JavaDoc时包含该注解说明
public @interface ExcelConverter {

	/**
	 * Java对象转Excel的转换规则（SpEL表达式）
	 * <p>
	 * 当字段值从Java对象写入Excel时，通过该表达式计算最终的Excel单元格值
	 * 优先级：{@link #converterData()} 有值时，此表达式失效
	 *
	 * @return SpEL表达式，默认值为 "#value"（直接使用字段的字符串形式作为Excel值）
	 * @note 表达式可用变量：
	 * - #value：当前Java字段的字符串类型值（自动将字段值转为String）
	 * @see #converterData() 静态键值对映射（优先级更高）
	 */
	String javaToExcel() default "#value";

	/**
	 * Excel转Java对象的转换规则（SpEL表达式）
	 * <p>
	 * 当字段值从Excel读取到Java对象时，通过该表达式计算最终的Java字段值
	 * 优先级：{@link #converterData()} 有值时，此表达式失效
	 *
	 * @return SpEL表达式，默认值为 "#value"（直接使用Excel单元格的字符串值作为Java字段值）
	 * @note 表达式可用变量：
	 * - #value：Excel单元格的字符串类型值（自动将单元格数据转为String）
	 * - #readCallData：{@link cn.idev.excel.metadata.data.ReadCellData}类型对象，包含Java字段的原始数据信息
	 * @see #converterData() 静态键值对映射（优先级更高）
	 */
	String excelToJava() default "#value";

	/**
	 * 静态键值对转换数据（优先级最高）
	 * <p>
	 * 当该属性配置非空值时，优先使用此键值对进行双向转换，忽略SpEL表达式配置
	 * 转换逻辑：
	 * 1. Java转Excel：根据Java字段值（作为key）查找对应的value，写入Excel
	 * 2. Excel转Java：根据Excel单元格值（作为value）查找对应的key，赋值给Java字段
	 * 若未找到匹配的键/值，默认保留原始值（具体逻辑由转换器实现类决定）
	 *
	 * @return 键值对字符串，格式为 "key1=value1,key2=value2"，默认值为空字符串（不启用静态映射）
	 * @example "M=男,F=女"：Java字段值M→Excel显示"男"；Excel读取"女"→Java字段值F
	 */
	String converterData() default "";
}
