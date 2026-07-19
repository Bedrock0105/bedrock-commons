package org.bedrock.common.authentication.handler;

import org.bedrock.common.authentication.model.OauthTokenResult;
import org.bedrock.common.authentication.support.ClientDetails;
import org.bedrock.common.authentication.support.UserDetails;

/**
 * token创建器接口
 */
public interface TokenCreateHandler {

	/**
	 * 创建token
	 * 生成token的参数是{@link org.bedrock.common.authentication.model.OauthTokenResult#getAdditionalInformation()}
	 *
	 * @param client      客户端
	 * @param userDetails 用户信息
	 * @return
	 */
	void accessToken(OauthTokenResult tokenResult, ClientDetails client, UserDetails userDetails);

	/**
	 * 创建刷新token
	 * 生成token的参数是{@link org.bedrock.common.authentication.model.OauthTokenResult#getAdditionalInformation()}
	 *
	 * @param client
	 * @param userDetails
	 * @return
	 */
	void refreshToken(OauthTokenResult tokenResult, ClientDetails client, UserDetails userDetails);
}
