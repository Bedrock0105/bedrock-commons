package org.bedrock.common.ai.factory.model.impl;

import com.google.genai.Client;
import com.google.genai.types.HttpOptions;
import org.bedrock.common.ai.enums.AiPlatformEnum;
import org.bedrock.common.ai.factory.model.AiModelBuilder;
import org.bedrock.common.ai.model.factory.model.ChatModelCreateParam;
import org.bedrock.common.ai.model.factory.model.EmbeddingModelCreateParam;
import org.bedrock.common.code.util.SpringUtil;
import org.bedrock.common.code.util.StringUtil;
import org.bedrock.common.spi.autoservice.AutoService;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.ai.google.genai.GoogleGenAiEmbeddingConnectionDetails;
import org.springframework.ai.google.genai.text.GoogleGenAiTextEmbeddingModel;
import org.springframework.ai.google.genai.text.GoogleGenAiTextEmbeddingOptions;
import org.springframework.ai.model.tool.ToolCallingManager;

/**
 * Gemini 谷歌大模型构造器
 */
@AutoService(AiModelBuilder.class)
public class GeminiAiModelBuilder implements AiModelBuilder {

	/**
	 * 工具调用管理器，用于函数调用能力处理
	 */
	private final ToolCallingManager toolCallingManager;

	/**
	 * 无参构造，从Spring容器获取 ToolCallingManager 单例
	 */
	public GeminiAiModelBuilder() {
		this.toolCallingManager = SpringUtil.getBean(ToolCallingManager.class);
	}

	/**
	 * 创建 Gemini 对话模型实例
	 */
	@Override
	public ChatModel createChatModel(ChatModelCreateParam param) {
		Client.Builder builder = Client.builder()
			.apiKey(param.key());

		if (StringUtil.isNotBlank(param.url())) {
			builder.httpOptions(HttpOptions.builder()
				.baseUrl(param.url())
				.build());
		}
		return GoogleGenAiChatModel.builder()
			.genAiClient(builder.build())
			.toolCallingManager(toolCallingManager)
			.build();
	}

	@Override
	public EmbeddingModel createEmbeddingModel(EmbeddingModelCreateParam param) {

		GoogleGenAiEmbeddingConnectionDetails connectionDetails =
			GoogleGenAiEmbeddingConnectionDetails.builder()
				.apiKey(param.key())
				.build();

		GoogleGenAiTextEmbeddingOptions options = GoogleGenAiTextEmbeddingOptions.builder()
			.model(param.model())
			.taskType(GoogleGenAiTextEmbeddingOptions.TaskType.RETRIEVAL_DOCUMENT)
			.build();

		return new GoogleGenAiTextEmbeddingModel(connectionDetails, options);
	}

	/**
	 * 获取当前构造器绑定的AI平台枚举
	 *
	 * @return AI平台标识 Gemini
	 */
	@Override
	public AiPlatformEnum getAiPlatform() {
		return AiPlatformEnum.GEMINI;
	}
}
