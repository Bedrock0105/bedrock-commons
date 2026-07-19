package org.bedrock.common.boot.config;

import org.bedrock.common.boot.listener.RequestListener;
import org.bedrock.common.boot.props.RequestContentProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration(after = ContentConfiguration.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class RequestListenerConfiguration {

	@Bean
	public ServletListenerRegistrationBean<RequestListener> requestListener(RequestContentProperties properties) {
		return new ServletListenerRegistrationBean<>(new RequestListener(properties));
	}
}
