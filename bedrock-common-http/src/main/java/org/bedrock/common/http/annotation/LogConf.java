package org.bedrock.common.http.annotation;


import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LogConf {

	/**
	 * 日志总开关（优先级最高）：false时所有日志都不输出
	 */
	boolean enabled() default true;

	/**
	 * 请求日志大类开关：false时，以下requestHeaderLog/requestBodyLog均不生效
	 */
	boolean requestLog() default true;

	/**
	 * 请求头日志（依赖requestLog和enabled为true）
	 */
	boolean requestHeaderLog() default true;

	/**
	 * 请求体日志（依赖requestLog和enabled为true，生产环境建议关闭大报文场景）
	 */
	boolean requestBodyLog() default false;

	/**
	 * 响应日志大类开关：false时，以下responseHeaderLog/responseBodyLog均不生效
	 */
	boolean responseLog() default true;

	/**
	 * 响应头日志（依赖responseLog和enabled为true）
	 */
	boolean responseHeaderLog() default true;

	/**
	 * 响应体日志（依赖responseLog和enabled为true，生产环境建议关闭大报文场景）
	 */
	boolean responseBodyLog() default false;

}
