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
import org.springframework.ai.mistralai.MistralAiChatModel;
import org.springframework.ai.mistralai.MistralAiEmbeddingModel;
import org.springframework.ai.mistralai.MistralAiEmbeddingOptions;
import org.springframework.ai.mistralai.api.MistralAiApi;
import org.springframework.ai.model.tool.ToolCallingManager;

/**
 * Mistral AI聊天模型构建器
 */
@AutoService(AiModelBuilder.class)
public class MistralAiModelBuilder implements AiModelBuilder {

	private final ToolCallingManager toolCallingManager;

	public MistralAiModelBuilder() {
		this.toolCallingManager = SpringUtil.getBean(ToolCallingManager.class);
	}

	@Override
	public ChatModel createChatModel(ChatModelCreateParam param) {
		MistralAiApi.Builder builder = MistralAiApi.builder()
			.apiKey(param.key());
		if (StringUtil.isNotBlank(param.url())) {
			builder.baseUrl(param.url());
		}
		return MistralAiChatModel.builder()
			.mistralAiApi(builder.build())
			.toolCallingManager(toolCallingManager)
			.build();

	}

	@Override
	public EmbeddingModel createEmbeddingModel(EmbeddingModelCreateParam param) {
		MistralAiApi.Builder builder = MistralAiApi.builder()
			.apiKey(param.key());
		if (StringUtil.isNotBlank(param.url())) {
			builder.baseUrl(param.url());
		}
		return MistralAiEmbeddingModel.builder()
			.mistralAiApi(builder.build())
			.options(MistralAiEmbeddingOptions.builder().withModel(param.model()).build())
			.build();
	}

	@Override
	public AiPlatformEnum getAiPlatform() {
		return AiPlatformEnum.MISTRAL;
	}

}
