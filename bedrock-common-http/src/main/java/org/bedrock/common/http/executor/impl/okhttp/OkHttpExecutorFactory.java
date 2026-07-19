package org.bedrock.common.http.executor.impl.okhttp;

import org.bedrock.common.http.HttpConfiguration;
import org.bedrock.common.http.HttpRequest;
import org.bedrock.common.http.enums.HttpExecutorEnum;
import org.bedrock.common.http.executor.HttpExecutor;
import org.bedrock.common.http.executor.HttpExecutorFactory;
import org.bedrock.common.spi.autoservice.AutoService;

@AutoService(HttpExecutorFactory.class)
public class OkHttpExecutorFactory implements HttpExecutorFactory {

	private final OkHttpExecutor executor;

	public OkHttpExecutorFactory() {
		this.executor = new OkHttpExecutor();
	}

	@Override
	public String getName() {
		return HttpExecutorEnum.OKHTTP.getName();
	}

	@Override
	public HttpExecutor getInstance(HttpRequest request, HttpConfiguration config) {
		return executor;
	}
}
