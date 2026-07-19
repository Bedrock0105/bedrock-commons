package org.bedrock.common.authentication.endpoint;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.bedrock.common.auth.constant.TokenConstant;
import org.bedrock.common.authentication.exception.ClientIdMissingException;
import org.bedrock.common.authentication.exception.ClientSecretMismatchException;
import org.bedrock.common.authentication.exception.GrantTypeMissingException;
import org.bedrock.common.authentication.exception.InvalidGrantTypeException;
import org.bedrock.common.authentication.model.OauthTokenParams;
import org.bedrock.common.authentication.model.OauthTokenResult;
import org.bedrock.common.authentication.provider.TokenGranter;
import org.bedrock.common.authentication.service.ClientDetailsService;
import org.bedrock.common.authentication.support.ClientDetails;
import org.bedrock.common.spi.configuration.AutoIgnore;
import org.bedrock.common.code.api.R;
import org.bedrock.common.code.util.Base64Util;
import org.bedrock.common.code.util.StringUtil;
import org.bedrock.common.code.util.WebUtil;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 就是个用户密码登录
 */
@AutoIgnore
@RestController
@Tag(name = "用户登录")
@RequiredArgsConstructor
public class TokenEndpoint {

	private final TokenGranter tokenGranter;

	private final ClientDetailsService clientDetailsService;

	public static final String AUTHENTICATION_SCHEME_BASIC = "Basic";

	@Operation(summary = "获取token")
	@PostMapping("/oauth/token")
	public R postAccessToken(OauthTokenParams params) {
		if (!StringUtils.hasText(params.getGrantType())) {
			throw new GrantTypeMissingException();
		}
		OauthTokenResult grant = tokenGranter.grant(params.getGrantType(), params, getClientDetails());
		if (grant == null) {
			throw new InvalidGrantTypeException();
		}
		return R.success(grant);
	}

	public ClientDetails getClientDetails() {
		String header = WebUtil.getHeader(TokenConstant.AUTH_TOKEN);
		if (StringUtil.isBlank(header)) {
			throw new ClientIdMissingException();
		}
		header = header.trim();
		if (!header.startsWith(AUTHENTICATION_SCHEME_BASIC)) {
			throw new ClientIdMissingException();
		}
		String token = Base64Util.decode(header.substring(6));
		int delim = token.indexOf(":");
		if (delim == -1) {
			throw new ClientIdMissingException();
		}
		String clientId = token.substring(0, delim);
		String clientSecret = token.substring(delim + 1);
		ClientDetails clientDetails = clientDetailsService.loadClientByClientId(clientId);
		if (clientDetails.isSecretRequired() || !clientDetails.getClientSecret().equals(clientSecret)) {
			throw new ClientSecretMismatchException();
		}
		return clientDetails;
	}
}
