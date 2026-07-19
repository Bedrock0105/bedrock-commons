package org.bedrock.common.http.interceptor;

import java.util.Set;

public interface InterceptorFactory {

	/**
	 * 获取拦截器
	 */
	Interceptor getInterceptor(Class<? extends Interceptor> clazz);

	/**
	 * 获取默认的拦截器列表
	 */
	Set<Interceptor> getDefaultInterceptors();

	/**
	 * 添加默认的拦截器
	 */
	void addDefaultInterceptor(Interceptor interceptor);
}
