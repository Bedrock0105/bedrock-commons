package org.bedrock.common.redis.config;

import lombok.RequiredArgsConstructor;
import org.bedrock.common.redis.props.RedisProperties;
import org.bedrock.common.redis.stream.processor.RSBeanPostProcessor;
import org.bedrock.common.redis.stream.template.DefaultRsMessageTemplate;
import org.bedrock.common.redis.stream.template.RSMessageTemplate;
import org.bedrock.common.code.server.ServerInfo;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ThreadPoolExecutor;

@RequiredArgsConstructor
@AutoConfiguration(after = RedisTemplateConfiguration.class)
@EnableConfigurationProperties(RedisProperties.class)
@ConditionalOnProperty(prefix = RedisProperties.Stream.PREFIX, name = "enabled", havingValue = "true", matchIfMissing = false)
public class RedisStreamMessageConfiguration {

	private final RedisTemplate<String, Object> redisTemplate;

	private final RedisProperties redisProperties;

	/**
	 * 获取一个 StreamMessageListenerContainer
	 */
	@Bean
	@ConditionalOnMissingBean
	public StreamMessageListenerContainer<String, MapRecord<String, String, Object>> redisListenerContainer(@Qualifier("redisListenerContainerExecutor") ThreadPoolTaskExecutor executor) {

		StreamMessageListenerContainer.StreamMessageListenerContainerOptions<String, MapRecord<String, String, Object>> build = StreamMessageListenerContainer.StreamMessageListenerContainerOptions
			.builder()
			.executor(executor)
			.keySerializer(StringRedisSerializer.UTF_8)
			.hashValueSerializer(redisTemplate.getHashValueSerializer())
			.hashKeySerializer(StringRedisSerializer.UTF_8)
			// 拉取消息超时时间
			.pollTimeout(Duration.ofSeconds(redisProperties.getStream().getSecondsPollTimeout()))
			// 批量抓取消息
			.batchSize(redisProperties.getStream().getBatchSize())
			.build();
		return StreamMessageListenerContainer
			.create(Objects.requireNonNull(redisTemplate.getConnectionFactory()), build);
//		container.start();//在这个地方启动，刚启动的时候会出现获取不到消息
//		return container;
	}

	@Bean(name = "redisListenerContainerExecutor")
	@ConditionalOnMissingBean(name = "redisListenerContainerExecutor")
	public ThreadPoolTaskExecutor redisListenerContainerExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		//核心线程数 注意在 redis stream 这种情况，线程数不要设置成 1 ，会出问题，总会有消息获取不到
		executor.setCorePoolSize(redisProperties.getStream().getCorePoolSize());
		//最大线程数
		executor.setMaxPoolSize(redisProperties.getStream().getMaxPoolSize());
		//任务队列的大小
		executor.setQueueCapacity(redisProperties.getStream().getQueueCapacity());
		//线程池名的前缀
		executor.setThreadNamePrefix(redisProperties.getStream().getThreadNamePrefix());
		//允许线程的空闲时间30秒
		executor.setKeepAliveSeconds(redisProperties.getStream().getKeepAliveSeconds());
		//设置线程池关闭的时候等待所有任务都完成再继续销毁其他的Bean
		executor.setWaitForTasksToCompleteOnShutdown(true);
		// 不拒绝，扔给主程序执行，确保所有的数据都正确执行
		executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
		//线程初始化
		executor.initialize();
		return executor;
	}

	/**
	 * 创建一个 BeanPostProcessor，用于处理 @RSMessageListener 注解
	 */
	@Bean
	public RSBeanPostProcessor rsBeanPostProcessor(ServerInfo serverInfo,
												   StreamMessageListenerContainer<String, MapRecord<String, String, Object>> redisListenerContainer) {
		return new RSBeanPostProcessor(serverInfo, redisListenerContainer, redisTemplate);
	}

	@Bean
	@ConditionalOnMissingBean
	public RSMessageTemplate rsMessageTemplate() {
		return new DefaultRsMessageTemplate(redisTemplate);
	}
}
