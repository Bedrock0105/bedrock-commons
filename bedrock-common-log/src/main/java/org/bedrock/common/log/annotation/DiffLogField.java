package org.bedrock.common.log.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DiffLogField {

	/**
	 * 字段名称
	 *
	 * @return
	 */
	String name();

	/**
	 * 字段解析方法
	 *
	 * @return
	 */
	String function() default "";

	/**
	 * 字典值
	 * 实例  {"1":"开启","0":"禁用"}
	 *
	 * @return
	 */
	String dict() default "";
}
