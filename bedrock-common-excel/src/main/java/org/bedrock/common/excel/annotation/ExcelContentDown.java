package org.bedrock.common.excel.annotation;

import org.bedrock.common.excel.model.ExcelDownOption;

import java.lang.annotation.*;

/**
 * Excel 字段下拉框配置注解
 * <p>
 * 标注在 Java 实体类的字段上，用于为该字段对应的 Excel 列生成下拉选择框，支持两种数据来源（静态+动态），
 * 可灵活配置下拉框的生效行范围（适配表头+数据行的常规Excel结构）。
 * 核心优先级：静态数据 {@link #dataDown()} > 动态SpEL表达式 {@link #value()}
 */
@Target({ElementType.FIELD}) // 仅支持标注在实体类字段上
@Retention(RetentionPolicy.RUNTIME) // 运行时保留，允许反射解析配置
@Documented // 生成JavaDoc时包含该注解说明
public @interface ExcelContentDown {

	/**
	 * 动态下拉选项（SpEL表达式）
	 * <p>
	 * 通过 SpEL 表达式动态获取下拉选项，支持从字典、枚举、配置等数据源动态加载，
	 * 表达式执行结果必须是 {@link String[]} 或 {@link java.util.List<String>} 或 {@link String}逗号分隔 或 {@link ExcelDownOption} 或 {@link java.util.List<ExcelDownOption>}  类型。
	 * 优先级低于 {@link #dataDown()}，当 {@link #dataDown()} 配置非空时，此属性失效。
	 *
	 * @return SpEL表达式字符串，默认空字符串（不启用动态获取）
	 * @example "T(org.bedrock.enums.GenderEnum).values().stream().map(T(org.bedrock.enums.GenderEnum)::getLabel).collect(T(java.util.stream.Collectors)::toList)"
	 */
	String value() default "";

	/**
	 * 静态下拉选项（优先级最高）
	 * <p>
	 * 直接配置固定的下拉选项，无需动态计算，性能更优。当配置非空数组时，优先使用此选项生成下拉框，
	 * 忽略 {@link #value()} 的SpEL表达式配置。
	 *
	 * @return 静态下拉选项数组，默认空数组（不启用静态选项）
	 * @example {"男", "女", "未知"}（具体显示效果由Excel解析逻辑决定）
	 */
	String[] dataDown() default {};

	/**
	 * 下拉框生效的起始行号（当前列专属）
	 * <p>
	 * 仅对当前字段对应的Excel列生效，采用Excel原生行号规则（从0开始计数）。
	 * 默认值1：适配「第0行是表头、第1行开始是数据行」的常规Excel模板结构，避免下拉框覆盖表头。
	 * 是下标，行数-1 就是下标
	 *
	 * @return 起始行号，默认值1
	 */
	int dropdownStartRow() default 1;

	/**
	 * 下拉框生效的结束行号（当前列专属）
	 * <p>
	 * 仅对当前字段对应的Excel列生效，控制下拉框的最大生效范围，避免无限制扩展影响Excel性能。
	 * 默认值1000：覆盖大部分业务场景的最大数据行需求，若需支持更多行可手动调整。
	 *
	 * @return 结束行号，默认值1000
	 */
	int dropdownEndRow() default 1000;

	/**
	 * 错误提示框
	 */
	boolean showErrorBox() default false;

	/**
	 * 错误提示框标题
	 */
	String errorTitle() default "";

	/**
	 * 错误提示信息
	 */
	String errorMessage() default "";

	/**
	 * 错误样式
	 * 0 --->STOP
	 * 1 --->WARNING
	 * 2 --->INFO
	 */
	int errorStyle() default 1;

	/**
	 * 输入提示框
	 */
	boolean showPromptBox() default false;

	/**
	 * 输入提示框标题
	 */
	String promptTitle() default "";

	/**
	 * 输入提示信息
	 */
	String promptMessage() default "";
}
