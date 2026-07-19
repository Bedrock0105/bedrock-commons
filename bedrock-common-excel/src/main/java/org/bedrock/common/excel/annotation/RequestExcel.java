package org.bedrock.common.excel.annotation;

import org.bedrock.common.excel.listener.ExcelListener;
import org.bedrock.common.excel.listener.IReadListener;

import java.lang.annotation.*;

/**
 * excel 导入注解，自动解析表格
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestExcel {

	/**
	 * 前端传输的excel文件参数名
	 */
	String value();

	/**
	 * 表头类
	 * 如果是Void.class 则自动获取参数 list参数的泛型
	 * 如果参数不是list 必须设置泛型
	 */
	Class<?> head() default Void.class;

	/**
	 * 监听器
	 */
	Class<? extends IReadListener> listener() default ExcelListener.class;

	/**
	 * 表单编号(0开始)
	 */
	int sheetNo() default 0;

	/**
	 * 表头行数
	 */
	int headRowNumber() default 1;

	/**
	 * 跳过的行数
	 */
	int skipRowNum() default 0;

}
