package org.bedrock.common.http.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Headers {

	/**
	 * 请求头
	 */
	Header[] value();
}
