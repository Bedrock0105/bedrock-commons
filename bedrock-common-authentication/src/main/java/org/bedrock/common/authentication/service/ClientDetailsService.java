package org.bedrock.common.authentication.service;

import org.bedrock.common.authentication.support.ClientDetails;

/**
 * 查询客户端
 */
public interface ClientDetailsService {

	/**
	 * 根据客户端ID查询客户端
	 *
	 * @param clientId 客户端ID
	 * @return 客户端
	 */
	ClientDetails loadClientByClientId(String clientId);
}
