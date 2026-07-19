package org.bedrock.common.code.eventbus;

import lombok.Getter;
import org.bedrock.common.code.util.AntPathUtil;
import org.bedrock.common.code.util.CollectionUtil;
import org.bedrock.common.code.util.StringUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * 主题节点实体
 * <p>
 * 采用树形结构组织多级 Topic，支持通配符 {@code *}、{@code **} 匹配规则，
 * 实现主题层级管理、订阅者维护、主题查找匹配能力，为事件总线提供树形 Topic 路由。
 * <p>
 * 结构说明：
 * 1. 根节点：通过 {@link #createRoot()} 创建，层级 level = 0
 * 2. 子节点：以 {@code /} 作为层级分隔符，自动递归构建树形结构
 * 3. 通配符规则：
 *    <ul>
 *        <li>{@code *}：匹配单个层级节点</li>
 *        <li>{@code **}：匹配任意多级节点</li>
 *    </ul>
 * 4. 线程安全：订阅者集合、子节点集合均做并发安全处理
 *
 */
public final class Topic {

	/**
	 * 当前主题下的订阅者集合，volatile 保证可见性，增删使用同步锁保证线程安全
	 */
	private volatile List<Consumer<?>> subscribers = new ArrayList<>();

	/**
	 * 父主题节点，根节点 parent 为 null
	 */
	@Getter
	private final Topic parent;

	/**
	 * 完整全路径主题名称（拼接父节点路径，以 / 分隔）
	 */
	private final String topics;

	/**
	 * 完整全路径主题名称
	 */
	private final String[] topicsArr;

	/**
	 * 是否包含通配符
	 */
	private final boolean isWildcard;

	/**
	 * 节点层级，根节点为 0，每向下一级层级 +1
	 */
	@Getter
	private final Integer level;

	/**
	 * 子主题节点集合，Key：单级主题名，Value：子主题节点
	 * 使用延迟初始化 + 双重检查锁保证并发安全
	 */
	private volatile Map<String, Topic> children;

	/**
	 * 私有构造方法，仅内部构建树形节点
	 *
	 * @param parent 父主题节点
	 * @param topic  当前单级主题名称
	 */
	private Topic(Topic parent, String topic) {
		this.parent = parent;
		this.level = parent == null ? 0 : parent.level + 1;
		// 拼接完整路径主题
		if (parent != null) {
			this.topics = Objects.equals(parent.topics, "/") ? "/" + topic : parent.topics + "/" + topic;
		} else {
			this.topics = topic;
		}
		topic = this.topics;
		// 移除开头斜杠
		if (topic.charAt(0) == '/') {
			topic = this.topics.substring(1);
		}
		if (StringUtil.isBlank(topic)) {
			this.topicsArr = new String[]{};
		} else {
			this.topicsArr = StringUtil.delimitedListToStringArray(topic, "/");
		}
		this.isWildcard = topics.contains("**");
	}

	/**
	 * 创建 Topic 根节点
	 *
	 * @return 根主题节点
	 */
	public static Topic createRoot() {
		return new Topic(null, "/");
	}

	/**
	 * 追加/创建子主题节点，支持多级路径（以 / 分隔）
	 *
	 * @param topic 多级主题路径
	 * @return 最终末端主题节点
	 */
	public Topic appendTopic(String topic) {
		if (StringUtil.isBlank(topic) || topic.equals("/")) {
			return this;
		}
		return createTopic(topic);
	}

	/**
	 * 递归创建多级主题节点（内部私有方法）
	 *
	 * @param topic 待创建的多级主题路径
	 * @return 路径末端主题节点
	 */
	private Topic createTopic(String topic) {
		// 移除开头斜杠
		if (topic.charAt(0) == '/') {
			topic = topic.substring(1);
		}
		// 按 / 分割为多级分段
		String[] split = StringUtil.delimitedListToStringArray(topic, "/");
		Topic parent = getChildren().computeIfAbsent(split[0], k -> new Topic(this, k));
		// 循环递归创建后续层级节点
		for (int i = 1; i < split.length; i++) {
			Topic finalParent = parent;
			parent = parent.getChildren().computeIfAbsent(split[i], k -> new Topic(finalParent, k));
		}
		return parent;
	}

	/**
	 * 为当前主题添加订阅者
	 *
	 * @param consumer 消息消费回调
	 */
	public void subscriber(Consumer<?> consumer) {
		synchronized (this) {
			subscribers.add(consumer);
		}
	}

	/**
	 * 移除当前主题的指定订阅者
	 *
	 * @param consumer 待移除的消费回调
	 */
	public void unsubscriber(Consumer<?> consumer) {
		synchronized (this) {
			subscribers.remove(consumer);
		}
	}

	/**
	 * 获取当前主题下所有订阅者（通配符版本）
	 *
	 * @return 订阅者集合
	 */
	public List<Consumer<?>> subscribers() {
		return this.subscribers;
	}

	/**
	 * 获取当前主题下所有订阅者（类型安全版本）
	 *
	 * @return 订阅者集合
	 */
	@SuppressWarnings("unchecked")
	public <T> List<Consumer<T>> getSubscribers() {
		return (List<Consumer<T>>) (List<?>) this.subscribers;
	}

	/**
	 * 根据主题路径查找匹配的所有主题节点（支持通配符）
	 *
	 * @param topic 目标主题路径
	 * @return 去重后的匹配主题节点集合
	 */
	public List<Topic> findTopic(String topic) {
		List<Topic> list = new ArrayList<>();
		findTopic(topic, list);
		// 节点去重后返回
		return list.stream().distinct().collect(Collectors.toList());
	}

	/**
	 * 查找主题入口重载方法（内部递归调用）
	 *
	 * @param topic 目标主题路径
	 * @param list  收集匹配到的主题节点
	 */
	private void findTopic(String topic, List<Topic> list) {
		// 移除开头斜杠
		if (topic.charAt(0) == '/') {
			topic = topic.substring(1);
		}
		String[] split = StringUtil.delimitedListToStringArray(topic, "/");
		findTopic(split, list, Collections.singleton(this));
	}

	/**
	 * 递归遍历子节点，匹配主题（核心递归方法）
	 *
	 * @param topics     分割后的目标主题分段数组
	 * @param list       收集匹配节点
	 * @param parentList 当前待遍历的父节点集合
	 */
	private void findTopic(String[] topics, List<Topic> list, Set<Topic> parentList) {
		Set<Topic> topicRegistries = new HashSet<>();
		for (Topic parent : parentList) {
			Map<String, Topic> children = parent.getChildren();
			for (Topic value : children.values()) {
				// 层级超过目标分段长度，直接跳出循环
				if (value.level > topics.length) {
					break;
				}
				/**
				 * 通配符匹配
				 */
				if (value.isWildcard && AntPathUtil.match(value.topics, "/".concat(StringUtil.arrayToDelimitedString(topics, "/")))) {
					list.add(value);
					topicRegistries.add(value);
					continue;
				}
				// 按分段匹配主题
				if (value.matchTopic(topics)) {
					// 层级完全一致，命中目标节点
					if (value.level == topics.length) {
						list.add(value);
					} else {
						// 层级不足，加入集合继续向下递归查找
						topicRegistries.add(value);
					}
				}
			}
			// 存在待继续遍历的节点，递归查询子节点
			if (CollectionUtil.isNotEmpty(topicRegistries)) {
				findTopic(topics, list, topicRegistries);
			}
		}
	}

	/**
	 * 校验当前节点是否匹配目标主题分段数组（支持 *、** 通配）
	 *
	 * @param topics 目标主题分段数组
	 * @return true = 匹配成功，false = 匹配失败
	 */
	private boolean matchTopic(String[] topics) {
		// 根节点默认匹配所有
		if (this.topics.equals("/")) {
			return true;
		}
		// 当前节点层级数 > 目标层级数，直接不匹配
		if (topicsArr.length > topics.length) {
			return false;
		}
		// 逐段比对，支持 * / ** 通配
		for (int i = 0; i < topicsArr.length; i++) {
			if (topicsArr[i].equals("*")) {
				continue;
			}
			if (!topicsArr[i].equals(topics[i])) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 获取子节点集合，延迟初始化 + 双重检查锁保证并发安全
	 *
	 * @return 子节点 Map
	 */
	private Map<String, Topic> getChildren() {
		if (this.children == null) {
			synchronized (this) {
				if (this.children == null) {
					this.children = new ConcurrentHashMap<>();
				}
			}
		}
		return this.children;
	}
}
