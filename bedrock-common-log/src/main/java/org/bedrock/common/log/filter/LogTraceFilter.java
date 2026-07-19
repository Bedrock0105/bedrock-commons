package org.bedrock.common.log.filter;

import jakarta.servlet.*;
import org.bedrock.common.log.handler.LogTraceIdHandler;

import java.io.IOException;

public class LogTraceFilter implements Filter {

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		boolean flag = LogTraceIdHandler.insertTraceId();
		try {
			chain.doFilter(request, response);
		} finally {
			if (flag) {
				LogTraceIdHandler.removeTraceId();
			}
		}
	}
}
