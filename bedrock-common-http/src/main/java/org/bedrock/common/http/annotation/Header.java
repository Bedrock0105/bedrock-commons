package org.bedrock.common.http.annotation;

import java.lang.annotation.*;

/**
 * HTTP 请求头参数注解
 * <p>核心作用：标记方法参数为 HTTP 请求头的取值来源，仅作用于方法参数上；
 * 支持动态占位符替换、默认值兜底，适配不同场景的请求头配置需求。</p>
 * <p>注解特性：
 * 1. 运行时保留（RetentionPolicy.RUNTIME），可通过反射解析注解信息；
 * 2. 仅作用于方法参数（ElementType.PARAMETER），类/方法等位置不生效；
 * 3. 包含完整文档说明（Documented），生成API文档时会包含该注解。
 * </p>
 *
 * @see Param 方法参数名称/值关联注解
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Header {

	/**
	 * HTTP 请求头名称（如 Content-Type、Authorization、Token 等）
	 * <p>使用规范：
	 * </br>
	 * 1. 建议显式指定，避免参数名称与实际请求头名不一致导致解析异常；
	 * </br>
	 * 2. 若未指定（默认空字符串），会把参数当成对象进行转换map，并使用对象属性名称作为请求头名称
	 * </p>
	 *
	 * @return 请求头名称，默认空字符串
	 * @example // 显式指定请求头名称为 Token
	 * public void getUser(@Header(name = "Token") String token) {}
	 */
	String name() default "";

	/**
	 * 请求头值
	 * <h3>一、占位符替换规则（核心特性）</h3>
	 * <table border="1">
	 *   <tr>
	 *     <th>占位符类型</th>
	 *     <th>语法示例</th>
	 *     <th>替换逻辑</th>
	 *     <th>支持级别</th>
	 *   </tr>
	 *   <tr>
	 *     <td>Spring 环境占位符</td>
	 *     <td>${app.token}</td>
	 *     <td>Spring 环境下替换为配置项/环境变量值</td>
	 *     <td>类级别、方法级别均支持</td>
	 *   </tr>
	 *   <tr>
	 *     <td>方法参数占位符</td>
	 *     <td>{userId}</td>
	 *     <td>替换为方法参数值（参数需用 {@link Param} 注解标识 或 匹配参数名称）</td>
	 *     <td>仅方法级别支持；类级别不执行替换，占位符原样保留</td>
	 *   </tr>
	 * </table>
	 *
	 * <h3>二、值优先级规则（注解标记在方法参数上时）</h3>
	 * <ol>
	 *   <li>优先级1：当前注解标记的参数的实际值（非空时）；</li>
	 *   <li>优先级2：参数值为空（null/空白字符串）时，使用当前属性配置的 defaultValue（含占位符替换后的值）。</li>
	 * </ol>
	 *
	 * @return 请求头默认值，默认空字符串
	 * @example // 示例1：Spring环境占位符 + 默认值兜底
	 * @Header(name = "Token", defaultValue = "${app.default.token}") String token
	 * <p>
	 * // 示例2：方法参数占位符 + 默认值兜底
	 * @Header(name = "UserId", defaultValue = "{userId}") String userId
	 * <p>
	 * // 示例3：参数值为空时，使用默认值
	 * // 若参数 token 为 null，请求头 Token 的值为 "default-token-123"
	 * @Header(name = "Token", defaultValue = "default-token-123") String token
	 */
	String defaultValue() default "";
}
