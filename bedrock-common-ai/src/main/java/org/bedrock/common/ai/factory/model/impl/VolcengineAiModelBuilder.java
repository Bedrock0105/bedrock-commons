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
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;

/**
 * 火山方舟模型构建器。
 * <p>
 * 适配火山方舟 OpenAI 兼容协议，复用 OpenAI 模型实现。
 * 官方文档：
 * <ul>
 *   <li>对话 / 嵌入：<a href="https://www.volcengine.com/docs/82379/1399009?lang=zh">OpenAI 兼容接口</a></li>
 *   <li>图像：{@code POST /api/v3/images/generations}</li>
 *   <li>文字转语音：{@code POST /api/v3/audio/speech}</li>
 *   <li>语音转文字：{@code POST /api/v3/audio/transcriptions}</li>
 * </ul>
 * 注意：Spring AI 内置 OpenAiAudioApi 默认请求 {@code /v1/audio/*}，
 * 火山方舟实际路径为 {@code /api/v3/audio/*}，需通过路径改写适配。
 * </p>
 */
@AutoService(AiModelBuilder.class)
public class VolcengineAiModelBuilder implements AiModelBuilder {

	/** 火山方舟 OpenAI 协议默认接口地址（含 /api，用于 chat/embedding/image 等） */
	public static final String BASE_URL = "https://ark.cn-beijing.volces.com/api";

	/**
	 * 语音专用 baseUrl（不含 path）。
	 * OpenAiAudioApi 默认拼接 {@code /v1/audio/*}，火山实际为 {@code /api/v3/audio/*}。
	 */
	public static final String AUDIO_BASE_URL = "https://ark.cn-beijing.volces.com";

	/** 对话补全接口路径 */
	public static final String COMPLETE_PATH = "/v3/chat/completions";

	/** 向量嵌入接口路径 */
	public static final String EMBEDDINGS_PATH = "/v3/embeddings";

	/** 图片生成接口路径（相对 {@link #BASE_URL}） */
	public static final String IMAGES_PATH = "v3/images/generations";

	/** 默认使用模型：doubao-seed-2-1-pro-260628 */
	public static final String MODEL_DEFAULT = "doubao-seed-2-1-pro-260628";

	private final ToolCallingManager toolCallingManager;

	public VolcengineAiModelBuilder() {
		this.toolCallingManager = SpringUtil.getBean(ToolCallingManager.class);
	}

	@Override
	public ChatModel createChatModel(ChatModelCreateParam param) {
		OpenAiApi.Builder builder = OpenAiApi.builder()
			.apiKey(param.key());
		OpenAiCompatibleChatSupport.applyCompletionsPath(builder, param.path(), COMPLETE_PATH);
		OpenAiCompatibleChatSupport.applyBaseUrl(builder, param.url(), BASE_URL);

		OpenAiChatOptions openAiChatOptions = OpenAiChatOptions.builder()
			.model(MODEL_DEFAULT)
			.temperature(0.7)
			.build();

		return OpenAiChatModel
			.builder()
			.defaultOptions(openAiChatOptions)
			.openAiApi(builder.build())
			.toolCallingManager(toolCallingManager)
			.build();
	}

	@Override
	public EmbeddingModel createEmbeddingModel(EmbeddingModelCreateParam param) {
		return OpenAiCompatibleEmbeddingSupport.create(param, BASE_URL, EMBEDDINGS_PATH);
	}

	@Override
	public ImageModel createImageModel(ImageModelCreateParam param) {
		return OpenAiCompatibleMediaSupport.createImageModel(param.url(), param.key(), BASE_URL, IMAGES_PATH, param.path());
	}

	@Override
	public TextToSpeechModel createTextToSpeechModel(TextToSpeechModelCreateParam param) {
		return OpenAiCompatibleMediaSupport.createTextToSpeechModel(
			resolveAudioBaseUrl(param.url()),
			param.key(),
			AUDIO_BASE_URL,
			VolcengineAiModelBuilder::rewriteAudioPath);
	}

	@Override
	public TranscriptionModel createTranscriptionModel(TranscriptionModelCreateParam param) {
		return OpenAiCompatibleMediaSupport.createTranscriptionModel(
			resolveAudioBaseUrl(param.url()),
			param.key(),
			AUDIO_BASE_URL,
			VolcengineAiModelBuilder::rewriteAudioPath);
	}

	@Override
	public AiPlatformEnum getAiPlatform() {
		return AiPlatformEnum.VOLCENGINE;
	}

	/**
	 * Spring AI 默认 {@code /v1/audio/...}，改写为 {@code /api/v3/audio/...}。
	 */
	static String rewriteAudioPath(String path) {
		if (path == null) {
			return null;
		}
		if (path.startsWith("/v1/audio/")) {
			return "/api/v3/audio/" + path.substring("/v1/audio/".length());
		}
		return path;
	}

	/**
	 * 若自定义 url 以 {@code /api} 结尾，则剥离后缀得到 host，供语音请求使用。
	 */
	private static String resolveAudioBaseUrl(String url) {
		if (StringUtil.isBlank(url)) {
			return null;
		}
		String normalized = url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
		if (normalized.endsWith("/api")) {
			return normalized.substring(0, normalized.length() - 4);
		}
		if (normalized.endsWith("/api/v3")) {
			return normalized.substring(0, normalized.length() - 7);
		}
		return normalized;
	}

}
