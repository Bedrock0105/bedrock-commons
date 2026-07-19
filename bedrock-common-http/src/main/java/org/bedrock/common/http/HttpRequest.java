package org.bedrock.common.http;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.bedrock.common.code.reflects.MethodMeta;
import org.bedrock.common.code.util.ObjectUtil;
import org.bedrock.common.code.util.StringUtil;
import org.bedrock.common.http.converter.Converter;
import org.bedrock.common.http.converter.ParameterConverter;
import org.bedrock.common.http.enums.*;
import org.bedrock.common.http.executor.HttpExecutor;
import org.bedrock.common.http.executor.HttpExecutorRegistry;
import org.bedrock.common.http.interceptor.Interceptor;
import org.bedrock.common.http.model.*;
import org.bedrock.common.http.model.param.*;
import org.bedrock.common.http.props.HttpClientProperties;
import org.bedrock.common.http.ssl.SslContextProvider;
import org.bedrock.common.http.ssl.impl.TrustAllSslContextProvider;
import org.bedrock.common.http.support.ClientMethod;
import org.bedrock.common.http.support.HttpSupport;
import org.bedrock.common.http.support.InterceptorChain;
import org.bedrock.common.http.support.InterceptorChainImpl;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

import java.util.Collection;

/**
 * HTTP 请求上下文类
 * 核心作用：封装HTTP请求全量上下文信息，提供链式配置API，定义请求执行核心流程
 * 注意：每个HTTP请求对应一个HttpRequest实例，非工具类
 */
@Setter
@Getter
public class HttpRequest {

	// ===================== 常量定义 =====================

	/**
	 * 空参数数组常量：用于构造无参数的HttpRequest实例
	 */
	private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

	/**
	 * 空方法元数据常量：兜底MethodMeta实例，关联默认创建方法
	 */
	private static final MethodMeta EMPTY_METHOD_META = MethodMeta.forMethod(ClientMethod.DefaultClient.class, "createHttpRequest");

	// ===================== 核心字段（移除boundary字段） =====================

	/**
	 * HTTP 请求方法（GET/POST等）
	 */
	private HttpMethod httpMethod;

	/**
	 * HTTP 协议版本：默认HTTP/1.1
	 */
	private ProtocolVersion protocolVersion;

	/**
	 * HTTP 全局配置：包含默认拦截器、执行器名称等全局参数
	 */
	private final HttpConfiguration config;

	/**
	 * 请求关联方法元数据：封装返回类型、泛型信息等
	 */
	private MethodMeta methodMeta;

	/**
	 * 执行器名称：优先级高于全局配置
	 */
	private String httpExecutorName;

	/**
	 * 方法入参数组：业务方法参数
	 */
	private final Object[] args;

	/**
	 * 请求参数：封装query、form、body、multipartForm等参数
	 */
	private final ParamMeta paramMeta = new ParamMeta(this);

	/**
	 * 字符集编码：默认UTF-8
	 */
	private Charset charset = StandardCharsets.UTF_8;

	/**
	 * 数据转换器：默认使用全局配置转换器
	 */
	private Converter converter;

	/**
	 * 请求参数转换器：默认使用全局配置转换器集合
	 */
	private Set<ParameterConverter> parameterConverters;

	/**
	 * 类级别请求元数据：初始化后不可修改
	 */
	private RequestMeta requestMeta;

	/**
	 * URL 元数据：封装scheme、host、port等URL拆分信息
	 */
	private UrlMeta urlMeta = new UrlMeta();

	/**
	 * 请求头列表：存储本次请求HTTP头信息
	 */
	private HeadersMeta headerMetas = new HeadersMeta();

	/**
	 * 请求体媒体类型分类：默认未知
	 */
	private MediaTypeCategory mediaTypeCategory = MediaTypeCategory.UNKNOWN;

	/**
	 * 请求拦截器集合：默认添加全局配置默认拦截器
	 */
	@Setter(AccessLevel.NONE)
	private Set<Interceptor> interceptors = new HashSet<>();

	/**
	 * 方法参数映射表：存储参数名称与下标，初始化后不可修改
	 */
	private Map<String, Integer> argsMap;

	/**
	 * 连接超时时间（毫秒）
	 */
	private int connectTimeout = -1;

	/**
	 * 读取超时（毫秒）
	 */
	private int readTimeout = -1;

	/**
	 * 写超时（毫秒）
	 */
	private int writeTimeout = -1;

	/**
	 * 重试元数据
	 */
	private RetryMeta retryMeta;

	/**
	 * HTTP代理元数据
	 */
	private HttpProxyMeta httpProxyMeta;

	/**
	 * 日志配置
	 */
	private HttpClientProperties.Log log;

	/**
	 * SSL上下文提供者：默认使用全局配置
	 */
	private SslContextProvider sslContextProvider;

	/**
	 * 禁用SSL验证：默认false
	 */
	private boolean disableSslValidation = false;
	// ===================== 构造方法 =====================

	/**
	 * 构造空方法元数据、空参数的HTTP请求实例
	 *
	 * @param config HTTP全局配置（非null）
	 */
	public HttpRequest(HttpConfiguration config) {
		this(config, EMPTY_METHOD_META, EMPTY_OBJECT_ARRAY);
	}

	/**
	 * 构造指定方法元数据、空参数的HTTP请求实例
	 *
	 * @param config     HTTP全局配置
	 * @param methodMeta 方法元数据
	 */
	public HttpRequest(HttpConfiguration config, MethodMeta methodMeta) {
		this(config, methodMeta, EMPTY_OBJECT_ARRAY);
	}

	/**
	 * 构造指定配置、方法元数据、参数的HTTP请求实例
	 * 默认添加全局配置默认拦截器
	 *
	 * @param config     HTTP全局配置
	 * @param methodMeta 方法元数据
	 * @param args       方法入参数组（不可为null）
	 */
	public HttpRequest(HttpConfiguration config, MethodMeta methodMeta, Object[] args) {
		this.config = config;
		this.methodMeta = methodMeta;
		this.args = args;
		this.interceptors.addAll(config.getDefaultInterceptors());
		this.argsMap = Collections.unmodifiableMap(HttpSupport.getMethodParamsMap(methodMeta));
		this.parameterConverters = config.getDefaultParameterConverters();
	}

	// ===================== 参数获取方法 =====================

	/**
	 * 获取指定索引位置的方法参数
	 *
	 * @param index 参数索引（从0开始）
	 * @return 指定索引参数值
	 */
	public Object args(int index) {
		if (index < 0 || index >= args.length) {
			return null;
		}
		return args[index];
	}

	/**
	 * 获取所有方法参数数组
	 *
	 * @return 方法入参数组（返回原数组引用）
	 */
	public Object[] args() {
		return args;
	}

	// ===================== 集成 ParamMeta 链式方法 =====================
	// --- Query参数相关 ---

	/**
	 * 添加单个需URL编码的Query参数
	 *
	 * @param name  参数名
	 * @param value 参数值
	 * @return 当前HttpRequest实例
	 */
	public HttpRequest addQuery(String name, Object value) {
		paramMeta.addQuery(name, value);
		return this;
	}

	/**
	 * 批量添加Query参数
	 *
	 * @param map 参数键值对集合
	 * @return 当前HttpRequest实例
	 */
	public HttpRequest addQuery(Map<String, Object> map) {
		paramMeta.addQuery(map);
		return this;
	}

	/**
	 * 添加单个无需URL编码的Query参数
	 *
	 * @param name  参数名
	 * @param value 参数值
	 * @return 当前HttpRequest实例
	 */
	public HttpRequest addQueryEncoded(String name, Object value) {
		paramMeta.addQueryEncoded(name, value);
		return this;
	}

	/**
	 * 批量添加无需URL编码的Query参数
	 *
	 * @param map 参数键值对集合
	 * @return 当前HttpRequest实例
	 */
	public HttpRequest addQueryEncoded(Map<String, Object> map) {
		paramMeta.addQueryEncoded(map);
		return this;
	}

	/**
	 * 判断Query参数是否为空
	 *
	 * @return 空返回true，非空返回false
	 */
	public boolean isQueryEmpty() {
		return this.paramMeta.isQueryEmpty();
	}

	/**
	 * 获取Query参数元数据
	 *
	 * @return QueryParamMeta实例
	 */
	public QueryParamMeta queryParamMeta() {
		return this.paramMeta.queryParamMeta();
	}

	/**
	 * 获取不可修改的Query参数列表
	 *
	 * @return Query参数列表
	 */
	public List<Param> queryParams() {
		return this.paramMeta.queryParams();
	}

	// --- 普通Form参数相关 ---

	/**
	 * 添加单个需编码的普通表单参数
	 *
	 * @param name  参数名
	 * @param value 参数值
	 * @return 当前HttpRequest实例
	 */
	public HttpRequest addForm(String name, Object value) {
		paramMeta.addForm(name, value);
		return this;
	}

	/**
	 * 批量添加普通表单参数
	 *
	 * @param map 表单参数键值对集合
	 * @return 当前HttpRequest实例
	 */
	public HttpRequest addForm(Map<String, Object> map) {
		paramMeta.addForm(map);
		return this;
	}

	/**
	 * 添加单个无需编码的普通表单参数
	 *
	 * @param name  参数名
	 * @param value 参数值
	 * @return 当前HttpRequest实例
	 */
	public HttpRequest addFormEncoded(String name, Object value) {
		paramMeta.addFormEncoded(name, value);
		return this;
	}

	/**
	 * 批量添加无需编码的普通表单参数
	 *
	 * @param map 表单参数键值对集合
	 * @return 当前HttpRequest实例
	 */
	public HttpRequest addFormEncoded(Map<String, Object> map) {
		paramMeta.addFormEncoded(map);
		return this;
	}

	/**
	 * 判断普通表单参数是否为空
	 *
	 * @return 空返回true，非空返回false
	 */
	public boolean isFormEmpty() {
		return this.paramMeta.isFormEmpty();
	}

	/**
	 * 获取普通表单参数元数据
	 *
	 * @return FormParamMeta实例
	 */
	public FormParamMeta formParamMeta() {
		return this.paramMeta.formParamMeta();
	}

	/**
	 * 获取不可修改的普通表单参数列表
	 *
	 * @return Form参数列表
	 */
	public List<Param> formParams() {
		return this.paramMeta.formParams();
	}

	// --- Body参数相关 ---

	/**
	 * 设置单个请求体对象
	 *
	 * @param body 单个请求体数据对象
	 * @return 当前HttpRequest实例
	 */
	public HttpRequest addBody(Object body) {
		paramMeta.addBody(body);
		return this;
	}

	/**
	 * 批量添加请求体键值对
	 *
	 * @param bodyMap 请求体键值对集合
	 * @return 当前HttpRequest实例
	 */
	public HttpRequest addBody(Map<String, Object> bodyMap) {
		paramMeta.addBody(bodyMap);
		return this;
	}

	/**
	 * 添加单个请求体键值对
	 *
	 * @param name  参数名
	 * @param value 参数值
	 * @return 当前HttpRequest实例
	 */
	public HttpRequest addBody(String name, Object value) {
		paramMeta.addBody(name, value);
		return this;
	}

	/**
	 * 判断是否设置了单个请求体对象
	 *
	 * @return 是返回true，否返回false
	 */
	public boolean isBody() {
		return this.paramMeta.isBody();
	}

	/**
	 * 判断是否存在请求体键值对
	 *
	 * @return 是返回true，否返回false
	 */
	public boolean isBodyMap() {
		return this.paramMeta.isBodyMap();
	}

	/**
	 * 获取请求体数据（优先返回键值对，再返回单个对象）
	 *
	 * @return 请求体数据
	 */
	public Object getBodyData() {
		return this.paramMeta.getBodyData();
	}

	/**
	 * 获取请求体参数元数据
	 *
	 * @return BodyParamMeta实例
	 */
	public BodyParamMeta bodyParamMeta() {
		return this.paramMeta.bodyParamMeta();
	}

	// --- MultipartForm参数相关 ---

	/**
	 * 添加单个普通多部分表单参数
	 *
	 * @param name  参数名
	 * @param value 参数值
	 * @return 当前HttpRequest实例
	 */
	public HttpRequest addMultipartForm(String name, Object value) {
		paramMeta.addMultipartForm(name, value);
		return this;
	}

	/**
	 * 添加带内容类型的普通多部分表单参数
	 *
	 * @param name        参数名
	 * @param value       参数值
	 * @param contentType 内容类型
	 * @return 当前HttpRequest实例
	 */
	public HttpRequest addMultipartForm(String name, Object value, String contentType) {
		paramMeta.addMultipartForm(name, value, contentType);
		return this;
	}

	/**
	 * 添加带内容类型的普通多部分表单参数
	 *
	 * @param name        参数名
	 * @param value       参数值
	 * @param contentType 内容类型
	 * @return 当前HttpRequest实例
	 */
	public HttpRequest addMultipartForm(String name, String fileName, Object value, String contentType) {
		paramMeta.addMultipartForm(name, fileName, value, contentType);
		return this;
	}

	/**
	 * 添加字节数组类型多部分表单参数（默认二进制流）
	 *
	 * @param name  参数名
	 * @param bytes 字节数组数据
	 * @return 当前HttpRequest实例
	 */
	public HttpRequest addMultipartForm(String name, byte[] bytes) {
		paramMeta.addMultipartForm(name, bytes);
		return this;
	}

	/**
	 * 添加带文件名的字节数组多部分表单参数（默认二进制流）
	 *
	 * @param name     参数名
	 * @param fileName 文件名
	 * @param bytes    字节数组数据
	 * @return 当前HttpRequest实例
	 */
	public HttpRequest addMultipartForm(String name, String fileName, byte[] bytes) {
		paramMeta.addMultipartForm(name, fileName, bytes);
		return this;
	}

	/**
	 * 添加带内容类型的字节数组多部分表单参数
	 *
	 * @param name        参数名
	 * @param bytes       字节数组数据
	 * @param contentType 内容类型
	 * @return 当前HttpRequest实例
	 */
	public HttpRequest addMultipartForm(String name, byte[] bytes, String contentType) {
		paramMeta.addMultipartForm(name, bytes, contentType);
		return this;
	}

	/**
	 * 添加带文件名和内容类型的字节数组多部分表单参数
	 *
	 * @param name        参数名
	 * @param fileName    文件名
	 * @param bytes       字节数组数据
	 * @param contentType 内容类型
	 * @return 当前HttpRequest实例
	 */
	public HttpRequest addMultipartForm(String name, String fileName, byte[] bytes, String contentType) {
		paramMeta.addMultipartForm(name, fileName, bytes, contentType);
		return this;
	}

	/**
	 * 添加文件类型多部分表单参数（默认二进制流）
	 *
	 * @param name 参数名
	 * @param file 文件对象
	 * @return 当前HttpRequest实例
	 */
	public HttpRequest addMultipartForm(String name, File file) {
		paramMeta.addMultipartForm(name, file);
		return this;
	}

	/**
	 * 添加带文件名的文件多部分表单参数（默认二进制流）
	 *
	 * @param name     参数名
	 * @param fileName 文件名
	 * @param file     文件对象
	 * @return 当前HttpRequest实例
	 */
	public HttpRequest addMultipartForm(String name, String fileName, File file) {
		paramMeta.addMultipartForm(name, fileName, file);
		return this;
	}

	/**
	 * 添加带内容类型的文件多部分表单参数
	 *
	 * @param name        参数名
	 * @param file        文件对象
	 * @param contentType 内容类型
	 * @return 当前HttpRequest实例
	 */
	public HttpRequest addMultipartForm(String name, File file, String contentType) {
		paramMeta.addMultipartForm(name, file, contentType);
		return this;
	}

	/**
	 * 添加带文件名和内容类型的文件多部分表单参数
	 *
	 * @param name        参数名
	 * @param fileName    文件名
	 * @param file        文件对象
	 * @param contentType 内容类型
	 * @return 当前HttpRequest实例
	 */
	public HttpRequest addMultipartForm(String name, String fileName, File file, String contentType) {
		paramMeta.addMultipartForm(name, fileName, file, contentType);
		return this;
	}

	/**
	 * 添加输入流类型多部分表单参数（默认二进制流）
	 *
	 * @param name        参数名
	 * @param inputStream 输入流对象
	 * @return 当前HttpRequest实例
	 */
	public HttpRequest addMultipartForm(String name, InputStream inputStream) {
		paramMeta.addMultipartForm(name, inputStream);
		return this;
	}

	/**
	 * 添加带文件名的输入流多部分表单参数（默认二进制流）
	 *
	 * @param name        参数名
	 * @param fileName    文件名
	 * @param inputStream 输入流对象
	 * @return 当前HttpRequest实例
	 */
	public HttpRequest addMultipartForm(String name, String fileName, InputStream inputStream) {
		paramMeta.addMultipartForm(name, fileName, inputStream);
		return this;
	}

	/**
	 * 添加带内容类型的输入流多部分表单参数
	 *
	 * @param name        参数名
	 * @param inputStream 输入流对象
	 * @param contentType 内容类型
	 * @return 当前HttpRequest实例
	 */
	public HttpRequest addMultipartForm(String name, InputStream inputStream, String contentType) {
		paramMeta.addMultipartForm(name, inputStream, contentType);
		return this;
	}

	/**
	 * 添加带文件名和内容类型的输入流多部分表单参数
	 *
	 * @param name        参数名
	 * @param fileName    文件名
	 * @param inputStream 输入流对象
	 * @param contentType 内容类型
	 * @return 当前HttpRequest实例
	 */
	public HttpRequest addMultipartForm(String name, String fileName, InputStream inputStream, String contentType) {
		paramMeta.addMultipartForm(name, fileName, inputStream, contentType);
		return this;
	}

	/**
	 * 判断多部分表单参数是否为空
	 *
	 * @return 空返回true，非空返回false
	 */
	public boolean isMultipartFormEmpty() {
		return this.paramMeta.isMultipartFormEmpty();
	}

	/**
	 * 获取多部分表单参数元数据
	 *
	 * @return MultipartFormMeta实例
	 */
	public MultipartFormMeta multipartFormMeta() {
		return this.paramMeta.multipartFormMeta();
	}

	/**
	 * 获取不可修改的多部分表单参数列表
	 *
	 * @return MultipartForm参数列表
	 */
	public List<MultipartFormMeta.Param> multipartFormParams() {
		return this.paramMeta.multipartFormParams();
	}

	// ===================== BinaryData参数相关方法（集成BinaryDataParamMeta） =====================

	/**
	 * 添加二进制参数（默认文件名为null，默认内容类型为文本类型）
	 *
	 * @param name  参数名称
	 * @param value 二进制数据值 （支持File、byte[]、InputStream等类型）
	 * @return 当前HttpRequest实例
	 */
	public HttpRequest addBinaryData(String name, Object value) {
		paramMeta.addBinaryData(name, value);
		return this;
	}

	/**
	 * 判断二进制参数是否为空
	 *
	 * @return true=参数为空（参数名称或数据值为null），false=参数非空
	 */
	public boolean isBinaryDataEmpty() {
		return this.paramMeta.isBinaryDataEmpty();
	}

	/**
	 * 获取二进制数据参数元数据
	 *
	 * @return BinaryDataParamMeta实例
	 */
	public BinaryDataParamMeta binaryDataParamMeta() {
		return this.paramMeta.binaryDataParamMeta();
	}

	// ===================== 执行器名称配置 =====================

	/**
	 * 链式设置HTTP执行器名称
	 *
	 * @param httpExecutorName 执行器名称
	 * @return 当前HttpRequest实例
	 */
	public HttpRequest withHttpExecutorName(String httpExecutorName) {
		this.httpExecutorName = httpExecutorName;
		return this;
	}

	/**
	 * 链式设置HTTP执行器名称（通过枚举）
	 *
	 * @param executorEnum 执行器枚举
	 * @return 当前HttpRequest实例
	 */
	public HttpRequest httpExecutorName(HttpExecutorEnum executorEnum) {
		return withHttpExecutorName(executorEnum.getName());
	}

	/**
	 * 获取HTTP执行器名称（优先级：实例配置 > 全局配置）
	 *
	 * @return 最终使用的执行器名称
	 */
	public String getHttpExecutorName() {
		if (StringUtil.isNotBlank(httpExecutorName)) {
			return httpExecutorName;
		}
		return config.getHttpExecutorName();
	}

	// ===================== 转换器配置 =====================

	/**
	 * 链式设置数据转换器
	 *
	 * @param converter 数据转换器
	 * @return 当前HttpRequest实例
	 */
	public HttpRequest converter(Converter converter) {
		this.converter = converter;
		return this;
	}

	/**
	 * 链式设置数据转换器（通过类型）
	 *
	 * @param converter 转换器类型
	 * @return 当前HttpRequest实例
	 */
	public HttpRequest converter(Class<? extends Converter> converter) {
		this.converter = config.getConverter(converter);
		return this;
	}

	/**
	 * 获取数据转换器（兜底全局默认转换器）
	 *
	 * @return 数据转换器
	 */
	public Converter getConverter() {
		if (converter == null) {
			return config.getDefaultConverter();
		}
		return converter;
	}

	/**
	 * 手动添加参数转换器
	 *
	 * @param converter 待添加的参数转换器
	 * @return 当前HttpRequest实例
	 */
	public HttpRequest addParameterConverter(ParameterConverter converter) {
		parameterConverters.add(converter);
		return this;
	}

	/**
	 * 手动添加参数转换器（通过类型）
	 *
	 * @param converter 转换器类型
	 * @return 当前HttpRequest实例
	 */
	public HttpRequest addParameterConverter(Class<? extends ParameterConverter> converter) {
		return addParameterConverter(config.getParameterConverter(converter));
	}

	/**
	 * 移除参数转换器
	 *
	 * @param converter 待移除的参数转换器
	 * @return 当前HttpRequest实例
	 */
	public HttpRequest removeParameterConverter(ParameterConverter converter) {
		parameterConverters.remove(converter);
		return this;
	}

	// ===================== 拦截器配置 =====================

	/**
	 * 链式添加单个请求拦截器
	 *
	 * @param interceptor 拦截器实例
	 * @return 当前HttpRequest实例
	 */
	public HttpRequest interceptor(Interceptor interceptor) {
		if (interceptor == null) {
			return this;
		}
		interceptors.add(interceptor);
		return this;
	}

	/**
	 * 链式添加单个请求拦截器（通过类型）
	 *
	 * @param interceptor 拦截器类型
	 * @return 当前HttpRequest实例
	 */
	public HttpRequest interceptor(Class<? extends Interceptor> interceptor) {
		if (interceptor == null) {
			return this;
		}
		interceptors.add(config.getInterceptor(interceptor));
		return this;
	}

	/**
	 * 链式批量添加请求拦截器
	 *
	 * @param interceptors 拦截器集合
	 * @return 当前HttpRequest实例
	 */
	public HttpRequest interceptors(Collection<Interceptor> interceptors) {
		if (interceptors == null) {
			return this;
		}
		this.interceptors.addAll(interceptors);
		return this;
	}

	// ===================== 请求头配置 =====================

	/**
	 * 链式添加单个请求头
	 *
	 * @param name  请求头名称
	 * @param value 请求头值
	 * @return 当前HttpRequest实例
	 */
	public HttpRequest addHeader(String name, String value) {
		this.headerMetas.addHeader(new HeaderMeta(name, value));
		return this;
	}

	/**
	 * 链式添加单个请求头
	 *
	 * @param headerMeta 请求头元数据
	 * @return 当前HttpRequest实例
	 */
	public HttpRequest addHeader(HeaderMeta headerMeta) {
		this.headerMetas.addHeader(headerMeta);
		return this;
	}

	/**
	 * 链式设置/覆盖单个请求头
	 *
	 * @param name  请求头名称
	 * @param value 请求头新值
	 * @return 当前HttpRequest实例
	 */
	public HttpRequest setHeader(String name, String value) {
		this.headerMetas.setHeader(name, value);
		return this;
	}

	/**
	 * 链式设置/覆盖单个请求头
	 *
	 * @param headerMeta 请求头元数据
	 * @return 当前HttpRequest实例
	 */
	public HttpRequest setHeader(HeaderMeta headerMeta) {
		this.headerMetas.setHeader(headerMeta);
		return this;
	}

	/**
	 * 链式批量添加请求头
	 *
	 * @param headerMetas 请求头元数据集合
	 * @return 当前HttpRequest实例
	 */
	public HttpRequest addHeaders(Collection<HeaderMeta> headerMetas) {
		if (headerMetas == null) {
			return this;
		}
		this.headerMetas.addHeaders(headerMetas);
		return this;
	}

	/**
	 * 批量添加请求头
	 * <p>一次性导入多个请求头元数据，适用于批量配置（如从配置文件/已有头列表导入）</p>
	 *
	 * @param headerMetas 请求头元数据集合（可为 null 或空集合：null 会抛出 {@link NullPointerException}，空集合无任何操作）
	 */
	public HttpRequest addHeaders(Map<String, String> headerMetas) {
		this.headerMetas.addHeaders(headerMetas);
		return this;
	}

	/**
	 * 添加Cookie元数据对象至集合
	 * 存在同名Cookie时，自动将新属性合并至原有Cookie；不存在则直接新增
	 *
	 * @param cookieMeta Cookie元数据对象
	 * @return 当前CookiesMeta对象本身，支持链式调用
	 */
	public HttpRequest addCookie(CookieMeta cookieMeta) {
		this.headerMetas.addCookie(cookieMeta);
		return this;
	}

	/**
	 * 快捷添加Cookie，通过名称和值构建Cookie对象并加入集合
	 * 存在同名Cookie时自动合并属性，不存在则新增
	 *
	 * @param name  Cookie名称
	 * @param value Cookie值
	 * @return 当前CookiesMeta对象本身，支持链式调用
	 */
	public HttpRequest addCookie(String name, String value) {
		this.headerMetas.addCookie(name, value);
		return this;
	}

	/**
	 * 解析单个Cookie格式字符串，转换为Cookie对象后加入集合
	 * 存在同名Cookie时自动合并属性，解析失败则忽略本次添加
	 *
	 * @param cookieStr 单个Cookie的标准格式字符串，如：token=123; Path=/; HttpOnly
	 * @return 当前CookiesMeta对象本身，支持链式调用
	 */
	public HttpRequest addCookie(String cookieStr) {
		this.headerMetas.addCookie(cookieStr);
		return this;
	}

	/**
	 * 批量解析HTTP请求头中的完整Cookie字符串，转换后批量加入集合
	 * 自动按标准分割多个Cookie，支持带特殊字符的Cookie值，解析失败的项自动忽略
	 *
	 * @param cookieHeaderStr 请求头Cookie字段的完整内容，如：token=123; userId=456; userName=张三
	 * @return 当前CookiesMeta对象本身，支持链式调用
	 */
	public HttpRequest addCookies(String cookieHeaderStr) {
		this.headerMetas.addCookies(cookieHeaderStr);
		return this;
	}

	/**
	 * 批量添加Cookie元数据对象集合至当前容器
	 * 存在同名Cookie时自动合并属性，不存在则直接新增
	 *
	 * @param cookieMetas 待添加的Cookie元数据对象集合
	 * @return 当前CookiesMeta对象本身，支持链式调用
	 */
	public HttpRequest addCookies(List<CookieMeta> cookieMetas) {
		this.headerMetas.addCookies(cookieMetas);
		return this;
	}

	/**
	 * 获取第一个匹配的请求头元数据
	 *
	 * @param name 请求头名称
	 * @return 匹配的请求头元数据
	 */
	public HeaderMeta getHeader(String name) {
		return this.headerMetas.getHeader(name);
	}

	/**
	 * 获取第一个匹配的请求头值
	 *
	 * @param name 请求头名称
	 * @return 匹配的请求头值
	 */
	public String getHeaderValue(String name) {
		return this.headerMetas.getHeaderValue(name);
	}

	/**
	 * 获取所有匹配的请求头值
	 *
	 * @param name 请求头名称
	 * @return 匹配的请求头值列表
	 */
	public List<String> getHeaderValues(String name) {
		return this.headerMetas.getHeaderValues(name);
	}

	/**
	 * 根据Cookie名称精准匹配获取对应的Cookie对象
	 *
	 * @param name Cookie名称
	 * @return 匹配到的CookieMeta对象，无匹配项时返回null
	 */
	public CookieMeta getCookie(String name) {
		return this.headerMetas.getCookie(name);
	}

	/**
	 * 获取所有的Cookie对象
	 *
	 * @return 匹配到的CookieMeta对象，无匹配项时返回null
	 */
	public List<CookieMeta> getCookie() {
		return this.headerMetas.getCookie();
	}

	/**
	 * 链式设置媒体类型分类（优先级：实例配置 > 全局配置）
	 *
	 * @param mediaTypeCategory 媒体类型分类枚举
	 * @return 当前HttpRequest实例
	 */
	public HttpRequest mediaTypeCategory(MediaTypeCategory mediaTypeCategory) {
		this.mediaTypeCategory = mediaTypeCategory;
		return this;
	}

	/**
	 * 获取媒体类型分类（优先级：实例配置 > 全局配置）
	 *
	 * @return 媒体类型分类枚举
	 */
	public MediaTypeCategory getMediaTypeCategory() {
		MediaTypeCategory mediaTypeCategory = this.mediaTypeCategory;
		if (mediaTypeCategory == null || mediaTypeCategory == MediaTypeCategory.UNKNOWN) {
			ContentType contentType = contentType();
			if (contentType != null) {
				mediaTypeCategory = contentType.getMediaTypeCategory();
			}
		}
		/**
		 * 默认媒体类型为表单格式
		 */
		if ((mediaTypeCategory == null || mediaTypeCategory == MediaTypeCategory.UNKNOWN)) {
			mediaTypeCategory = MediaTypeCategory.FORM_URLENCODED;
		}
		return mediaTypeCategory;
	}

	/**
	 * 获取Content-Type枚举
	 *
	 * @return ContentType枚举
	 */
	public ContentType contentType() {
		return ContentType.create(getContentType());
	}

	/**
	 * 获取Content-Type字符串
	 *
	 * @return Content-Type字符串
	 */
	public String getContentType() {
		return getHeaderValue(HeaderEnum.CONTENT_TYPE.getValue());
	}

	/**
	 * 链式设置Content-Type
	 *
	 * @param contentType Content-Type字符串
	 * @return 当前HttpRequest实例
	 */
	public HttpRequest contentType(String contentType) {
		return setHeader(HeaderEnum.CONTENT_TYPE.getValue(), contentType);
	}

	/**
	 * 链式设置Content-Type
	 *
	 * @param contentType ContentType枚举
	 * @return 当前HttpRequest实例
	 */
	public HttpRequest contentType(ContentType contentType) {
		return setHeader(HeaderEnum.CONTENT_TYPE.getValue(), contentType.toString());
	}

	/**
	 * 链式设置Content-Type
	 *
	 * @param contentType Content-Type字符串
	 * @return 当前HttpRequest实例
	 */
	public HttpRequest setContentType(String contentType) {
		return setHeader(HeaderEnum.CONTENT_TYPE.getValue(), contentType);
	}

	/**
	 * 链式设置Content-Type
	 *
	 * @param contentType ContentType枚举
	 * @return 当前HttpRequest实例
	 */
	public HttpRequest setContentType(ContentType contentType) {
		return setHeader(HeaderEnum.CONTENT_TYPE.getValue(), contentType.toString());
	}

	/**
	 * 删除指定名称的请求头
	 *
	 * @param name 请求头名称
	 */
	public void removeHeader(String name) {
		this.headerMetas.removeHeader(name);
	}

	// ===================== URL 元数据配置 =====================

	/**
	 * 链式设置URL协议
	 *
	 * @param scheme 协议名（http/https等）
	 * @return 当前HttpRequest实例
	 */
	public HttpRequest scheme(String scheme) {
		urlMeta.setScheme(scheme);
		return this;
	}

	/**
	 * 获取URL协议
	 *
	 * @return 协议名
	 */
	public String scheme() {
		return urlMeta.getScheme();
	}

	/**
	 * 链式设置URL基础路径
	 *
	 * @param basePath 基础路径（如/api/v1）
	 * @return 当前HttpRequest实例
	 */
	public HttpRequest basePath(String basePath) {
		urlMeta.setBasePath(basePath);
		return this;
	}

	/**
	 * 获取URL基础路径
	 *
	 * @return 基础路径
	 */
	public String basePath() {
		return urlMeta.getBasePath();
	}

	/**
	 * 链式设置URL业务路径
	 *
	 * @param path 业务路径（如/user/list）
	 * @return 当前HttpRequest实例
	 */
	public HttpRequest path(String path) {
		urlMeta.setPath(path);
		return this;
	}

	/**
	 * 获取URL业务路径
	 *
	 * @return 业务路径
	 */
	public String path() {
		return urlMeta.getPath();
	}

	/**
	 * 链式设置URL用户认证信息
	 *
	 * @param userInfo 用户信息（如admin:123456）
	 * @return 当前HttpRequest实例
	 */
	public HttpRequest userInfo(String userInfo) {
		urlMeta.setUserInfo(userInfo);
		return this;
	}

	/**
	 * 获取URL用户认证信息
	 *
	 * @return 用户信息
	 */
	public String userInfo() {
		return urlMeta.getUserInfo();
	}

	/**
	 * 链式设置URL锚点
	 *
	 * @param ref 锚点字符串
	 * @return 当前HttpRequest实例
	 */
	public HttpRequest ref(String ref) {
		urlMeta.setRef(ref);
		return this;
	}

	/**
	 * 获取URL锚点
	 *
	 * @return 锚点字符串
	 */
	public String ref() {
		return urlMeta.getRef();
	}

	/**
	 * 链式设置URL端口号
	 *
	 * @param port 端口号（如8080）
	 * @return 当前HttpRequest实例
	 */
	public HttpRequest port(int port) {
		urlMeta.setPort(port);
		return this;
	}

	/**
	 * 获取URL端口号
	 *
	 * @return 端口号
	 */
	public int port() {
		return urlMeta.getPort();
	}

	/**
	 * 链式设置URL主机名
	 *
	 * @param host 主机名（如example.com）
	 * @return 当前HttpRequest实例
	 */
	public HttpRequest host(String host) {
		urlMeta.setHost(host);
		return this;
	}

	/**
	 * 获取URL主机名
	 *
	 * @return 主机名
	 */
	public String host() {
		return urlMeta.getHost();
	}

	/**
	 * 链式设置完整URL
	 *
	 * @param url 完整URL字符串
	 * @return 当前HttpRequest实例
	 */
	public HttpRequest url(String url) {
		urlMeta.setUrl(url);
		return this;
	}

	/**
	 * 获取完整URL字符串
	 *
	 * @return 完整URL字符串
	 */
	public String url() {
		return urlMeta.toString();
	}

	/**
	 * 获取完整不带锚点的基础URL字符串
	 *
	 * @return 完整URL字符串
	 */
	public String buildUrlString() {
		return urlMeta.buildUrlString();
	}

	/**
	 * 转换为URL对象
	 *
	 * @return URL对象
	 */
	public URL toURL() {
		return urlMeta.toURL();
	}

	/**
	 * 判断是否SSL
	 */
	public boolean isSSL() {
		return urlMeta.isSSL();
	}

	// ===================== HTTP 方法配置 =====================

	/**
	 * 链式设置HTTP请求方法
	 *
	 * @param method HTTP请求方法
	 * @return 当前HttpRequest实例
	 */
	public HttpRequest method(HttpMethod method) {
		this.httpMethod = method;
		return this;
	}

	/**
	 * 获取HTTP请求方法
	 *
	 * @return HTTP请求方法
	 */
	public HttpMethod method() {
		return this.httpMethod;
	}

	/**
	 * 链式设置HTTP协议版本
	 *
	 * @param protocolVersion HTTP协议版本
	 * @return 当前HttpRequest实例
	 */
	public HttpRequest protocolVersion(ProtocolVersion protocolVersion) {
		if (protocolVersion == ProtocolVersion.NONE) {
			return this;
		}
		this.protocolVersion = protocolVersion;
		return this;
	}

	public ProtocolVersion getProtocolVersion() {
		if (protocolVersion == null) {
			return config.getProtocolVersion();
		}
		return protocolVersion;
	}

	/**
	 * 设置HTTP连接超时时间
	 */
	public HttpRequest connectTimeout(int connectTimeout) {
		this.connectTimeout = connectTimeout;
		return this;
	}

	/**
	 * 设置HTTP读取超时时间
	 */
	public HttpRequest readTimeout(int readTimeout) {
		this.readTimeout = readTimeout;
		return this;
	}

	/**
	 * 设置HTTP写入超时时间
	 */
	public HttpRequest writeTimeout(int writeTimeout) {
		this.writeTimeout = writeTimeout;
		return this;
	}

	/**
	 * 设置重试配置
	 */
	public HttpRequest retry(RetryMeta retryMeta) {
		this.retryMeta = retryMeta;
		return this;
	}

	/**
	 * 链式设置代理配置
	 */
	public HttpRequest proxy(HttpProxyMeta proxyMeta) {
		this.httpProxyMeta = proxyMeta;
		return this;
	}

	/**
	 * 链式获取代理配置
	 */
	public HttpProxyMeta proxy() {
		return this.httpProxyMeta;
	}

	/**
	 * 链式设置日志配置
	 */
	public HttpRequest log(HttpClientProperties.Log log) {
		this.log = log;
		return this;
	}

	/**
	 * 配置当前请求的SSL上下文提供者，通过指定实现类的Class对象绑定
	 *
	 * @param clazz SSL上下文提供者实现类的Class对象
	 * @return 当前HttpRequest实例，支持链式调用
	 */
	public HttpRequest ssl(Class<? extends SslContextProvider> clazz) {
		this.sslContextProvider = config.getSslContextProvider(clazz);
		return this;
	}

	/**
	 * 配置当前请求的SSL上下文提供者，直接绑定自定义的提供者实例
	 *
	 * @param sslContextProvider SSL上下文提供者实例
	 * @return 当前HttpRequest实例，支持链式调用
	 */
	public HttpRequest ssl(SslContextProvider sslContextProvider) {
		this.sslContextProvider = sslContextProvider;
		return this;
	}

	/**
	 * 获取当前请求绑定的SSL上下文提供者实例
	 *
	 * @return 已绑定的SSL上下文提供者，未配置则返回null
	 */
	public SslContextProvider ssl() {
		return this.sslContextProvider;
	}

	/**
	 * 禁用SSL证书有效性校验
	 */
	public HttpRequest disableSslValidation() {
		this.disableSslValidation = true;
		this.sslContextProvider = TrustAllSslContextProvider.INSTANCE;
		return this;
	}

	/**
	 * 启用/禁用SSL证书有效性校验
	 *
	 * @param disable true=禁用SSL校验(忽略证书/域名匹配)，false=启用严格的SSL校验规则
	 * @return 当前HttpRequest实例，支持链式调用
	 */
	public HttpRequest enableSslValidation(boolean disable) {
		this.disableSslValidation = disable;
		if (this.disableSslValidation) {
			this.sslContextProvider = TrustAllSslContextProvider.INSTANCE;
		} else if (this.sslContextProvider != null && this.sslContextProvider == TrustAllSslContextProvider.INSTANCE) {
			this.sslContextProvider = null;
		}
		return this;
	}

	// ===================== Boundary 相关方法（修正：无字段，基于请求头操作） =====================

	/**
	 * 链式设置boundary（直接更新到Content-Type请求头）
	 *
	 * @param boundary 待设置的分隔符
	 * @return 当前HttpRequest实例
	 */
	public HttpRequest setBoundary(String boundary) {
		if (StringUtil.isBlank(boundary)) {
			return this;
		}
		ContentType contentType = contentType();
		contentType.addParameter("boundary", boundary);
		this.setContentType(contentType);
		return this;
	}

	/**
	 * 从Content-Type请求头中准确截取boundary（修复截取问题）
	 * 支持格式：
	 * 1. multipart/form-data; boundary=xxx
	 * 2. multipart/form-data;boundary=xxx
	 * 3. multipart/form-data; boundary="xxx"
	 * 4. multipart/form-data; BOUNDARY=xxx（大小写不敏感）
	 *
	 * @return 截取到的纯净boundary（无引号、无多余空格），无则返回null
	 */
	public String getBoundary() {
		ContentType contentType = contentType();
		if (ObjectUtil.isEmpty(contentType) || !contentType.getMediaType().equalsIgnoreCase(ContentType.MULTIPART)) {
			return null;
		}
		return contentType.getParameter("boundary");
	}

	/**
	 * 获取或创建boundary（无字段依赖，基于请求头）
	 * 逻辑：1. 先从请求头获取；2. 获取不到则生成唯一boundary；3. 设置到请求头；4. 返回boundary
	 *
	 * @return 有效boundary字符串
	 */
	public String getOrCreateBoundary() {
		// 1. 尝试从请求头获取
		String boundary = getBoundary();
		if (StringUtil.isNotBlank(boundary)) {
			return boundary;
		}
		// 2. 生成唯一boundary（UUID保证唯一性，兼容HTTP规范）
		String generatedBoundary = "HttpRequest-" + UUID.randomUUID().toString().replace("-", "");
		// 3. 设置到请求头
		this.setBoundary(generatedBoundary);
		// 4. 返回生成的boundary
		return generatedBoundary;
	}
// ===================== 请求执行方法 =====================

	/**
	 * 执行请求并按方法元数据返回类型解析结果
	 *
	 * @return 解析后的响应结果
	 */
	public Object execute() {
		Class<?> returnClassType = methodMeta.getReturnClassType();
		if (HttpRequest.class.isAssignableFrom(returnClassType)) {
			return this;
		}
		Type returnType = methodMeta.getGenericReturnType();
		if (returnType == null) {
			returnType = methodMeta.getReturnClassType();
		}
		return execute(returnType);
	}

	/**
	 * 执行请求并按指定类型解析结果
	 *
	 * @param type 响应目标类型
	 * @param <T>  结果泛型类型
	 * @return 解析后的指定类型结果
	 */
	public <T> T execute(Type type) {
		if (type instanceof Class<?> classType) {
			if (HttpRequest.class.isAssignableFrom(classType)) {
				return (T) this;
			}
		}
		InterceptorChainImpl chain = new InterceptorChainImpl(interceptors);
		return execute(type, chain);
	}

	/**
	 * 执行请求并按指定类型解析结果（指定拦截器链）
	 *
	 * @param type  响应目标类型
	 * @param chain 拦截器链
	 * @param <T>   结果泛型类型
	 * @return 解析后的指定类型结果
	 */
	public <T> T execute(Type type, InterceptorChain chain) {
		HttpResponse response = chain.doInterceptor(this, methodMeta);
		return response.result(type);
	}

	/**
	 * 实际发起HTTP请求（拦截器链执行完毕后调用）
	 *
	 * @return HTTP原始响应对象
	 */
	public HttpResponse executeResponse() {
		HttpExecutor httpExecutor = HttpExecutorRegistry.getHttpExecutor(this);
		return httpExecutor.execute(this);
	}
}
