package org.bedrock.common.code.eventbus.model;

import lombok.Data;

/**
 * 事件消息记录类，用于封装 EventBus 中的消息内容。
 */
@Data
public class EventMessage<T> {

	/**
	 * 消息主题
	 */
	private String topic;

	/**
	 * 消息内容
	 */
	private T message;

	/**
	 * 消息时间戳（毫秒）
	 */
	private Long timestamp;

	/**
	 * 获取消息主题
	 *
	 * @return 消息主题
	 */
	public String topic() {
		return this.topic;
	}

	/**
	 * 获取消息内容
	 *
	 * @return 消息内容
	 */
	public T message() {
		return this.message;
	}

	/**
	 * 获取消息时间戳
	 *
	 * @return 消息时间戳（毫秒）
	 */
	public Long timestamp() {
		return this.timestamp;
	}

	/**
	 * 构造一个事件消息，时间戳默认为当前系统时间。
	 *
	 * @param topic   消息主题
	 * @param message 消息内容
	 */
	public EventMessage(String topic, T message) {
		this(topic, message, System.currentTimeMillis());
	}

	/**
	 * 构造一个事件消息
	 *
	 * @param topic     消息主题
	 * @param message   消息内容
	 * @param timestamp 事件戳
	 */
	public EventMessage(String topic, T message, Long timestamp) {
		this.topic = topic;
		this.message = message;
		this.timestamp = timestamp;
	}
}
