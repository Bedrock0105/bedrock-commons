package org.bedrock.common.http.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * query参数注解
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Query {

	/**
	 * 参数名称
	 * 生效规则：
	 * 1.  若未指定该属性（默认空字符串），则按参数对象整体进行结构化解析
	 * 2.  若未指定该属性（默认空字符串），并且执行的值是null，默认使用参数对象名称，值是空字符串
	 */
	@AliasFor("value")
	String name() default "";

	/**
	 * 参数名称
	 * 生效规则：
	 * 1.  若未指定该属性（默认空字符串），则按参数对象整体进行结构化解析
	 * 2.  若未指定该属性（默认空字符串），并且执行的值是null，默认使用参数对象名称，值是空字符串
	 */
	@AliasFor("name")
	String value() default "";

	/**
	 * 是否启用 URL 编码
	 */
	boolean encoded() default true;
}
