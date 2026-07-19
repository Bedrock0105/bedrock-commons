package org.bedrock.common.datascope.support;

import org.bedrock.common.auth.entity.AuthUser;
import org.bedrock.common.datascope.model.DataScopeModel;
import org.springframework.lang.Nullable;

/**
 * sql权限 处理
 * 抽离出来，方便扩展
 */
@FunctionalInterface
public interface DataPermissionSqlProcessor {

	/**
	 * sql权限处理
	 */
	@Nullable
	String sqlCondition(DataScopeModel dataScope, AuthUser authUser, String originalSql);
}
