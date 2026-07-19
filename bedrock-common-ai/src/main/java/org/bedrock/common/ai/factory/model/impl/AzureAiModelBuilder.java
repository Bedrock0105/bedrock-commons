package org.bedrock.common.ai.factory.model.impl;

import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import org.bedrock.common.ai.enums.AiPlatformEnum;
import org.bedrock.common.ai.factory.model.AiModelBuilder;
import org.bedrock.common.ai.model.factory.model.ChatModelCreateParam;
import org.bedrock.common.ai.model.factory.model.EmbeddingModelCreateParam;
import org.bedrock.common.ai.model.factory.model.ImageModelCreateParam;
import org.bedrock.common.ai.model.factory.model.TranscriptionModelCreateParam;
import org.bedrock.common.code.util.SpringUtil;
import org.bedrock.common.code.util.StringUtil;
import org.bedrock.common.spi.autoservice.AutoService;
import org.springframework.ai.audio.transcription.TranscriptionModel;
import org.springframework.ai.azure.openai.AzureOpenAiAudioTranscriptionModel;
import org.springframework.ai.azure.openai.AzureOpenAiAudioTranscriptionOptions;
import org.springframework.ai.azure.openai.AzureOpenAiChatModel;
import org.springframework.ai.azure.openai.AzureOpenAiEmbeddingModel;
import org.springframework.ai.azure.openai.AzureOpenAiEmbeddingOptions;
import org.springframework.ai.azure.openai.AzureOpenAiImageModel;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.model.tool.ToolCallingManager;

/**
 * Azure OpenAI 模型构建器。
 * <p>
 * 文档依据（Spring AI 1.1.x）：
 * <ul>
 *   <li>图像：Azure OpenAI Image Generation（AzureOpenAiImageModel）</li>
 *   <li>语音转文字：Azure OpenAI Transcriptions（AzureOpenAiAudioTranscriptionModel）</li>
 * </ul>
 * Spring AI 1.1.x 未提供 Azure 文字转语音模型实现，故不覆盖 {@code createTextToSpeechModel}。
 * </p>
 */
@AutoService(AiModelBuilder.class)
public class AzureAiModelBuilder implements AiModelBuilder {

	private final ToolCallingManager toolCallingManager;

	public AzureAiModelBuilder() {
		this.toolCallingManager = SpringUtil.getBean(ToolCallingManager.class);
	}

	@Override
	public ChatModel createChatModel(ChatModelCreateParam param) {
		return AzureOpenAiChatModel.builder()
			.openAIClientBuilder(buildClientBuilder(param.url(), param.key()))
			.toolCallingManager(toolCallingManager)
			.build();
	}

	@Override
	public EmbeddingModel createEmbeddingModel(EmbeddingModelCreateParam param) {
		return new AzureOpenAiEmbeddingModel(
			buildClient(param.url(), param.key()),
			MetadataMode.EMBED,
			AzureOpenAiEmbeddingOptions.builder().deploymentName(param.model()).build());
	}

	@Override
	public ImageModel createImageModel(ImageModelCreateParam param) {
		return new AzureOpenAiImageModel(buildClient(param.url(), param.key()));
	}

	@Override
	public TranscriptionModel createTranscriptionModel(TranscriptionModelCreateParam param) {
		return new AzureOpenAiAudioTranscriptionModel(
			buildClient(param.url(), param.key()),
			AzureOpenAiAudioTranscriptionOptions.builder().build());
	}

	@Override
	public AiPlatformEnum getAiPlatform() {
		return AiPlatformEnum.AZURE_OPENAI;
	}

	private OpenAIClient buildClient(String url, String key) {
		return buildClientBuilder(url, key).buildClient();
	}

	private OpenAIClientBuilder buildClientBuilder(String url, String key) {
		OpenAIClientBuilder openAIClientBuilder = new OpenAIClientBuilder()
			.credential(new AzureKeyCredential(key));
		if (StringUtil.isNotBlank(url)) {
			openAIClientBuilder.endpoint(url);
		}
		return openAIClientBuilder;
	}

}
