package org.bedrock.common.authentication.config;

import lombok.RequiredArgsConstructor;
import org.bedrock.common.authentication.handler.TokenEnhancerHandler;
import org.bedrock.common.authentication.provider.PasswordTokenGranter;
import org.bedrock.common.authentication.provider.RefreshTokenGranter;
import org.bedrock.common.authentication.provider.TokenGranter;
import org.bedrock.common.authentication.service.UserDetailsService;
import org.bedrock.common.authentication.support.PasswordEncoder;
import org.springframework.beans.factory.ObjectProvider;

import java.util.List;

@RequiredArgsConstructor
public class DefaultAuthorizationServerConfigurer implements AuthorizationServerConfigurer {

	private final ObjectProvider<List<TokenEnhancerHandler>> tokenEnhancerHandler;

	private final UserDetailsService userDetailsService;

	private final PasswordEncoder passwordEncoder;

	@Override
	public void configure(List<TokenGranter> tokenGranters) {
		/**
		 * 密码授权
		 */
		PasswordTokenGranter passwordTokenGranter = new PasswordTokenGranter(
			tokenEnhancerHandler.getIfAvailable(), userDetailsService, passwordEncoder);
		/**
		 * 刷新token
		 */
		RefreshTokenGranter refreshTokenGranter = new RefreshTokenGranter(
			tokenEnhancerHandler.getIfAvailable(), userDetailsService);
		tokenGranters.add(passwordTokenGranter);
		tokenGranters.add(refreshTokenGranter);
	}

	@Override
	public int getOrder() {
		return 0;
	}
}
