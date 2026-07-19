package org.bedrock.common.http;

import lombok.Getter;
import lombok.Setter;
import org.bedrock.common.http.converter.Converter;
import org.bedrock.common.http.converter.ConverterRegistry;
import org.bedrock.common.http.converter.ParameterConverter;
import org.bedrock.common.http.converter.ParameterConverterRegistry;
import org.bedrock.common.http.enums.ProtocolVersion;
import org.bedrock.common.http.interceptor.DefaultInterceptorFactory;
import org.bedrock.common.http.interceptor.Interceptor;
import org.bedrock.common.http.interceptor.InterceptorFactory;
import org.bedrock.common.http.props.HttpClientProperties;
import org.bedrock.common.http.proxy.ClientRegistry;
import org.bedrock.common.http.ssl.SslContextProvider;
import org.bedrock.common.http.ssl.SslContextProviderRegistry;

import java.io.Serializable;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

/**
 * HTTP客户端核心通用配置类
 * <p>
 * 核心职责：统一管理HTTP客户端所有核心配置项与组件注册中心，提供全局统一的配置入口，支持链式流式配置
 * 管理范围：客户端代理注册、拦截器生命周期、数据转换器、SSL上下文提供者、参数转换器、基础连接属性、日志开关等
 * 特性：内置默认单例配置、序列化支持、所有配置操作均支持链式调用，解耦各组件依赖关系
 */
@Getter
@Setter
public class HttpConfiguration implements Serializable {

	/**
	 * 默认全局配置单例，提供开箱即用的默认配置能力
	 */
	private static final HttpConfiguration DEFAULT_CONFIG = new HttpConfiguration();

	/**
	 * 客户端代理实例注册中心，关联当前配置实例，统一管理各类HTTP客户端的创建与缓存
	 */
	private final ClientRegistry clientRegistry = new ClientRegistry(this);

	/**
	 * 拦截器工厂，默认加载默认实现类，统一管理拦截器的创建、缓存与维护
	 */
	private InterceptorFactory interceptorFactory = new DefaultInterceptorFactory(this);

	/**
	 * HTTP客户端基础配置属性封装，存储连接超时、协议版本、日志配置等基础属性
	 */
	private HttpClientProperties httpClientProperties = new HttpClientProperties();

	/**
	 * 请求/响应数据转换器注册中心，管理全局默认的数据转换器实现
	 */
	private ConverterRegistry converterRegistry = new ConverterRegistry();

	/**
	 * SSL上下文提供者注册中心，统一管理所有SslContextProvider接口的实现类
	 */
	private SslContextProviderRegistry sslContextProviderRegistry = new SslContextProviderRegistry();

	/**
	 * 请求参数转换器注册中心，管理所有参数转换器的注册与获取逻辑
	 */
	private ParameterConverterRegistry parameterConverterRegistry = new ParameterConverterRegistry();

	// ===================== 静态配置方法 =====================

	/**
	 * 获取默认的全局配置单例实例
	 *
	 * @return 全局唯一的默认HTTP配置实例
	 */
	public static HttpConfiguration getDefault() {
		return DEFAULT_CONFIG;
	}

	// ===================== 客户端相关方法 =====================

	/**
	 * 获取指定接口类型的客户端代理实例
	 *
	 * @param type 客户端接口的Class对象
	 * @return 对应接口的客户端代理实例
	 */
	public <T> T getClient(Class<T> type) {
		return clientRegistry.getClient(type);
	}

	/**
	 * 刷新指定接口类型的客户端工厂配置
	 * <p>
	 * 刷新后会销毁原有客户端代理实例并重新创建，最新的配置会立即生效
	 *
	 * @param type 客户端接口的Class对象
	 */
	public void refreshFactory(Class<?> type) {
		clientRegistry.refreshFactory(type);
	}

	// ===================== 拦截器相关方法 =====================

	/**
	 * 获取指定类型的拦截器实例
	 * <p>
	 * 若该拦截器实例未初始化，则通过拦截器工厂完成实例创建与缓存
	 *
	 * @param clazz 拦截器的Class对象
	 * @return 对应类型的拦截器实例
	 */
	public Interceptor getInterceptor(Class<? extends Interceptor> clazz) {
		return interceptorFactory.getInterceptor(clazz);
	}

	/**
	 * 获取全局默认拦截器集合
	 * <p>
	 * 该集合内的拦截器会作用于所有的HTTP请求执行链路，返回不可修改的原集合
	 *
	 * @return 全局默认拦截器集合
	 */
	public Set<Interceptor> getDefaultInterceptors() {
		return interceptorFactory.getDefaultInterceptors();
	}

	/**
	 * 添加全局默认拦截器，支持链式调用
	 * <p>
	 * 新增的拦截器将加入全局拦截器链路，对所有HTTP请求生效
	 *
	 * @param interceptor 待添加的拦截器实例
	 * @return 当前配置实例，支持链式调用
	 */
	public HttpConfiguration addDefaultInterceptor(Interceptor interceptor) {
		interceptorFactory.addDefaultInterceptor(interceptor);
		return this;
	}

	// ===================== 数据转换器相关方法 =====================

	/**
	 * 获取全局默认的请求/响应数据转换器实例
	 * <p>
	 * 用于处理所有请求参数与响应结果的默认序列化/反序列化逻辑
	 *
	 * @return 全局默认转换器实例
	 */
	public Converter getDefaultConverter() {
		return converterRegistry.getDefaultConverter();
	}

	/**
	 * 获取指定类型的数据转换器实例
	 * <p>
	 * 若该类型转换器未注册，则返回全局默认转换器兜底
	 *
	 * @param clazz 转换器的Class对象
	 * @return 指定类型的转换器实例
	 */
	public Converter getConverter(Class<? extends Converter> clazz) {
		return converterRegistry.getConverter(clazz);
	}

	// ===================== SSL上下文提供者相关方法 =====================

	/**
	 * 获取指定类型的SSL上下文提供者实例
	 * <p>
	 * 从SSL注册中心中获取，无实例则自动创建并完成注册
	 *
	 * @param clazz SSL上下文提供者的实现类Class对象
	 * @return 指定类型的SSL上下文提供者实例
	 */
	public SslContextProvider getSslContextProvider(Class<? extends SslContextProvider> clazz) {
		return sslContextProviderRegistry.getProvider(clazz);
	}

	/**
	 * 注册SSL上下文提供者实例，支持链式调用
	 * <p>
	 * 以实现类的Class对象作为唯一标识，完成实例注册到SSL注册中心
	 *
	 * @param provider SSL上下文提供者实例
	 * @return 当前配置实例，支持链式调用
	 */
	public HttpConfiguration registerSslContextProvider(SslContextProvider provider) {
		sslContextProviderRegistry.register(provider.getClass(), provider);
		return this;
	}

	/**
	 * 移除指定类型的SSL上下文提供者注册信息，支持链式调用
	 *
	 * @param providerKey SSL上下文提供者的实现类Class对象
	 * @return 当前配置实例，支持链式调用
	 */
	public HttpConfiguration unregisterSslContextProvider(Class<? extends SslContextProvider> providerKey) {
		sslContextProviderRegistry.unregister(providerKey);
		return this;
	}

	// ===================== 请求参数转换器相关方法 =====================

	/**
	 * 添加全局默认参数转换器，支持链式调用
	 * <p>
	 * 新增的转换器将参与所有请求参数的格式化与转换流程
	 *
	 * @param converter 待添加的参数转换器实例
	 * @return 当前配置实例，支持链式调用
	 */
	public HttpConfiguration addDefaultParamConverter(ParameterConverter converter) {
		parameterConverterRegistry.addDefaultConverter(converter);
		return this;
	}

	/**
	 * 移除全局默认参数转换器，支持链式调用
	 * <p>
	 * 移除后该转换器将不再参与请求参数的转换流程
	 *
	 * @param converter 待移除的参数转换器实例
	 * @return 当前配置实例，支持链式调用
	 */
	public HttpConfiguration removeDefaultParamConverter(ParameterConverter converter) {
		parameterConverterRegistry.removeDefaultConverter(converter);
		return this;
	}

	/**
	 * 获取全局默认参数转换器的有序集合（返回拷贝对象）
	 * <p>
	 * 排序规则：优先按转换器执行优先级{@link ParameterConverter#getOrder()}升序，优先级为空则后置
	 * 优先级相同时，按转换器类名进行自然排序，保证执行顺序稳定
	 * 注：返回的是拷贝集合，修改不会影响原配置中的转换器列表
	 *
	 * @return 有序的参数转换器集合
	 */
	public Set<ParameterConverter> getDefaultParameterConverters() {
		TreeSet<ParameterConverter> converterSet = new TreeSet<>(
			Comparator.comparing(
				ParameterConverter::getOrder,
				Comparator.nullsLast(Integer::compare)
			).thenComparing(
				conv -> conv.getClass().getName()
			)
		);
		converterSet.addAll(parameterConverterRegistry.getDefaultConverters());
		return converterSet;
	}

	/**
	 * 获取指定类型的参数转换器实例
	 * <p>
	 * 若该类型转换器未注册，直接返回null
	 *
	 * @param clazz 参数转换器的Class对象
	 * @return 指定类型的参数转换器实例，无则返回null
	 */
	public ParameterConverter getParameterConverter(Class<? extends ParameterConverter> clazz) {
		return parameterConverterRegistry.getConverter(clazz);
	}

	// ===================== 基础连接配置属性相关方法 =====================

	/**
	 * 获取HTTP执行器的实例名称
	 * <p>
	 * 用于区分不同的HTTP执行器实例，做个性化配置隔离
	 *
	 * @return HTTP执行器名称
	 */
	public String getHttpExecutorName() {
		return httpClientProperties.getHttpExecutorName();
	}

	/**
	 * 设置HTTP执行器的实例名称，支持链式调用
	 *
	 * @param httpExecutorName HTTP执行器名称
	 * @return 当前配置实例，支持链式调用
	 */
	public HttpConfiguration httpExecutorName(String httpExecutorName) {
		httpClientProperties.setHttpExecutorName(httpExecutorName);
		return this;
	}

	/**
	 * 获取HTTP连接超时时间
	 * <p>
	 * 单位：秒，默认值参考{HttpClientProperties}的默认配置
	 *
	 * @return 连接超时时间（秒）
	 */
	public int getConnectTimeout() {
		return httpClientProperties.getConnectTimeout();
	}

	/**
	 * 设置HTTP连接超时时间，支持链式调用
	 * <p>
	 * 单位：秒，推荐配置：内网接口3~5秒，外网接口5~8秒
	 *
	 * @param connectTimeout 连接超时时间（秒）
	 * @return 当前配置实例，支持链式调用
	 */
	public HttpConfiguration connectTimeout(int connectTimeout) {
		httpClientProperties.setConnectTimeout(connectTimeout);
		return this;
	}

	/**
	 * 获取HTTP读取超时时间
	 * <p>
	 * 单位：秒，默认值参考{HttpClientProperties}的默认配置
	 *
	 * @return 读取超时时间（秒）
	 */
	public int getReadTimeout() {
		return httpClientProperties.getReadTimeout();
	}

	/**
	 * 设置HTTP读取超时时间，支持链式调用
	 * <p>
	 * 单位：秒，推荐配置：常规接口10~30秒，大文件下载60~300秒
	 *
	 * @param readTimeout 读取超时时间（秒）
	 * @return 当前配置实例，支持链式调用
	 */
	public HttpConfiguration readTimeout(int readTimeout) {
		httpClientProperties.setReadTimeout(readTimeout);
		return this;
	}

	/**
	 * 获取HTTP写入超时时间
	 * <p>
	 * 单位：秒，默认值参考{HttpClientProperties}的默认配置
	 *
	 * @return 写入超时时间（秒）
	 */
	public int getWriteTimeout() {
		return httpClientProperties.getWriteTimeout();
	}

	/**
	 * 设置HTTP写入超时时间，支持链式调用
	 * <p>
	 * 单位：秒，推荐配置：常规请求5~10秒，大文件上传30~60秒
	 *
	 * @param writeTimeout 写入超时时间（秒）
	 * @return 当前配置实例，支持链式调用
	 */
	public HttpConfiguration writeTimeout(int writeTimeout) {
		httpClientProperties.setWriteTimeout(writeTimeout);
		return this;
	}

	/**
	 * 获取HTTP协议版本
	 * <p>
	 * 若未手动配置，则返回协议版本的默认值{ProtocolVersion#getDefault()}
	 *
	 * @return HTTP协议版本枚举
	 */
	public ProtocolVersion getProtocolVersion() {
		ProtocolVersion protocolVersion = httpClientProperties.getProtocolVersion();
		if (protocolVersion == null) {
			protocolVersion = ProtocolVersion.getDefault();
		}
		return protocolVersion;
	}

	/**
	 * 设置HTTP协议版本，支持链式调用
	 *
	 * @param protocolVersion HTTP协议版本枚举
	 * @return 当前配置实例，支持链式调用
	 */
	public HttpConfiguration protocolVersion(ProtocolVersion protocolVersion) {
		httpClientProperties.setProtocolVersion(protocolVersion);
		return this;
	}

	/**
	 * 获取HTTP请求响应的日志配置详情
	 * <p>
	 * 包含全局日志开关、请求/响应日志开关、请求头/体/响应头/体日志精细化开关
	 *
	 * @return 日志配置对象
	 */
	public HttpClientProperties.Log getLog() {
		return httpClientProperties.getLog();
	}

	/**
	 * 全局启用/禁用HTTP请求响应日志，支持链式调用
	 * <p>
	 * 优先级最高，全局禁用后所有日志相关配置均失效，无任何日志输出
	 *
	 * @param enable true=启用日志，false=禁用日志
	 * @return 当前配置实例，支持链式调用
	 */
	public HttpConfiguration enableLog(boolean enable) {
		httpClientProperties.getLog().setEnabled(enable);
		return this;
	}

	/**
	 * 启用/禁用请求日志总开关，支持链式调用
	 * <p>
	 * 依赖全局日志启用，禁用后请求头、请求体日志均不会输出
	 *
	 * @param requestLog true=启用请求日志，false=禁用请求日志
	 * @return 当前配置实例，支持链式调用
	 */
	public HttpConfiguration requestLog(boolean requestLog) {
		httpClientProperties.getLog().setRequestLog(requestLog);
		return this;
	}

	/**
	 * 启用/禁用请求头日志，支持链式调用
	 * <p>
	 * 依赖全局日志+请求日志均启用，才会输出请求头相关日志
	 *
	 * @param requestHeadersLog true=启用请求头日志，false=禁用请求头日志
	 * @return 当前配置实例，支持链式调用
	 */
	public HttpConfiguration requestHeadersLog(boolean requestHeadersLog) {
		httpClientProperties.getLog().setRequestHeaderLog(requestHeadersLog);
		return this;
	}

	/**
	 * 启用/禁用请求体日志，支持链式调用
	 * <p>
	 * 依赖全局日志+请求日志均启用，生产环境建议在大报文场景下禁用，避免日志冗余
	 *
	 * @param requestBodyLog true=启用请求体日志，false=禁用请求体日志
	 * @return 当前配置实例，支持链式调用
	 */
	public HttpConfiguration requestBodyLog(boolean requestBodyLog) {
		httpClientProperties.getLog().setRequestBodyLog(requestBodyLog);
		return this;
	}

	/**
	 * 启用/禁用响应日志总开关，支持链式调用
	 * <p>
	 * 依赖全局日志启用，禁用后响应头、响应体日志均不会输出
	 *
	 * @param responseLog true=启用响应日志，false=禁用响应日志
	 * @return 当前配置实例，支持链式调用
	 */
	public HttpConfiguration responseLog(boolean responseLog) {
		httpClientProperties.getLog().setResponseLog(responseLog);
		return this;
	}

	/**
	 * 启用/禁用响应头日志，支持链式调用
	 * <p>
	 * 依赖全局日志+响应日志均启用，才会输出响应头相关日志
	 *
	 * @param responseHeadersLog true=启用响应头日志，false=禁用响应头日志
	 * @return 当前配置实例，支持链式调用
	 */
	public HttpConfiguration responseHeadersLog(boolean responseHeadersLog) {
		httpClientProperties.getLog().setResponseHeaderLog(responseHeadersLog);
		return this;
	}

	/**
	 * 启用/禁用响应体日志，支持链式调用
	 * <p>
	 * 依赖全局日志+响应日志均启用，生产环境建议在大报文场景下禁用，避免日志冗余
	 *
	 * @param responseBodyLog true=启用响应体日志，false=禁用响应体日志
	 * @return 当前配置实例，支持链式调用
	 */
	public HttpConfiguration responseBodyLog(boolean responseBodyLog) {
		httpClientProperties.getLog().setResponseBodyLog(responseBodyLog);
		return this;
	}

}
