package org.bedrock.common.http.annotation;

import org.bedrock.common.http.interceptor.impl.BodyInterceptor;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;


/**
 * 请求体/表单参数绑定注解
 * 1.  标注参数将根据请求头Content-Type，放入Form（普通表单）、Body（请求体）或FormData（多部分表单）
 * 2.  未指定Content-Type时，参数默认按普通表单格式处理
 * 3.  需在{@link BodyInterceptor}执行前设置Content-Type
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Body {

	/**
	 * 参数名称（与value互为别名）
	 * 生效规则：
	 * 1.  未指定（默认空串）：按参数对象整体结构化解析，支持Form、FormData、Body场景
	 * 2.  Content-Type为application/json/xml：仅最后一个未指定name的参数生效；指定name则按键值对解析为Map
	 * 3.  Content-Type为form/formdata：指定name按键值对解析；未指定name则拆解对象属性为多个键值对
	 */
	@AliasFor("value")
	String name() default "";

	/**
	 * 参数名称（与name互为别名）
	 */
	@AliasFor("name")
	String value() default "";

	/**
	 * 文件名（仅formdata场景生效）
	 * 支持{name}参数替换，默认空串（使用默认文件名）
	 */
	String fileName() default "";

	/**
	 * 是否启用URL编码（仅普通表单场景生效）
	 * 默认true（符合HTTP普通表单参数传输规范）
	 */
	boolean encoded() default true;

	/**
	 * 单个参数专属Content-Type（仅多部分表单场景生效）
	 * 默认空串（使用多部分表单默认Content-Type，通常为text/plain）
	 */
	String contentType() default "";
}
