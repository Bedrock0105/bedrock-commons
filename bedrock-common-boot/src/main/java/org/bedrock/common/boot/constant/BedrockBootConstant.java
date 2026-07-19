package org.bedrock.common.boot.constant;

import org.bedrock.common.auth.constant.TokenConstant;

public interface BedrockBootConstant {

	String CONTENT_LOCAL_KEY = "contentLocal";

	/**
	 * 请求ID
	 */
	String CONTENT_LOCAL_KEY_REQUEST_ID = "requestId";

	/**
	 * 租户ID
	 */
	String CONTENT_LOCAL_KEY_TENANT_ID = TokenConstant.TENANT_ID;

	/**
	 * 账号ID
	 */
	String CONTENT_LOCAL_KEY_ACCOUNT_ID = "accountId";
}
