package org.bedrock.common.redis.serializer;

import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;
import lombok.Getter;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

public class ProtostuffRedisSerializer implements RedisSerializer<Object> {

	private final Schema<ObjectWrapper> schema;

	public ProtostuffRedisSerializer() {
		this.schema = RuntimeSchema.getSchema(ObjectWrapper.class);
	}

	@Override
	public byte[] serialize(Object object) throws SerializationException {
		if (object == null) {
			return new byte[0];
		}
		LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
		try {
			return ProtostuffIOUtil.toByteArray(new ObjectWrapper<>(object), schema, buffer);
		} catch (Exception e) {
			throw new SerializationException("Error serializing object with Protostuff", e);
		} finally {
			buffer.clear();
		}
	}

	@Override
	public Object deserialize(byte[] bytes) throws SerializationException {
		if (bytes == null || bytes.length == 0) {
			return null;
		}
		try {
			ObjectWrapper<Object> wrapper = new ObjectWrapper<>();
			ProtostuffIOUtil.mergeFrom(bytes, wrapper, schema);
			return wrapper.getObject();
		} catch (Exception e) {
			throw new SerializationException("Error deserializing object with Protostuff", e);
		}
	}

	@Getter
	public static class ObjectWrapper<T> {

		private T object;

		public ObjectWrapper() {
		}

		public ObjectWrapper(T object) {
			this.object = object;
		}

		public void setObject(T object) {
			this.object = object;
		}

	}
}


