package org.bedrock.common.redis.stream.template;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.Record;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class DefaultRsMessageTemplate implements RSMessageTemplate {

	private final RedisTemplate<String, Object> redisTemplate;

	@Override
	public RecordId send(Record<String, ?> message) {
		if (message instanceof MapRecord) {
			return redisTemplate.opsForStream().add(message);
		}
		return redisTemplate.opsForStream().add(MapRecord.create(message.getRequiredStream(), Map.of(DEFAULT_STREAM_OBJECT_KEY, message.getValue())));
	}

	@Override
	public Long acknowledge(String topic, String group, String... recordIds) {
		return redisTemplate.opsForStream().acknowledge(topic, group, recordIds);
	}

	@Override
	public Long trim(String key, long count) {
		return redisTemplate.opsForStream().trim(key, count);
	}

	@Override
	public Long trim(String key, long count, boolean approximateTrimming) {
		return redisTemplate.opsForStream().trim(key, count, approximateTrimming);
	}

	@Override
	public Long delete(String key, RecordId... recordIds) {
		return redisTemplate.opsForStream().delete(key, recordIds);
	}
}
