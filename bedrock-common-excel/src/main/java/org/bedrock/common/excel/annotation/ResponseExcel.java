package org.bedrock.common.excel.annotation;

import cn.idev.excel.support.ExcelTypeEnum;

import java.lang.annotation.*;

/**
 * excel 导出注解，自动解析表格
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ResponseExcel {

	/**
	 * 文件名
	 */
	String fileName();

	/**
	 * sheet
	 */
	Sheet[] sheets();

	/**
	 * 文件类型 （xlsx xls）
	 */
	ExcelTypeEnum excelType() default ExcelTypeEnum.XLSX;

	/**
	 * 密码
	 */
	String password() default "";

	/**
	 * 内存中处理
	 */
	boolean inMemory() default false;

	/**
	 * sheet
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Documented
	@interface Sheet {

		/**
		 * sheet 名称
		 */
		String name();

		/**
		 * 表头类
		 */
		Class<?> head();

		/**
		 * sheet 索引
		 */
		int index() default 0;

	}
}
