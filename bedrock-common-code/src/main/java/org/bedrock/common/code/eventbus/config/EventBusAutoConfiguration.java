package org.bedrock.common.code.eventbus.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bedrock.common.code.eventbus.EventBus;
import org.bedrock.common.code.eventbus.impl.LocalEventBusImpl;
import org.bedrock.common.code.eventbus.impl.RedisEventBusImpl;
import org.bedrock.common.code.props.EventBusProperties;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Redis 事件总线自动配置类
 * <p>
 * 当项目中存在 Redis 相关依赖时，自动配置 Redis 事件总线实例。
 * 支持通过 Spring 容器注入 {@link EventBus} 使用。
 * </p>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * @Autowired
 * private EventBus eventBus;
 *
 * // 订阅主题
 * Subscribes<String> subscribes = Subscribes.<String>builder()
 *     .topic("order.create")
 *     .subscribe(msg -> System.out.println("收到订单创建消息: " + msg))
 *     .build();
 * eventBus.subscribe(subscribes);
 *
 * // 发布消息
 * eventBus.publish("order.create", "订单123");
 * }</pre>
 */
@Slf4j
@AutoConfiguration
@RequiredArgsConstructor
@EnableConfigurationProperties(EventBusProperties.class)
@ConditionalOnProperty(prefix = EventBusProperties.PREFIX, name = "enabled", havingValue = "true", matchIfMissing = false)
@ConditionalOnMissingBean(EventBus.class)
public class EventBusAutoConfiguration {

	@ConditionalOnProperty(prefix = EventBusProperties.PREFIX, name = "type", havingValue = "LOCAL", matchIfMissing = true)
	public static class EventBusLocalAutoConfiguration {

		@Bean
		public EventBus localEventBus(@Qualifier("eventBusExecutor") ObjectProvider<Executor> executor) {
			log.info("创建本地事件总线实例");
			return new LocalEventBusImpl(executor.getIfAvailable(Executors::newCachedThreadPool));
		}
	}

	@ConditionalOnProperty(prefix = EventBusProperties.PREFIX, name = "type", havingValue = "REDIS")
	@ConditionalOnClass({RedisTemplate.class, RedisConnectionFactory.class})
	public static class EventBusRedisAutoConfiguration {

		/**
		 * Redis消息监听器容器
		 * 这个容器加载了RedisConnectionFactory和消息监听器
		 * 可以添加多个监听不同话题的redis监听器，只需要把消息监听器和相应的消息订阅处理器绑定，该消息监听器
		 * 通过反射技术调用消息订阅处理器的相关方法进行一些业务处理
		 *
		 * @param redisConnectionFactory 连接工厂
		 * @return redis消息监听容器
		 */
		@Bean
		@ConditionalOnMissingBean
		public RedisMessageListenerContainer container(RedisConnectionFactory redisConnectionFactory) {
			RedisMessageListenerContainer container = new RedisMessageListenerContainer();
			container.setConnectionFactory(redisConnectionFactory);
			container.setErrorHandler(throwable -> log.error("Redis监听器接收到订阅消息处理失败, {}", throwable.getMessage(), throwable));
			return container;
		}

		@Bean
		public EventBus redisEventBus(@Qualifier("eventBusExecutor") ObjectProvider<Executor> executor,
		                              RedisTemplate<String, Object> redisTemplate,
		                              RedisMessageListenerContainer listenerContainer,
		                              EventBusProperties properties) {
			log.info("创建redis事件总线实例");
			return new RedisEventBusImpl(executor.getIfAvailable(Executors::newCachedThreadPool), properties, redisTemplate, listenerContainer);
		}
	}
}
