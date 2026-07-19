package org.bedrock.common.authentication.config;

import lombok.RequiredArgsConstructor;
import org.bedrock.common.authentication.endpoint.TokenEndpoint;
import org.bedrock.common.authentication.handler.TokenCreateHandler;
import org.bedrock.common.authentication.handler.TokenEnhancerHandler;
import org.bedrock.common.authentication.handler.impl.JwtTokenCreateHandler;
import org.bedrock.common.authentication.handler.impl.JwtTokenEnhancerHandler;
import org.bedrock.common.authentication.provider.CompositeTokenGranter;
import org.bedrock.common.authentication.provider.TokenGranter;
import org.bedrock.common.authentication.service.ClientDetailsService;
import org.bedrock.common.authentication.service.Impl.ClientDetailsServiceImpl;
import org.bedrock.common.authentication.service.UserDetailsService;
import org.bedrock.common.authentication.support.PasswordEncoder;
import org.bedrock.common.code.util.DigestUtil;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.util.*;

@AutoConfiguration
@RequiredArgsConstructor
public class AuthenticationConfiguration {

	private final JdbcClient jdbcClient;

	@Bean
	@ConditionalOnMissingBean(PasswordEncoder.class)
	public PasswordEncoder passwordEncoder() {
		return rawPassword -> DigestUtil.md5Hex(DigestUtil.sha512Hex(DigestUtil.sha1(rawPassword)) + rawPassword);
	}

	@Bean
	@ConditionalOnMissingBean
	public ClientDetailsService clientDetailsService() {
		return new ClientDetailsServiceImpl(jdbcClient);
	}

	@Bean
	@ConditionalOnMissingBean
	public TokenCreateHandler tokenCreateHandler() {
		return new JwtTokenCreateHandler();
	}

	@Bean(name = "jwtTokenEnhancerHandler")
	@ConditionalOnMissingBean(name = "jwtTokenEnhancerHandler")
	public TokenEnhancerHandler jwtTokenEnhancerHandler(TokenCreateHandler tokenCreateHandler) {
		return new JwtTokenEnhancerHandler(tokenCreateHandler);
	}

	/**
	 * 默认授权服务配置
	 */
	@Bean(name = "defaultAuthorizationServerConfigurer")
	@ConditionalOnMissingBean(name = "defaultAuthorizationServerConfigurer")
	public AuthorizationServerConfigurer defaultAuthorizationServerConfigurer(ObjectProvider<List<TokenEnhancerHandler>> tokenEnhancerHandler,
																			  UserDetailsService userDetailsService,
																			  PasswordEncoder passwordEncoder) {
		return new DefaultAuthorizationServerConfigurer(tokenEnhancerHandler, userDetailsService, passwordEncoder);
	}

	@Bean
	@ConditionalOnMissingBean
	public TokenGranter tokenGranter(List<AuthorizationServerConfigurer> configurer) {
		List<TokenGranter> tokenGranters = new ArrayList<>();
		configurer.sort(Comparator.comparingInt(AuthorizationServerConfigurer::getOrder));
		for (AuthorizationServerConfigurer authorizationServerConfigurer : configurer) {
			authorizationServerConfigurer.configure(tokenGranters);
		}
		return new CompositeTokenGranter(tokenGranters);
	}

	@Bean
	@ConditionalOnMissingBean(TokenEndpoint.class)
	public TokenEndpoint tokenEndpoint(TokenGranter tokenGranter,
									   ClientDetailsService clientDetailsService) {
		return new TokenEndpoint(tokenGranter, clientDetailsService);
	}
}
