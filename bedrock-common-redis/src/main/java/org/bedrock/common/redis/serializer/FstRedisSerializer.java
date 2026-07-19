package org.bedrock.common.redis.serializer;

import org.nustaq.serialization.FSTConfiguration;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class FstRedisSerializer implements RedisSerializer<Object> {

	// FSTConfiguration 是线程安全的
	private final FSTConfiguration fstConfiguration;

	public FstRedisSerializer() {
		fstConfiguration = FSTConfiguration.createDefaultConfiguration();
		// 性能优化配置
		fstConfiguration.setShareReferences(false); // 关闭引用共享，提高速度
		System.setProperty("fst.fieldBased", "true"); // 基于字段名称// 结构模式，优化序列化
		// 允许序列化内部类
		System.setProperty("fst.allow.any.class", "true");
		System.setProperty("fst.ignore.private", "false");
		// 核心兼容性配置
		fstConfiguration.setShareReferences(false);        // 关闭引用共享，提高兼容性
		fstConfiguration.setStructMode(false);             // 关闭结构模式，允许字段变更
		fstConfiguration.setCrossPlatform(false);          // 关闭跨平台模式（提高性能）
		// 版本兼容性配置
		fstConfiguration.setForceSerializable(true);      // 不强制Serializable
		// 性能与兼容性平衡
		fstConfiguration.setPreferSpeed(true);             // 优先速度（稍降低兼容性）
		// 或者：conf.setPreferSpeed(false);   // 优先兼容性（稍降低速度）
		// 预注册常用类
		this.fstConfiguration.registerClass(String.class, Integer.class, Long.class,
			Double.class, Float.class, Boolean.class,
			ArrayList.class, HashMap.class, HashSet.class);
	}

	@Override
	public byte[] serialize(Object object) throws SerializationException {
		if (object == null) {
			return new byte[0];
		}
		try {
			return fstConfiguration.asByteArray(object);
		} catch (Exception e) {
			throw new SerializationException("FST序列化失败: " + e.getMessage(), e);
		}
	}

	@Override
	public Object deserialize(byte[] bytes) throws SerializationException {
		if (bytes == null || bytes.length == 0) {
			return null;
		}
		try {
			return fstConfiguration.asObject(bytes);
		} catch (Exception e) {
			throw new SerializationException("FST反序列化失败: " + e.getMessage(), e);
		}
	}
}
