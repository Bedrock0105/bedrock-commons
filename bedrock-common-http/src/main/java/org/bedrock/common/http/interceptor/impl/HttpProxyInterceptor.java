package org.bedrock.common.http.interceptor.impl;

import org.bedrock.common.code.reflects.MethodMeta;
import org.bedrock.common.http.HttpRequest;
import org.bedrock.common.http.HttpResponse;
import org.bedrock.common.http.annotation.Header;
import org.bedrock.common.http.annotation.HttpProxy;
import org.bedrock.common.http.interceptor.Interceptor;
import org.bedrock.common.http.model.HttpProxyMeta;
import org.bedrock.common.http.support.HttpSupport;
import org.bedrock.common.http.support.InterceptorChain;
import org.bedrock.common.spi.autoservice.AutoService;
import org.springframework.core.Ordered;

/**
 * HTTP代理配置拦截器
 * 核心功能：解析方法上的{HttpProxy}注解配置，自动为请求绑定代理信息、代理认证账号密码及请求头，优先级高优先执行
 */
@AutoService(Interceptor.class)
public class HttpProxyInterceptor implements Interceptor {

	/**
	 * 拦截处理：从方法元数据中解析代理注解，配置请求代理信息后执行链路
	 *
	 * @param request    请求对象
	 * @param methodMeta 方法元数据，包含注解配置信息
	 * @param chain      拦截器执行链
	 * @return 执行后的响应结果
	 */
	@Override
	public HttpResponse interceptor(HttpRequest request, MethodMeta methodMeta, InterceptorChain chain) {
		// 获取方法上的HttpProxy代理注解，无注解则直接放行请求
		HttpProxy annotation = methodMeta.getAnnotation(HttpProxy.class);
		if (annotation == null) {
			return chain.doInterceptor(request, methodMeta);
		}

		// 根据注解配置创建代理元数据对象
		HttpProxyMeta httpProxyMeta = HttpProxyMeta.create(annotation.type(), annotation.host(), annotation.port());
		// 设置代理认证的账号密码
		httpProxyMeta.setUsername(annotation.username());
		httpProxyMeta.setPassword(annotation.password());

		// 解析注解中配置的请求头，解析占位符后添加至请求
		for (Header header : annotation.headers().value()) {
			httpProxyMeta.addHeader(header.name(), HttpSupport.resolvePlaceholders(header.defaultValue(), request));
		}

		// 将代理配置绑定到当前请求
		request.proxy(httpProxyMeta);
		// 执行后续拦截器链路
		return chain.doInterceptor(request, methodMeta);
	}

	/**
	 * 拦截器执行优先级：高优先级执行
	 * 优先级说明：保证代理配置在请求执行前最先生效，所有后续拦截器/请求都复用该代理配置
	 */
	@Override
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE + 100001;
	}
}
