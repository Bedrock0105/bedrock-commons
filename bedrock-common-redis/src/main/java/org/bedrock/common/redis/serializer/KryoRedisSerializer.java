package org.bedrock.common.redis.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.CompatibleFieldSerializer;
import com.esotericsoftware.kryo.util.DefaultInstantiatorStrategy;
import com.esotericsoftware.kryo.util.Pool;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class KryoRedisSerializer implements RedisSerializer<Object> {

	private static final int BUFFER_SIZE = 4096;

	private final Pool<Kryo> pool;

	private final Pool<Output> outputPool;

	private final Pool<Input> inputPool;

	public KryoRedisSerializer() {
		this.pool = createPool();
		this.inputPool = createInputPool();
		this.outputPool = createOutputPool();
	}

	@Override
	public byte[] serialize(Object object) throws SerializationException {
		if (object == null) {
			return new byte[0];
		}
		Kryo kryo = null;
		Output output = null;

		try {
			kryo = pool.obtain();
			output = outputPool.obtain();
			kryo.writeClassAndObject(output, object);
			return output.toBytes();
		} catch (Exception e) {
			throw new SerializationException("Kryo序列化失败: " + e.getMessage(), e);
		} finally {
			if (kryo != null) {
				pool.free(kryo);
			}
			if (output != null) {
				outputPool.free(output);
			}
		}
	}

	@Override
	public Object deserialize(byte[] bytes) throws SerializationException {
		if (bytes == null || bytes.length == 0) {
			return null;
		}
		Kryo kryo = null;
		Input input = null;
		try {
			kryo = pool.obtain();
			input = inputPool.obtain();
			input.setBuffer(bytes);
			return kryo.readClassAndObject(input);
		} catch (Exception e) {
			throw new SerializationException("Kryo反序列化失败: " + e.getMessage(), e);
		} finally {
			if (kryo != null) {
				pool.free(kryo);
			}
			if (input != null) {
				inputPool.free(input);
			}

		}
	}

	private static Pool<Output> createOutputPool() {
		return new Pool<>(false, true, 1000) {
			@Override
			protected Output create() {
				return new Output(BUFFER_SIZE, -1);
			}
		};
	}

	private static Pool<Input> createInputPool() {
		return new Pool<>(false, true, 1000) {
			@Override
			protected Input create() {
				return new Input();
			}
		};
	}

	private static Pool<Kryo> createPool() {
		return new Pool<>(false, true, 1000) {
			@Override
			protected Kryo create() {
				Kryo kryo = getKryo();
				// 注册常用类（可选，提高序列化效率）
				kryo.register(String.class);
				kryo.register(Integer.class);
				kryo.register(Long.class);
				kryo.register(Double.class);
				kryo.register(Float.class);
				kryo.register(Boolean.class);
				kryo.register(byte[].class);
				kryo.register(ArrayList.class);
				kryo.register(HashMap.class);
				kryo.register(HashSet.class);
				// 如果确定没有循环引用，可关闭引用追踪以提升性能
				return kryo;
			}
		};
	}

	private static Kryo getKryo() {
		Kryo kryo = new Kryo();
		// --- 重要：Java 17 兼容性配置 ---
		// 设置实例化策略，避免在Java高版本下实例化对象时出现问题
		kryo.setInstantiatorStrategy(new DefaultInstantiatorStrategy(
			new org.objenesis.strategy.StdInstantiatorStrategy()));
		kryo.setDefaultSerializer(CompatibleFieldSerializer.class);
		// ----------------------------------
		// 注册需要序列化的类
		// 配置优化（重要！）
		kryo.setReferences(false);       // 关闭引用跟踪，提高性能
		kryo.setRegistrationRequired(false); // 不需要预注册类
		kryo.setCopyReferences(false);       // 不复制引用
		return kryo;
	}
}
