package org.bedrock.common.code.eventbus;

import org.bedrock.common.code.eventbus.enums.EventBusType;

/**
 * 事件总线统一顶层接口
 * <p>
 * 基于 Topic 实现发布-订阅模式，支持本地内存、Redis Pub/Sub 多种实现。
 * 核心特性：消息实时推送，<b>先发布、后订阅的客户端无法接收历史消息</b>，不做消息持久化与回放。
 * 支持单/多主题订阅、通配符主题匹配，适用于进程内组件通信、跨服务实时事件通知场景。
 * </p>
 *
 * <p>
 * 通配符规则与使用约束：
 * <ul>
 *     <li>{@code *}：匹配单个层级节点</li>
 *     <li>{@code **}：匹配任意多级节点，<b>仅建议放在路径末尾使用</b></li>
 * </ul>
 * 主题路径以 / 作为层级分隔符。
 * <br>
 * </p>
 *
 * <h3>使用说明</h3>
 * <ol>
 * <li>订阅：构造 {@link Subscribes} 配置对象，指定主题与消费回调，调用 {@link #subscribe(Subscribes)} 完成订阅</li>
 * <li>发消息：指定目标 Topic，调用 {@link #publish(String, Object)} 发布消息，所有在线订阅者实时接收</li>
 * <li>取消订阅：传入原有订阅配置对象，调用 {@link #unsubscribe(Subscribes)} 解绑消费回调</li>
 * <li>类型判断：通过 {@link #getEventBusType()} 区分当前是本地实现还是跨服务实现</li>
 * </ol>
 *
 * <h3>简单示例</h3>
 * <pre>{@code
 * // 1. 注入事件总线实例
 * @Autowired
 * private EventBus eventBus;
 *
 * // 2. 构造订阅配置，支持单个/多个普通主题
 * Subscribes<String> subscribes = Subscribes.<String>builder()
 *         .topic("order.create", "order.update")
 *         .subscribe(msg -> System.out.println("收到事件：" + msg))
 *         .build();
 *
 * // 3. 执行订阅
 * eventBus.subscribe(subscribes);
 * eventBus.subscribe(wildcardSub);
 *
 * // 4. 发布消息
 * eventBus.publish("order.create", "新订单已创建");
 * eventBus.publish("order/1001/create", "订单创建完成");
 *
 * // 5. 取消订阅
 * eventBus.unsubscribe(subscribes);
 * eventBus.unsubscribe(wildcardSub);
 * }</pre>
 *
 * @see Subscribes
 * @see EventBusType
 */

public interface EventBus {

	/**
	 * 基于订阅配置完成主题订阅
	 *
	 * @param subscribe 订阅配置实体，包含主题数组、消息消费回调
	 */
	void subscribe(Subscribes<?> subscribe);

	/**
	 * 基于订阅配置取消对应主题的订阅
	 *
	 * @param subscribe 待取消的订阅配置实体
	 */
	void unsubscribe(Subscribes<?> subscribe);

	/**
	 * 向指定主题发布消息
	 * 消息仅推送给当前已在线的订阅者，无历史消息回放
	 *
	 * @param topic   消息主题路径，支持通配符匹配
	 * @param message 待发布的消息内容
	 */
	void publish(String topic, Object message);

	/**
	 * 获取当前事件总线实现类型
	 *
	 * @return 总线类型 {@link EventBusType}
	 */
	EventBusType getEventBusType();
}
