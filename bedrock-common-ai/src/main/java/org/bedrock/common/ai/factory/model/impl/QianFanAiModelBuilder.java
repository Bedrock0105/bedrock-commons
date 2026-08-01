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
 * 百度千帆 大模型构造器
 * 适配千帆 OpenAI 兼容协议，复用 OpenAiChatModel 实现对话能力
 * 官方文档参考：<a href="https://cloud.baidu.com/doc/qianfan-api/s/3m7of64lb">千帆 OpenAI 兼容接口文档</a>
 */
@AutoService(AiModelBuilder.class)
public class QianFanAiModelBuilder implements AiModelBuilder {

	/**
	 * 千帆 OpenAI 协议默认接口地址（对话、图像）
	 */
	public static final String BASE_URL = "https://qianfan.baidubce.com";

	/**
	 * 嵌入专用 OpenAI 协议 baseUrl
	 */
	public static final String EMBEDDING_BASE_URL = "https://qianfan.baidubce.com/v2";

	/**
	 * 向量嵌入接口路径
	 */
	public static final String EMBEDDINGS_PATH = "/embeddings";

	/**
	 * 图片生成接口路径
	 */
	public static final String IMAGE_PATH = "/v2/images/generations";

	/**
	 * 对话补全接口路径
	 */
	public static final String COMPLETE_PATH = "/v2/chat/completion";

	/**
	 * 默认使用模型：ernie-5.0
	 */
	public static final String MODEL_DEFAULT = "ernie-5.0";

	/**
	 * 工具调用管理器，用于函数调用能力处理
	 */
	private final ToolCallingManager toolCallingManager;

	/**
	 * 无参构造，从Spring容器获取 ToolCallingManager 单例
	 */
	public QianFanAiModelBuilder() {
		this.toolCallingManager = SpringUtil.getBean(ToolCallingManager.class);
	}

	@Override
	public ChatModel createChatModel(ChatModelCreateParam param) {
		return OpenAiCompatibleModelFactory.createOpenAiChatModel(param, BASE_URL, COMPLETE_PATH, MODEL_DEFAULT, toolCallingManager);
	}

	@Override
	public EmbeddingModel createEmbeddingModel(EmbeddingModelCreateParam param) {
		return OpenAiCompatibleModelFactory.create(param, EMBEDDING_BASE_URL, EMBEDDINGS_PATH);
	}

	@Override
	public ImageModel createImageModel(ImageModelCreateParam param) {
		return OpenAiCompatibleModelFactory.createImageModel(param, BASE_URL, IMAGE_PATH);
	}

	@Override
	public AiPlatformEnum getAiPlatform() {
		return AiPlatformEnum.QIAN_FAN;
	}

}
