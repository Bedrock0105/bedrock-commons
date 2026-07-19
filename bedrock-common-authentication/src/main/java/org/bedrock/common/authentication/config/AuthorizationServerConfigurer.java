package org.bedrock.common.authentication.config;

import org.bedrock.common.authentication.provider.TokenGranter;
import org.springframework.core.Ordered;

import java.util.List;

/**
 * 动态添加配置
 */
public interface AuthorizationServerConfigurer extends Ordered {

	/**
	 * 添加授权服务
	 */
	default void configure(List<TokenGranter> tokenGranters) {
	}
}
