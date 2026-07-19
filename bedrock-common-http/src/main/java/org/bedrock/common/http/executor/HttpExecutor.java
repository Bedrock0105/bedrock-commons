package org.bedrock.common.http.executor;

import org.bedrock.common.http.HttpRequest;
import org.bedrock.common.http.HttpResponse;

/**
 * http 执行器
 */
@FunctionalInterface
public interface HttpExecutor {

	/**
	 * 执行
	 *
	 * @param request 请求
	 * @return 响应
	 */
	HttpResponse execute(HttpRequest request);
}
