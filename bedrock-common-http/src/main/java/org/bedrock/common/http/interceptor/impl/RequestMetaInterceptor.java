package org.bedrock.common.http.interceptor.impl;

import org.bedrock.common.code.reflects.MethodMeta;
import org.bedrock.common.code.util.StringUtil;
import org.bedrock.common.http.HttpRequest;
import org.bedrock.common.http.HttpResponse;
import org.bedrock.common.http.converter.Converter;
import org.bedrock.common.http.interceptor.Interceptor;
import org.bedrock.common.http.model.HeadersMeta;
import org.bedrock.common.http.model.HttpProxyMeta;
import org.bedrock.common.http.model.RequestMeta;
import org.bedrock.common.http.props.HttpClientProperties;
import org.bedrock.common.http.ssl.SslContextProvider;
import org.bedrock.common.http.support.InterceptorChain;
import org.bedrock.common.spi.autoservice.AutoService;
import org.springframework.core.Ordered;

/**
 * 处理以下对象
 *
 * @see RequestMeta
 */
@AutoService(Interceptor.class)
public class RequestMetaInterceptor implements Interceptor {

	@Override
	public HttpResponse interceptor(HttpRequest request, MethodMeta methodMeta, InterceptorChain chain) {
		RequestMeta requestMeta = request.getRequestMeta();
		if (requestMeta == null) {
			return chain.doInterceptor(request, methodMeta);
		}
		/**
		 * 添加请求头
		 */
		HeadersMeta headerMetas = requestMeta.getHeaderMetas();
		if (headerMetas != null && !headerMetas.isEmpty()) {
			request.getHeaderMetas().addHeaders(headerMetas);
		}

		/**
		 * 设置ContentType
		 */
		if (StringUtil.isNotBlank(requestMeta.getContentType())) {
			request.setContentType(requestMeta.getContentType());
		}
		/**
		 * 添加URL信息
		 */
		request.getUrlMeta().mergeFrom(requestMeta.getUrlMeta());
		/**
		 * 添加参数转换器
		 */
		requestMeta.getParameterConverters().forEach(request::addParameterConverter);
		/**
		 * 设置转换器
		 */
		Converter converter = requestMeta.getConverter();
		if (converter != null) {
			request.converter(converter);
		}
		/**
		 * 设置代理
		 */
		HttpProxyMeta httpProxyMeta = requestMeta.getHttpProxyMeta();
		if (httpProxyMeta != null) {
			request.proxy(httpProxyMeta);
		}

		/**
		 * 设置日志
		 */
		HttpClientProperties.Log log = requestMeta.getLog();
		if (log != null) {
			request.log(log);
		}
		/**
		 * 设置SSL
		 */
		SslContextProvider sslContextProvider = requestMeta.getSslContextProvider();
		if (sslContextProvider != null) {
			request.ssl(sslContextProvider);
		}
		/**
		 * 设置SSL验证
		 */
		request.enableSslValidation(requestMeta.isDisableSslValidation());
		return chain.doInterceptor(request, methodMeta);
	}

	@Override
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE + 50;
	}
}
