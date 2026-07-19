package org.bedrock.common.http.annotation;

import java.lang.annotation.*;
import java.net.Proxy;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface HttpProxy {

	/**
	 * 代理类型 (HTTP/SOCKS等)
	 */
	Proxy.Type type() default Proxy.Type.HTTP;

	/**
	 * 代理服务器地址
	 */
	String host();

	/**
	 * 代理服务器端口
	 */
	int port();

	/**
	 * 代理认证用户名
	 */
	String username() default "";

	/**
	 * 代理认证密码
	 */
	String password() default "";

	/**
	 * 全局请求头（所有接口请求均会携带）
	 *
	 * @return 请求头配置集合
	 */
	Headers headers() default @Headers({});
}
