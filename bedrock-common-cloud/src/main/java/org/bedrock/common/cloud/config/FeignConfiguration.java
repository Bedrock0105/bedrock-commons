package org.bedrock.common.cloud.config;

import org.bedrock.common.cloud.feign.DefaultFeignRequestInterceptor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class FeignConfiguration {

	@Bean
	@ConditionalOnMissingBean(name = "defaultFeignRequestInterceptor")
	public DefaultFeignRequestInterceptor defaultFeignRequestInterceptor() {
		return new DefaultFeignRequestInterceptor();
	}
}



