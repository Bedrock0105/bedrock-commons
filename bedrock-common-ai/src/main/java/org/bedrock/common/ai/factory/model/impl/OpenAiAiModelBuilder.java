package org.bedrock.common.ai.factory.model.impl;

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
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.openai.OpenAiAudioSpeechModel;
import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiImageModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.openai.api.OpenAiAudioApi;
import org.springframework.ai.openai.api.OpenAiImageApi;

/**
 * OpenAI 模型构建器。
 * <p>
 * 文档依据（Spring AI 1.1.x）：
 * <ul>
 *   <li>图像：OpenAI Image Generation</li>
 *   <li>文字转语音：OpenAI Text-to-Speech</li>
 *   <li>语音转文字：OpenAI Transcriptions</li>
 * </ul>
 * </p>
 */
@AutoService(AiModelBuilder.class)
public class OpenAiAiModelBuilder implements AiModelBuilder {

	private final ToolCallingManager toolCallingManager;

	public OpenAiAiModelBuilder() {
		this.toolCallingManager = SpringUtil.getBean(ToolCallingManager.class);
	}

	@Override
	public ChatModel createChatModel(ChatModelCreateParam param) {
		OpenAiApi.Builder builder = OpenAiApi.builder()
			.apiKey(param.key());
		if (StringUtil.isNotBlank(param.url())) {
			builder.baseUrl(param.url());
		}
		if (StringUtil.isNotBlank(param.path())) {
			builder.completionsPath(param.path());
		}
		return OpenAiChatModel
			.builder()
			.openAiApi(builder.build())
			.toolCallingManager(toolCallingManager)
			.build();
	}

	@Override
	public EmbeddingModel createEmbeddingModel(EmbeddingModelCreateParam param) {
		return OpenAiCompatibleModelFactory.create(param, null);
	}

	@Override
	public ImageModel createImageModel(ImageModelCreateParam param) {
		OpenAiImageApi.Builder builder = OpenAiImageApi.builder()
			.apiKey(param.key());
		if (StringUtil.isNotBlank(param.url())) {
			builder.baseUrl(param.url());
		}
		if (StringUtil.isNotBlank(param.path())) {
			builder.imagesPath(param.path());
		}
		return new OpenAiImageModel(builder.build());
	}

	@Override
	public TextToSpeechModel createTextToSpeechModel(TextToSpeechModelCreateParam param) {
		return new OpenAiAudioSpeechModel(buildAudioApi(param.url(), param.key()));
	}

	@Override
	public TranscriptionModel createTranscriptionModel(TranscriptionModelCreateParam param) {
		return new OpenAiAudioTranscriptionModel(buildAudioApi(param.url(), param.key()));
	}

	@Override
	public AiPlatformEnum getAiPlatform() {
		return AiPlatformEnum.OPENAI;
	}

	private OpenAiAudioApi buildAudioApi(String url, String key) {
		OpenAiAudioApi.Builder builder = OpenAiAudioApi.builder()
			.apiKey(key);
		if (StringUtil.isNotBlank(url)) {
			builder.baseUrl(url);
		}
		return builder.build();
	}

}
