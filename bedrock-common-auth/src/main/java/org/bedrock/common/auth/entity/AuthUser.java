package org.bedrock.common.auth.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import org.bedrock.common.code.constant.StringPool;
import org.bedrock.common.code.util.DateUtil;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@Setter
public class AuthUser implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 令牌id
	 */
	private String tokenId;


	/**
	 * 用户id
	 */
	private Long userId;

	/**
	 * 用户名(用户账号)
	 */
	private String username;

	/**
	 * 手机号
	 */
	private String phone;

	/**
	 * 昵称
	 */
	private String nickname;

	/**
	 * 租户id
	 */
	private String tenantId;

	/**
	 * 部门id
	 */
	private List<Long> deptIds = List.of();

	/**
	 * 角色id
	 */
	private List<Long> roleIds = List.of();

	/**
	 * 权限
	 */
	private List<String> roleAlias = List.of();

	/**
	 * 登录IP地址
	 */
	private String ipaddr;

	/**
	 * 登录时间
	 */
	@JsonFormat(pattern = DateUtil.PATTERN_DATETIME)
	@DateTimeFormat(pattern = DateUtil.PATTERN_DATETIME)
	private LocalDateTime loginTime;

	/**
	 * 过期时间
	 */
	@JsonFormat(pattern = DateUtil.PATTERN_DATETIME)
	@DateTimeFormat(pattern = DateUtil.PATTERN_DATETIME)
	private LocalDateTime expireTime;

	/**
	 * 扩展数据
	 */
	private Map<String, Object> params = Map.of();

	/**
	 * 获取第一个部门id
	 */
	public Long getDeptIdFirst() {
		if (deptIds == null || deptIds.isEmpty()) {
			return -1L;
		}
		return deptIds.get(0);
	}

	/**
	 * 获取部门id
	 * 逗号分隔
	 */
	public String getDeptId() {
		return deptIds.stream().map(String::valueOf).collect(Collectors.joining(StringPool.COMMA));
	}

	/**
	 * 获取角色id
	 * 逗号分隔
	 */
	public String getRoleId() {
		return roleIds.stream().map(String::valueOf).collect(Collectors.joining(StringPool.COMMA));
	}

	/**
	 * 获取角色id
	 * 逗号分隔
	 */
	public String getRoleAliasStr() {
		return String.join(StringPool.COMMA, roleAlias);
	}
}
