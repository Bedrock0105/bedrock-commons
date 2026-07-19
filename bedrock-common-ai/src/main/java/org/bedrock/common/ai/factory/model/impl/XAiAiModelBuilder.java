package org.bedrock.common.ai.factory.model.impl;

import org.bedrock.common.ai.enums.AiPlatformEnum;
import org.bedrock.common.ai.factory.model.AiModelBuilder;
import org.bedrock.common.ai.model.factory.model.ChatModelCreateParam;
import org.bedrock.common.ai.model.factory.model.ImageModelCreateParam;
import org.bedrock.common.code.util.SpringUtil;
import org.bedrock.common.spi.autoservice.AutoService;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;

/**
 * xAI Grok 模型构建器。
 * <p>
 * 适配 xAI OpenAI 兼容协议。
 * 官方文档：
 * <ul>
 *   <li>对话：<a href="https://docs.x.ai/developers/quickstart">Quickstart</a></li>
 *   <li>图像：<a href="https://docs.x.ai/developers/model-capabilities/images/generation">Image Generation</a></li>
 * </ul>
 * </p>
 */
@AutoService(AiModelBuilder.class)
public class XAiAiModelBuilder implements AiModelBuilder {

	/** xAI OpenAI 协议默认接口地址 */
	public static final String BASE_URL = "https://api.x.ai";

	/** 对话补全接口路径 */
	public static final String COMPLETE_PATH = "/v1/chat/completions";

	/** 默认使用模型：grok-build-0.1 */
	public static final String MODEL_DEFAULT = "grok-build-0.1";

	/** 工具调用管理器，用于函数调用能力处理 */
	private final ToolCallingManager toolCallingManager;

	/**
	 * 无参构造，从Spring容器获取 ToolCallingManager 单例
	 */
	public XAiAiModelBuilder() {
		this.toolCallingManager = SpringUtil.getBean(ToolCallingManager.class);
	}

	/**
	 * 创建 xAI 对话模型实例
	 * 1. 优先使用入参自定义接口地址，为空则采用官方默认地址
	 * 2. 基于 OpenAI 兼容协议构建 Api 客户端
	 * 3. 内置默认模型名称、温度等参数配置
	 *
	 * @param param 模型构建参数（平台地址、密钥等）
	 * @return 适配xAI协议的OpenAiChatModel实例
	 */
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
	public ImageModel createImageModel(ImageModelCreateParam param) {
		return OpenAiCompatibleMediaSupport.createImageModel(param.url(), param.key(), BASE_URL, OpenAiCompatibleMediaSupport.DEFAULT_IMAGES_PATH, param.path());
	}

	@Override
	public AiPlatformEnum getAiPlatform() {
		return AiPlatformEnum.XAI;
	}
}
