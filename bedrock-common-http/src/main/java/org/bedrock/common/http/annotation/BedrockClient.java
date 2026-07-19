package org.bedrock.common.http.annotation;

import org.bedrock.common.http.converter.Converter;
import org.bedrock.common.http.converter.ParameterConverter;
import org.bedrock.common.http.enums.ProtocolVersion;
import org.bedrock.common.http.interceptor.Interceptor;
import org.bedrock.common.http.ssl.SslContextProvider;

import java.lang.annotation.*;

/**
 * Bedrock HTTP客户端标识注解
 * 标注该注解的接口类会被Spring扫描并自动注册为HTTP客户端Bean，实现接口自动代理为HTTP请求客户端
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface BedrockClient {

	/**
	 * 完整请求根地址（核心必填属性，优先级最高）
	 * <p>示例：https://api.example.com:8080、http://127.0.0.1:9090</p>
	 * <p>Spring Boot环境下支持${app.token}形式的占位符解析</p>
	 *
	 * @return 完整的HTTP请求根地址（协议+域名/IP+端口）
	 */
	String url() default "";

	/**
	 * 请求基础路径（地址关联属性，与url强绑定）
	 * <p>示例：/api/v1/user、/order/service，要求以/开头</p>
	 * <p>最终请求完整路径 = url + basePath + 接口方法上配置的路径</p>
	 * <p>Spring Boot环境下支持${app.token}形式的占位符解析</p>
	 *
	 * @return 端口后的统一路径前缀
	 */
	String basePath() default "";

	/**
	 * 请求内容类型（Content-Type，高频基础配置）
	 * <p>示例：application/json、application/x-www-form-urlencoded、multipart/form-data</p>
	 * <p>配置后所有接口请求都会携带该请求头</p>
	 *
	 * @return Content-Type请求头值
	 */
	String contentType() default "";

	/**
	 * 全局请求头配置（所有接口请求均会统一携带）
	 *
	 * @return 全局请求头配置集合
	 */
	Headers headers() default @Headers({});

	/**
	 * 自定义请求参数转换器（请求前置处理，按执行链路排序在前）
	 * 配置后会替换/增强默认的参数转换规则，处理接口入参的格式化与转换
	 *
	 * @return 参数转换器实现类数组
	 */
	Class<? extends ParameterConverter>[] parameterConverters() default {};

	/**
	 * 自定义请求拦截器（请求前后的增强逻辑，包裹请求执行）
	 * 可实现请求签名、日志打印、参数统一处理、响应结果预处理等自定义逻辑
	 *
	 * @return 拦截器实现类数组
	 */
	Class<? extends Interceptor>[] interceptors() default {};

	/**
	 * 响应数据转换器（请求后置处理，按执行链路排序在后）
	 * 用于将服务端响应结果转换为接口方法的返回值类型，默认使用内置全局转换器
	 *
	 * @return 响应转换器实现类，默认值为标记类不生效
	 */
	Class<? extends Converter> converter() default Converter.class;

	/**
	 * HTTP请求协议版本配置（高级网络配置）
	 *
	 * @return 协议版本枚举，默认NONE自动适配
	 */
	ProtocolVersion protocolVersion() default ProtocolVersion.NONE;

	/**
	 * 指定SSL上下文证书提供者（SSL相关配置，与下方属性强关联）
	 * 配置自定义的SSL证书信任策略，适配HTTPS请求的证书校验规则
	 *
	 * @return SSL上下文提供者实现类
	 */
	Class<? extends SslContextProvider> sslContextProvider() default SslContextProvider.class;

	/**
	 * 是否禁用SSL证书有效性校验（SSL兜底配置，紧跟SSL提供者）
	 * true=忽略证书合法性、域名匹配等所有SSL校验，适用于内网/测试环境自签名证书
	 * false=启用严格的SSL证书校验规则，生产环境推荐默认值
	 *
	 * @return 是否禁用SSL验证，默认false
	 */
	String disableSslValidation() default "false";
}
