package org.bedrock.common.ai.enums;

import lombok.Getter;

/**
 * AI 模型类型
 */
@Getter
public enum AiModelTypeEnum {

	CHAT("对话"),
	IMAGE("图片"),
	SPEECH("文字转语音"),
	EMBEDDING("向量"),
	;

	private final String description;

	AiModelTypeEnum(String description) {
		this.description = description;
	}
}
