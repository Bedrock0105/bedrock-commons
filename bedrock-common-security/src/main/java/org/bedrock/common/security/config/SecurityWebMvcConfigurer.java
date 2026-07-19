package org.bedrock.common.security.config;

import lombok.RequiredArgsConstructor;
import org.bedrock.common.security.interceptor.SecurityInterceptor;
import org.bedrock.common.security.props.SecurityProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@AutoConfiguration
@ConditionalOnProperty(prefix = SecurityProperties.PREFIX, name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class SecurityWebMvcConfigurer implements WebMvcConfigurer {

	private final SecurityProperties securityProperties;

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(new SecurityInterceptor())
			.addPathPatterns("/**")
			.excludePathPatterns(securityProperties.getIgnoreUrls());
	}
}
