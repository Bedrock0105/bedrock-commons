package org.bedrock.common.code.eventbus;

import lombok.extern.slf4j.Slf4j;
import org.bedrock.common.code.eventbus.model.EventMessage;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

/**
 * 事件总线抽象类
 * <p>
 * 提供基于主题（Topic）的发布-订阅模式实现，支持异步消息分发。
 * 内部维护一个 Topic 树形结构以支持层级主题匹配，并通过线程池实现非阻塞的消息消费。
 */
@Slf4j
public abstract class AbstractEventBus implements EventBus {

	/**
	 * 自定义线程池，用于异步执行消息分发与消费逻辑
	 * 确保消息处理不会阻塞发布线程，提高系统吞吐量
	 */
	protected final Executor executor;

	/**
	 * Topic 树形结构根节点，统一管理所有业务主题
	 * 支持层级主题（如 "order/create"），便于进行通配符或前缀匹配查找
	 */
	protected final Topic eventBus = Topic.createRoot();

	/**
	 * 订阅配置与对应 Topic 节点映射关系
	 * Key：订阅配置对象（Subscribes），Value：该配置绑定的所有主题节点集合
	 * 用途：
	 * 1. 防止重复订阅同一配置
	 * 2. 在取消订阅时，快速定位并移除该配置关联的所有 Topic 下的监听器
	 */
	protected final Map<Subscribes<?>, Set<Topic>> subscribes = new ConcurrentHashMap<>();

	/**
	 * 订阅指定的主题配置
	 * <p>
	 * 将订阅配置中的每个主题字符串注册到 Topic 树中，并将对应的消费者绑定到这些主题节点上。
	 * 如果该订阅配置已存在，则跳过以避免重复注册。
	 *
	 * @param topic 订阅配置对象，包含主题列表和对应的消费者逻辑
	 */
	@Override
	public void subscribe(Subscribes<?> topic) {
		// 防止重复订阅
		if (subscribes.containsKey(topic)) {
			log.warn("[{}] 订阅已存在，跳过重复订阅: {}", getEventBusType().getTitle(), topic);
			return;
		}

		Set<Topic> topicSet = new HashSet<>();
		for (String to : topic.getTopic()) {
			// 在 Topic 树中追加或获取主题节点
			Topic topicRegistry = eventBus.appendTopic(to);
			Consumer<? extends EventMessage<?>> subscribe = topic.getSubscribe();
			// 将消费者绑定到主题节点
			topicRegistry.subscriber(subscribe);
			topicSet.add(topicRegistry);
		}
		// 记录订阅配置与主题节点的映射关系
		subscribes.put(topic, topicSet);
	}

	/**
	 * 取消订阅指定的主题配置
	 * <p>
	 * 从映射关系中移除该配置，并遍历其关联的所有 Topic 节点，移除对应的消费者监听器。
	 *
	 * @param subscribe 需要取消的订阅配置对象
	 */
	@Override
	public void unsubscribe(Subscribes<?> subscribe) {
		Set<Topic> topicRegistries = subscribes.remove(subscribe);
		if (topicRegistries != null) {
			for (Topic topicRegistry : topicRegistries) {
				// 从主题节点中移除对应的消费者
				topicRegistry.unsubscriber(subscribe.getSubscribe());
			}
			log.debug("[{}] 成功取消订阅: {}", getEventBusType().getTitle(), subscribe);
		} else {
			log.warn("[{}] 订阅不存在，无需取消: {}", getEventBusType().getTitle(), subscribe);
		}
	}

	/**
	 * 发布消息到指定主题
	 * <p>
	 * 该方法为异步执行，通过线程池提交任务。
	 * 1. 在 Topic 树中查找匹配的主题节点。
	 * 2. 遍历所有匹配节点的订阅者。
	 * 3. 为每个订阅者再次提交异步任务执行消费逻辑，确保单个消费者的异常不影响其他消费者。
	 *
	 * @param message 发布的消息内容
	 */
	protected void onMessage(EventMessage<?> message) {
		CompletableFuture.runAsync(() -> {
			// 查找匹配的主题节点列表
			List<? extends Topic> eventBusTopic = eventBus.findTopic(message.topic());
			if (eventBusTopic == null || eventBusTopic.isEmpty()) {
				log.debug("[{}] 主题[{}] 无匹配的订阅者", getEventBusType().getTitle(), message.topic());
				return;
			}

			// 遍历所有匹配的主题节点
			eventBusTopic.forEach(t -> {
				List<Consumer<EventMessage<?>>> subscribers = t.getSubscribers();
				if (subscribers != null && !subscribers.isEmpty()) {
					// 遍历该节点下的所有订阅者
					subscribers.forEach(c -> {
						try {
							// 为每个订阅者单独提交异步任务，隔离异常影响
							CompletableFuture.runAsync(() -> c.accept(message), executor)
								.exceptionally(e -> {
									log.error("[{}] 订阅者[{}] 消费消息发生异常", getEventBusType().getTitle(), c, e);
									return null;
								});
						} catch (Exception e) {
							// 捕获提交任务时的异常（极少发生，通常由线程池拒绝策略引起）
							log.error("[{}] 主题[{}] 消费消息发生异常", getEventBusType().getTitle(), message.topic(), e);
						}
					});
				}
			});
		}, executor).exceptionally(e -> {
			// 捕获外层异步任务执行过程中的异常
			log.error("[{}] 主题[{}] 消息发布异步任务执行失败", getEventBusType().getTitle(), message.topic(), e);
			return null;
		});
	}

	/**
	 * 构造函数
	 *
	 * @param executor 用于异步处理消息的线程池
	 */
	public AbstractEventBus(Executor executor) {
		this.executor = executor;
	}
}
