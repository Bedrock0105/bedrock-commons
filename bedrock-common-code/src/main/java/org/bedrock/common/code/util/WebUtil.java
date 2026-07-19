package org.bedrock.common.code.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.bedrock.common.code.constant.StringPool;
import org.springframework.lang.Nullable;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Objects;

public abstract class WebUtil extends org.springframework.web.util.WebUtils {

	/**
	 * 获取客户端IP
	 *
	 * @param request 请求对象
	 * @return IP地址
	 */
	private static final String[] HEADERS_TO_TRY = {
		"X-Forwarded-For",
		"Proxy-Client-IP",
		"WL-Proxy-Client-IP",
		"HTTP_X_FORWARDED_FOR",
		"HTTP_X_FORWARDED",
		"HTTP_X_CLUSTER_CLIENT_IP",
		"HTTP_CLIENT_IP",
		"HTTP_FORWARDED_FOR",
		"HTTP_FORWARDED",
		"HTTP_VIA",
		"REMOTE_ADDR",
		"X-Forwarded-For",
		"X-Real-IP",
	};


	/**
	 * 获取请求的方式
	 *
	 */
	public static String getRequestMethod() {
		return Objects.requireNonNull(getRequest()).getMethod();
	}


	/**
	 * 获取请求的URI
	 *
	 */
	public static String getRequestUri() {
		return Objects.requireNonNull(getRequest()).getRequestURI();
	}

	/**
	 * 获取请求地址
	 *
	 */
	public static String getRequestUrl() {
		return Objects.requireNonNull(getRequest()).getRequestURL().toString();
	}

	/**
	 * 获取当前请求对象
	 */
	@Nullable
	public static HttpServletRequest getRequest() {
		ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		if (servletRequestAttributes == null) {
			return null;
		}
		return servletRequestAttributes.getRequest();
	}

	/**
	 * 获取当前响应对象
	 */
	@Nullable
	public static HttpServletResponse getResponse() {
		ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		if (servletRequestAttributes == null) {
			return null;
		}
		return servletRequestAttributes.getResponse();
	}

	/**
	 * 获取请求头
	 *
	 * @param name
	 * @return
	 */
	public static String getHeader(String name) {
		return Objects.requireNonNull(getRequest()).getHeader(name);
	}

	/**
	 * 获取IP
	 */
	public static String getIP() {
		return getIP(getRequest());
	}

	/**
	 * 获取IP
	 */
	public static String getIP(HttpServletRequest request) {
		if (request == null) {
			return StringPool.EMPTY;
		}
		for (String header : HEADERS_TO_TRY) {
			String ip = request.getHeader(header);
			if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
				return getClientIpFromMultiIp(ip);
			}
		}

		return request.getRemoteAddr();
	}

	private static String getClientIpFromMultiIp(String ip) {
		// 多个IP时取第一个IP
		if (ip != null && ip.contains(",")) {
			String[] ips = ip.split(",");
			for (String subIp : ips) {
				if (!"unknown".equalsIgnoreCase(subIp.trim())) {
					return subIp.trim();
				}
			}
		}
		return ip;
	}

}
