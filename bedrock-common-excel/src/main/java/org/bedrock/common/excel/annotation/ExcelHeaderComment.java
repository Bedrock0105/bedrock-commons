package org.bedrock.common.excel.annotation;

import java.lang.annotation.*;

/**
 * 表头批注注解（悬停显示说明）
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ExcelHeaderComment {

	/**
	 * 批注内容（表头说明）
	 */
	String value();

	/**
	 * 批注作者
	 */
	String author() default "系统自动生成";

	/**
	 * 批注行高
	 */
	int rowHeight() default 3;
}
