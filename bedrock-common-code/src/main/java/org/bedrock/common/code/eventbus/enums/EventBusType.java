package org.bedrock.common.code.eventbus.enums;

import lombok.Getter;

/**
 * 事件总线实现类型枚举
 *
 */
@Getter
public enum EventBusType {
	/**
	 * 本地内存模式，仅当前进程内生效
	 */
	LOCAL("本地事件总线"),

	/**
	 * Redis 发布订阅模式，支持跨服务、跨实例通信
	 */
	REDIS("Redis事件总线");

	private final String title;

	EventBusType(String title) {
		this.title = title;
	}
}
