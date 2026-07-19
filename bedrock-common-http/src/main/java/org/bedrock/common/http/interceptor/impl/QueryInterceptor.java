package org.bedrock.common.http.interceptor.impl;

import org.bedrock.common.code.reflects.MethodMeta;
import org.bedrock.common.code.util.ObjectUtil;
import org.bedrock.common.code.util.StringUtil;
import org.bedrock.common.http.HttpRequest;
import org.bedrock.common.http.HttpResponse;
import org.bedrock.common.http.annotation.Query;
import org.bedrock.common.http.interceptor.Interceptor;
import org.bedrock.common.http.support.InterceptorChain;
import org.bedrock.common.spi.autoservice.AutoService;
import org.springframework.core.MethodParameter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;

/**
 * 处理以下注解
 *
 * @see Query
 */
@AutoService(Interceptor.class)
public class QueryInterceptor implements Interceptor {

	@Override
	public HttpResponse interceptor(HttpRequest request, MethodMeta methodMeta, InterceptorChain chain) {
		for (MethodParameter parameter : methodMeta.getParameters()) {
			Query query = parameter.getParameterAnnotation(Query.class);
			if (query == null) {
				continue;
			}
			Object paramValue = request.args(parameter.getParameterIndex());
			if (paramValue == null) {
				request.addQuery(StringUtil.isBlank(query.name()) ? parameter.getParameterName() : query.name(), "");
				continue;
			}
			String name = getName(query);
			if (query.encoded()) {
				request.addQuery(name, paramValue);
				continue;
			}
			request.addQueryEncoded(name, paramValue);
		}
		return chain.doInterceptor(request, methodMeta);
	}

	public String getName(Query query) {
		return StringUtil.isNotBlank(query.name()) ? query.name() : query.value();
	}

	@Override
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE + 10000;
	}
}
