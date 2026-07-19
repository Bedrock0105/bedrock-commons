package org.bedrock.common.ai.enums;

/**
 * Token 用量数据来源。
 */
public enum TokenUsageSource {

	/**
	 * 模型响应元数据上报（{@link org.springframework.ai.chat.metadata.Usage}）。
	 */
	MODEL_REPORTED,

	/**
	 * 本地估算（jtokkit 对请求/响应文本计数）。
	 */
	LOCAL_ESTIMATED,

}
