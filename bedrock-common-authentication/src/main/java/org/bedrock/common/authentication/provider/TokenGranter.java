package org.bedrock.common.authentication.provider;

import org.bedrock.common.authentication.model.OauthTokenParams;
import org.bedrock.common.authentication.model.OauthTokenResult;
import org.bedrock.common.authentication.support.ClientDetails;

/**
 * 认证核心类
 */
public interface TokenGranter {

	/**
	 * 获取token
	 *
	 * @param grantType     授权类型
	 * @param params        参数
	 * @param clientDetails 客户端信息
	 * @return token
	 */
	OauthTokenResult grant(String grantType, OauthTokenParams params, ClientDetails clientDetails);
}
