package org.bedrock.common.http.interceptor.impl;

import org.bedrock.common.code.reflects.MethodMeta;
import org.bedrock.common.code.util.AnnotationUtil;
import org.bedrock.common.http.HttpRequest;
import org.bedrock.common.http.HttpResponse;
import org.bedrock.common.http.annotation.LogConf;
import org.bedrock.common.http.interceptor.Interceptor;
import org.bedrock.common.http.support.HttpSupport;
import org.bedrock.common.http.support.InterceptorChain;
import org.bedrock.common.spi.autoservice.AutoService;
import org.springframework.core.Ordered;

/**
 * 处理以下注解
 *
 * @see LogConf
 */
@AutoService(Interceptor.class)
public class LogConfInterceptor implements Interceptor {

	@Override
	public HttpResponse interceptor(HttpRequest request, MethodMeta methodMeta, InterceptorChain chain) {
		LogConf annotation = methodMeta.getAnnotation(LogConf.class);
		if (annotation != null) {
			request.setLog(HttpSupport.buildLog(annotation));
		}
		return chain.doInterceptor(request, methodMeta);
	}

	/**
	 * 日志打印放到后面
	 */
	@Override
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE + 100001;
	}
}
