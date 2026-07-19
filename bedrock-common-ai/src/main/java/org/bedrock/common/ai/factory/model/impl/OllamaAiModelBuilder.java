package org.bedrock.common.ai.factory.model.impl;

import org.bedrock.common.ai.enums.AiPlatformEnum;
import org.bedrock.common.ai.factory.model.AiModelBuilder;
import org.bedrock.common.ai.model.factory.model.ChatModelCreateParam;
import org.bedrock.common.ai.model.factory.model.EmbeddingModelCreateParam;
import org.bedrock.common.code.util.SpringUtil;
import org.bedrock.common.code.util.StringUtil;
import org.bedrock.common.spi.autoservice.AutoService;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaEmbeddingOptions;

/**
 * Ollama
 */
@AutoService(AiModelBuilder.class)
public class OllamaAiModelBuilder implements AiModelBuilder {

	private final ToolCallingManager toolCallingManager;

	public OllamaAiModelBuilder() {
		this.toolCallingManager = SpringUtil.getBean(ToolCallingManager.class);
	}

	@Override
	public ChatModel createChatModel(ChatModelCreateParam param) {
		OllamaApi.Builder builder = OllamaApi.builder();
		if (StringUtil.isNotBlank(param.url())) {
			builder.baseUrl(param.url());
		}
		return OllamaChatModel.builder()
			.ollamaApi(builder.build())
			.toolCallingManager(toolCallingManager)
			.build();
	}

	@Override
	public EmbeddingModel createEmbeddingModel(EmbeddingModelCreateParam param) {
		OllamaApi.Builder builder = OllamaApi.builder();
		if (StringUtil.isNotBlank(param.url())) {
			builder.baseUrl(param.url());
		}
		return OllamaEmbeddingModel.builder()
			.ollamaApi(builder.build())
			.defaultOptions(OllamaEmbeddingOptions.builder().model(param.model()).build())
			.build();
	}

	@Override
	public AiPlatformEnum getAiPlatform() {
		return AiPlatformEnum.OLLAMA;
	}

}
