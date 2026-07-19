package org.bedrock.common.auth.util;

import io.jsonwebtoken.Claims;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import org.bedrock.common.auth.constant.RoleAliasConstant;
import org.bedrock.common.auth.constant.TokenConstant;
import org.bedrock.common.auth.entity.AuthUser;
import org.bedrock.common.code.constant.StringPool;
import org.bedrock.common.code.util.*;
import org.slf4j.MDC;
import org.springframework.lang.NonNull;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class AuthUtil {

	public final static String AUTH_USER = "LocalAuthorizationUser";

	/**
	 * 获取当前用户 信息
	 *
	 * @return
	 */
	@Nullable
	public static AuthUser getAuthUser() {
		return getAuthUser(WebUtil.getRequest());
	}

	/**
	 * 获取当前用户id
	 *
	 * @return
	 */

	public static Long getUserId() {
		AuthUser authUser = getAuthUser();
		return authUser == null ? -1L : authUser.getUserId();
	}

	/**
	 * 用户名(用户账号)
	 *
	 * @return
	 */
	public static String getUsername() {
		AuthUser authUser = getAuthUser();
		return authUser == null ? StringPool.EMPTY : authUser.getUsername();
	}

	/**
	 * 获取当前用户手机号码
	 *
	 * @return
	 */
	public static String getPhone() {
		AuthUser authUser = getAuthUser();
		return authUser == null ? StringPool.EMPTY : authUser.getPhone();
	}

	/**
	 * 获取当前用户昵称
	 *
	 * @return
	 */
	public static String getNickname() {
		AuthUser authUser = getAuthUser();
		return authUser == null ? StringPool.EMPTY : authUser.getNickname();
	}

	/**
	 获取当前上下文的租户 ID
	 <p>获取逻辑（优先级从高到低）：
	 <ul>
	 <li>1. 从MDC（日志上下文）中获取租户ID，键为{@link TokenConstant#TENANT_ID}
	 注：MDC 中的租户 ID 由请求头解析后设置（即优先使用请求头传递的租户标识）</li>
	 <li>2. 若MDC中未获取到（请求头无租户信息），则调用{@link #getLoginTenantId()}获取当前登录用户关联的租户ID</li>
	 </ul>
	 <p>返回规则：
	 <ul>
	 <li>若上述步骤均未获取到有效租户ID，返回空字符串（{@link StringPool#EMPTY}）</li>
	 </ul>
	 <p>适用场景：
	 <ul>
	 <li>需优先使用请求头指定的租户ID，请求头无信息时降级使用登录用户所属租户的场景</li>
	 <li>区别于{@link #getLoginTenantId()}：本方法可能返回请求头指定的非登录用户租户ID，而后者仅返回登录用户绑定的租户ID</li>
	 </ul>
	 @return 租户 ID（非 null，获取不到时返回空字符串）
	 @see TokenConstant#TENANT_ID
	 @see #getLoginTenantId()
	 */
	public static String getTenantId() {
		String tenantId = MDC.get(TokenConstant.TENANT_ID);
		if (StringUtil.isBlank(tenantId)) {
			return getLoginTenantId();
		}
		return StringUtil.isBlank(tenantId) ? StringPool.EMPTY : tenantId;
	}

	/**
	 * 获取当前登录人的租户ID
	 */
	public static String getLoginTenantId() {
		String tenantId = Optional.ofNullable(getAuthUser())
			.map(AuthUser::getTenantId)
			.filter(StringUtil::isNotBlank) // 过滤空字符串或空格
			.orElse(null);
		return StringUtil.isBlank(tenantId) ? StringPool.EMPTY : tenantId;
	}

	/**
	 * 获取当前用户部门ID
	 *
	 * @return
	 */
	public static List<Long> getDeptIds() {
		AuthUser authUser = getAuthUser();
		return authUser == null ? List.of() : authUser.getDeptIds();
	}

	/**
	 * 获取当前用户部门ID
	 *
	 * @return
	 */
	public static Long getDeptIdFirst() {
		AuthUser authUser = getAuthUser();
		return authUser == null ? -1L : authUser.getDeptIdFirst();
	}

	public static String getDeptId() {
		AuthUser authUser = getAuthUser();
		return authUser == null ? StringPool.EMPTY : authUser.getDeptId();
	}

	/**
	 * 获取当前用户角色ID
	 *
	 * @return
	 */
	public static List<Long> getRoleIds() {
		AuthUser authUser = getAuthUser();
		return authUser == null ? List.of() : authUser.getRoleIds();
	}

	public static String getRoleId() {
		AuthUser authUser = getAuthUser();
		return authUser == null ? StringPool.EMPTY : authUser.getRoleId();

	}

	/**
	 * 获取当前用户权限
	 *
	 * @return
	 */
	public static List<String> getRoleAlias() {
		AuthUser authUser = getAuthUser();
		return authUser == null ? List.of() : authUser.getRoleAlias();
	}

	/**
	 * 获取当前用户权限
	 */
	public static String getRoleAliasStr() {
		AuthUser authUser = getAuthUser();
		return authUser == null ? StringPool.EMPTY : authUser.getRoleAliasStr();
	}

	/**
	 * 获取当前tokenID
	 *
	 * @return
	 */
	public static String getTokenId() {
		AuthUser authUser = getAuthUser();
		return authUser == null ? StringPool.EMPTY : authUser.getTokenId();
	}

	/**
	 * 获取当前用户登录ip
	 *
	 * @return
	 */
	public static String getIpaddr() {
		AuthUser authUser = getAuthUser();
		return authUser == null ? StringPool.EMPTY : authUser.getIpaddr();
	}

	/**
	 * 获取当前用户登录时间
	 *
	 * @return
	 */
	@Nullable
	public static LocalDateTime getLoginTime() {
		AuthUser authUser = getAuthUser();
		return authUser == null ? null : authUser.getLoginTime();
	}

	/**
	 * 获取当前用户登录过期时间
	 *
	 * @return
	 */
	@Nullable
	public static LocalDateTime getExpireTime() {
		AuthUser authUser = getAuthUser();
		return authUser == null ? null : authUser.getExpireTime();
	}

	/**
	 * 获取当前用户其他信息
	 *
	 * @return
	 */
	public static Map<String, Object> getParams() {
		AuthUser authUser = getAuthUser();
		return authUser == null ? Map.of() : authUser.getParams();
	}

	/**
	 * 判断当前用户是否为管理员
	 *
	 * @return
	 */
	public static boolean isAdmin() {
		return isRole(RoleAliasConstant.ROLE_ADMIN);
	}

	/**
	 * 获取当前用户是否为超级管理员
	 *
	 * @return
	 */
	public static boolean isAdministrator() {
		return isRole(RoleAliasConstant.ROLE_ADMINISTRATOR);
	}

	/**
	 * 判断当前用户是否为指定角色
	 */
	public static boolean isRole(String role) {
		return AuthUtil.getRoleAlias().contains(role);
	}

	/**
	 * 获取当前登录用户
	 */
	public static String getToken(HttpServletRequest request) {
		if (request == null) {
			return null;
		}
		String token = request.getHeader(TokenConstant.AUTH_TOKEN);
		if (!StringUtil.hasText(token)) {
			token = request.getParameter(TokenConstant.AUTH_TOKEN);
		}
		return getToken(token);
	}

	/**
	 * 获取当前登录用户
	 */
	public static String getToken(String token) {
		if (!StringUtil.hasText(token)) {
			return token;
		}
		if (token.startsWith(TokenConstant.AUTH_BEARER) || token.startsWith(TokenConstant.AUTH_BEARER.toLowerCase())) {
			token = token.substring(TokenConstant.AUTH_BEARER.length() + 1);
		}
		return token;
	}

	/**
	 * 获取当前登录用户
	 */
	@Nullable
	public static AuthUser getAuthUser(HttpServletRequest request) {
		AuthUser authUser = ThreadLocalUtil.get(AUTH_USER);
		if (authUser == null) {
			String token = getToken(request);
			if (!StringUtil.hasText(token)) {
				return null;
			}
			authUser = getAuthUser(token);
			if (authUser == null) {
				return null;
			}
			ThreadLocalUtil.set(AUTH_USER, authUser);
		}
		return authUser;
	}

	/**
	 * 获取当前登录用户
	 */
	@Nullable
	public static AuthUser getAuthUser(String token) {
		Claims claims = JwtUtil.parsePayload(token);
		if (claims == null) {
			return null;
		}
		if (!TokenConstant.AUTH_BEARER.equalsIgnoreCase(claims.get(TokenConstant.TOKEN_TYPE, String.class))) {
			return null;
		}
		return getAuthUser(claims);
	}

	/**
	 * 获取当前登录用户
	 */
	@NonNull
	@SuppressWarnings("unchecked")
	public static AuthUser getAuthUser(Claims claims) {
		AuthUser authUser = new AuthUser();
		authUser.setTokenId(claims.getId());
		authUser.setExpireTime(DateUtil.toLocalDateTime(claims.getExpiration()));
		authUser.setLoginTime(DateUtil.toLocalDateTime(claims.getNotBefore()));
		authUser.setUserId(claims.get(TokenConstant.USERID, Long.class));
		authUser.setUsername(claims.get(TokenConstant.USERNAME, String.class));
		authUser.setPhone(claims.get(TokenConstant.PHONE, String.class));
		authUser.setNickname(claims.get(TokenConstant.NICKNAME, String.class));
		authUser.setTenantId(claims.get(TokenConstant.TENANT_ID, String.class));
		authUser.setIpaddr(claims.get(TokenConstant.IPADDR, String.class));
		String deptIds = claims.get(TokenConstant.DEPT_IDS, String.class);
		if (StringUtil.isNotBlank(deptIds)) {
			authUser.setDeptIds(NumberUtil.toListLong(deptIds));
		}
		String roleIds = claims.get(TokenConstant.ROLE_IDS, String.class);
		if (StringUtil.isNotBlank(roleIds)) {
			authUser.setRoleIds(NumberUtil.toListLong(roleIds));
		}
		String roleAlias = claims.get(TokenConstant.ROLE_ALIAS, String.class);
		if (StringUtil.isNotBlank(roleAlias)) {
			authUser.setRoleAlias(StringUtil.toListStr(roleAlias));
		}
		Map<String, Object> map = claims.get(TokenConstant.PARAMS, Map.class);
		if (CollectionUtil.isNotEmpty(map)) {
			authUser.setParams(Collections.unmodifiableMap(map));
		}
		return authUser;
	}
}
