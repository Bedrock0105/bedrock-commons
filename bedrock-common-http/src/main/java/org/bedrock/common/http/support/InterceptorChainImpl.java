package org.bedrock.common.http.support;

import lombok.Getter;
import lombok.Setter;
import org.bedrock.common.http.HttpRequest;
import org.bedrock.common.http.HttpResponse;
import org.bedrock.common.http.interceptor.Interceptor;
import org.bedrock.common.code.reflects.MethodMeta;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Getter
public class InterceptorChainImpl implements InterceptorChain {

	private final List<Interceptor> interceptors;

	@Setter
	private int location = -1;

	@Override
	public HttpResponse doInterceptor(HttpRequest request, MethodMeta methodMeta) {
		location++;
		if (location >= interceptors.size()) {
			return request.executeResponse();
		}
		return interceptors.get(location).interceptor(request, methodMeta, this);
	}

	public InterceptorChainImpl() {
		this.interceptors = List.of();
	}

	public InterceptorChainImpl(Set<Interceptor> interceptors) {
		this.interceptors = new ArrayList<>(interceptors);
		this.interceptors.sort(Comparator.comparing(Interceptor::getOrder));
	}
}
