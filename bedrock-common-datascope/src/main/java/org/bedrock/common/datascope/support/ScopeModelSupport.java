package org.bedrock.common.datascope.support;

import org.bedrock.common.auth.entity.AuthUser;
import org.bedrock.common.datascope.model.DataScopeModel;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Set;

public interface ScopeModelSupport {

	/**
	 * 根据mapperId获取数据权限信息
	 */
	@Nullable
	DataScopeModel selectDataScopeById(String mapperId, List<Long> roleIds);

	/**
	 * 获取部门
	 * 下级数据
	 */
	Set<Long> selectDeptChildById(Long deptId);

	/**
	 * 自定义sql 处理
	 */
	Set<Long> selectAuthHandlerUserId(DataScopeModel dataScopeModel, AuthUser authUser);
}
