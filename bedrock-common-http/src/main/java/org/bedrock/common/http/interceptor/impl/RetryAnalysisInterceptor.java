package org.bedrock.common.http.interceptor.impl;

import org.bedrock.common.code.reflects.MethodMeta;
import org.bedrock.common.code.util.AnnotationUtil;
import org.bedrock.common.code.util.BeanUtil;
import org.bedrock.common.http.HttpRequest;
import org.bedrock.common.http.HttpResponse;
import org.bedrock.common.http.annotation.Retry;
import org.bedrock.common.http.interceptor.Interceptor;
import org.bedrock.common.http.model.RetryMeta;
import org.bedrock.common.http.support.InterceptorChain;
import org.bedrock.common.spi.autoservice.AutoService;
import org.springframework.core.Ordered;

@AutoService(Interceptor.class)
public class RetryAnalysisInterceptor implements Interceptor {

	@Override
	public HttpResponse interceptor(HttpRequest request, MethodMeta methodMeta, InterceptorChain chain) {
		Retry annotation = AnnotationUtil.findAnnotation(methodMeta.getAnnotatedElement(), Retry.class);
		if (annotation == null) {
			return chain.doInterceptor(request, methodMeta);
		}
		RetryMeta retryMeta = new RetryMeta();
		retryMeta.setMaxRetryCount(annotation.maxRetryCount());
		retryMeta.setRetrySleepMillis(annotation.retrySleepMillis());
		if (annotation.retryPredicate() != RetryMeta.DefaultRetryPredicate.class) {
			retryMeta.setRetryPredicate(BeanUtil.instantiateClass(annotation.retryPredicate()));
		}
		request.setRetryMeta(retryMeta);
		return chain.doInterceptor(request, methodMeta);
	}

	@Override
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE + 100001;
	}
}
