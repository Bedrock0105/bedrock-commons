package org.bedrock.common.ai.factory.model.impl;

import io.micrometer.observation.ObservationRegistry;
import org.bedrock.common.ai.enums.AiPlatformEnum;
import org.bedrock.common.ai.factory.model.AiModelBuilder;
import org.bedrock.common.ai.model.factory.model.ChatModelCreateParam;
import org.bedrock.common.ai.model.factory.model.EmbeddingModelCreateParam;
import org.bedrock.common.ai.model.factory.model.ImageModelCreateParam;
import org.bedrock.common.code.util.SpringUtil;
import org.bedrock.common.code.util.StringUtil;
import org.bedrock.common.spi.autoservice.AutoService;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.ai.zhipuai.ZhiPuAiChatModel;
import org.springframework.ai.zhipuai.ZhiPuAiChatOptions;
import org.springframework.ai.zhipuai.ZhiPuAiEmbeddingModel;
import org.springframework.ai.zhipuai.ZhiPuAiEmbeddingOptions;
import org.springframework.ai.zhipuai.ZhiPuAiImageModel;
import org.springframework.ai.zhipuai.api.ZhiPuAiApi;
import org.springframework.ai.zhipuai.api.ZhiPuAiImageApi;
import org.springframework.web.client.RestClient;

/**
 * 智谱清言模型构建器。
 * <p>
 * 文档依据（Spring AI 1.1.x）：图像生成支持 {@link ZhiPuAiImageModel} / CogView。
 * Spring AI 1.1.x 未提供智谱文字转语音、语音转文字模型实现，故不覆盖对应方法。
 * </p>
 */
@AutoService(AiModelBuilder.class)
public class ZhiPuAiModelBuilder implements AiModelBuilder {

	private final ToolCallingManager toolCallingManager;

	public ZhiPuAiModelBuilder() {
		this.toolCallingManager = SpringUtil.getBean(ToolCallingManager.class);
	}

	@Override
	public ChatModel createChatModel(ChatModelCreateParam param) {
		ZhiPuAiApi.Builder builder = ZhiPuAiApi.builder()
			.apiKey(param.key());
		if (StringUtil.isNotBlank(param.url())) {
			builder.baseUrl(param.url());
		}
		if (StringUtil.isNotBlank(param.path())) {
			builder.completionsPath(param.path());
		}

		return new ZhiPuAiChatModel(builder.build(),
			ZhiPuAiChatOptions
				.builder()
				.model(ZhiPuAiApi.DEFAULT_CHAT_MODEL)
				.temperature(0.7)
				.build(),
			toolCallingManager,
			RetryUtils.DEFAULT_RETRY_TEMPLATE,
			ObservationRegistry.NOOP);
	}

	@Override
	public EmbeddingModel createEmbeddingModel(EmbeddingModelCreateParam param) {
		ZhiPuAiApi.Builder builder = ZhiPuAiApi.builder()
			.apiKey(param.key());
		if (StringUtil.isNotBlank(param.url())) {
			builder.baseUrl(param.url());
		}
		if (StringUtil.isNotBlank(param.path())) {
			builder.embeddingsPath(param.path());
		}
		return new ZhiPuAiEmbeddingModel(builder.build(), MetadataMode.EMBED,
			ZhiPuAiEmbeddingOptions.builder().model(param.model()).build());
	}

	@Override
	public ImageModel createImageModel(ImageModelCreateParam param) {
		ZhiPuAiImageApi imageApi;
		if (StringUtil.isNotBlank(param.url())) {
			imageApi = new ZhiPuAiImageApi(param.url(), param.key(), RestClient.builder());
		}
		else {
			imageApi = new ZhiPuAiImageApi(param.key());
		}
		return new ZhiPuAiImageModel(imageApi);
	}

	@Override
	public AiPlatformEnum getAiPlatform() {
		return AiPlatformEnum.ZHI_PU;
	}

}
