package org.bedrock.common.authentication.provider;

import jakarta.annotation.Nullable;
import org.bedrock.common.authentication.exception.PasswordMismatchException;
import org.bedrock.common.authentication.handler.TokenEnhancerHandler;
import org.bedrock.common.authentication.model.OauthTokenParams;
import org.bedrock.common.authentication.model.OauthTokenResult;
import org.bedrock.common.authentication.service.UserDetailsService;
import org.bedrock.common.authentication.support.ClientDetails;
import org.bedrock.common.authentication.support.PasswordEncoder;
import org.bedrock.common.authentication.support.UserDetails;

import java.util.List;

public class PasswordTokenGranter extends AbstractTokenGranter {

	private final PasswordEncoder passwordEncoder;

	private static final String GRANT_TYPE = "password";

	public PasswordTokenGranter(@Nullable List<TokenEnhancerHandler> tokenEnhancerHandlers,
								UserDetailsService userDetailsService,
								PasswordEncoder passwordEncoder) {
		super(tokenEnhancerHandlers, userDetailsService, GRANT_TYPE);
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	protected OauthTokenResult getAccessToken(ClientDetails client, OauthTokenParams params) {
		UserDetails userDetails = getUserDetails(params);
		if (!passwordEncoder.matches(params.getPassword(), userDetails.getPassword())) {
			throw new PasswordMismatchException();
		}
		return createToken(client, userDetails, params);
	}
}
