package org.bedrock.common.redis.config;

import lombok.RequiredArgsConstructor;
import org.bedrock.common.redis.props.RedisProperties;
import org.bedrock.common.redis.serializer.FstRedisSerializer;
import org.bedrock.common.redis.serializer.KryoRedisSerializer;
import org.bedrock.common.redis.serializer.ProtostuffRedisSerializer;
import org.bedrock.common.redis.util.RedisUtil;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.*;

@RequiredArgsConstructor
@AutoConfiguration(before = RedisAutoConfiguration.class)
@EnableConfigurationProperties(RedisProperties.class)
public class RedisTemplateConfiguration {

	private final RedisProperties redisProperties;

	@Bean
	@ConditionalOnMissingBean
	public RedisSerializer<Object> redisSerializer() {
		if (redisProperties.getSerializer() == RedisProperties.RedisSerializerEnum.PROTOSTUFF) {
			return new ProtostuffRedisSerializer();
		}
		if (redisProperties.getSerializer() == RedisProperties.RedisSerializerEnum.JSON) {
			return new GenericJackson2JsonRedisSerializer();
		}
		if (redisProperties.getSerializer() == RedisProperties.RedisSerializerEnum.KRYO) {
			return new KryoRedisSerializer();
		}
		if (redisProperties.getSerializer() == RedisProperties.RedisSerializerEnum.FST) {
			return new FstRedisSerializer();
		}
		return new JdkSerializationRedisSerializer();

	}

	@Bean
	@ConditionalOnMissingBean(name = "redisTemplate")
	public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory,
													   RedisSerializer<Object> redisSerializer) {
		RedisTemplate<String, Object> template = new RedisTemplate<>();

		// 2. 配置 String 序列化器（用于 key 和 hashKey，避免 key 乱码）
		StringRedisSerializer stringSerializer = StringRedisSerializer.UTF_8;

		// 3. 为不同类型设置序列化器
		template.setKeySerializer(stringSerializer);       // key 用 String 序列化
		template.setValueSerializer(redisSerializer);
		template.setHashKeySerializer(stringSerializer);   // hashKey 用 String 序列化
		template.setHashValueSerializer(redisSerializer);
		template.setConnectionFactory(redisConnectionFactory);
		template.afterPropertiesSet();
		/**
		 * 初始化 RedisTemplate
		 */
		RedisUtil.init(template);
		return template;
	}

	@Bean
	@ConditionalOnMissingBean
	public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
		StringRedisTemplate stringRedisTemplate = new StringRedisTemplate(redisConnectionFactory);
		RedisUtil.init(stringRedisTemplate);
		return stringRedisTemplate;
	}
}
