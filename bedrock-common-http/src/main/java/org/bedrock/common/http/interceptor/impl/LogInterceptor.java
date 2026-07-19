package org.bedrock.common.http.interceptor.impl;

import lombok.extern.slf4j.Slf4j;
import org.bedrock.common.code.reflects.MethodMeta;
import org.bedrock.common.http.HttpRequest;
import org.bedrock.common.http.HttpResponse;
import org.bedrock.common.http.enums.MediaTypeCategory;
import org.bedrock.common.http.interceptor.Interceptor;
import org.bedrock.common.http.model.ContentType;
import org.bedrock.common.http.model.HeadersMeta;
import org.bedrock.common.http.props.HttpClientProperties;
import org.bedrock.common.http.support.InterceptorChain;
import org.bedrock.common.spi.autoservice.AutoService;
import org.springframework.core.Ordered;

import java.util.Locale;

/**
 * HTTP请求响应日志打印拦截器
 * 功能：按需打印请求/响应日志，支持开关配置、请求头/体、响应头/体、耗时打印，支持日志分级控制
 * 执行优先级：Ordered.LOWEST_PRECEDENCE-20 后置执行，保证日志能打印完整请求响应信息
 */
@Slf4j
@AutoService(Interceptor.class)
public class LogInterceptor implements Interceptor {

	/**
	 * 核心拦截方法：执行请求前后打印日志，按需开关控制
	 */
	@Override
	public HttpResponse interceptor(HttpRequest request, MethodMeta methodMeta, InterceptorChain chain) {
		// 获取日志配置：优先取当前请求的日志配置，无则取全局配置
		HttpClientProperties.Log logConf = getLogConfig(request);
		// 日志总开关关闭，直接执行链路不打印日志
		if (logConf == null || !logConf.isEnabled()) {
			return chain.doInterceptor(request, methodMeta);
		}

		// 打印请求日志
		printRequestLog(request, logConf);
		// 执行请求链路，获取响应结果
		HttpResponse response = chain.doInterceptor(request, methodMeta);
		// 打印响应日志
		printResponseLog(request, response, logConf);

		return response;
	}

	/**
	 * 统一获取日志配置：请求级配置 > 全局配置
	 *
	 * @param request 请求对象
	 * @return 生效的日志配置
	 */
	private HttpClientProperties.Log getLogConfig(HttpRequest request) {
		HttpClientProperties.Log logConf = request.getLog();
		if (logConf == null) {
			logConf = request.getConfig().getLog();
		}
		return logConf;
	}

	/**
	 * 按需打印请求日志，完全复用原有打印逻辑，无修改
	 */
	private void printRequestLog(HttpRequest request, HttpClientProperties.Log logConf) {
		if (!logConf.isRequestLog()) {
			return;
		}
		StringBuilder sb = new StringBuilder();
		// 拼接请求首行：--> 请求方法 URL 协议版本
		sb.append("--> ")
			.append(request.method())
			.append(" ")
			.append(request.url())
			.append(" ")
			.append(request.getProtocolVersion());

		// 拼接请求头信息
		if (logConf.isRequestHeaderLog()) {
			appendHeaders(sb, request.getHeaderMetas());
		}

		// 拼接请求体参数信息
		if (logConf.isRequestBodyLog()) {
			sb.append(System.lineSeparator())
				.append(System.lineSeparator())
				.append(request.getParamMeta());
		}

		// 拼接请求结束标识
		if (logConf.isRequestHeaderLog() || logConf.isRequestBodyLog()) {
			sb.append(System.lineSeparator())
				.append("--> ")
				.append("END ")
				.append(request.method());
		}
		// 打印请求日志
		log.info("{}{}", System.lineSeparator(), sb);
	}

	/**
	 * 按需打印响应日志，完全复用原有打印逻辑，无修改，仅优化文案+修复隐患
	 */
	private void printResponseLog(HttpRequest request, HttpResponse response, HttpClientProperties.Log logConf) {
		if (!logConf.isResponseLog()) {
			return;
		}
		StringBuilder sb = new StringBuilder();
		// 拼接响应首行：<-- 响应状态码 (耗时ms)
		sb.append("<-- ")
			.append(response.status())
			.append(" ")
			.append(response.isSuccess() ? "OK" : "ERROR")
			.append(" (")
			.append(response.betweenMillis())
			.append("ms) ");

		// 拼接响应头信息
		if (logConf.isResponseHeaderLog()) {
			appendHeaders(sb, response.getHeadersMeta());
		}

		// 拼接响应体信息
		if (logConf.isResponseBodyLog()) {
			appendResponseBody(sb, response);
		}

		// 拼接响应结束标识
		if (logConf.isResponseHeaderLog() || logConf.isResponseBodyLog()) {
			sb.append(System.lineSeparator())
				.append("<-- ")
				.append("END ")
				.append(request.scheme().toUpperCase(Locale.ROOT));
		}
		// 打印响应日志
		log.info("{}{}", System.lineSeparator(), sb);
	}

	/**
	 * 公共方法：拼接请求头/响应头信息，消除重复代码
	 *
	 * @param sb          字符串拼接器
	 * @param headerMetas 头信息元数据
	 */
	private void appendHeaders(StringBuilder sb, HeadersMeta headerMetas) {
		// 空指针兜底：头信息为空时直接返回，避免NPE
		if (headerMetas == null || headerMetas.getHeaderMetas() == null) {
			return;
		}
		headerMetas.getHeaderMetas().forEach(header ->
			sb.append(System.lineSeparator())
				.append("\t")
				.append(header.getName())
				.append(": ")
				.append(header.getValue())
		);
	}

	/**
	 * 公共方法：拼接响应体信息，优化指定文案，增加异常兜底，不修改原有逻辑
	 */
	private void appendResponseBody(StringBuilder sb, HttpResponse response) {
		ContentType contentType = response.getContentType();
		MediaTypeCategory mediaTypeCategory = contentType.getMediaTypeCategory();
		sb.append(System.lineSeparator())
			.append(System.lineSeparator())
			.append("\t");
		try {
			String result = response.result(String.class);
			// 非二进制/未知类型，打印响应体字符串
			if (mediaTypeCategory != MediaTypeCategory.BINARY_DATA && mediaTypeCategory != MediaTypeCategory.UNKNOWN) {
				sb.append(result);
			} else {
				// ✅ 优化你指定的打印文案：美观规范，语义不变
				sb.append("[二进制响应体 | 字节长度: ").append(response.bytes().length).append(" bytes]");
			}
		} catch (Exception e) {
			// 新增异常兜底：防止解析响应体失败导致日志打印异常，不影响主流程
			sb.append("[响应体解析失败：").append(e.getMessage()).append("]");
		}
	}

	/**
	 * 日志拦截器执行优先级：后置执行，保证打印完整的请求响应信息
	 */
	@Override
	public int getOrder() {
		return Ordered.LOWEST_PRECEDENCE - 20;
	}
}
