package org.bedrock.common.log.operation.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OperationLogModel {

	/**
	 * @return 方法执行成功后的日志模版
	 */
	private String success;

	/**
	 * @return 方法执行失败后的日志模版
	 */
	private String fail;

	/**
	 * @return 操作日志的类型，比如：订单类型、商品类型
	 */
	private String type;

	/**
	 * @return 日志的子类型，比如订单的C端日志，和订单的B端日志，type都是订单类型，但是子类型不一样
	 */
	private String subType;

	/**
	 * @return 日志的额外信息
	 */
	private String extra;

	/**
	 * @return 是否记录日志
	 */
	private String condition;
}
