package org.bedrock.common.datascope.config;

import lombok.RequiredArgsConstructor;
import org.bedrock.common.datascope.handler.AuthHandler;
import org.bedrock.common.datascope.plugins.DataScopeInnerInterceptor;
import org.bedrock.common.datascope.props.DataScopeProperties;
import org.bedrock.common.datascope.support.DataPermissionSqlProcessor;
import org.bedrock.common.datascope.support.DefaultDataPermissionSqlProcessor;
import org.bedrock.common.datascope.support.DefaultScopeModelSupport;
import org.bedrock.common.datascope.support.ScopeModelSupport;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.util.Set;

@AutoConfiguration
@RequiredArgsConstructor
@EnableConfigurationProperties(DataScopeProperties.class)
public class DataScopeConfiguration {

	private final DataScopeProperties dataScopeProperties;

	private final JdbcClient jdbcClient;

	@Bean
	@ConditionalOnMissingBean
	public AuthHandler authHandler() {
		return (dataScopeModel, authUser) -> Set.of();
	}

	@Bean
	@ConditionalOnMissingBean
	public ScopeModelSupport scopeModelSupport(AuthHandler authHandler) {
		return new DefaultScopeModelSupport(jdbcClient, authHandler);
	}

	@Bean
	@ConditionalOnMissingBean
	public DataPermissionSqlProcessor dataPermissionSqlProcessor(ScopeModelSupport scopeModelSupport) {
		return new DefaultDataPermissionSqlProcessor(scopeModelSupport);
	}

	@Bean("dataScopeInnerInterceptor")
	@ConditionalOnMissingBean(name = "dataScopeInnerInterceptor")
	public DataScopeInnerInterceptor dataScopeInnerInterceptor(DataPermissionSqlProcessor dataPermissionSqlProcessor) {
		return new DataScopeInnerInterceptor(dataScopeProperties, dataPermissionSqlProcessor);
	}


}
