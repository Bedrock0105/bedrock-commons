package org.bedrock.common.authentication.handler.impl;

import lombok.RequiredArgsConstructor;
import org.bedrock.common.authentication.handler.TokenCreateHandler;
import org.bedrock.common.authentication.handler.TokenEnhancerHandler;
import org.bedrock.common.authentication.model.OauthTokenParams;
import org.bedrock.common.authentication.model.OauthTokenResult;
import org.bedrock.common.authentication.support.ClientDetails;
import org.bedrock.common.authentication.support.UserDetails;

@RequiredArgsConstructor
public class JwtTokenEnhancerHandler implements TokenEnhancerHandler {

	private final TokenCreateHandler tokenCreateHandler;

	@Override
	public OauthTokenResult enhance(OauthTokenResult tokenResult, ClientDetails client, UserDetails userDetails, OauthTokenParams params) {
		tokenCreateHandler.accessToken(tokenResult, client, userDetails);
		tokenCreateHandler.refreshToken(tokenResult, client, userDetails);
		return tokenResult;
	}

	@Override
	public int getOrder() {
		return LOWEST_PRECEDENCE - 100000;
	}
}
