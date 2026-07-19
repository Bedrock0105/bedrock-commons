package org.bedrock.common.authentication.support;

/**
 * 用户信息
 */
public interface UserDetails {

	/**
	 * 获取用户名
	 *
	 * @return
	 */
	String getUsername();

	/**
	 * 获取密码
	 */
	String getPassword();

	/**
	 * 获取用户是否启用
	 */
	boolean isEnabled();
}
