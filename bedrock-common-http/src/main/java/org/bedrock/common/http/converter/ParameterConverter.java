package org.bedrock.common.http.converter;

import org.bedrock.common.http.annotation.Param;
import org.springframework.core.MethodParameter;
import org.springframework.core.Ordered;
import org.springframework.lang.Nullable;

/**
 * 参数转换器接口
 * <p>核心职责：
 * 1. 适配 HTTP 场景下各类参数的类型转换（聚焦基本数据类型）；
 * 2. 支持优先级控制（继承 {@link Ordered}），可自定义转换器执行顺序。
 * </p>
 * <p>核心规则：
 * 1. 若转换器匹配当前参数类型，执行自定义转换逻辑；
 * 2. 若未匹配到对应转换器，默认调用参数值的 {@link Object#toString()} 方法转换为字符串；
 * 3. 特殊类型（如 {@link java.util.Date}、{@link java.time.LocalDateTime}）无自定义转换器时，直接返回其 toString() 结果。
 * </p>
 *
 * @see Ordered 优先级控制接口，用于定义转换器执行顺序
 * @see MethodParameter 方法参数元数据载体
 */
public interface ParameterConverter extends Ordered {

	/**
	 * 判断当前转换器是否支持处理指定的方法参数
	 * <p>核心作用：参数转换前置校验，避免无效转换器执行，提升转换效率。</p>
	 *
	 * @param parameter 方法参数元数据（包含参数类型、注解、名称等信息）
	 * @return true=当前转换器支持该参数转换，false=不支持
	 * @example // 示例：判断是否支持 LocalDateTime 类型参数
	 * boolean supports = converter.supportsParameter(new MethodParameter(method, 0));
	 */
	boolean supportsParameter(@Nullable MethodParameter parameter, String key, Object value);

	/**
	 * 执行参数转换，将参数值转换为字符串（适配 HTTP 场景的最终传输格式）
	 *
	 * @param parameter 方法参数元数据（包含参数类型、注解等上下文信息）
	 * @param key       参数关联的标识键，分两种场景：
	 *                  <ul>
	 *                    <li>模板替换场景：{name} 中的「name」（或 {@link Param} 注解指定的值）；</li>
	 *                    <li>Query/Body/Header 参数场景：Query/Body/Header 注解指定的参数名称；</li>
	 *                  </ul>
	 * @param value     待转换的原始参数值（可为 null，需自行处理空值逻辑）
	 * @return 转换后的字符串：null 值建议返回空字符串，非空按转换器规则转换
	 * @example // 示例：将 LocalDateTime 转换为 yyyy-MM-dd HH:mm:ss 格式字符串
	 * String result = converter.resolveArgument(parameter, "createTime", LocalDateTime.now());
	 */
	String resolveArgument(@Nullable MethodParameter parameter, String key, Object value);

	/**
	 * 获取转换器优先级（默认最高优先级）
	 * <p>优先级规则：数值越小，执行优先级越高；多个转换器匹配时，优先执行高优先级转换器。</p>
	 *
	 * @return 优先级数值，默认 {@link Ordered#HIGHEST_PRECEDENCE}（最高优先级）
	 */
	default int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE;
	}
}
