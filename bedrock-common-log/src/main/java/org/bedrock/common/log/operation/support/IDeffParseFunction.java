package org.bedrock.common.log.operation.support;

public interface IDeffParseFunction {

	/**
	 * 函数名称
	 *
	 * @return
	 */
	String functionName();

	/**
	 * 执行函数
	 *
	 * @return
	 */
	String deff(Object newValue, Object oldValue);

	/**
	 * 执行函数
	 *
	 * @return
	 */
	String deff(Object newValue);

}
