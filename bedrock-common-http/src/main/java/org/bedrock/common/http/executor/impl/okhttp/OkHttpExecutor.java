package org.bedrock.common.http.executor.impl.okhttp;

import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okhttp3.internal.Util;
import org.bedrock.common.code.util.ObjectUtil;
import org.bedrock.common.code.util.StringUtil;
import org.bedrock.common.http.HttpRequest;
import org.bedrock.common.http.HttpResponse;
import org.bedrock.common.http.enums.HeaderEnum;
import org.bedrock.common.http.enums.HttpMethod;
import org.bedrock.common.http.enums.MediaTypeCategory;
import org.bedrock.common.http.enums.ProtocolVersion;
import org.bedrock.common.http.executor.HttpExecutor;
import org.bedrock.common.http.model.ContentType;
import org.bedrock.common.http.model.HeadersMeta;
import org.bedrock.common.http.model.HttpProxyMeta;
import org.bedrock.common.http.model.param.BinaryDataParamMeta;
import org.bedrock.common.http.model.param.MultipartFormMeta;
import org.bedrock.common.http.model.param.Param;
import org.bedrock.common.http.ssl.SslContextProvider;
import org.bedrock.common.http.support.HttpSupport;

import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * OkHttp 实现的 HTTP 请求执行器
 */
@Slf4j
public class OkHttpExecutor implements HttpExecutor {

	/**
	 * 全局基础OkHttpClient，复用连接池、线程池等重量级资源
	 */
	private static final OkHttpClient httpClient = new OkHttpClient();

	/**
	 * 协议版本映射关系，自定义枚举转OkHttp原生协议，支持自动降级
	 */
	private static final Map<ProtocolVersion, List<Protocol>> PROTOCOLMAP = Map.of(
		ProtocolVersion.HTTP_1_0, List.of(Protocol.HTTP_1_0, Protocol.HTTP_1_1),
		ProtocolVersion.HTTP_1_1, List.of(Protocol.HTTP_1_1),
		ProtocolVersion.HTTP_2, List.of(Protocol.HTTP_2, Protocol.HTTP_1_1),
		ProtocolVersion.HTTP_3, List.of(Protocol.HTTP_2, Protocol.HTTP_1_1)
	);

	/**
	 * 执行Http请求，封装请求执行全流程并返回统一响应对象
	 *
	 * @param request 统一请求参数封装对象
	 * @return 统一响应结果封装对象
	 */
	@Override
	public HttpResponse execute(HttpRequest request) {
		LocalDateTime requestTime = LocalDateTime.now();
		try {
			Request.Builder requestBuilder = new Request.Builder().url(builderUrl(request));
			RequestBody requestBody = builderBody(request);
			setHeaders(request, requestBuilder);
			OkHttpClient customClient = builderHttpClient(request, OkHttpExecutor.httpClient);
			Request build = requestBuilder.method(request.method().name(), requestBody).build();
			Call call = customClient.newCall(build);
			Response execute = call.execute();
			return builderResponse(execute, request, requestTime);
		} catch (Throwable e) {
			log.error("[okHttp 发起请求异常]请求异常", e);
			return new OkHttpResponse(request, e, requestTime, LocalDateTime.now());
		}
	}

	/**
	 * 构建自定义配置的OkHttpClient实例
	 * 基于全局基础HttpClient，定制化配置：超时时间、Http协议版本、代理配置(含代理认证+代理专属请求头)
	 *
	 * @param request    自定义请求对象，携带所有定制化配置参数
	 * @param httpClient 全局基础OkHttpClient，复用连接池/线程池等核心资源
	 * @return 定制化后的OkHttpClient实例
	 */
	private OkHttpClient builderHttpClient(HttpRequest request, OkHttpClient httpClient) {
		OkHttpClient.Builder builder = httpClient.newBuilder();

		// 配置连接超时：配置值>0时生效，沿用全局默认值
		if (request.getConnectTimeout() > 0) {
			builder.connectTimeout(request.getConnectTimeout(), TimeUnit.MILLISECONDS);
		}
		// 配置读取超时：配置值>0时生效，沿用全局默认值
		if (request.getReadTimeout() > 0) {
			builder.readTimeout(request.getReadTimeout(), TimeUnit.MILLISECONDS);
		}
		// 配置写入超时：配置值>0时生效，沿用全局默认值
		if (request.getWriteTimeout() > 0) {
			builder.writeTimeout(request.getWriteTimeout(), TimeUnit.MILLISECONDS);
		}

		// 设置指定的Http协议版本，从映射关系中获取适配的协议列表
		builder.protocols(PROTOCOLMAP.get(request.getProtocolVersion()));

		// 配置代理信息：存在代理配置时才生效
		HttpProxyMeta proxy = request.proxy();
		if (proxy != null) {
			// 创建代理对象：指定代理类型+代理服务器地址端口
			builder.proxy(new Proxy(proxy.getType(), new InetSocketAddress(proxy.getHost(), proxy.getPort())));

			// 代理需要认证/携带代理请求头时，配置代理认证器
			if ((StringUtil.isNotBlank(proxy.getUsername()) && StringUtil.isNotBlank(proxy.getPassword()))
				|| !proxy.getHeaderMetas().isEmpty()) {
				builder.proxyAuthenticator((route, response) -> {
					Request.Builder requestBuilder = response.request().newBuilder();
					// 配置代理基础认证：账号密码非空时，添加代理认证头
					if (StringUtil.isNotBlank(proxy.getUsername()) && StringUtil.isNotBlank(proxy.getPassword())) {
						String credential = Credentials.basic(proxy.getUsername(), proxy.getPassword());
						requestBuilder.header(HeaderEnum.PROXY_AUTHORIZATION.getValue(), credential);
					}
					// 配置代理专属请求头：添加代理服务器需要的自定义请求头
					if (!proxy.getHeaderMetas().isEmpty()) {
						proxy.getHeaderMetas().getHeaderMetas().forEach(header ->
							requestBuilder.header(header.getName(), header.getValue())
						);
					}
					return requestBuilder.build();
				});
			}
		}
		/**
		 * 配置HTTPS请求的SSL证书校验规则（核心SSL初始化逻辑）
		 * 仅当当前请求为HTTPS协议、且配置了有效的SSL上下文提供者时，才执行自定义SSL配置
		 * 适配自定义的证书信任策略（如：信任所有证书、指定证书校验等），替换OkHttp默认的SSL校验逻辑
		 */
		if (request.isSSL() && request.ssl() != null) {
			// 获取当前请求绑定的SSL上下文提供者实例（如TrustAllSslContextProvider）
			SslContextProvider sslContextProvider = request.ssl();
			// 从SSL提供者中获取初始化完成的SSL上下文对象
			SSLContext sslContext = sslContextProvider.getSslContext(request);
			// 从SSL提供者中获取自定义的X509证书信任管理器
			X509TrustManager disabledTrustManager = sslContextProvider.getX509TrustManager(request);
			// 为OkHttp构建器配置自定义SSL套接字工厂+信任管理器，生效自定义证书校验规则
			builder.sslSocketFactory(sslContext.getSocketFactory(), disabledTrustManager);
			// 为OkHttp构建器配置自定义域名匹配校验器，生效自定义域名校验策略（如忽略域名校验）
			builder.hostnameVerifier(sslContextProvider.getHostnameVerifier(request));
		}
		return builder.build();
	}

	/**
	 * 构建统一的响应对象，转换OkHttp响应为自定义响应
	 */
	public HttpResponse builderResponse(Response response, HttpRequest request, LocalDateTime requestTime) throws IOException {
		return new OkHttpResponse(request, response, requestTime, LocalDateTime.now());
	}

	/**
	 * 为请求设置请求头信息
	 */
	public void setHeaders(HttpRequest request, Request.Builder requestBuilder) {
		HeadersMeta headerMetas = request.getHeaderMetas();
		if (headerMetas == null) return;

		headerMetas.getHeaderMetas().forEach(header ->
			requestBuilder.addHeader(header.getName(), header.getValue())
		);
	}

	/**
	 * 根据请求方式和媒体类型，构建对应类型的请求体
	 */
	public RequestBody builderBody(HttpRequest request) {
		// GET/HEAD/TRACE 协议规范无请求体
		if (request.method() == HttpMethod.GET ||
			request.method() == HttpMethod.HEAD ||
			request.method() == HttpMethod.TRACE) {
			return null;
		}

		MediaTypeCategory mediaTypeCategory = request.getMediaTypeCategory();
		if (mediaTypeCategory == MediaTypeCategory.FORM_URLENCODED) {
			return buildFormUrlEncodedBody(request);
		}
		if (mediaTypeCategory == MediaTypeCategory.STRING_REQUEST_BODY) {
			return buildStringRequestBody(request);
		}
		if (mediaTypeCategory == MediaTypeCategory.MULTIPART_FORM_DATA) {
			return buildMultipartFormDataBody(request);
		}
		if (mediaTypeCategory == MediaTypeCategory.BINARY_DATA) {
			return buildBinaryDataBody(request);
		}
		return Util.EMPTY_REQUEST;
	}

	/**
	 * 构建表单类型请求体 application/x-www-form-urlencoded
	 */
	private RequestBody buildFormUrlEncodedBody(HttpRequest request) {
		if (request.isFormEmpty()) return Util.EMPTY_REQUEST;

		FormBody.Builder builder = new FormBody.Builder();
		List<Param> params = request.formParams();
		for (Param param : params) {
			String name = param.name();
			// 处理单个带名称的参数
			if (StringUtil.isNotBlank(name)) {
				String convertedValue = HttpSupport.paramConverter(request, name, param.value());
				if (param.encoded()) {
					builder.add(name, convertedValue);
				} else {
					builder.addEncoded(name, convertedValue);
				}
				continue;
			}
			// 处理结构化参数，转换为Map后批量添加
			Map<String, Object> paramMap = request.getConverter().convertToMultiValueMap(param.value(), request);
			paramMap.forEach((k, v) -> {
				String valueStr = StringUtil.toStr(v, "");
				if (param.encoded()) {
					builder.add(k, valueStr);
				} else {
					builder.addEncoded(k, valueStr);
				}
			});
		}
		return builder.build();
	}

	/**
	 * 构建字符串请求体，适配JSON/XML等文本类型
	 */
	private RequestBody buildStringRequestBody(HttpRequest request) {
		if (!request.isBody() && !request.isBodyMap()) return Util.EMPTY_REQUEST;

		Object bodyData = request.getBodyData();
		String body = request.getConverter().convertToRequestString(bodyData, request, request.contentType());
		return RequestBody.create(body, MediaType.parse(request.getContentType()));
	}

	/**
	 * 构建多部件表单请求体 multipart/form-data，支持文件上传与普通参数混合
	 */
	private RequestBody buildMultipartFormDataBody(HttpRequest request) {
		if (request.isMultipartFormEmpty()) return Util.EMPTY_REQUEST;

		MultipartBody.Builder builder = new MultipartBody.Builder(request.getOrCreateBoundary());
		builder.setType(Objects.requireNonNull(MediaType.parse(request.getContentType())));
		List<MultipartFormMeta.Param> params = request.multipartFormParams();

		for (MultipartFormMeta.Param param : params) {
			ContentType contentType = param.contentType();
			if (ObjectUtil.isEmpty(contentType)) {
				contentType = ContentType.create(ContentType.FORM_URLENCODED);
			}
			MediaTypeCategory typeMediaTypeCategory = contentType.getMediaTypeCategory();
			if (typeMediaTypeCategory == MediaTypeCategory.UNKNOWN) {
				typeMediaTypeCategory = MediaTypeCategory.FORM_URLENCODED;
			}
			// 处理普通表单参数
			if (typeMediaTypeCategory == MediaTypeCategory.FORM_URLENCODED || typeMediaTypeCategory == MediaTypeCategory.MULTIPART_FORM_DATA) {
				String name = param.name();
				if (StringUtil.isNotBlank(name)) {
					String convertedValue = HttpSupport.paramConverter(request, name, param.value());
					if (param.contentType() == null) {
						builder.addFormDataPart(name, convertedValue);
					} else {
						builder.addFormDataPart(name, param.fileName(), RequestBody.create(convertedValue, MediaType.parse(contentType.toString())));
					}
					continue;
				}
				Map<String, Object> paramMap = request.getConverter().convertToMultiValueMap(param.value(), request);
				paramMap.forEach((k, v) -> {
					String val = StringUtil.toStr(v, "");
					if (param.contentType() == null) {
						builder.addFormDataPart(k, val);
					} else {
						builder.addFormDataPart(k, param.fileName(), RequestBody.create(val, MediaType.parse(param.contentType().toString())));
					}
				});
				continue;
			}
			// 处理字符串类型参数
			if (typeMediaTypeCategory == MediaTypeCategory.STRING_REQUEST_BODY) {
				String body = request.getConverter().convertToRequestString(param.value(), request, contentType);
				builder.addFormDataPart(param.name(), param.fileName(), RequestBody.create(body, MediaType.parse(contentType.getMediaType())));
				continue;
			}
			// 处理二进制文件参数
			if (typeMediaTypeCategory == MediaTypeCategory.BINARY_DATA) {
				Object value = param.value();
				if (value instanceof File file) {
					builder.addFormDataPart(param.name(), param.fileName(), RequestBody.create(file, MediaType.parse(contentType.toString())));
				} else {
					byte[] bytes = request.getConverter().convertToRequestBytes(value, request, contentType);
					builder.addFormDataPart(param.name(), param.fileName(), RequestBody.create(bytes, MediaType.parse(contentType.toString())));
				}
			}
		}
		return builder.build();
	}

	/**
	 * 构建二进制数据请求体，支持文件、字节数组等二进制类型
	 */
	private RequestBody buildBinaryDataBody(HttpRequest request) {
		if (request.isBinaryDataEmpty()) return Util.EMPTY_REQUEST;

		BinaryDataParamMeta binaryDataParamMeta = request.binaryDataParamMeta();
		Object value = binaryDataParamMeta.getValue();
		if (value instanceof File file) {
			return RequestBody.create(file, MediaType.parse(request.getContentType()));
		} else {
			byte[] bytes = request.getConverter().convertToRequestBytes(value, request, request.contentType());
			return RequestBody.create(bytes, MediaType.parse(request.getContentType()));
		}
	}

	/**
	 * 构建带参数的完整请求URL，自动拼接Query和GET请求的Form参数
	 */
	public HttpUrl builderUrl(HttpRequest request) {
		HttpUrl httpUrl = HttpUrl.parse(request.url());
		if (httpUrl == null) {
			throw new IllegalArgumentException(String.format("Url 不能解析: %s: [%s]。", request.method(), request.url()));
		}
		// 无参数时直接返回原URL
		if (request.isQueryEmpty() && (request.method() != HttpMethod.GET || request.isFormEmpty())) {
			return httpUrl;
		}

		HttpUrl.Builder httpUrlBuilder = httpUrl.newBuilder();
		addParamsToUrlBuilder(httpUrlBuilder, request, request.queryParams());
		addParamsToUrlBuilder(httpUrlBuilder, request, request.formParams());
		return httpUrlBuilder.build();
	}

	/**
	 * 批量添加参数到URL构建器，复用参数拼接逻辑
	 */
	private void addParamsToUrlBuilder(HttpUrl.Builder urlBuilder, HttpRequest request, Iterable<Param> params) {
		for (Param param : params) {
			String name = param.name();
			if (StringUtil.isNotBlank(name)) {
				String convertedValue = HttpSupport.paramConverter(request, name, param.value());
				if (param.encoded()) {
					urlBuilder.addQueryParameter(name, convertedValue);
				} else {
					urlBuilder.addEncodedQueryParameter(name, convertedValue);
				}
				continue;
			}
			Map<String, Object> paramMap = request.getConverter().convertToMultiValueMap(param.value(), request);
			paramMap.forEach((k, v) -> {
				String valueStr = StringUtil.toStr(v, "");
				if (param.encoded()) {
					urlBuilder.addQueryParameter(k, valueStr);
				} else {
					urlBuilder.addEncodedQueryParameter(k, valueStr);
				}
			});
		}
	}
}
