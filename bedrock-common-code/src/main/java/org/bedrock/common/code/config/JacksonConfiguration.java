package org.bedrock.common.code.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.bedrock.common.code.util.JsonUtil;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration(before = JacksonAutoConfiguration.class)
public class JacksonConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public ObjectMapper objectMapper() {
		return JsonUtil.getInstance();
	}
}
