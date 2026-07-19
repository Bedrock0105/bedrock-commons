package org.bedrock.common.code.eventbus.impl;

import lombok.extern.slf4j.Slf4j;
import org.bedrock.common.code.eventbus.AbstractEventBus;
import org.bedrock.common.code.eventbus.enums.EventBusType;
import org.bedrock.common.code.eventbus.model.EventMessage;

import java.util.concurrent.Executor;

/**
 * 本地内存事件总线实现
 * <p>
 * 基于树形 Topic 结构实现发布订阅能力，仅当前进程内生效。
 * 消息实时分发，不持久化历史消息，后订阅者无法接收过往消息。
 * 消息消费统一交由自定义线程池异步执行，单个消费异常互不影响。
 * </p>
 */
@Slf4j
public class LocalEventBusImpl extends AbstractEventBus {

	public LocalEventBusImpl(Executor executor) {
		super(executor);
	}

	/**
	 * 向指定主题发布消息，异步分发至所有匹配的订阅者
	 *
	 * @param topic   消息主题
	 * @param message 消息内容
	 */
	@Override
	public void publish(String topic, Object message) {
		onMessage(new EventMessage<>(topic, message));
	}

	/**
	 * 获取当前事件总线实现类型
	 *
	 * @return 本地总线类型标识
	 */
	@Override
	public EventBusType getEventBusType() {
		return EventBusType.LOCAL;
	}
}
