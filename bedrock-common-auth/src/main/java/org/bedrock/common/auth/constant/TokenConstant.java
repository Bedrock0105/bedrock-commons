package org.bedrock.common.auth.constant;

import org.springframework.http.HttpHeaders;

public interface TokenConstant {

	String TENANT_ID = "tenantId";

	String AUTH_AUTHORIZATION = HttpHeaders.AUTHORIZATION;

	String AUTH_TOKEN = HttpHeaders.AUTHORIZATION;

	String AUTH_BEARER = "Bearer";

	String TOKEN_TYPE = "token_type";

	/**
	 * 用户id
	 */
	String USERID = "userId";

	/**
	 * 用户名
	 */
	String USERNAME = "username";

	/**
	 * 手机号
	 */
	String PHONE = "phone";

	/**
	 * 昵称
	 */
	String NICKNAME = "nickname";

	/**
	 * 部门id
	 */
	String DEPT_IDS = "deptIds";

	/**
	 * 角色id
	 */
	String ROLE_IDS = "roleIds";

	/**
	 * 权限
	 */
	String ROLE_ALIAS = "roleAlias";

	/**
	 * 登录IP地址
	 */
	String IPADDR = "ipaddr";

	/**
	 * 附件参数
	 */
	String PARAMS = "params";
}
