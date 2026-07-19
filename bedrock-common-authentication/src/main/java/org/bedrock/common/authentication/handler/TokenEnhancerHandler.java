package org.bedrock.common.authentication.handler;

import org.bedrock.common.authentication.model.OauthTokenParams;
import org.bedrock.common.authentication.model.OauthTokenResult;
import org.bedrock.common.authentication.support.ClientDetails;
import org.bedrock.common.authentication.support.UserDetails;
import org.springframework.core.Ordered;

/**
 * token增强处理
 */
public interface TokenEnhancerHandler extends Ordered {

	/**
	 * token增强处理
	 *
	 * @param tokenResult 令牌结果
	 * @param client 客户端信息
	 * @param userDetails 用户信息
	 * @param params 请求参数
	 */
	OauthTokenResult enhance(OauthTokenResult tokenResult, ClientDetails client, UserDetails userDetails, OauthTokenParams params);

	@Override
	default int getOrder() {
		return HIGHEST_PRECEDENCE;
	}
}
