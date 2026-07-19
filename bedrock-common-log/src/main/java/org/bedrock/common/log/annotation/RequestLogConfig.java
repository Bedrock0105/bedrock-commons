package org.bedrock.common.log.annotation;

import org.bedrock.common.log.props.LogProperties;

import java.lang.annotation.*;

/**
 * 请求日志描述
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestLogConfig {

	/**
	 * 日志记录级别
	 * <p>指定日志输出的级别（如INFO/DEBUG/ERROR），决定日志在不同环境下的输出策略</p>
	 *
	 * @return 日志级别枚举，默认{@link LogProperties.LogLevel#INFO}
	 */
	LogProperties.LogLevel level() default LogProperties.LogLevel.INFO;

	/**
	 * 日志内容每行的最大字符长度
	 * <p>当日志内容单行长度超过该值时，会自动截断并添加省略标识，避免日志内容过长</p>
	 *
	 * @return 最大长度值，默认{@link Integer#MAX_VALUE}（不限制长度）
	 */
	int maxLength() default Integer.MAX_VALUE;

	/**
	 * 排除日志记录的处理器名称列表
	 * <p>指定不需要打印日志的Handler名称（支持模糊匹配或全类名匹配），
	 * 匹配的Handler将跳过所有日志记录逻辑</p>
	 *
	 * @return 排除的Handler名称数组，默认空数组（不排除任何Handler）
	 */
	String[] excludedHandlers() default {};

	/**
	 * 是否打印处理器（Handler）的生命周期日志
	 * <p>控制是否记录Handler的进入、执行中、退出等生命周期环节的日志，
	 * 包含Handler名称、处理阶段等信息</p>
	 *
	 * @return true（默认）打印Handler日志，false不打印
	 */
	boolean handlersLog() default true;

	/**
	 * 是否打印目标方法的入参日志
	 * <p>控制是否记录方法调用时的参数信息，包含参数名称、参数类型及参数值（敏感信息需自行脱敏）</p>
	 *
	 * @return true（默认）打印方法参数日志，false不打印
	 */
	boolean methodParamsLog() default true;

	/**
	 * 是否打印方法返回的状态日志
	 * <p>控制是否记录方法执行完成后的返回状态（如"方法执行成功"/"方法抛出异常"），
	 * 不包含具体返回数据内容</p>
	 *
	 * @return true（默认）打印返回状态日志，false不打印
	 */
	boolean methodReturnLog() default true;

	/**
	 * 是否打印方法返回值的具体内容日志
	 * <p>在{@link #methodReturnLog()}为true的前提下，控制是否记录方法返回的具体数据内容，
	 * 适用于需要查看返回结果详情的场景</p>
	 *
	 * @return true（默认）打印返回值内容，false仅打印返回状态不打印内容
	 */
	boolean methodReturnMsgLog() default true;
}
