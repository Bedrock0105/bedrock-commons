package org.bedrock.common.ai.factory.model;

import org.bedrock.common.ai.enums.AiPlatformEnum;
import org.bedrock.common.ai.model.factory.model.ChatModelCreateParam;
import org.bedrock.common.ai.model.factory.model.EmbeddingModelCreateParam;
import org.bedrock.common.ai.model.factory.model.ImageModelCreateParam;
import org.bedrock.common.ai.model.factory.model.TextToSpeechModelCreateParam;
import org.bedrock.common.ai.model.factory.model.TranscriptionModelCreateParam;
import org.springframework.ai.audio.transcription.TranscriptionModel;
import org.springframework.ai.audio.tts.TextToSpeechModel;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.image.ImageModel;
import org.springframework.core.Ordered;

/**
 * AI 模型构造器。
 * <p>每个 AI 平台独立实现，负责创建对应平台的 ChatModel、EmbeddingModel 等实例。</p>
 */
public interface AiModelBuilder extends Ordered {

	/**
	 * 根据参数创建当前平台专属聊天模型。
	 *
	 * @param param 模型构建参数
	 * @return ChatModel 聊天模型实例
	 */
	ChatModel createChatModel(ChatModelCreateParam param);

	/**
	 * 根据参数创建当前平台专属嵌入模型。
	 * <p>平台不支持时抛出 {@link UnsupportedOperationException}。</p>
	 *
	 * @param param 嵌入模型构建参数
	 * @return EmbeddingModel 嵌入模型实例
	 */
	default EmbeddingModel createEmbeddingModel(EmbeddingModelCreateParam param) {
		throw new UnsupportedOperationException("AI 平台[" + getAiPlatform() + "]暂不支持创建向量模型");
	}

	/**
	 * 根据参数创建当前平台专属图像模型。
	 * <p>平台不支持时抛出 {@link UnsupportedOperationException}。</p>
	 *
	 * @param param 图像模型构建参数
	 * @return ImageModel 图像模型实例
	 */
	default ImageModel createImageModel(ImageModelCreateParam param) {
		throw new UnsupportedOperationException("AI 平台[" + getAiPlatform() + "]暂不支持创建图像模型");
	}

	/**
	 * 根据参数创建当前平台专属文字转语音模型。
	 * <p>平台不支持时抛出 {@link UnsupportedOperationException}。</p>
	 *
	 * @param param 文字转语音模型构建参数
	 * @return TextToSpeechModel 文字转语音模型实例
	 */
	default TextToSpeechModel createTextToSpeechModel(TextToSpeechModelCreateParam param) {
		throw new UnsupportedOperationException("AI 平台[" + getAiPlatform() + "]暂不支持创建文字转语音模型");
	}

	/**
	 * 根据参数创建当前平台专属语音转文字模型。
	 * <p>平台不支持时抛出 {@link UnsupportedOperationException}。</p>
	 *
	 * @param param 语音转文字模型构建参数
	 * @return TranscriptionModel 语音转文字模型实例
	 */
	default TranscriptionModel createTranscriptionModel(TranscriptionModelCreateParam param) {
		throw new UnsupportedOperationException("AI 平台[" + getAiPlatform() + "]暂不支持创建语音转文字模型");
	}

	/**
	 * 获取当前构造器对应的AI平台枚举。
	 *
	 * @return AiPlatformEnum 平台标识
	 */
	AiPlatformEnum getAiPlatform();

	@Override
	default int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE;
	}

}
