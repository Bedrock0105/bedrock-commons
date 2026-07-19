package org.bedrock.common.http.interceptor.impl;

import org.bedrock.common.code.reflects.MethodMeta;
import org.bedrock.common.code.util.ObjectUtil;
import org.bedrock.common.code.util.StringUtil;
import org.bedrock.common.http.HttpRequest;
import org.bedrock.common.http.HttpResponse;
import org.bedrock.common.http.annotation.Header;
import org.bedrock.common.http.annotation.Headers;
import org.bedrock.common.http.converter.ParameterConverter;
import org.bedrock.common.http.interceptor.Interceptor;
import org.bedrock.common.http.support.HttpSupport;
import org.bedrock.common.http.support.InterceptorChain;
import org.bedrock.common.spi.autoservice.AutoService;
import org.springframework.core.MethodParameter;
import org.springframework.core.Ordered;

import java.util.Map;
import java.util.Set;

/**
 * 处理以下注解
 *
 * @see Headers
 * @see Header
 */
@AutoService(Interceptor.class)
public class HeadersInterceptor implements Interceptor {

	@Override
	public HttpResponse interceptor(HttpRequest request, MethodMeta methodMeta, InterceptorChain chain) {
		Headers annotation = methodMeta.getAnnotation(Headers.class);
		if (annotation != null) {
			for (Header header : annotation.value()) {
				request.addHeader(header.name(), HttpSupport.resolvePlaceholders(header.defaultValue(), request));
			}
		}
		/**
		 * 解析方法参数上的@Header注解，将参数绑定为HTTP请求头
		 * 核心流程：
		 * 1.  遍历所有方法参数，筛选带有@Header注解的参数
		 * 2.  按「参数值为空」「注解名称为空（对象转Map）」「注解名称非空（单个请求头）」分层处理
		 * 3.  支持参数转换器自定义解析，兜底直接转为字符串
		 */
		for (MethodParameter parameter : methodMeta.getParameters()) {
			Header parameterAnnotation = parameter.getParameterAnnotation(Header.class);
			if (parameterAnnotation == null) {
				continue;
			}

			// 缓存注解中的请求头名称，避免多次重复调用
			String headerName = parameterAnnotation.name();
			int paramIndex = parameter.getParameterIndex();
			Object paramValue = request.args(paramIndex);

			// 1. 处理参数值为空的场景：使用注解默认值并解析占位符
			if (ObjectUtil.isEmpty(paramValue)) {
				String defaultValue = HttpSupport.resolvePlaceholders(parameterAnnotation.defaultValue(), request);
				request.addHeader(StringUtil.isBlank(headerName) ? parameter.getParameterName() : headerName, defaultValue);
				continue;
			}

			// 2. 处理注解名称为空的场景：将参数对象转为Map，批量添加请求头
			if (StringUtil.isBlank(headerName)) {
				handleObjectToHeaderMap(request, paramValue);
				continue;
			}

			// 3. 处理注解名称非空的场景：使用参数转换器或直接转字符串添加单个请求头
			handleSingleHeaderValue(request, parameter, headerName, paramValue);
		}

		return chain.doInterceptor(request, methodMeta);
	}

	/**
	 * 将参数对象转为Map，批量添加为HTTP请求头
	 *
	 * @param request    HTTP请求实例
	 * @param paramValue 待转换的参数对象
	 */
	private void handleObjectToHeaderMap(HttpRequest request, Object paramValue) {
		// 增加Map空值校验，避免空指针异常
		Map<String, Object> headerMap = request.getConverter().convertToMultiValueMap(paramValue, request);
		if (headerMap == null || headerMap.isEmpty()) {
			return;
		}
		// 遍历Map，批量添加请求头（值为空时转为空字符串）
		for (Map.Entry<String, Object> entry : headerMap.entrySet()) {
			String key = entry.getKey();
			String value = StringUtil.toStr(entry.getValue(), "");
			request.addHeader(key, value);
		}
	}

	/**
	 * 处理单个请求头的值（优先使用参数转换器，兜底直接转字符串）
	 *
	 * @param request    HTTP请求实例
	 * @param parameter  方法参数元数据
	 * @param headerName 请求头名称
	 * @param paramValue 参数值
	 */
	private void handleSingleHeaderValue(HttpRequest request, MethodParameter parameter, String headerName, Object paramValue) {
		// 增加参数转换器集合空值校验，避免空指针异常
		Set<ParameterConverter> parameterConverters = request.getParameterConverters();
		if (parameterConverters == null || parameterConverters.isEmpty()) {
			request.addHeader(headerName, StringUtil.toStr(paramValue, ""));
			return;
		}

		// 遍历参数转换器，使用支持当前参数的转换器解析值
		for (ParameterConverter converter : parameterConverters) {
			if (converter.supportsParameter(parameter, headerName, paramValue)) {
				String resolvedValue = converter.resolveArgument(parameter, headerName, paramValue);
				request.addHeader(headerName, resolvedValue);
				return;
			}
		}
	}

	@Override
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE + 1000;
	}
}
