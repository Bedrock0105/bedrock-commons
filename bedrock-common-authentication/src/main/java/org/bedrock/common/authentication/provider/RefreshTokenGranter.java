package org.bedrock.common.authentication.provider;

import io.jsonwebtoken.Claims;
import org.bedrock.common.auth.constant.TokenConstant;
import org.bedrock.common.auth.util.JwtUtil;
import org.bedrock.common.authentication.exception.TokenRefreshFailedException;
import org.bedrock.common.authentication.handler.TokenEnhancerHandler;
import org.bedrock.common.authentication.model.OauthTokenParams;
import org.bedrock.common.authentication.model.OauthTokenResult;
import org.bedrock.common.authentication.service.UserDetailsService;
import org.bedrock.common.authentication.support.ClientDetails;
import org.bedrock.common.authentication.support.UserDetails;
import org.springframework.lang.Nullable;

import java.util.List;

public class RefreshTokenGranter extends AbstractTokenGranter {

	private static final String REFRESH_TOKEN = "refresh_token";

	public RefreshTokenGranter(@Nullable List<TokenEnhancerHandler> tokenEnhancerHandlers,
							   UserDetailsService userDetailsService) {
		super(tokenEnhancerHandlers, userDetailsService, REFRESH_TOKEN);
	}

	@Override
	protected OauthTokenResult getAccessToken(ClientDetails client, OauthTokenParams params) {
		Claims claims = JwtUtil.parsePayload(params.getRefreshToken());
		if (claims == null) {
			throw new TokenRefreshFailedException();
		}
		if (!OauthTokenResult.REFRESH_TOKEN.equals(claims.get(OauthTokenResult.TOKEN_TYPE))) {
			throw new TokenRefreshFailedException();
		}
		String username = claims.get(TokenConstant.USERNAME, String.class);
		UserDetails userDetails = userDetailsService.loadUserByUsername(username, params);
		return createToken(client, userDetails, params);
	}
}
