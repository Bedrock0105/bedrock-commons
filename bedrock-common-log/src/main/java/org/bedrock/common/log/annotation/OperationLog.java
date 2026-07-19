package org.bedrock.common.log.annotation;

import org.bedrock.common.log.operation.support.IParseFunction;
import org.bedrock.common.log.operation.support.LogRecordContext;

import java.lang.annotation.*;

/**
 * 操作日志配置接口
 * 定义方法执行成功/失败时的日志模板、日志类型、记录条件等核心配置，支持动态解析与自定义扩展。
 *
 * <h3>通用语法规则（所有方法通用）</h3>
 * 1. <b>Spring EL表达式支持</b>：需用 {{ }} 包裹表达式，示例：{{#param.name}}（获取方法参数param的name属性）
 * 2. <b>内置变量</b>：
 * - #_errorMsg：方法执行失败时的错误对象（仅fail()/condition()在失败场景下可用）
 * - #_ret：方法执行后的返回对象（success()/fail()/condition()/extra()均可用）
 * 3. <b>上下文变量扩展</b>：
 * 通过 {@link LogRecordContext#putVariable(String, Object)} 手动添加变量，使用时通过 #key 引用，示例：
 * LogRecordContext.putVariable("userId", 123); → 模板中用 {{#userId}} 获取值
 * 4. <b>自定义对象转换器</b>：
 * 实现 {@link IParseFunction} 接口并交给Spring容器管理，通过唯一的 functionName 调用，格式：
 * {functionName{变量}} → 示例：{TO_JSON{#param}}（TO_JSON为IParseFunction的functionName返回值，#param会传入apply方法）
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Repeatable(OperationLogs.class)
public @interface OperationLog {

	/**
	 * @return 方法执行成功后的日志模板（必填，不可为空）
	 * 示例："用户{{#param.username}}成功创建订单，订单号：{{#_ret.orderNo}}"
	 */
	String success();

	/**
	 * @return 方法执行失败后的日志模板（默认空字符串，为空时不记录失败日志）
	 * 示例："用户{{#param.username}}创建订单失败，原因：{{#_errorMsg.message}}"
	 */
	String fail() default "";

	/**
	 * @return 操作日志的一级类型（必填，用于日志分类，如"订单操作"、"商品管理"）
	 */
	String type();

	/**
	 * @return 操作日志的二级子类型（默认空字符串，用于细分类型，如同一"订单操作"下的"C端下单"、"B端改单"）
	 */
	String subType() default "";

	/**
	 * @return 日志的额外补充信息（默认空字符串，用于存储非核心但需保留的信息）
	 */
	String extra() default "";

	/**
	 * @return 是否记录日志的条件（默认空字符串，空时默认记录日志）
	 * <b>注意</b>：表达式必须返回 boolean 类型，true=记录日志，false=不记录
	 * 示例："{{#param.needLog == true}}"（仅当方法参数param的needLog为true时记录日志）
	 */
	String condition() default "";

}
