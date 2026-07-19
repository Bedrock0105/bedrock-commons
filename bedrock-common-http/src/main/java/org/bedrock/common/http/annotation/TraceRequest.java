package org.bedrock.common.http.annotation;

import org.bedrock.common.http.converter.Converter;
import org.bedrock.common.http.converter.ParameterConverter;
import org.bedrock.common.http.enums.HttpMethod;
import org.bedrock.common.http.enums.MediaTypeCategory;
import org.bedrock.common.http.enums.ProtocolVersion;
import org.bedrock.common.http.interceptor.Interceptor;
import org.bedrock.common.http.ssl.SslContextProvider;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * GET请求专用注解（继承{Request}注解，默认指定GET请求方式）
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Request(method = HttpMethod.TRACE)
public @interface TraceRequest {

	/**
	 * 请求地址（与value互为别名，二选一配置即可）
	 * <p>支持两种占位符：Spring环境变量占位符${app.api.path}、方法参数动态占位符{userId}</p>
	 * <p>最终请求路径 = 类上url + 类上basePath + 当前方法url</p>
	 *
	 * @return 方法级请求地址/路径
	 */
	@AliasFor(annotation = Request.class)
	String url() default "";

	/**
	 * 请求地址（与url互为别名，简化书写）
	 */
	@AliasFor(annotation = Request.class)
	String value() default "";

	/**
	 * 请求内容类型 Content-Type
	 * <p>示例：application/json、application/x-www-form-urlencoded、multipart/form-data</p>
	 * <p>优先级高于类上全局配置，为空则沿用全局配置</p>
	 *
	 * @return 内容类型字符串，默认空
	 */
	@AliasFor(annotation = Request.class)
	String contentType() default "";

	/**
	 * 请求头配置
	 * <p>配置当前方法的专属请求头，会与类上全局请求头合并，同名请求头方法配置覆盖全局</p>
	 *
	 * @return 请求头配置集合，默认空配置
	 */
	@AliasFor(annotation = Request.class)
	Headers headers() default @Headers({});

	/**
	 * 请求媒体类型分类
	 * <p>用于快速归类请求体的解析规则，与contentType强关联，辅助框架识别请求格式</p>
	 *
	 * @return 媒体类型分类枚举，默认UNKNOWN表示自动识别
	 */
	@AliasFor(annotation = Request.class)
	MediaTypeCategory mediaTypeCategory() default MediaTypeCategory.UNKNOWN;

	/**
	 * 连接超时时间，单位：毫秒
	 * <p>默认值：-1 表示沿用全局HttpConfiguration的连接超时配置</p>
	 * <p>配置大于0的数值则覆盖全局配置，生效当前方法</p>
	 *
	 * @return 连接超时时间（ms）
	 */
	@AliasFor(annotation = Request.class)
	int connectTimeout() default -1;

	/**
	 * 读取超时时间，单位：毫秒
	 * <p>默认值：-1 表示沿用全局HttpConfiguration的读取超时配置</p>
	 * <p>配置大于0的数值则覆盖全局配置，生效当前方法</p>
	 *
	 * @return 读取超时时间（ms）
	 */
	@AliasFor(annotation = Request.class)
	int readTimeout() default -1;

	/**
	 * 写入超时时间，单位：毫秒
	 * <p>默认值：-1 表示沿用全局HttpConfiguration的写入超时配置</p>
	 * <p>配置大于0的数值则覆盖全局配置，生效当前方法</p>
	 *
	 * @return 写入超时时间（ms）
	 */
	@AliasFor(annotation = Request.class)
	int writeTimeout() default -1;

	/**
	 * 自定义请求参数转换器
	 * <p>请求前置处理，用于格式化/转换当前方法的入参，优先级高于全局转换器</p>
	 * <p>执行链路：参数转换 → 进入拦截器 → 发送请求</p>
	 *
	 * @return 参数转换器实现类数组，默认空数组
	 */
	@AliasFor(annotation = Request.class)
	Class<? extends ParameterConverter>[] parameterConverters() default {};

	/**
	 * 自定义请求拦截器
	 * <p>包裹当前请求的执行链路，可实现请求签名、日志打印、参数预处理、响应结果拦截等逻辑</p>
	 * <p>执行链路：参数转换完成后进入拦截器，优先级高于全局拦截器</p>
	 *
	 * @return 拦截器实现类数组，默认空数组
	 */
	@AliasFor(annotation = Request.class)
	Class<? extends Interceptor>[] interceptors() default {};

	/**
	 * 请求响应数据转换器
	 * <p>请求后置处理，用于将服务端响应结果转换为方法的返回值类型</p>
	 * <p>优先级高于全局转换器，默认Converter.class表示沿用全局配置</p>
	 *
	 * @return 响应转换器实现类
	 */
	@AliasFor(annotation = Request.class)
	Class<? extends Converter> converter() default Converter.class;

	/**
	 * HTTP请求协议版本
	 * <p>默认值：NONE 表示沿用全局HttpConfiguration的协议版本配置</p>
	 *
	 * @return 协议版本枚举，默认自动适配
	 */
	@AliasFor(annotation = Request.class)
	ProtocolVersion protocolVersion() default ProtocolVersion.NONE;

	/**
	 * 指定当前请求的SSL上下文证书提供者
	 * <p>优先级高于类上配置，为空则沿用全局SSL策略</p>
	 *
	 * @return SSL上下文提供者实现类
	 */
	@AliasFor(annotation = Request.class)
	Class<? extends SslContextProvider> sslContextProvider() default SslContextProvider.class;

	/**
	 * 是否禁用当前请求的SSL证书有效性校验
	 * true=忽略证书合法性、域名匹配等所有SSL校验，适用于内网/测试环境自签名证书
	 * false=启用严格的SSL证书校验规则，生产环境推荐默认值
	 * <p>优先级高于类上配置，全局禁用则此配置失效</p>
	 *
	 * @return 是否禁用SSL验证，默认false
	 */
	@AliasFor(annotation = Request.class)
	String disableSslValidation() default "";
}
