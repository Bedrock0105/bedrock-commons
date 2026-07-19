package org.bedrock.common.security.config;

import lombok.RequiredArgsConstructor;
import org.bedrock.common.security.aspect.PrePermissionAop;
import org.bedrock.common.security.expression.SecurityExpressionExpressionRoot;
import org.bedrock.common.security.props.SecurityProperties;
import org.bedrock.common.code.annotation.BedrockPropertySource;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.simple.JdbcClient;

@AutoConfiguration
@RequiredArgsConstructor
@EnableConfigurationProperties(SecurityProperties.class)
@BedrockPropertySource("classpath:/security.yml")
public class SecurityConfiguration {


	private final JdbcClient jdbcClient;

	@Bean
	public SecurityExpressionExpressionRoot securityExpressionExpressionRoot() {
		return new SecurityExpressionExpressionRoot(jdbcClient);
	}

	@Bean
	public PrePermissionAop prePermissionAop(SecurityExpressionExpressionRoot securityExpressionExpressionRoot) {
		return new PrePermissionAop(securityExpressionExpressionRoot);
	}
}
