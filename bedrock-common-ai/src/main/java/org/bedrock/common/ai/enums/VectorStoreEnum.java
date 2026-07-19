package org.bedrock.common.ai.enums;

import lombok.Getter;

/**
 * 向量存储枚举
 */
@Getter
public enum VectorStoreEnum {
	REDIS("redis", "Redis 向量"),
	MILVUS("Milvus", "Milvus 向量"),
	ELASTICSEARCH("elasticsearch", "Elasticsearch 向量"),
	SIMPLE("simple", "本地向量"),
	;

	private final String name;

	private final String description;

	VectorStoreEnum(String name, String description) {
		this.name = name;
		this.description = description;
	}
}
