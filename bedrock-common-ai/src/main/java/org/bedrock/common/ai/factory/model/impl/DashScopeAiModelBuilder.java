package org.bedrock.common.ai.factory.model.impl;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.api.DashScopeAudioSpeechApi;
import com.alibaba.cloud.ai.dashscope.api.DashScopeAudioTranscriptionApi;
import com.alibaba.cloud.ai.dashscope.api.DashScopeImageApi;
import com.alibaba.cloud.ai.dashscope.audio.transcription.DashScopeAudioTranscriptionModel;
import com.alibaba.cloud.ai.dashscope.audio.transcription.DashScopeAudioTranscriptionOptions;
import com.alibaba.cloud.ai.dashscope.audio.tts.DashScopeAudioSpeechModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.embedding.text.DashScopeEmbeddingModel;
import com.alibaba.cloud.ai.dashscope.embedding.text.DashScopeEmbeddingOptions;
import com.alibaba.cloud.ai.dashscope.image.DashScopeImageModel;
import org.bedrock.common.ai.enums.AiPlatformEnum;
import org.bedrock.common.ai.factory.model.AiModelBuilder;
import org.bedrock.common.ai.model.factory.model.ChatModelCreateParam;
import org.bedrock.common.ai.model.factory.model.EmbeddingModelCreateParam;
import org.bedrock.common.ai.model.factory.model.ImageModelCreateParam;
import org.bedrock.common.ai.model.factory.model.TextToSpeechModelCreateParam;
import org.bedrock.common.ai.model.factory.model.TranscriptionModelCreateParam;
import org.bedrock.common.code.util.SpringUtil;
import org.bedrock.common.code.util.StringUtil;
import org.bedrock.common.spi.autoservice.AutoService;
import org.springframework.ai.audio.transcription.TranscriptionModel;
import org.springframework.ai.audio.tts.TextToSpeechModel;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.model.SimpleApiKey;
import org.springframework.ai.model.tool.ToolCallingManager;

/**
 * 阿里云通义千问（DashScope）模型构建器。
 * <p>
 * 文档依据（spring-ai-alibaba-dashscope 1.1.x）：
 * <ul>
 *   <li>图像：DashScopeImageModel</li>
 *   <li>文字转语音：DashScopeAudioSpeechModel</li>
 *   <li>语音转文字：DashScopeAudioTranscriptionModel（实现 Spring AI TranscriptionModel）</li>
 * </ul>
 * </p>
 */
@AutoService(AiModelBuilder.class)
public class DashScopeAiModelBuilder implements AiModelBuilder {

	private final ToolCallingManager toolCallingManager;

	public DashScopeAiModelBuilder() {
		this.toolCallingManager = SpringUtil.getBean(ToolCallingManager.class);
	}

	@Override
	public ChatModel createChatModel(ChatModelCreateParam param) {
		DashScopeApi.Builder builder = DashScopeApi
			.builder()
			.apiKey(param.key());
		if (StringUtil.isNotBlank(param.url())) {
			builder.baseUrl(param.url());
		}
		if (StringUtil.isNotBlank(param.path())) {
			builder.completionsPath(param.path());
		}
		return DashScopeChatModel.builder()
			.dashScopeApi(builder.build())
			.toolCallingManager(toolCallingManager)
			.build();
	}

	@Override
	public EmbeddingModel createEmbeddingModel(EmbeddingModelCreateParam param) {
		DashScopeApi.Builder builder = DashScopeApi
			.builder()
			.apiKey(param.key());
		if (StringUtil.isNotBlank(param.url())) {
			builder.baseUrl(param.url());
		}
		if (StringUtil.isNotBlank(param.path())) {
			builder.embeddingsPath(param.path());
		}
		return DashScopeEmbeddingModel.builder()
			.dashScopeApi(builder.build())
			.defaultOptions(DashScopeEmbeddingOptions
				.builder()
				.textType(DashScopeApi.DEFAULT_EMBEDDING_TEXT_TYPE)
				.model(param.model())
				.build())
			.build();
	}

	@Override
	public ImageModel createImageModel(ImageModelCreateParam param) {
		DashScopeImageApi.Builder builder = DashScopeImageApi.builder()
			.apiKey(param.key());
		if (StringUtil.isNotBlank(param.url())) {
			builder.baseUrl(param.url());
		}
		if (StringUtil.isNotBlank(param.path())) {
			builder.imagesPath(param.path());
		}
		return new DashScopeImageModel(builder.build());
	}

	@Override
	public TextToSpeechModel createTextToSpeechModel(TextToSpeechModelCreateParam param) {
		DashScopeAudioSpeechApi.Builder builder = DashScopeAudioSpeechApi.builder()
			.apiKey(new SimpleApiKey(param.key()));
		if (StringUtil.isNotBlank(param.url())) {
			builder.baseUrl(param.url());
		}
		return new DashScopeAudioSpeechModel(builder.build());
	}

	@Override
	public TranscriptionModel createTranscriptionModel(TranscriptionModelCreateParam param) {
		DashScopeAudioTranscriptionApi.Builder builder = DashScopeAudioTranscriptionApi.builder()
			.apiKey(new SimpleApiKey(param.key()));
		if (StringUtil.isNotBlank(param.url())) {
			builder.baseUrl(param.url());
		}
		return new DashScopeAudioTranscriptionModel(
			builder.build(),
			DashScopeAudioTranscriptionOptions.builder().build());
	}

	@Override
	public AiPlatformEnum getAiPlatform() {
		return AiPlatformEnum.DASH_SCOPE;
	}

}
