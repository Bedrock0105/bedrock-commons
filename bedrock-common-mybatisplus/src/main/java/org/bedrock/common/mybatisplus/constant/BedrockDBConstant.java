package org.bedrock.common.mybatisplus.constant;

public interface BedrockDBConstant {

	/**
	 * 删除状态[0:正常,1:删除]
	 */
	Integer DB_NOT_DELETED = 0;
	Integer DB_IS_DELETED = 1;

	/**
	 * 状态[0:禁用,1:正常]
	 */
	Integer DB_STATUS_NORMAL = 1;
	Integer DB_STATUS_DISABLE = 0;

	/**
	 * 顶级父级ID
	 */
	Long DB_TOP_PARENT_ID = 0L;
}
