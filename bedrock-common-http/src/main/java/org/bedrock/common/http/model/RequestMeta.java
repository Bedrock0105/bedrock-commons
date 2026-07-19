package org.bedrock.common.http.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bedrock.common.http.converter.Converter;
import org.bedrock.common.http.converter.ParameterConverter;
import org.bedrock.common.http.enums.ProtocolVersion;
import org.bedrock.common.http.interceptor.Interceptor;
import org.bedrock.common.http.props.HttpClientProperties;
import org.bedrock.common.http.ssl.SslContextProvider;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 请求元数据封装类
 * <p>核心功能：聚合 HTTP 请求的核心元信息，包括所属接口类、URL 详情、请求头、拦截器等，提供链式调用 API 简化配置</p>
 * <p>典型场景：用于封装自定义注解解析后的请求配置，为 HTTP 客户端发起请求提供统一的元数据支撑</p>
 */
@Getter
@Setter
@ToString
public class RequestMeta {

	/**
	 * 所属接口类
	 * <p>标识当前请求元数据归属的业务接口 Class 对象（如 UserApi.class），用于关联接口级别的配置</p>
	 */
	private Class<?> interfaceClass;

	/**
	 * URL 元数据对象
	 * <p>封装请求的完整 URL 信息（scheme、host、port、basePath、path、query 等），默认初始化空 UrlMeta 实例</p>
	 *
	 * @see UrlMeta
	 */
	private UrlMeta urlMeta = new UrlMeta();

	/**
	 * 请求头元数据列表
	 * <p>存储自定义 HTTP 请求头（如 Content-Type、Authorization 等），默认初始化空 ArrayList，保证非空</p>
	 *
	 * @see HeaderMeta
	 */
	private HeadersMeta headerMetas = new HeadersMeta();

	/**
	 * 请求内容类型
	 * <p>指定本次请求的 Content-Type，默认为空字符串（表示不指定 Content-Type）</p>
	 */
	private String contentType;

	/**
	 * 请求拦截器集合
	 * <p>存储请求生命周期的拦截器（如请求前参数校验、请求后响应处理），默认初始化空 HashSet，保证非空且去重</p>
	 *
	 * @see Interceptor
	 */
	private Set<Interceptor> interceptors = new HashSet<>();

	/**
	 * 请求参数转换器：指定本次请求的参数转换器（默认使用全局配置的转换器）
	 */
	private Set<ParameterConverter> parameterConverters = new HashSet<>();

	/**
	 * 日志配置
	 */
	private HttpClientProperties.Log log;

	/**
	 * 数据转换器：指定本次请求的数据转换器（默认使用全局配置的转换器
	 */
	private Converter converter;

	/**
	 * 请求协议版本
	 */
	private ProtocolVersion protocolVersion;

	/**
	 * HTTP代理元数据
	 */
	private HttpProxyMeta httpProxyMeta;

	/**
	 * SSL上下文提供者
	 */
	private SslContextProvider sslContextProvider;

	/**
	 * 禁用SSL验证：默认false
	 */
	private boolean disableSslValidation = false;

	/**
	 * 添加单个请求拦截器
	 * <p>拦截器会自动去重（基于 Set 特性），重复添加同一拦截器不会生效</p>
	 *
	 * @param interceptor 拦截器实例（非 null，否则 HashSet 会存储 null 元素）
	 */
	public void addInterceptor(Interceptor interceptor) {
		interceptors.add(interceptor);
	}

	/**
	 * 批量添加请求拦截器
	 * <p>批量导入拦截器集合，自动去重，适用于一次性配置多个拦截器的场景</p>
	 *
	 * @param interceptors 拦截器集合（可为空，空集合时无操作）
	 */
	public void addInterceptors(Collection<Interceptor> interceptors) {
		this.interceptors.addAll(interceptors);
	}

	/**
	 * 添加单个请求头
	 * <p>快速构建 HeaderMeta 并添加到请求头列表，简化单个请求头的配置</p>
	 *
	 * @param name  请求头名称（如 "Content-Type"，非空）
	 * @param value 请求头值（如 "application/json"，可为空）
	 * @see HeaderMeta#HeaderMeta(String, String)
	 */
	public void addHeader(String name, String value) {
		headerMetas.addHeader(new HeaderMeta(name, value));
	}

	/**
	 * 批量添加请求头
	 * <p>批量导入请求头元数据列表，适用于一次性配置多个请求头的场景</p>
	 *
	 * @param headerMetas 请求头元数据集合（可为空，空集合时无操作）
	 */
	public void addHeaders(Collection<HeaderMeta> headerMetas) {
		this.headerMetas.addHeaders(headerMetas);
	}

	/**
	 * 链式设置请求 URL
	 * <p>委托给 {@link UrlMeta#setUrl(String)} 实现，支持链式调用</p>
	 *
	 * @param url 请求 URL 字符串（如 "https://example.com/api/user"）
	 * @return 当前 RequestMeta 实例（链式调用）
	 * @throws IllegalArgumentException 当 URL 为空或格式非法时抛出（由 UrlMeta 内部校验）
	 */
	public RequestMeta setUrl(String url) {
		urlMeta.setUrl(url);
		return this;
	}

	/**
	 * 链式设置 URL 基础路径
	 * <p>委托给 {@link UrlMeta#setBasePath(String)} 实现，支持链式调用</p>
	 * <p>基础路径通常用于接口级别的路径前缀（如 /api/v1），与业务路径拼接为完整路径</p>
	 *
	 * @param basePath URL 基础路径（如 "/api/v1"）
	 * @return 当前 RequestMeta 实例（链式调用）
	 */
	public RequestMeta setBasePath(String basePath) {
		urlMeta.setBasePath(basePath);
		return this;
	}

	/**
	 * 手动添加默认转换器
	 * <p>补充SPI自动加载的转换器列表，适用于非SPI方式注册的自定义转换器（如临时添加、内部私有转换器）。</p>
	 * <p><b>线程安全</b>：基于 {@link CopyOnWriteArrayList} 的add方法，无需额外加锁。</p>
	 *
	 * @param converter 待添加的参数转换器（不可为null，否则会向列表中添加null元素，建议调用方提前校验）
	 */
	public RequestMeta addParameterConverter(ParameterConverter converter) {
		parameterConverters.add(converter);
		return this;
	}

	/**
	 * 链式设置请求日志配置
	 * <p>设置日志配置，用于控制请求日志的打印（如是否打印请求头、请求体、响应头、响应体等）</p>
	 *
	 * @param log 日志配置（不可为null，否则会向列表中添加null元素，建议调用方提前校验）
	 * @return 当前 RequestMeta 实例（链式调用）
	 */
	public RequestMeta log(HttpClientProperties.Log log) {
		this.log = log;
		return this;
	}

	/**
	 * 链式设置 HTTP 代理元数据
	 *
	 * @param httpProxyMeta
	 * @return
	 */
	public RequestMeta httpProxyMeta(HttpProxyMeta httpProxyMeta) {
		this.httpProxyMeta = httpProxyMeta;
		return this;
	}

	/**
	 * 链式设置 SSL 上下文提供者
	 *
	 * @param sslContextProvider
	 * @return
	 */
	public RequestMeta sslContextProvider(SslContextProvider sslContextProvider) {
		this.sslContextProvider = sslContextProvider;
		return this;
	}

	/**
	 * 启用/禁用SSL证书有效性校验
	 *
	 * @param disable true=禁用SSL校验(忽略证书/域名匹配)，false=启用严格的SSL校验规则
	 * @return 当前HttpRequest实例，支持链式调用
	 */
	public RequestMeta enableSslValidation(boolean disable) {
		this.disableSslValidation = disable;
		return this;
	}

	/**
	 * 链式设置请求协议版本
	 *
	 * @param protocolVersion
	 * @return
	 */
	public RequestMeta protocolVersion(ProtocolVersion protocolVersion) {
		this.protocolVersion = protocolVersion;
		return this;
	}

}

