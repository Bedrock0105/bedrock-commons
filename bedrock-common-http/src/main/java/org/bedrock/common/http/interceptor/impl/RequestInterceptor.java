package org.bedrock.common.http.interceptor.impl;

import org.bedrock.common.code.reflects.MethodMeta;
import org.bedrock.common.code.util.StringUtil;
import org.bedrock.common.code.util.UriUtil;
import org.bedrock.common.http.HttpRequest;
import org.bedrock.common.http.HttpResponse;
import org.bedrock.common.http.annotation.Header;
import org.bedrock.common.http.annotation.Request;
import org.bedrock.common.http.converter.Converter;
import org.bedrock.common.http.converter.ParameterConverter;
import org.bedrock.common.http.enums.MediaTypeCategory;
import org.bedrock.common.http.enums.ProtocolVersion;
import org.bedrock.common.http.interceptor.Interceptor;
import org.bedrock.common.http.ssl.SslContextProvider;
import org.bedrock.common.http.support.HttpSupport;
import org.bedrock.common.http.support.InterceptorChain;
import org.bedrock.common.spi.autoservice.AutoService;
import org.springframework.core.Ordered;

/**
 * 处理以下注解
 *
 * @see org.bedrock.common.http.annotation.Request
 */
@AutoService(Interceptor.class)
public class RequestInterceptor implements Interceptor {

	@Override
	public HttpResponse interceptor(HttpRequest request, MethodMeta methodMeta, InterceptorChain chain) {
		Request requestAnnotation = methodMeta.getMergedAnnotation(Request.class);
		if (requestAnnotation == null) {
			return chain.doInterceptor(request, methodMeta);
		}
		/**
		 * 设置url
		 */
		String url = requestAnnotation.url();
		if (StringUtil.isNotBlank(url)) {
			url = HttpSupport.resolvePlaceholders(url, request);
			if (UriUtil.isFullUrl(url)) {
				request.url(url);
			} else {
				request.path(url);
			}
		}
		if (request.method() == null) {
			request.method(requestAnnotation.method());
		}
		for (Header header : requestAnnotation.headers().value()) {
			request.addHeader(header.name(), HttpSupport.resolvePlaceholders(header.defaultValue(), request));
		}
		/**
		 * 设置contentType
		 */
		String contentType = requestAnnotation.contentType();
		if (StringUtil.isNotBlank(contentType)) {
			request.contentType(contentType);
		}
		/**
		 * 添加参数转换器
		 */
		Class<? extends ParameterConverter>[] classes = requestAnnotation.parameterConverters();
		for (Class<? extends ParameterConverter> clazz : classes) {
			request.addParameterConverter(clazz);
		}
		/**
		 * 添加转换器
		 */
		Class<? extends Converter> converter = requestAnnotation.converter();
		if (converter != Converter.class) {
			request.converter(converter);
		}
		/**
		 * 设置请求类型
		 */
		MediaTypeCategory mediaTypeCategory = requestAnnotation.mediaTypeCategory();
		if (mediaTypeCategory != MediaTypeCategory.UNKNOWN) {
			request.mediaTypeCategory(mediaTypeCategory);
		}
		/**
		 * 添加请求协议版本
		 */
		ProtocolVersion protocolVersion = requestAnnotation.protocolVersion();
		if (protocolVersion != ProtocolVersion.NONE) {
			request.protocolVersion(protocolVersion);
		}
		/**
		 * 设置超时时间
		 */
		request.connectTimeout(requestAnnotation.connectTimeout())
			.readTimeout(requestAnnotation.readTimeout())
			.writeTimeout(requestAnnotation.writeTimeout());

		/**
		 * 添加SSLContext提供者
		 */
		if (requestAnnotation.sslContextProvider() != SslContextProvider.class) {
			request.ssl(requestAnnotation.sslContextProvider());
		}
		/**
		 * 禁用SSL验证
		 */
		String disableSslValidation = requestAnnotation.disableSslValidation();
		if (StringUtil.isNotBlank(disableSslValidation)) {
			request.enableSslValidation(Boolean.parseBoolean(disableSslValidation));
		}
		return chain.doInterceptor(request, methodMeta);
	}

	@Override
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE + 100;
	}
}
