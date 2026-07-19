package org.bedrock.common.security.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.bedrock.common.auth.util.AuthUtil;
import org.bedrock.common.security.enums.ErrorEnums;
import org.bedrock.common.security.exception.NotLoginException;
import org.springframework.web.servlet.HandlerInterceptor;

public class SecurityInterceptor implements HandlerInterceptor {

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		if (AuthUtil.getAuthUser() == null) {
			throw new NotLoginException(ErrorEnums.USER_NOT_LOGIN.getCode(), ErrorEnums.USER_NOT_LOGIN.getMessage());
		}
		return true;
	}
}
