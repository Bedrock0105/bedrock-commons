package org.bedrock.common.code.eventbus.impl;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.bedrock.common.code.eventbus.AbstractEventBus;
import org.bedrock.common.code.eventbus.enums.EventBusType;
import org.bedrock.common.code.eventbus.model.EventMessage;
import org.bedrock.common.code.props.EventBusProperties;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import java.util.*;
import java.util.concurrent.Executor;

/**
 * Redis 事件总线实现（单通道模式）
 * <p>
 * 基于 Redis Pub/Sub 机制实现跨服务、跨实例的发布订阅能力。
 * 采用单通道模式：所有消息统一发布到固定频道，由监听器解析真实主题后分发。
 * </p>
 *
 * <h3>核心特性</h3>
 * <ul>
 *   <li>单通道设计：仅订阅一个固定频道，降低 Redis 负担</li>
 *   <li>树形 Topic：支持层级化主题管理，类似本地实现</li>
 *   <li>跨进程通信：不同服务实例间可互相收发消息</li>
 *   <li>异常隔离：单个消费者异常不影响其他消费者</li>
 * </ul>
 *
 * <h3>工作原理</h3>
 * <ol>
 *   <li>订阅：记录 topic 与消费者的映射关系（内存中）</li>
 *   <li>发布：将 {真实topic, 消息内容} 包装后发送到固定频道</li>
 *   <li>接收：监听固定频道，解析出真实 topic，匹配并调用消费者</li>
 * </ol>
 *
 * <h3>注意事项</h3>
 * <ul>
 *   <li>消息不持久化，先发布后订阅无法接收历史消息</li>
 *   <li>所有实例都会收到所有消息，通过内存过滤实现精确分发</li>
 *   <li>建议为不同业务模块设置独立的前缀，避免冲突</li>
 * </ul>
 */
@Slf4j
public class RedisEventBusImpl extends AbstractEventBus {

	/**
	 * 事件总线配置属性
	 */
	private final EventBusProperties properties;

	/**
	 * Redis String 操作模板
	 */
	private final RedisTemplate<String, Object> redisTemplate;

	/**
	 * Redis 消息监听容器，管理订阅者
	 */
	private final RedisMessageListenerContainer listenerContainer;

	/**
	 * 初始化：注册固定频道的监听器
	 */
	@PostConstruct
	public void init() {
		// 订阅固定频道
		listenerContainer.addMessageListener((message, pattern) -> {
			EventMessage<?> deserialize = (EventMessage<?>) redisTemplate.getValueSerializer()
				.deserialize(message.getBody());
			onMessage(deserialize);
		}, new ChannelTopic(properties.getGlobalTopic()));
		log.info("[{}] 初始化完成，监听固定频道: {}", getEventBusType().getTitle(), properties.getGlobalTopic());
	}

	/**
	 * 向指定主题发布消息，包装后发送到固定频道
	 *
	 * @param topic   消息主题
	 * @param message 消息内容
	 */
	@Override
	public void publish(String topic, Object message) {
		try {
			// 发布到固定频道
			redisTemplate.convertAndSend(properties.getGlobalTopic(), new EventMessage<>(topic, message));
			log.debug("[Redis事件总线] 主题[{}] 消息发布成功", topic);
		} catch (Exception e) {
			log.error("[Redis事件总线] 主题[{}] 消息发布失败", topic, e);
		}
	}

	/**
	 * 获取当前事件总线实现类型
	 *
	 * @return Redis 总线类型标识
	 */
	@Override
	public EventBusType getEventBusType() {
		return EventBusType.REDIS;
	}

	public RedisEventBusImpl(Executor executor, EventBusProperties properties, RedisTemplate<String, Object> redisTemplate, RedisMessageListenerContainer listenerContainer) {
		super(executor);
		this.properties = properties;
		this.redisTemplate = redisTemplate;
		this.listenerContainer = listenerContainer;
	}
}
