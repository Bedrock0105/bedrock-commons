package org.bedrock.common.datascope.config;

import com.alibaba.druid.filter.FilterEventAdapter;
import lombok.RequiredArgsConstructor;
import org.bedrock.common.datascope.aspect.DataAuthFilterAspect;
import org.bedrock.common.datascope.plugins.DruidDataScopeAuthInterceptor;
import org.bedrock.common.datascope.props.DataScopeProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass(FilterEventAdapter.class)
@RequiredArgsConstructor
public class DruidDataScopeFilterConfiguration {

	@Bean("druidDataScopeAuthInterceptor")
	@ConditionalOnMissingBean(name = "druidDataScopeAuthInterceptor")
	public DruidDataScopeAuthInterceptor druidDataScopeAuthInterceptor(DataScopeProperties dataScopeProperties) {
		return new DruidDataScopeAuthInterceptor(dataScopeProperties);
	}

	@Bean
	public DataAuthFilterAspect dataAuthFilterAspect() {
		return new DataAuthFilterAspect();
	}
}
