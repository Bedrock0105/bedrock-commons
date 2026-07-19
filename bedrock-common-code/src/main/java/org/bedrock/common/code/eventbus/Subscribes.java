package org.bedrock.common.code.eventbus;

import lombok.Getter;
import lombok.Setter;
import org.bedrock.common.code.eventbus.model.EventMessage;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * 订阅配置实体
 * <p>
 * 支持一次性配置多个消息主题与对应消费回调，用于事件总线统一订阅/取消订阅操作。
 * 配合 {@link EventBus} 接口使用，可批量绑定多个 TopicRegistry 到同一个消费者。
 * </p>
 *
 */
@Getter
@Setter
public class Subscribes<T> {

	/**
	 * 消息主题数组，支持同时订阅多个主题
	 */
	private String[] topic;

	/**
	 * 消息消费回调函数，接收主题推送的消息数据
	 */
	private Consumer<EventMessage<T>> subscribe;

	/**
	 * 私有构造方法，仅由构建器创建实例
	 *
	 * @param topic     主题数组
	 * @param subscribe 消费回调
	 */
	private Subscribes(String[] topic, Consumer<EventMessage<T>> subscribe) {
		this.topic = topic;
		this.subscribe = subscribe;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) return false;
		Subscribes<?> that = (Subscribes<?>) o;
		return Objects.deepEquals(topic, that.topic) && Objects.equals(subscribe, that.subscribe);
	}

	@Override
	public int hashCode() {
		return Objects.hash(Arrays.hashCode(topic), subscribe);
	}

	@Override
	public String toString() {
		return "Subscribes{" +
			"topic=" + Arrays.toString(topic) +
			", subscribe=" + subscribe +
			'}';
	}

	/**
	 * 获取构建器实例，链式构建订阅配置
	 *
	 * @param <T> 消息泛型类型
	 * @return 订阅配置构建器
	 */
	public static <T> SubscribesBuilder<T> builder() {
		return new SubscribesBuilder<T>();
	}

	/**
	 * 订阅配置构建器
	 * 采用链式调用方式组装主题、消费回调等参数
	 *
	 * @param <T> 消息泛型类型
	 */
	public static class SubscribesBuilder<T> {

		/**
		 * 消息主题数组
		 */
		private String[] topic;

		/**
		 * 消息消费回调函数
		 */
		private Consumer<EventMessage<T>> subscribe;

		/**
		 * 设置单个/多个消息主题
		 *
		 * @param topic 可变参数，支持传入一个或多个主题名称
		 * @return 当前构建器对象
		 */
		public SubscribesBuilder<T> topic(String... topic) {
			this.topic = topic;
			return this;
		}

		/**
		 * 设置消息消费回调
		 *
		 * @param subscribe 消费回调函数
		 * @return 当前构建器对象
		 */
		public SubscribesBuilder<T> subscribe(Consumer<EventMessage<T>> subscribe) {
			this.subscribe = subscribe;
			return this;
		}

		/**
		 * 完成参数构建，生成订阅配置实例
		 *
		 * @return 组装完成的 {@link Subscribes} 对象
		 */
		public Subscribes<T> build() {
			return new Subscribes<>(this.topic, this.subscribe);
		}
	}
}
