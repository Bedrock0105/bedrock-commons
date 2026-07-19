package org.bedrock.common.cloud.feign;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.bedrock.common.boot.constant.BedrockBootConstant;
import org.bedrock.common.code.util.ThreadLocalUtil;
import org.springframework.http.HttpHeaders;


public class DefaultFeignRequestInterceptor implements RequestInterceptor {

	@Override
	public void apply(RequestTemplate requestTemplate) {
		HttpHeaders headers = ThreadLocalUtil.get(BedrockBootConstant.CONTENT_LOCAL_KEY);
		if (headers != null && !headers.isEmpty()) {
			headers.forEach((key, values) ->
				values.forEach(value -> requestTemplate.header(key, value))
			);
		}
	}

}
