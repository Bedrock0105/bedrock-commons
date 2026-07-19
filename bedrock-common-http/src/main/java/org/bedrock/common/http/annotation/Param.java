package org.bedrock.common.http.annotation;

import java.lang.annotation.*;

/**
 * 主要用于指定参数名称，模版解析里面使用
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Param {

	/**
	 * 参数名称
	 */
	String value();
}
