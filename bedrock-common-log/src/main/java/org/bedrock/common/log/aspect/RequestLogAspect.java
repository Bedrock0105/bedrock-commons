package org.bedrock.common.log.aspect;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.bedrock.common.code.util.*;
import org.bedrock.common.log.annotation.RequestLogConfig;
import org.bedrock.common.log.props.LogProperties;
import org.bedrock.common.log.util.LogUtil;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 接口请求日志和接口返回日志
 */
@Slf4j
@Aspect
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLogAspect {

	private final LogProperties logProperties;

	private final RequestLogConfig defaultRequestLogConfig;

	private final Map<Method, RequestLogConfig> annotationCache = new ConcurrentHashMap<>();

	@Around("""
		((@within(org.springframework.web.bind.annotation.RestController)
		|| @within(org.springframework.stereotype.Controller))
		&& execution(!static org.bedrock.common.code.api.R *(..)))
		&& (!@annotation(org.bedrock.common.log.annotation.ExcludeRequestLog)
		||@within(org.bedrock.common.log.annotation.ExcludeRequestLog))
		""")
	public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
		if (!logProperties.isEnabled()) {
			return joinPoint.proceed();
		}
		HttpServletRequest request = WebUtil.getRequest();
		if (request == null) {
			/**
			 * 非web环境直接执行方法
			 */
			return joinPoint.proceed();
		}
		if (isSkipPath(request.getRequestURI())) {
			return joinPoint.proceed();
		}
		RequestLogConfig logAnnotation = getRequestLogAnnotation(joinPoint);
		if (logAnnotation.level() == LogProperties.LogLevel.OFF) {
			return joinPoint.proceed();
		}

		boolean isFileUpload = isFileUploadRequest(request);
		String requestId = request.getRequestId();
		/**
		 * 打印请求信息
		 */
		requestLog(request, joinPoint, logAnnotation, requestId, isFileUpload);

		/**
		 * 不打印返回结果，
		 * 直接返回
		 */
		if (!logAnnotation.methodReturnLog()) {
			return joinPoint.proceed();
		}
		// 5. 执行目标方法
		long startTime = System.currentTimeMillis();
		Object result = null;
		boolean success = true;
		String errorMessage = null;
		try {
			// 执行目标方法
			result = joinPoint.proceed();
			return result;
		} catch (Throwable ex) {
			success = false;
			errorMessage = ex.getMessage();
			throw ex;

		} finally {
			long executionTime = System.currentTimeMillis() - startTime;
			// 记录响应日志
			logResponseInfo(joinPoint, request, requestId, result, logAnnotation, executionTime, success, errorMessage);
		}
	}

	/**
	 * 获取接口日志配置
	 */
	private RequestLogConfig getRequestLogAnnotation(ProceedingJoinPoint joinPoint) {
		MethodSignature signature = (MethodSignature) joinPoint.getSignature();
		Method method = signature.getMethod();

		RequestLogConfig requestLogConfig = annotationCache.get(method);
		if (requestLogConfig != null) {
			return requestLogConfig;
		}
		requestLogConfig = AnnotationUtil.findAnnotation(method, RequestLogConfig.class);
		if (requestLogConfig == null) {
			requestLogConfig = defaultRequestLogConfig;
		}
		annotationCache.put(method, requestLogConfig);
		return requestLogConfig;
	}

	public boolean isSkipPath(String path) {
		for (String excludePath : logProperties.getExclude()) {
			if (AntPathUtil.match(excludePath, path)) {
				return true;
			}
		}
		return false;
	}

	private void requestLog(HttpServletRequest request, ProceedingJoinPoint joinPoint, RequestLogConfig logAnnotation, String requestId, boolean isFileUpload) {
		StringBuilder sb = new StringBuilder();
		sb.append("\n🎯 ===================== 请求日志 =====================");
		sb.append("\n📦 请求ID: ").append(requestId);
		sb.append("\n📍 请求信息:");
		sb.append("\n   • 方法: ").append(request.getMethod());
		sb.append("\n   • URL: ").append(getFullURL(request));
		sb.append("\n   • URI: ").append(request.getRequestURI());
		sb.append("\n   • 客户端IP: ").append(WebUtil.getIP(request));
		sb.append("\n   • 内容类型: ").append(request.getContentType());
		sb.append("\n   • 用户代理: ").append(abbreviate(request.getHeader("user-agent")));
		// 如果是文件上传请求，添加特殊标识
		if (isFileUpload) {
			sb.append("\n   • 请求类型: 📁 文件上传");
		}
		if (logAnnotation.handlersLog()) {
			// 请求头
			sb.append("\n📋 请求头:");
			Map<String, String> headers = getSafeHeaders(request, logAnnotation);
			if (headers.isEmpty()) {
				sb.append("\n   (无)");
			} else {
				headers.forEach((k, v) -> sb.append("\n   • ").append(k).append(": ").append(v));
			}
		}

		if (logAnnotation.methodParamsLog()) {
			// 方法参数
			sb.append("\n🔍 请求参数:");
			Object[] args = joinPoint.getArgs();
			List<LogUtil.FileInfo> fileInfos = new ArrayList<>();
			Map<String, Object> params = new HashMap<>();
			if (args.length == 0) {
				sb.append("\n   (无)");
			} else {
				MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
				String[] parameterNames = methodSignature.getParameterNames();
				LogUtil.logParameters(args, parameterNames, params, fileInfos);
			}
			// 记录文件信息
			if (!fileInfos.isEmpty()) {
				sb.append("\n   📁 上传文件:");
				for (LogUtil.FileInfo fileInfo : fileInfos) {
					sb.append("\n      • 文件名: ").append(fileInfo.originalFilename())
						.append(" | 大小: ").append(formatFileSize(fileInfo.size()))
						.append(" | 类型: ").append(fileInfo.contentType())
						.append(fileInfo.empty() ? " | ⚠️ 空文件" : "");
				}
			}
			// 记录其他参数
			if (!params.isEmpty()) {
				sb.append("\n   📝 方法参数:").append(abbreviate(JsonUtil.toJson(params)));
			}
		}
		sb.append("\n⏰ 请求时间: ").append(LocalDateTime.now().format(DateUtil.DATETIME_FORMATTER));
		sb.append("\n🎯 ===================== 请求日志 =====================");
		logAnnotation.level().log(log, "REQUEST - {}", sb);
	}

	/**
	 * 记录响应信息
	 */
	private void logResponseInfo(ProceedingJoinPoint joinPoint, HttpServletRequest request,
								 String requestId, Object result, RequestLogConfig logAnnotation,
								 long executionTime, boolean success, String errorMessage) {

		StringBuilder sb = new StringBuilder();
		sb.append("\n✅ ===================== 响应日志 =====================");
		sb.append("\n📦 请求ID: ").append(requestId);
		sb.append("\n📍 请求信息:");
		sb.append("\n   • 方法: ").append(request.getMethod());
		sb.append("\n   • URI: ").append(request.getRequestURI());

		// 执行结果
		sb.append("\n📊 执行结果:");
		sb.append("\n   • 状态: ").append(success ? "✅ 成功" : "❌ 失败");
		sb.append("\n   • 耗时: ").append(executionTime).append("ms");

		if (!success) {
			sb.append("\n   • 错误: ").append(errorMessage);
		} else if (logAnnotation.methodReturnMsgLog()) {
			// 响应数据
			sb.append("\n📨 响应数据:");
			String responseData = JsonUtil.canConvertToJson(result) ? abbreviate(JsonUtil.toJson(result)) : "此对象不可以序列化";
			sb.append("\n   ").append(responseData);
		}

		// 方法信息
		sb.append("\n🎯 方法信息:");
		sb.append("\n   • 类: ").append(joinPoint.getTarget().getClass().getSimpleName());
		sb.append("\n   • 方法: ").append(joinPoint.getSignature().getName());

		sb.append("\n⏰ 响应时间: ").append(LocalDateTime.now().format(DateUtil.DATETIME_FORMATTER));
		sb.append("\n✅ ===================== 响应日志 =====================");

		logAnnotation.level().log(log, "RESPONSE - {}", sb.toString());
	}

	/**
	 * 获取完整的URL
	 */
	private static String getFullURL(HttpServletRequest request) {
		StringBuffer requestURL = request.getRequestURL();
		String queryString = request.getQueryString();
		if (queryString == null) {
			return requestURL.toString();
		} else {
			return requestURL.append('?').append(queryString).toString();
		}
	}

	/**
	 * 获取安全的请求头（过滤敏感信息）
	 */
	private Map<String, String> getSafeHeaders(HttpServletRequest request, RequestLogConfig logAnnotation) {
		Map<String, String> headers = new LinkedHashMap<>();
		Enumeration<String> headerNames = request.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			String headerName = headerNames.nextElement();
			// 跳过敏感头信息
			boolean isExcluded = false;
			for (String excluded : logAnnotation.excludedHandlers()) {
				if (excluded.equals(headerName)) {
					isExcluded = true;
					break;
				}
			}
			if (isExcluded) {
				headers.put(headerName, "***MASKED***");
				continue;
			}
			// 获取头值
			String headerValue = request.getHeader(headerName);
			if (headerValue != null && !headerValue.trim().isEmpty()) {
				headers.put(headerName, headerValue);
			}
		}

		return headers;
	}

	/**
	 * 缩写文本，避免日志过长
	 */
	private String abbreviate(String text) {
		if (text == null || text.length() <= logProperties.getMaxLength()) {
			return text;
		}
		return text.substring(0, logProperties.getMaxLength()) + "...[长度:" + text.length() + "]";
	}

	/**
	 * 检查是否为文件上传请求
	 */
	private boolean isFileUploadRequest(HttpServletRequest request) {
		String contentType = request.getContentType();
		return contentType != null && contentType.toLowerCase().contains("multipart/form-data");
	}

	/**
	 * 格式化文件大小
	 */
	private String formatFileSize(long size) {
		if (size < 1024) {
			return size + " B";
		} else if (size < 1024 * 1024) {
			return String.format("%.1f KB", size / 1024.0);
		} else if (size < 1024 * 1024 * 1024) {
			return String.format("%.1f MB", size / (1024.0 * 1024));
		} else {
			return String.format("%.1f GB", size / (1024.0 * 1024 * 1024));
		}
	}

	public RequestLogAspect(LogProperties logProperties) {
		this.logProperties = logProperties;
		this.defaultRequestLogConfig = new RequestLogConfig() {
			@Override
			public Class<? extends Annotation> annotationType() {
				return RequestLogConfig.class;
			}

			@Override
			public LogProperties.LogLevel level() {
				return logProperties.getLevel();
			}

			@Override
			public int maxLength() {
				return logProperties.getMaxLength();
			}

			@Override
			public String[] excludedHandlers() {
				return logProperties.getExcludedHandlers();
			}

			@Override
			public boolean handlersLog() {
				return logProperties.isHandlersLog();
			}

			@Override
			public boolean methodParamsLog() {
				return logProperties.isMethodParamsLog();
			}

			@Override
			public boolean methodReturnLog() {
				return logProperties.isMethodReturnLog();
			}

			@Override
			public boolean methodReturnMsgLog() {
				return logProperties.isMethodReturnMsgLog();
			}
		};
	}
}
