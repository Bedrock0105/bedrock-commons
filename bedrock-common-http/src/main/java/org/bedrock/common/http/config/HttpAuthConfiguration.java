package org.bedrock.common.http.config;

import org.bedrock.common.http.HttpConfiguration;
import org.bedrock.common.http.props.HttpClientProperties;
import org.bedrock.common.http.spring.annotation.HttpScan;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@HttpScan(value = "org.bedrock.**.client.**")
@EnableConfigurationProperties(HttpClientProperties.class)
public class HttpAuthConfiguration {

	@Bean
	@ConditionalOnMissingBean(HttpConfiguration.class)
	public HttpConfiguration httpConfiguration(HttpClientProperties properties) {
		HttpConfiguration config = HttpConfiguration.getDefault();
		config.setHttpClientProperties(properties);
		return config;
	}
}
