package org.bedrock.common.boot.config;

import org.bedrock.common.boot.async.ContextTaskDecorator;
import org.bedrock.common.boot.props.RequestContentProperties;
import org.bedrock.common.code.annotation.BedrockPropertySource;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.task.ThreadPoolTaskExecutorCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.concurrent.ThreadPoolExecutor;

@AutoConfiguration
@EnableAsync
@EnableScheduling
@EnableConfigurationProperties(RequestContentProperties.class)
@BedrockPropertySource("classpath:/boot.yml")
public class ContentConfiguration {

	@Bean
	public ContextTaskDecorator contextTaskDecorator() {
		return new ContextTaskDecorator();
	}

	@Bean
	public ThreadPoolTaskExecutorCustomizer taskExecutorCustomizer() {
		return taskExecutor -> {
			taskExecutor.setThreadNamePrefix("async-task-");
			taskExecutor.setTaskDecorator(contextTaskDecorator());
			taskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
		};
	}

	@Bean
	public ThreadPoolTaskExecutorCustomizer taskSchedulerCustomizer() {
		return taskExecutor -> {
			taskExecutor.setThreadNamePrefix("async-scheduler");
			taskExecutor.setTaskDecorator(contextTaskDecorator());
			taskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
		};
	}
}
