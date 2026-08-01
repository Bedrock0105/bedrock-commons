package org.bedrock.common.ai.factory.model.impl;

import org.bedrock.common.ai.model.factory.model.*;
import org.bedrock.common.code.util.StringUtil;
import org.springframework.ai.audio.transcription.TranscriptionModel;
import org.springframework.ai.audio.tts.TextToSpeechModel;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.openai.*;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.openai.api.OpenAiAudioApi;
import org.springframework.ai.openai.api.OpenAiImageApi;
import org.springframework.ai.retry.RetryUtils;

/**
 * OpenAI 兼容协议模型工厂构建工具
 * <p>
 * 适配所有遵循 OpenAI 兼容HTTP协议的大模型服务，支持构建：
 * Chat对话模型、文生图模型、向量嵌入模型、语音TTS、语音转写模型
 * </p>
 * 支持自定义地址、自定义接口路径、密钥隔离，适配第三方兼容接口（通义千问兼容、智谱兼容、本地Ollama、vLLM等）
 */
final class OpenAiCompatibleModelFactory {

	/**
	 * Embedding向量模型默认接口路径
	 */
	private static final String DEFAULT_EMBEDDINGS_PATH = "/v1/embeddings";

	/**
	 * 创建 OpenAI 兼容协议 Chat 对话模型
	 *
	 * @param param              模型创建参数（密钥、自定义地址、自定义路径、指定模型名）
	 * @param defaultBaseUrl     默认服务地址，参数未指定url时使用
	 * @param defaultPath        默认completions接口路径，参数未指定path时使用
	 * @param defaultModel       默认模型名称
	 * @param toolCallingManager 工具调用管理器，支持Function Calling
	 * @return OpenAiChatModel 对话模型实例
	 */
	static ChatModel createOpenAiChatModel(ChatModelCreateParam param,
	                                       String defaultBaseUrl,
	                                       String defaultPath,
	                                       String defaultModel,
	                                       ToolCallingManager toolCallingManager) {
		OpenAiApi.Builder builder = OpenAiApi.builder()
			.apiKey(param.key());
		// 优先使用参数传入地址，无则使用全局默认地址
		if (StringUtil.isNotBlank(param.url())) {
			builder.baseUrl(param.url());
		} else if (StringUtil.isNotBlank(defaultBaseUrl)) {
			builder.baseUrl(defaultBaseUrl);
		}
		// 优先使用参数自定义路径，无则使用默认路径
		if (StringUtil.isNotBlank(param.path())) {
			builder.completionsPath(param.path());
		} else if (StringUtil.isNotBlank(defaultPath)) {
			builder.completionsPath(defaultPath);
		}
		// 模型默认参数配置
		OpenAiChatOptions openAiChatOptions = OpenAiChatOptions.builder()
			.model(defaultModel)
			.temperature(0.7)
			.build();

		return OpenAiChatModel
			.builder()
			.defaultOptions(openAiChatOptions)
			.openAiApi(builder.build())
			.toolCallingManager(toolCallingManager)
			.build();
	}

	/**
	 * 创建 OpenAI 兼容协议文生图模型（ImageModel）
	 *
	 * @param param             模型创建参数
	 * @param defaultBaseUrl    默认服务地址
	 * @param defaultImagesPath 默认图像生成接口路径
	 * @return OpenAiImageModel 图像生成模型实例
	 */
	static ImageModel createImageModel(ImageModelCreateParam param, String defaultBaseUrl, String defaultImagesPath) {
		OpenAiImageApi.Builder builder = OpenAiImageApi.builder()
			.apiKey(param.key());
		if (StringUtil.isNotBlank(param.url())) {
			builder.baseUrl(param.url());
		} else if (StringUtil.isNotBlank(defaultBaseUrl)) {
			builder.baseUrl(defaultBaseUrl);
		}
		if (StringUtil.isNotBlank(param.path())) {
			builder.imagesPath(param.path());
		} else if (StringUtil.isNotBlank(defaultImagesPath)) {
			builder.imagesPath(defaultImagesPath);
		}
		return new OpenAiImageModel(builder.build());
	}

	/**
	 * 创建 OpenAI 兼容协议语音合成TTS模型
	 *
	 * @param param          TTS模型创建参数
	 * @param defaultBaseUrl 默认音频服务地址
	 * @return OpenAiAudioSpeechModel 文本转语音模型
	 */
	static TextToSpeechModel createTextToSpeechModel(TextToSpeechModelCreateParam param, String defaultBaseUrl) {
		return new OpenAiAudioSpeechModel(buildAudioApi(param.url(), param.key(), defaultBaseUrl));
	}

	/**
	 * 创建 OpenAI 兼容协议语音转写（ASR）模型
	 *
	 * @param param          语音转写模型创建参数
	 * @param defaultBaseUrl 默认音频服务地址
	 * @return OpenAiAudioTranscriptionModel 语音识别转写模型
	 */
	static TranscriptionModel createTranscriptionModel(TranscriptionModelCreateParam param, String defaultBaseUrl) {
		return new OpenAiAudioTranscriptionModel(buildAudioApi(param.url(), param.key(), defaultBaseUrl));
	}

	/**
	 * 构建OpenAiAudioApi音频公共客户端（TTS/ASR共用）
	 *
	 * @param url            自定义服务地址
	 * @param key            接口密钥
	 * @param defaultBaseUrl 默认服务地址
	 * @return OpenAiAudioApi 音频接口客户端
	 */
	private static OpenAiAudioApi buildAudioApi(String url, String key, String defaultBaseUrl) {
		OpenAiAudioApi.Builder builder = OpenAiAudioApi.builder()
			.apiKey(key);
		if (StringUtil.isNotBlank(url)) {
			builder.baseUrl(url);
		} else if (StringUtil.isNotBlank(defaultBaseUrl)) {
			builder.baseUrl(defaultBaseUrl);
		}
		return builder.build();
	}

	/**
	 * 创建OpenAI兼容向量嵌入模型，使用默认 /v1/embeddings 路径
	 *
	 * @param param          向量模型创建参数
	 * @param defaultBaseUrl 默认服务地址
	 * @return OpenAiEmbeddingModel 向量嵌入模型实例
	 */
	static EmbeddingModel create(EmbeddingModelCreateParam param, String defaultBaseUrl) {
		return create(param, defaultBaseUrl, DEFAULT_EMBEDDINGS_PATH);
	}

	/**
	 * 创建OpenAI兼容向量嵌入模型，支持自定义embeddings接口路径
	 *
	 * @param param          向量模型创建参数
	 * @param defaultBaseUrl 默认服务地址
	 * @param embeddingsPath 自定义embedding接口路径
	 * @return OpenAiEmbeddingModel 向量嵌入模型实例
	 */
	static EmbeddingModel create(EmbeddingModelCreateParam param, String defaultBaseUrl, String embeddingsPath) {
		OpenAiApi.Builder apiBuilder = OpenAiApi.builder()
			.apiKey(param.key());

		// 优先使用参数自定义路径，否则使用传入默认路径
		String resolvedPath = StringUtil.isNotBlank(param.path()) ? param.path() : embeddingsPath;
		if (StringUtil.isNotBlank(resolvedPath)) {
			apiBuilder.embeddingsPath(resolvedPath);
		}
		// 地址优先级：参数地址 > 默认地址
		if (StringUtil.isNotBlank(param.url())) {
			apiBuilder.baseUrl(param.url());
		} else if (StringUtil.isNotBlank(defaultBaseUrl)) {
			apiBuilder.baseUrl(defaultBaseUrl);
		}
		OpenAiEmbeddingOptions options = OpenAiEmbeddingOptions.builder()
			.model(param.model())
			.build();
		return new OpenAiEmbeddingModel(
			apiBuilder.build(),
			MetadataMode.EMBED,
			options,
			RetryUtils.DEFAULT_RETRY_TEMPLATE);
	}

}
