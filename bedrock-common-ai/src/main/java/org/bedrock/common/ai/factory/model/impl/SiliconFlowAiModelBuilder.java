package org.bedrock.common.ai.factory.model.impl;

import org.bedrock.common.ai.enums.AiPlatformEnum;
import org.bedrock.common.ai.factory.model.AiModelBuilder;
import org.bedrock.common.ai.model.factory.model.ChatModelCreateParam;
import org.bedrock.common.ai.model.factory.model.EmbeddingModelCreateParam;
import org.bedrock.common.ai.model.factory.model.ImageModelCreateParam;
import org.bedrock.common.ai.model.factory.model.TextToSpeechModelCreateParam;
import org.bedrock.common.ai.model.factory.model.TranscriptionModelCreateParam;
import org.bedrock.common.code.util.SpringUtil;
import org.bedrock.common.spi.autoservice.AutoService;
import org.springframework.ai.audio.transcription.TranscriptionModel;
import org.springframework.ai.audio.tts.TextToSpeechModel;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.model.tool.ToolCallingManager;

/**
 * 硅基流动模型构建器。
 * <p>
 * 适配硅基流动 OpenAI 兼容协议，复用 OpenAI 模型实现。
 * 官方文档：
 * <ul>
 *   <li>对话：<a href="https://docs.siliconflow.cn/cn/userguide/quickstart">快速上手</a></li>
 *   <li>图像：<a href="https://docs.siliconflow.cn/cn/api-reference/images/images-generations">创建图片生成</a></li>
 *   <li>文字转语音：<a href="https://docs.siliconflow.cn/cn/api-reference/audio/create-speech">创建文本转语音</a></li>
 *   <li>语音转文字：<a href="https://docs.siliconflow.cn/cn/api-reference/audio/create-audio-transcriptions">创建语音转文本</a></li>
 * </ul>
 * </p>
 */
@AutoService(AiModelBuilder.class)
public class SiliconFlowAiModelBuilder implements AiModelBuilder {

	/**
	 * 硅基流动 OpenAI 协议默认接口地址
	 */
	public static final String BASE_URL = "https://api.siliconflow.cn";

	/**
	 * 对话补全接口路径
	 */
	public static final String COMPLETE_PATH = "/v1/chat/completions";

	/**
	 * 默认使用模型：Qwen/Qwen2.5-72B-Instruct
	 */
	public static final String MODEL_DEFAULT = "Qwen/Qwen2.5-72B-Instruct";

	/**
	 * 图片生成接口路径
	 */
	static final String IMAGES_PATH = "v1/images/generations";

	private final ToolCallingManager toolCallingManager;

	public SiliconFlowAiModelBuilder() {
		this.toolCallingManager = SpringUtil.getBean(ToolCallingManager.class);
	}

	@Override
	public ChatModel createChatModel(ChatModelCreateParam param) {
		return OpenAiCompatibleModelFactory.createOpenAiChatModel(param, BASE_URL, COMPLETE_PATH, MODEL_DEFAULT, toolCallingManager);
	}

	@Override
	public EmbeddingModel createEmbeddingModel(EmbeddingModelCreateParam param) {
		return OpenAiCompatibleModelFactory.create(param, BASE_URL);
	}

	@Override
	public ImageModel createImageModel(ImageModelCreateParam param) {
		return OpenAiCompatibleModelFactory.createImageModel(param, BASE_URL, IMAGES_PATH);
	}

	@Override
	public TextToSpeechModel createTextToSpeechModel(TextToSpeechModelCreateParam param) {
		return OpenAiCompatibleModelFactory.createTextToSpeechModel(param, BASE_URL);
	}

	@Override
	public TranscriptionModel createTranscriptionModel(TranscriptionModelCreateParam param) {
		return OpenAiCompatibleModelFactory.createTranscriptionModel(param, BASE_URL);
	}

	@Override
	public AiPlatformEnum getAiPlatform() {
		return AiPlatformEnum.SILICON_FLOW;
	}

}
