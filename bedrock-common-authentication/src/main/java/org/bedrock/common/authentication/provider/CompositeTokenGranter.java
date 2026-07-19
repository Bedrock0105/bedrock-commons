package org.bedrock.common.authentication.provider;

import lombok.RequiredArgsConstructor;
import org.bedrock.common.authentication.model.OauthTokenParams;
import org.bedrock.common.authentication.model.OauthTokenResult;
import org.bedrock.common.authentication.support.ClientDetails;

import java.util.List;

@RequiredArgsConstructor
public class CompositeTokenGranter implements TokenGranter {

	private final List<TokenGranter> tokenGranters;

	@Override
	public OauthTokenResult grant(String grantType, OauthTokenParams params, ClientDetails clientDetails) {
		for (TokenGranter tokenGranter : tokenGranters) {
			OauthTokenResult result = tokenGranter.grant(grantType, params, clientDetails);
			if (result != null) {
				return result;
			}
		}
		return null;
	}
}
