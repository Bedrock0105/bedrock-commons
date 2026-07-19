package org.bedrock.common.security.expression;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bedrock.common.auth.util.AuthUtil;
import org.bedrock.common.code.constant.CacheConstant;
import org.bedrock.common.code.util.*;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
public class SecurityExpressionExpressionRoot {

	public static final String API_PERMISSION_PREFIX = "api:permission:role:id:";

	/**
	 * 查询接口权限的sql
	 */
	// language=SQL
	@Setter
	private String selectApiPermissionSql = """
		select bpa.permission, bpa.path, bpa.method
		from bedrock_role_permission brp
			inner join bedrock_permission_api bpa on brp.permission_id = bpa.id
		where   brp.role_id = ? and brp.perm_type = 1 and bpa.is_deleted = 0
		""";

	private final JdbcClient jdbcClient;

	/**
	 * 是否有权限
	 */
	public boolean hasPermission() {
		String requestUri = WebUtil.getRequestUri();
		String requestMethod = WebUtil.getRequestMethod();
		return getApiPermissions()
			.stream()
			.filter(permission -> requestMethod.equalsIgnoreCase(permission.getMethod()))
			.anyMatch(permission -> AntPathUtil.match(permission.getPath(), requestUri));
	}

	/**
	 * 是否有权限
	 *
	 * @param permission 权限编号
	 * @return
	 */
	public boolean hasPermission(String permission) {
		String requestMethod = WebUtil.getRequestMethod();
		return getApiPermissions()
			.stream()
			.filter(apiPermission -> requestMethod.equalsIgnoreCase(apiPermission.getMethod()))
			.anyMatch(apiPermission -> apiPermission.getPermission().equals(permission));
	}

	/**
	 * 是否有权限
	 *
	 * @param permissions 权限编号
	 * @return
	 */
	public boolean hasAnyPermission(String... permissions) {
		String requestMethod = WebUtil.getRequestMethod();
		List<String> list = Arrays.asList(permissions);
		return getApiPermissions()
			.stream()
			.filter(apiPermission -> requestMethod.equalsIgnoreCase(apiPermission.getMethod()))
			.anyMatch(apiPermission -> list.contains(apiPermission.getPermission()));
	}

	/**
	 * 是否有当前角色
	 */
	public boolean hasRole(String role) {
		return AuthUtil.getRoleAlias().contains(role);
	}

	/**
	 * 是否有当前角色
	 */
	public boolean hasAnyRole(String... roles) {
		List<String> roleAlias = AuthUtil.getRoleAlias();
		return Arrays.stream(roles)
			.filter(StringUtil::isNotBlank)
			.anyMatch(roleAlias::contains);
	}

	/**
	 * Always grants access.
	 */
	public boolean permitAll() {
		return true;
	}

	/**
	 * Always denies access
	 */
	public boolean denyAll() {
		return false;
	}

	/**
	 * 获取当前用户的权限
	 *
	 * @return
	 */
	private List<ApiPermission> getApiPermissions() {
		List<Long> roleIds = AuthUtil.getRoleIds();
		if (roleIds.isEmpty()) {
			return Collections.emptyList();
		}
		List<ApiPermission> apiPermissions = new ArrayList<>();
		for (Long roleId : roleIds) {
			List<ApiPermission> permissions = CacheUtil.get(CacheConstant.API_CACHE,
				API_PERMISSION_PREFIX + roleId,
				() -> jdbcClient.sql(selectApiPermissionSql)
					.param(roleId)
					.query(ApiPermission.class)
					.list());
			if (CollectionUtil.isNotEmpty(permissions)) {
				apiPermissions.addAll(permissions);
			}
		}

		return apiPermissions;
	}

	/**
	 * 获取权限信息
	 */
	@Getter
	@Setter
	public static class ApiPermission implements Serializable {

		/**
		 * 接口唯一标识（建议格式：模块:功能:操作，如 user:info:query）
		 */
		private String permission;

		/**
		 * 接口路径（如：/api/v1/user/{id}）
		 */
		private String path;

		/**
		 * HTTP请求方法（GET/POST/PUT/DELETE等）
		 */
		private String method;
	}
}
