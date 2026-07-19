package org.bedrock.common.datascope.enums;

/**
 * 权限类型枚举
 */
public enum DataScopeTypeEnum {
	/**
	 * 全部数据权限
	 */
	DATA_SCOPE_ALL,

	/**
	 * 自定数据权限
	 * where 语句
	 */
	DATA_SCOPE_CUSTOM,

	/**
	 * 部门数据权限
	 */
	DATA_SCOPE_DEPT,

	/**
	 * 部门及以下数据权限
	 */
	DATA_SCOPE_DEPT_AND_CHILD,

	/**
	 * 仅本人数据权限
	 */
	DATA_SCOPE_SELF,

	/**
	 * 自定义权限
	 * 完成sql
	 */
	DATA_SCOPE_CUSTOM_AUTH;

}
