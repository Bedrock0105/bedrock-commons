package org.bedrock.common.ai.factory.model.impl;

import org.bedrock.common.ai.enums.AiPlatformEnum;
import org.bedrock.common.ai.factory.model.AiModelBuilder;
import org.bedrock.common.ai.model.factory.model.ChatModelCreateParam;
import org.bedrock.common.ai.model.factory.model.EmbeddingModelCreateParam;
import org.bedrock.common.ai.model.factory.model.ImageModelCreateParam;
import org.bedrock.common.code.util.SpringUtil;
import org.bedrock.common.spi.autoservice.AutoService;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.model.tool.ToolCallingManager;

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

	/**
	 * 火山方舟 OpenAI 协议默认接口地址（含 /api，用于 chat/embedding/image 等）
	 */
	public static final String BASE_URL = "https://ark.cn-beijing.volces.com/api";

	/**
	 * 语音专用 baseUrl（不含 path）。
	 * OpenAiAudioApi 默认拼接 {@code /v1/audio/*}，火山实际为 {@code /api/v3/audio/*}。
	 */
	public static final String AUDIO_BASE_URL = "https://ark.cn-beijing.volces.com";

	/**
	 * 对话补全接口路径
	 */
	public static final String COMPLETE_PATH = "/v3/chat/completions";

	/**
	 * 向量嵌入接口路径
	 */
	public static final String EMBEDDINGS_PATH = "/v3/embeddings";

	/**
	 * 图片生成接口路径（相对 {@link #BASE_URL}）
	 */
	public static final String IMAGES_PATH = "v3/images/generations";

	/**
	 * 默认使用模型：doubao-seed-2-1-pro-260628
	 */
	public static final String MODEL_DEFAULT = "doubao-seed-2-1-pro-260628";

	private final ToolCallingManager toolCallingManager;

	public VolcengineAiModelBuilder() {
		this.toolCallingManager = SpringUtil.getBean(ToolCallingManager.class);
	}

	@Override
	public ChatModel createChatModel(ChatModelCreateParam param) {
		return OpenAiCompatibleModelFactory.createOpenAiChatModel(param, BASE_URL, COMPLETE_PATH, MODEL_DEFAULT, toolCallingManager);
	}

	@Override
	public EmbeddingModel createEmbeddingModel(EmbeddingModelCreateParam param) {
		return OpenAiCompatibleModelFactory.create(param, BASE_URL, EMBEDDINGS_PATH);
	}

	@Override
	public ImageModel createImageModel(ImageModelCreateParam param) {
		return OpenAiCompatibleModelFactory.createImageModel(param, BASE_URL, IMAGES_PATH);
	}

	@Override
	public AiPlatformEnum getAiPlatform() {
		return AiPlatformEnum.VOLCENGINE;
	}


}
