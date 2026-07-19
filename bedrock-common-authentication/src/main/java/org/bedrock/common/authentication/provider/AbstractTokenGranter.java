package org.bedrock.common.authentication.provider;

import lombok.NonNull;
import org.bedrock.common.authentication.exception.ClientNotGrantTypeException;
import org.bedrock.common.authentication.exception.UsernameNotEnabledException;
import org.bedrock.common.authentication.exception.UsernameNotFoundException;
import org.bedrock.common.authentication.handler.TokenEnhancerHandler;
import org.bedrock.common.authentication.model.OauthTokenParams;
import org.bedrock.common.authentication.model.OauthTokenResult;
import org.bedrock.common.authentication.service.UserDetailsService;
import org.bedrock.common.authentication.support.ClientDetails;
import org.bedrock.common.authentication.support.UserDetails;
import org.bedrock.common.code.util.CollectionUtil;
import org.bedrock.common.code.util.StringUtil;
import org.springframework.lang.Nullable;

import java.util.*;

/**
 * 认证抽象类
 */
public abstract class AbstractTokenGranter implements TokenGranter {

	/**
	 * 令牌增强处理
	 */
	protected final List<TokenEnhancerHandler> tokenEnhancerHandlers;

	protected final UserDetailsService userDetailsService;

	private final String grantType;

	public AbstractTokenGranter(@Nullable List<TokenEnhancerHandler> tokenEnhancerHandlers,
								UserDetailsService userDetailsService,
								String grantType) {
		if (CollectionUtil.isNotEmpty(tokenEnhancerHandlers)) {
			tokenEnhancerHandlers.sort(Comparator.comparingInt(TokenEnhancerHandler::getOrder));
		}
		this.tokenEnhancerHandlers = tokenEnhancerHandlers;
		this.userDetailsService = userDetailsService;
		this.grantType = grantType;
	}

	@Override
	public OauthTokenResult grant(String grantType, OauthTokenParams params, ClientDetails clientDetails) {
		if (!this.grantType.equals(grantType)) {
			return null;
		}
		validateGrantType(grantType, clientDetails);
		return getAccessToken(clientDetails, params);
	}

	/**
	 * 创建令牌
	 * 可以重写次方法，来实现自己的认证
	 */
	protected OauthTokenResult getAccessToken(ClientDetails client, OauthTokenParams params) {
		return createToken(client, getUserDetails(params), params);
	}

	/**
	 * 创建令牌
	 */
	protected OauthTokenResult createToken(ClientDetails client, UserDetails userDetails, OauthTokenParams params) {
		if (userDetails == null) {
			throw new UsernameNotFoundException();
		}
		if (!userDetails.isEnabled()) {
			throw new UsernameNotEnabledException();
		}
		OauthTokenResult result = new OauthTokenResult();
		Set<String> scope = client.getScope();
		if (StringUtil.isNotBlank(params.getScope())) {
			scope = StringUtil.commaDelimitedListToSet(params.getScope());
		}
		String accessTokenUuid = UUID.randomUUID().toString();
		result.setAccessToken(accessTokenUuid);
		String refreshTokenUuid = UUID.randomUUID().toString();
		result.setRefreshToken(refreshTokenUuid);
		result.setScope(scope);
		return enhance(result, client, userDetails, params);
	}

	/**
	 * 令牌增强
	 */
	protected OauthTokenResult enhance(OauthTokenResult tokenResult,
									   ClientDetails client,
									   UserDetails userDetails,
									   OauthTokenParams params) {
		OauthTokenResult result = tokenResult;
		if (CollectionUtil.isNotEmpty(tokenEnhancerHandlers)) {
			for (TokenEnhancerHandler tokenEnhancerHandler : this.tokenEnhancerHandlers) {
				result = tokenEnhancerHandler.enhance(result, client, userDetails, params);
			}
		}
		return result;
	}

	/**
	 * 获取用户信息
	 */
	@NonNull
	protected UserDetails getUserDetails(OauthTokenParams params) {
		return userDetailsService.loadUserByUsername(params.getUsername(), params);
	}

	protected void validateGrantType(String grantType, ClientDetails clientDetails) {
		Collection<String> authorizedGrantTypes = clientDetails.getAuthorizedGrantTypes();
		if (authorizedGrantTypes != null && !authorizedGrantTypes.isEmpty()
			&& !authorizedGrantTypes.contains(grantType)) {
			throw new ClientNotGrantTypeException();
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		AbstractTokenGranter that = (AbstractTokenGranter) o;
		return Objects.equals(grantType, that.grantType);
	}

	@Override
	public int hashCode() {
		return Objects.hash(grantType);
	}
}
