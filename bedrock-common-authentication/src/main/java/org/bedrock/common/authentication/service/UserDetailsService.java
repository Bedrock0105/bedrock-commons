package org.bedrock.common.authentication.service;

import org.bedrock.common.authentication.model.OauthTokenParams;
import org.bedrock.common.authentication.support.UserDetails;
import org.springframework.lang.NonNull;

public interface UserDetailsService {

	/**
	 * 查找用户信息
	 *
	 * @param username 账号
	 * @param params   登录参数
	 * @return 用户信息
	 */
	@NonNull
	UserDetails loadUserByUsername(String username, OauthTokenParams params);
}
