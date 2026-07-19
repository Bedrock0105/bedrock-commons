package org.bedrock.common.ai.factory.model.impl;

import org.bedrock.common.ai.enums.AiPlatformEnum;
import org.bedrock.common.ai.factory.model.AiModelBuilder;
import org.bedrock.common.ai.model.factory.model.ChatModelCreateParam;
import org.bedrock.common.ai.model.factory.model.EmbeddingModelCreateParam;
import org.bedrock.common.code.util.SpringUtil;
import org.bedrock.common.code.util.StringUtil;
import org.bedrock.common.spi.autoservice.AutoService;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.minimax.MiniMaxChatModel;
import org.springframework.ai.minimax.MiniMaxChatOptions;
import org.springframework.ai.minimax.MiniMaxEmbeddingModel;
import org.springframework.ai.minimax.MiniMaxEmbeddingOptions;
import org.springframework.ai.minimax.api.MiniMaxApi;
import org.springframework.ai.model.tool.ToolCallingManager;

/**
 * MiniMax Ai Chat Model Builder
 */
@AutoService(AiModelBuilder.class)
public class MiniMaxAiModelBuilder implements AiModelBuilder {

	private final ToolCallingManager toolCallingManager;

	public MiniMaxAiModelBuilder() {
		this.toolCallingManager = SpringUtil.getBean(ToolCallingManager.class);
	}

	@Override
	public ChatModel createChatModel(ChatModelCreateParam param) {
		MiniMaxApi miniMaxApi;
		if (StringUtil.isNotBlank(param.url())) {
			miniMaxApi = new MiniMaxApi(param.url(), param.key());
		} else {
			miniMaxApi = new MiniMaxApi(param.key());
		}
		return new MiniMaxChatModel(miniMaxApi,
			MiniMaxChatOptions
				.builder()
				.model(MiniMaxApi.DEFAULT_CHAT_MODEL)
				.temperature(0.7)
				.build(),
			toolCallingManager);
	}

	@Override
	public EmbeddingModel createEmbeddingModel(EmbeddingModelCreateParam param) {
		MiniMaxApi miniMaxApi;
		if (StringUtil.isNotBlank(param.url())) {
			miniMaxApi = new MiniMaxApi(param.url(), param.key());
		}
		else {
			miniMaxApi = new MiniMaxApi(param.key());
		}
		String model = StringUtil.isNotBlank(param.model()) ? param.model() : MiniMaxApi.DEFAULT_EMBEDDING_MODEL;
		return new MiniMaxEmbeddingModel(miniMaxApi, MetadataMode.EMBED,
			MiniMaxEmbeddingOptions.builder().model(model).build());
	}

	@Override
	public AiPlatformEnum getAiPlatform() {
		return AiPlatformEnum.MINIMAX;
	}

}
