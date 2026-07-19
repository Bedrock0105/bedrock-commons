package org.bedrock.common.log.config;

import jakarta.servlet.DispatcherType;
import org.bedrock.common.log.aspect.LogTraceAspect;
import org.bedrock.common.log.filter.LogTraceFilter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;

@AutoConfiguration
public class LogTraceConfiguration {

	@Bean
	public LogTraceAspect logTraceAspect() {
		return new LogTraceAspect();
	}

	@Bean
	public FilterRegistrationBean<LogTraceFilter> logTraceFilterRegistration() {
		FilterRegistrationBean<LogTraceFilter> registration = new FilterRegistrationBean<>();
		registration.setDispatcherTypes(DispatcherType.REQUEST);
		registration.setFilter(new LogTraceFilter());
		registration.addUrlPatterns("/*");
		registration.setName("LogTraceFilter");
		registration.setOrder(Ordered.LOWEST_PRECEDENCE);
		return registration;
	}
}
