package org.bedrock.common.datascope.support;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bedrock.common.auth.entity.AuthUser;
import org.bedrock.common.datascope.handler.AuthHandler;
import org.bedrock.common.datascope.model.DataScopeModel;
import org.bedrock.common.code.constant.CacheConstant;
import org.bedrock.common.code.util.CacheUtil;
import org.bedrock.common.code.util.CollectionUtil;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@RequiredArgsConstructor
public class DefaultScopeModelSupport implements ScopeModelSupport {

	public static final String SCOPE_AUTH_HANDLER_PREFIX = "scope:auth:handler:userid:";

	public static final String SCOPE_DEPT_CHILD_PREFIX = "scope:dept:child:id:";

	public static final String SCOPE_DATA_SCOPE_MAPPER_ROLE_PREFIX = "scope:data:scope:roleid:";

	private final JdbcClient jdbcClient;

	/**
	 * 数据权限处理器
	 */
	private final AuthHandler authHandler;

	/**
	 * 查询数据权限的sql
	 */
	@Setter
	// language=SQL
	private String selectDataScopeByIdSql = """
		select bpd.mapper_id as mapperId,bpd.scope_type as scopeType,
		       bpd.scope_column as scopeColumn,bpd.scope_field as scopeField,
		       bpd.scope_value as scopeValue
		from bedrock_role_permission brp
			inner join bedrock_permission_datascope bpd on brp.permission_id = bpd.id
		where  brp.role_id = ? and brp.perm_type = 2 and bpd.is_deleted = 0
		""";

	/**
	 * 查询部门子部门的sql
	 */
	@Setter
	// language=SQL
	private String selectDateChildByIdSql = "select id from bedrock_dept where ancestors like concat('%',?,'%')";

	@Override
	public DataScopeModel selectDataScopeById(String mapperId, List<Long> roleIds) {
		for (Long roleId : roleIds) {
			List<DataScopeModel> models = CacheUtil.get(CacheConstant.SCOPE_CACHE,
				SCOPE_DATA_SCOPE_MAPPER_ROLE_PREFIX + roleId,
				() -> jdbcClient.sql(selectDataScopeByIdSql)
					.param(roleId)
					.query(DataScopeModel.class)
					.list());
			if (CollectionUtil.isNotEmpty(models)) {
				Optional<DataScopeModel> first = models
					.stream()
					.filter(model -> model.getMapperId().equals(mapperId))
					.findFirst();
				if (first.isPresent()) {
					return first.get();
				}
			}
		}
		return null;
	}

	@Override
	public Set<Long> selectDeptChildById(Long deptId) {
		return CacheUtil.get(CacheConstant.SCOPE_CACHE, SCOPE_DEPT_CHILD_PREFIX + deptId,
			() -> jdbcClient.sql(selectDateChildByIdSql)
				.param(deptId)
				.query(Long.class)
				.set());
	}

	@Override
	public Set<Long> selectAuthHandlerUserId(DataScopeModel dataScopeModel, AuthUser authUser) {
		return CacheUtil.get(CacheConstant.SCOPE_CACHE, SCOPE_AUTH_HANDLER_PREFIX + authUser.getUserId(), () -> authHandler.getAuthScope(dataScopeModel, authUser));
	}
}
