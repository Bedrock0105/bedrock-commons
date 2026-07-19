package org.bedrock.common.authentication.handler.impl;

import org.bedrock.common.auth.constant.TokenConstant;
import org.bedrock.common.auth.util.JwtUtil;
import org.bedrock.common.authentication.handler.TokenCreateHandler;
import org.bedrock.common.authentication.model.OauthTokenResult;
import org.bedrock.common.authentication.support.ClientDetails;
import org.bedrock.common.authentication.support.UserDetails;

import java.util.HashMap;
import java.util.Map;

public class JwtTokenCreateHandler implements TokenCreateHandler {

	@Override
	public void accessToken(OauthTokenResult tokenResult, ClientDetails client, UserDetails userDetails) {
		// 令牌id
		String uuid = tokenResult.getAccessToken();
		Map<String, Object> map = new HashMap<>(tokenResult.getAdditionalInformation());
		map.computeIfAbsent(TokenConstant.USERNAME, (k) -> userDetails.getUsername());
		map.computeIfAbsent(OauthTokenResult.TOKEN_TYPE, (k) -> tokenResult.getTokenType());
		map.computeIfAbsent(OauthTokenResult.SCOPE, (k) -> tokenResult.getScope());
		String token = JwtUtil.createToken(uuid, map, client.getAccessTokenValiditySeconds());
		tokenResult.setAccessToken(token);
		tokenResult.setExpireTime(System.currentTimeMillis() + client.getAccessTokenValiditySeconds() * 1000);
	}

	@Override
	public void refreshToken(OauthTokenResult tokenResult, ClientDetails client, UserDetails userDetails) {
		// 令牌id
		String uuid = tokenResult.getRefreshToken();
		/**
		 * 刷新token只需要一个username 即可
		 */
		Map<String, Object> map = Map.of(TokenConstant.USERNAME, userDetails.getUsername(),
			OauthTokenResult.TOKEN_TYPE, OauthTokenResult.REFRESH_TOKEN,
			OauthTokenResult.SCOPE, tokenResult.getScope());
		String token = JwtUtil.createToken(uuid, map, client.getRefreshTokenValiditySeconds());
		tokenResult.setRefreshToken(token);
		tokenResult.setRefreshExpireTime(System.currentTimeMillis() + client.getAccessTokenValiditySeconds() * 1000);
	}
}
