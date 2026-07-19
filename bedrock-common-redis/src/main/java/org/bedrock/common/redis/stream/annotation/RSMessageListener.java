package org.bedrock.common.redis.stream.annotation;

import lombok.Getter;
import org.springframework.data.redis.connection.stream.ReadOffset;

import java.lang.annotation.*;

/**
 * 基于redis stream 消息监听器注解
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RSMessageListener {

	/**
	 * topic
	 */
	String topic();

	/**
	 * 是否需要组消费
	 * 默认不需要
	 * 不需要就是广播的形式所有的消费者都能收到
	 */
	boolean isGroup() default false;

	/**
	 * 读取位置：封装Redis Stream中消息的消费起始点，用于指定从Stream的哪个位置开始读取消息，对应Redis Stream的offset概念
	 *
	 * <p>核心作用：
	 * 通过Spring Data Redis提供的{@link org.springframework.data.redis.connection.stream.ReadOffset}类实现，
	 * 定义了消费Stream消息时的"起始偏移量"规则，确保消费者能精准控制读取范围（如从最早消息、最新消息、特定位置或未处理消息开始）。
	 *
	 * <p>常见读取模式（对应{@code ReadOffset}的静态方法）：
	 * <ul>
	 *   <li>{@code ReadOffset.latest()}：从Stream中"最新的消息"开始读取（仅消费后续新增的消息，忽略历史消息）；
	 *   <li>{@code ReadOffset.from(String offset)}：从指定的具体offset（如"1695800000000-0"）开始读取；
	 *   <li>{@code ReadOffset.from(RecordId offset)}：通过消息ID（RecordId）指定起始位置，功能同上；
	 *   <li>消费者组场景：配合{@code ReadOffset.lastConsumed()}，从消费者组中"上次未确认的消息"开始读取（断点续传）。
	 * </ul>
	 *
	 * <p>详细规则与实现细节可参考{@link org.springframework.data.redis.connection.stream.ReadOffset}类的源码注释。
	 */
	ReadOffsetEnum readOffset() default ReadOffsetEnum.LAST_CONSUMED;

	/**
	 * group-name是关联到流的消费者组的名称。
	 */
	String consumerGroup() default "";

	/**
	 * consumer-name是客户端用于在消费者组内标识自己的字符串
	 */
	String consumerName() default "";

	/**
	 * isGroup 是true 生效
	 * 自动 ack
	 * 组消费生效
	 */
	boolean autoAck() default false;

	/**
	 * 保留最新多少数据
	 *
	 * @return int
	 */
	int trim() default -1;

	@Getter
	enum ReadOffsetEnum {
		/**
		 * 开始
		 */
		START(ReadOffset.from("0-0")),

		/**
		 * 最新的
		 */
		LATEST(ReadOffset.latest()),

		/**
		 * 组里面没有被ack的数据
		 */
		LAST_CONSUMED(ReadOffset.lastConsumed());

		private final ReadOffset readOffset;

		ReadOffsetEnum(ReadOffset readOffset) {
			this.readOffset = readOffset;
		}
	}
}
