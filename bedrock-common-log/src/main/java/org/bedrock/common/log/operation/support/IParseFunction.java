package org.bedrock.common.log.operation.support;

public interface IParseFunction {

	/**
	 * 函数名称
	 *
	 * @return
	 */
	String functionName();

	/**
	 * 执行函数
	 *
	 * @param value
	 * @return
	 */
	String apply(Object value);

}
