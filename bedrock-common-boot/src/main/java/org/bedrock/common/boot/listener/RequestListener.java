package org.bedrock.common.boot.listener;

import jakarta.servlet.ServletRequestEvent;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.bedrock.common.boot.constant.BedrockBootConstant;
import org.bedrock.common.boot.props.RequestContentProperties;
import org.bedrock.common.code.util.StringUtil;
import org.bedrock.common.code.util.ThreadLocalUtil;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;

@RequiredArgsConstructor
public class RequestListener implements jakarta.servlet.ServletRequestListener {

	private final RequestContentProperties properties;

	@Override
	public void requestInitialized(ServletRequestEvent sre) {
		HttpServletRequest request = (HttpServletRequest) sre.getServletRequest();
		String requestId = request.getRequestId();
		if (StringUtil.hasText(requestId)) {
			MDC.put(BedrockBootConstant.CONTENT_LOCAL_KEY_REQUEST_ID, requestId);
		}
		String tenantId = request.getHeader(properties.getTenantId());
		if (StringUtil.hasText(tenantId)) {
			MDC.put(BedrockBootConstant.CONTENT_LOCAL_KEY_TENANT_ID, tenantId);
		}
		String accountId = request.getHeader(properties.getAccountId());
		if (StringUtil.hasText(accountId)) {
			MDC.put(BedrockBootConstant.CONTENT_LOCAL_KEY_ACCOUNT_ID, accountId);
		}
		ThreadLocalUtil.set(BedrockBootConstant.CONTENT_LOCAL_KEY, getHeaders(request));
	}

	public HttpHeaders getHeaders(HttpServletRequest request) {
		HttpHeaders headers = new HttpHeaders();
		properties.getHeaders()
			.forEach(header -> {
				String requestHeader = request.getHeader(header);
				if (StringUtil.hasText(requestHeader)) {
					headers.add(header, requestHeader);
				}
			});
		return headers;
	}

	@Override
	public void requestDestroyed(ServletRequestEvent sre) {
		MDC.clear();
		ThreadLocalUtil.clear();
	}
}
