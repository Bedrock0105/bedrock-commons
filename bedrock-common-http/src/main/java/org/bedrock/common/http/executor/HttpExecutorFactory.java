package org.bedrock.common.http.executor;

import org.bedrock.common.http.HttpConfiguration;
import org.bedrock.common.http.HttpRequest;

public interface HttpExecutorFactory {

	/**
	 * 工厂名称
	 */
	String getName();

	/**
	 * 获取HttpExecutor实例
	 */
	HttpExecutor getInstance(HttpRequest request, HttpConfiguration config);
}
