package org.bedrock.common.http.interceptor;

import org.bedrock.common.http.HttpRequest;
import org.bedrock.common.http.HttpResponse;
import org.bedrock.common.code.reflects.MethodMeta;
import org.bedrock.common.http.support.InterceptorChain;
import org.springframework.core.Ordered;

/**
 * 全局拦截器
 */
public interface Interceptor extends Ordered {

	/**
	 * 拦截器处理
	 *
	 * @param request    请求对象
	 * @param methodMeta 方法参数
	 * @param chain      拦截器链
	 */
	HttpResponse interceptor(HttpRequest request, MethodMeta methodMeta, InterceptorChain chain);

	/**
	 * 拦截器执行顺序
	 */
	@Override
	default int getOrder() {
		return Ordered.LOWEST_PRECEDENCE - 1000000;
	}
}
