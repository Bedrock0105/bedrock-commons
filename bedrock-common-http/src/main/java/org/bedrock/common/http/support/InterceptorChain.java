package org.bedrock.common.http.support;

import org.bedrock.common.http.HttpRequest;
import org.bedrock.common.http.HttpResponse;
import org.bedrock.common.code.reflects.MethodMeta;

@FunctionalInterface
public interface InterceptorChain {

	/**
	 * 拦截器链
	 */
	HttpResponse doInterceptor(HttpRequest request, MethodMeta methodMeta);

}
