package org.bedrock.common.redis.props;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = RedisProperties.PREFIX)
public class RedisProperties {

	public static final String PREFIX = "bedrock.redis";

	private RedisSerializerEnum serializer = RedisSerializerEnum.PROTOSTUFF;

	/**
	 * Redis序列化方式枚举：定义支持的序列化策略，包含各方式的特性、适用场景及依赖信息
	 */
	public enum RedisSerializerEnum {

		/**
		 * JDK原生序列化
		 * <p>核心特点：Java原生支持，兼容性极强（支持所有可序列化类），但性能差、序列化后字节体积大</p>
		 * <p>适用场景：需有特殊性能要求，或需序列化复杂Java对象（如带实现无参构造的类）</p>
		 * <p>序列化实现类：{@link org.springframework.data.redis.serializer.JdkSerializationRedisSerializer}</p>
		 * <p>依赖：无需额外引入，Spring Data Redis自带</p>
		 */
		JDK,

		/**
		 * JSON序列化（基于Jackson）
		 * <p>核心特点：序列化结果为JSON文本，可读性强，支持跨语言（需对方解析JSON），性能中等</p>
		 * <p>适用场景：需人工查看Redis数据（如调试），或简单跨语言场景（如前后端数据交互）</p>
		 * <p>序列化实现类：{@link org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer}</p>
		 * <p>依赖：无需额外引入，Spring Boot Starter默认集成Jackson</p>
		 */
		JSON,

		/**
		 * Protostuff序列化
		 * <p>核心特点：基于Protobuf协议，二进制格式，压缩率极高（字节体积最小），支持跨语言，性能较好</p>
		 * <p>适用场景：多语言协作（如Java后端与Go/Python客户端），或需节省Redis存储空间的场景</p>
		 * <p>序列化实现类：{@link org.bedrock.common.redis.serializer.ProtostuffRedisSerializer}</p>
		 * <p>依赖：需引入Protostuff核心库：
		 * <pre>
		 * &lt;dependency&gt;
		 *     &lt;groupId&gt;io.protostuff&lt;/groupId&gt;
		 *     &lt;artifactId&gt;protostuff-core&lt;/artifactId&gt;
		 * &lt;/dependency&gt;
		 * &lt;dependency&gt;
		 *     &lt;groupId&gt;io.protostuff&lt;/groupId&gt;
		 *     &lt;artifactId&gt;protostuff-runtime&lt;/artifactId&gt;
		 * &lt;/dependency&gt;
		 * </pre>
		 */
		PROTOSTUFF,

		/**
		 * Kryo序列化
		 * <p>核心特点：Java专用二进制序列化，性能极致（序列化/反序列化速度最快），字节体积较小</p>
		 * <p>适用场景：纯Java环境，对性能要求极高的高并发场景（如高频Redis缓存读写、RPC通信）</p>
		 * <p>这个有bug，有时候会反序列化失败</p>
		 * <p>序列化实现类：{@link org.bedrock.common.redis.serializer.KryoRedisSerializer}</p>
		 * <p>依赖：需引入Kryo库：
		 * <pre>
		 * &lt;dependency&gt;
		 *     &lt;groupId&gt;com.esotericsoftware&lt;/groupId&gt;
		 *     &lt;artifactId&gt;kryo&lt;/artifactId&gt;
		 * &lt;/dependency&gt;
		 * </pre>
		 */
		KRYO,

		/**
		 * FST序列化
		 * <p>核心特点：Java专用二进制序列化，性能接近Kryo，开箱即用（线程安全、无需手动注册类），兼容性极佳</p>
		 * <p>适用场景：纯Java环境，追求性能与易用性平衡（如普通业务系统的Redis缓存）</p>
		 * <p>序列化实现类：{@link org.bedrock.common.redis.serializer.FstRedisSerializer}</p>
		 * <p>依赖：需引入FST库：
		 * <pre>
		 * &lt;dependency&gt;
		 *     &lt;groupId&gt;de.ruedigermoeller&lt;/groupId&gt;
		 *     &lt;artifactId&gt;fst&lt;/artifactId&gt;
		 * &lt;/dependency&gt;
		 * </pre>
		 */
		FST
	}

	/**
	 * Redis Stream配置
	 */
	private Stream stream = new Stream();

	@Getter
	@Setter
	public static class Stream {

		public static final String PREFIX = RedisProperties.PREFIX + ".stream";

		/**
		 * 是否启用Redis Stream
		 */
		private boolean enabled = false;

		/**
		 * Stream批量处理大小
		 */
		private int batchSize = 10;

		/**
		 * Stream拉取消息超时时间
		 * 单位秒 最好不要太大
		 */
		private int secondsPollTimeout = 5;

		/**
		 * 核心线程数
		 */
		private int corePoolSize = 10;

		/**
		 * 最大线程数
		 */
		private int maxPoolSize = 100;

		/**
		 * 任务队列的大小
		 */
		private int queueCapacity = 1000;

		/**
		 * 线程池名的前缀
		 */
		private String threadNamePrefix = "redisListenerStreamExecutor-";

		/**
		 * 允许线程的空闲时间
		 */
		private int keepAliveSeconds = 30;
	}

}
