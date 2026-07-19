package org.bedrock.common.redis.stream.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bedrock.common.redis.stream.annotation.RSMessageListener;
import org.bedrock.common.redis.stream.template.RSMessageTemplate;
import org.bedrock.common.code.server.ServerInfo;
import org.bedrock.common.code.util.AnnotationUtil;
import org.bedrock.common.code.util.CollectionUtil;
import org.bedrock.common.code.util.ReflectionUtil;
import org.bedrock.common.code.util.StringUtil;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.lang.Nullable;

import java.lang.Record;
import java.lang.reflect.Method;
import java.util.*;

/**
 * spring bean 后置处理器
 */
@Slf4j
@RequiredArgsConstructor
public class RSBeanPostProcessor implements BeanPostProcessor, CommandLineRunner {

	private final ServerInfo serverInfo;

	private final StreamMessageListenerContainer<String, MapRecord<String, String, Object>> redisListenerContainer;

	private final RedisTemplate<String, Object> redisTemplate;

	/**
	 * bean定义
	 * 用来暂时存储包含@RSMessageListener注解的bean
	 */
	private List<BeanDef> beanDefs = new ArrayList<>();

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		ReflectionUtil.doWithMethods(bean.getClass(), method -> {
			RSMessageListener mergedAnnotation = Objects.requireNonNull(AnnotationUtil.findAnnotation(method, RSMessageListener.class));
			beanDefs.add(BeanDef.of(method, bean, mergedAnnotation));
		}, method -> AnnotatedElementUtils.isAnnotated(method, RSMessageListener.class));
		return bean;
	}

	private void invokeMethod(Method method, MapRecord<String, String, Object> record, Object bean, @Nullable Consumer consumer) {
		int parameterCount = method.getParameterCount();
		if (parameterCount == 0) {
			ReflectionUtil.invokeMethod(method, bean);
			return;
		}
		Object[] args = new Object[parameterCount];
		Class<?>[] parameterTypes = method.getParameterTypes();
		for (int i = 0; i < method.getParameterCount(); i++) {
			Class<?> parameterType = parameterTypes[i];
			Map<String, Object> recordValue = record.getValue();
			if (ObjectRecord.class.isAssignableFrom(parameterType)) {
				if (recordValue.containsKey(RSMessageTemplate.DEFAULT_STREAM_OBJECT_KEY)) {
					args[i] = ObjectRecord.create(record.getRequiredStream(), recordValue.get(RSMessageTemplate.DEFAULT_STREAM_OBJECT_KEY));
					continue;
				}
				args[i] = ObjectRecord.create(record.getRequiredStream(), recordValue);
				continue;
			}
			if (MapRecord.class.isAssignableFrom(parameterType)) {
				args[i] = record;
				continue;
			}
			if (Consumer.class.isAssignableFrom(parameterType)) {
				args[i] = consumer;
				continue;
			}
			if (parameterType.equals(Record.class)) {
				args[i] = record;
				continue;
			}
			if (RecordId.class.isAssignableFrom(parameterType)) {
				args[i] = record.getId();
				continue;
			}
			if (Map.class.isAssignableFrom(parameterType)) {
				if (recordValue.containsKey(RSMessageTemplate.DEFAULT_STREAM_OBJECT_KEY)) {
					args[i] = recordValue.get(RSMessageTemplate.DEFAULT_STREAM_OBJECT_KEY);
					continue;
				}
				args[i] = recordValue;
				continue;
			}
			if (recordValue.containsKey(RSMessageTemplate.DEFAULT_STREAM_OBJECT_KEY)) {
				Object obj = recordValue.get(RSMessageTemplate.DEFAULT_STREAM_OBJECT_KEY);
				if (obj.getClass().isAssignableFrom(parameterType)) {
					args[i] = obj;
					continue;
				}
			}
			args[i] = null;

		}
		ReflectionUtil.invokeMethod(method, bean, args);
	}

	/**
	 * 校验消费组是否存在，不存在则创建，否则会出现异常
	 * Error in execution; nested exception is io.lettuce.core.RedisCommandExecutionException: NOGROUP No such key 'streamKey' or consumer group 'consumerGroup' in XREADGROUP with GROUP option
	 *
	 * @param streamKey     streamKey
	 * @param readOffset    偏移量
	 * @param consumerGroup consumerGroup
	 */
	private void checkAndCreatGroup(String streamKey, ReadOffset readOffset, String consumerGroup) {
		if (Boolean.TRUE.equals(redisTemplate.hasKey(streamKey))) {
			StreamOperations<String, String, Object> streamOperations = redisTemplate.opsForStream();
			StreamInfo.XInfoGroups groups = streamOperations.groups(streamKey);
			if (groups.isEmpty() || groups.stream().noneMatch(data -> consumerGroup.equals(data.groupName()))) {
				creatGroup(streamKey, readOffset, consumerGroup);
			}
		} else {
			creatGroup(streamKey, readOffset, consumerGroup);
		}
	}

	/**
	 * 创建组
	 */
	private void creatGroup(String key, ReadOffset readOffset, String group) {
		StreamOperations<String, String, Object> streamOperations = redisTemplate.opsForStream();
		String groupName = streamOperations.createGroup(key, readOffset, group);
		log.info("创建组成功:{}", groupName);
	}

	/**
	 * Bean定义
	 */
	private record BeanDef(Method method, Object bean, RSMessageListener annotation) {

		public static BeanDef of(Method method, Object bean, RSMessageListener annotation) {
			return new BeanDef(method, bean, annotation);
		}
	}

	@Override
	public void run(String... args) throws Exception {
		if (CollectionUtil.isEmpty(beanDefs)) {
			return;
		}
		beanDefs.forEach(beanDef -> {
			Object bean = beanDef.bean();
			Method method = beanDef.method();
			RSMessageListener annotation = beanDef.annotation();
			log.info("读取到 @RSMessageListener 注解，开始执行监听逻辑。topic: {}, 方法: {}.{}()",
				annotation.topic(),                     // 监听的 topic
				bean.getClass().getSimpleName(),        // 方法所在类的简单类名
				method.getName());
			boolean group = annotation.isGroup();
			String topic = annotation.topic();
			ReadOffset readOffset = annotation.readOffset().getReadOffset();
			StreamOffset<String> offset = StreamOffset.create(topic, readOffset);
			if (!group) {
				redisListenerContainer.receive(offset, message -> {
					try {
						invokeMethod(method, message, bean, null);
					} catch (Exception e) {
						log.error("redis stream 消费出现异常错误：{}", e.getMessage(), e);
					} finally {
						if (annotation.trim() > 0) {
							redisTemplate.opsForStream()
								.trim(topic, annotation.trim());
						}
					}
				});
				return;
			}
			String consumerGroup = StringUtil.isBlank(annotation.consumerGroup()) ? serverInfo.getAppName() : annotation.consumerGroup();
			String consumerName = StringUtil.isBlank(annotation.consumerName()) ? serverInfo.getAddress() : annotation.consumerName();
			// 获取 StreamOffset
			checkAndCreatGroup(topic, readOffset, consumerGroup);
			// 创建消费者
			Consumer consumer = Consumer.from(consumerGroup, consumerName);
			StreamMessageListenerContainer.StreamReadRequest<String> streamReadRequest =
				StreamMessageListenerContainer
					.StreamReadRequest.builder(offset)
					.errorHandler((error) -> log.error("redis stream 异常 ：{}", error.getMessage(), error))
					.cancelOnError(e -> false)
					.consumer(consumer)
					.autoAcknowledge(false)
					.build();

			redisListenerContainer.register(streamReadRequest, message -> {
				try {
					invokeMethod(method, message, bean, consumer);
					if (annotation.autoAck()) {
						redisTemplate.opsForStream()
							.acknowledge(consumer.getGroup(), message);
					}
				} catch (Exception e) {
					log.error("redis stream 消费出现异常错误：{}", e.getMessage(), e);
				} finally {
					if (annotation.trim() > 0) {
						redisTemplate.opsForStream()
							.trim(topic, annotation.trim());
					}
				}
			});
		});
		beanDefs.clear();
		beanDefs = Collections.emptyList();
		redisListenerContainer.start();
	}
}
