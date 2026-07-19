package org.bedrock.common.datascope.handler;

import org.bedrock.common.auth.entity.AuthUser;
import org.bedrock.common.datascope.model.DataScopeModel;
import org.springframework.lang.Nullable;

import java.util.Set;

/**
 * 获取要执行的sql
 */
public interface AuthHandler {

	/**
	 * 自定义权限处理器
	 * 返回如果是空 则是所有的权限
	 *
	 * @param dataScopeModel 权限模型
	 * @param authUser       当前操作用户
	 */
	// language=SQL
	@Nullable
	Set<Long> getAuthScope(DataScopeModel dataScopeModel, AuthUser authUser);
}
