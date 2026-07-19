package org.bedrock.common.code.config;

import org.bedrock.common.code.util.SpringUtil;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;

@EnableCaching
@AutoConfiguration
public class ToolConfiguration {

	@Bean
	public SpringUtil springUtil() {
		return new SpringUtil();
	}
}
