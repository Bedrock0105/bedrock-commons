package org.bedrock.common.code.props;

import lombok.Data;
import org.bedrock.common.code.eventbus.enums.EventBusType;
import org.springframework.boot.context.properties.ConfigurationProperties;


/**
 * 事件总线配置属性类
 * <p>
 * 用于统一配置事件总线全局参数，根据总线类型（本地/Redis）差异化生效。
 * Redis 模式下基于固定全局主题做统一收发，内部再还原真实业务主题完成路由分发，
 * 上层业务使用方式与本地事件总线完全一致，无感知切换。
 * </p>
 */
@Data
@ConfigurationProperties(prefix = EventBusProperties.PREFIX)
public class EventBusProperties {

	/**
	 * 配置前缀
	 */
	public static final String PREFIX = "bedrock.eventbus";

	/**
	 * 是否开启事件总线功能
	 * 默认关闭，开启后才会初始化对应总线实例及监听逻辑
	 */
	private boolean enabled = false;

	/**
	 * 事件总线实现类型
	 * <ul>
	 *     <li>LOCAL：本地内存实现，仅当前进程内生效</li>
	 *     <li>REDIS：Redis Pub/Sub 跨服务实现，支持多实例集群互通</li>
	 * </ul>
	 */
	private EventBusType type = EventBusType.LOCAL;

	/**
	 * 全局统一主题（Redis 模式核心配置）
	 * <p><b>生效规则：</b></p>
	 * <ol>
	 * <li>总线类型为 LOCAL 本地模式：该配置不生效，使用业务自定义 Topic 收发消息</li>
	 * <li>总线类型为 REDIS 模式：全局唯一固定频道，作为跨服务消息传输载体</li>
	 * </ol>
	 *
	 * <p><b>整体运行流程：</b></p>
	 * <ol>
	 * <li>服务启动时，自动对该全局主题注册统一监听器；</li>
	 * <li>业务发布消息：统一往该全局主题推送，消息体内携带<b>真实业务 Topic</b>与消息内容；</li>
	 * <li>监听接收消息：从全局主题拿到数据后，解析出内部真实业务 Topic；</li>
	 * <li>内部路由：复用本地总线的树形 Topic、通配符匹配逻辑，分发给对应消费者；</li>
	 * </ol>
	 *
	 * <p><b>使用特点：</b></p>
	 * 上层业务订阅、发布接口完全复用本地总线写法，无需感知 Redis 全局频道，
	 * 框架内部完成主题透传与路由，两种模式行为保持一致。
	 */
	private String globalTopic = "bedrock:eventbus:channel";
}
