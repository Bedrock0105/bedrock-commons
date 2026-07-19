package org.bedrock.common.code.support;

import org.bedrock.common.code.util.CollectionUtil;

public interface ResultCode {

	/**
	 * 获取状态码
	 *
	 * @return int
	 */
	int getCode();

	/**
	 * 获取状态描述信息
	 *
	 * @return String
	 */
	String getMessage();

	/**
	 * 格式化描述信息（支持占位符 %s）
	 *
	 * @param args 可变参数
	 * @return String
	 */
	default String formatMessage(Object... args) {
		if (CollectionUtil.isEmpty(args)) {
			return getMessage();
		}
		return String.format(getMessage(), args);
	}
}
