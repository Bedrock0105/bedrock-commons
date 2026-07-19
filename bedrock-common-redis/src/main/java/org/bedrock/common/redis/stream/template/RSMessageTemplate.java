package org.bedrock.common.redis.stream.template;

import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.connection.stream.Record;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Map;

/**
 * redis stream 消息发送器
 */
public interface RSMessageTemplate {

	String DEFAULT_STREAM_OBJECT_KEY = "@defaultPayload";


	/**
	 * 发送消息
	 *
	 * @param topic   topic
	 * @param message 消息内容
	 * @return 消息Id
	 */
	default RecordId send(String topic, Object message) {
		return send(ObjectRecord.create(topic, message));
	}

	/**
	 * 发送消息
	 *
	 * @param topic   topic
	 * @param message 消息内容
	 */
	default RecordId send(String topic, Map<String, Object> message) {
		return send(MapRecord.create(topic, message));
	}

	/**
	 * 发送消息
	 */
	RecordId send(Record<String, ?> message);

	/**
	 * 执行ack
	 *
	 * @param topic     topic
	 * @param group     消费组
	 * @param recordIds 消息
	 */
	default Long acknowledge(String topic, String group, RecordId... recordIds) {
		return acknowledge(topic, group, Arrays.stream(recordIds).map(RecordId::getValue).toArray(String[]::new));
	}

	/**
	 * 执行ack
	 */
	default Long acknowledge(String topic, Consumer consumer, String... recordIds) {
		return acknowledge(topic, consumer.getGroup(), recordIds);
	}

	/**
	 * 执行ack
	 */
	default Long acknowledge(String topic, Consumer consumer, RecordId... recordIds) {
		return acknowledge(topic, consumer.getGroup(), recordIds);
	}

	/**
	 * 执行ack
	 */
	default Long acknowledge(String group, Record<String, ?> record) {
		return acknowledge(record.getRequiredStream(), group, record.getId());
	}

	/**
	 * Acknowledge one or more records as processed.
	 *
	 * @param topic     the stream key.
	 * @param group     name of the consumer group.
	 * @param recordIds record id's to acknowledge.
	 * @return length of acknowledged records. {@literal null} when used in pipeline / transaction.
	 * @see <a href="https://redis.io/commands/xack">Redis Documentation: XACK</a>
	 */
	Long acknowledge(String topic, String group, String... recordIds);

	/**
	 * Trims the stream to {@code count} elements.
	 *
	 * @param key   the stream key.
	 * @param count length of the stream.
	 * @return number of removed entries. {@literal null} when used in pipeline / transaction.
	 * @see <a href="https://redis.io/commands/xtrim">Redis Documentation: XTRIM</a>
	 */
	Long trim(String key, long count);

	/**
	 * Trims the stream to {@code count} elements.
	 *
	 * @param key                 the stream key.
	 * @param count               length of the stream.
	 * @param approximateTrimming the trimming must be performed in a approximated way in order to maximize performances.
	 * @return number of removed entries. {@literal null} when used in pipeline / transaction.
	 * @see <a href="https://redis.io/commands/xtrim">Redis Documentation: XTRIM</a>
	 * @since 2.4
	 */
	Long trim(String key, long count, boolean approximateTrimming);

	/**
	 * Removes the specified records from the stream. Returns the number of records deleted, that may be different from
	 * the number of IDs passed in case certain IDs do not exist.
	 *
	 * @param key       the stream key.
	 * @param recordIds stream record Id's.
	 * @return number of removed entries. {@literal null} when used in pipeline / transaction.
	 * @see <a href="https://redis.io/commands/xdel">Redis Documentation: XDEL</a>
	 */
	default Long delete(String key, String... recordIds) {
		return delete(key, Arrays.stream(recordIds).map(RecordId::of).toArray(RecordId[]::new));
	}

	/**
	 * Removes a given {@link Record} from the stream.
	 *
	 * @param record must not be {@literal null}.
	 * @return he {@link Mono} emitting the number of removed records.
	 */
	default Long delete(Record<String, ?> record) {
		return delete(record.getStream(), record.getId());
	}

	/**
	 * Removes the specified records from the stream. Returns the number of records deleted, that may be different from
	 * the number of IDs passed in case certain IDs do not exist.
	 *
	 * @param key       the stream key.
	 * @param recordIds stream record Id's.
	 * @return the {@link Mono} emitting the number of removed records.
	 * @see <a href="https://redis.io/commands/xdel">Redis Documentation: XDEL</a>
	 */
	Long delete(String key, RecordId... recordIds);
}
