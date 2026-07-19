package org.bedrock.common.mybatisplus.config;

import com.alibaba.druid.spring.boot3.autoconfigure.DruidDataSourceAutoConfigure;
import com.alibaba.druid.spring.boot3.autoconfigure.stat.DruidWebStatFilterConfiguration;
import lombok.RequiredArgsConstructor;
import org.bedrock.common.mybatisplus.filter.SqlLogFilter;
import org.bedrock.common.mybatisplus.props.MybatisPlusProperties;
import org.bedrock.common.code.annotation.BedrockPropertySource;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration(before = {DruidDataSourceAutoConfigure.class, DruidWebStatFilterConfiguration.class})
@RequiredArgsConstructor
@BedrockPropertySource("classpath:/druid.yml")
public class DruidConfiguration {

	private final MybatisPlusProperties properties;

	@Bean(name = "sqlLogFilter")
	@ConditionalOnMissingBean(name = "sqlLogFilter")
	public SqlLogFilter sqlLogFilter() {
		return new SqlLogFilter(properties);
	}
}
