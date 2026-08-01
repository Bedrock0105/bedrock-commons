package org.bedrock.common.ai.factory.model.impl;

import org.bedrock.common.ai.enums.AiPlatformEnum;
import org.bedrock.common.ai.factory.model.AiModelBuilder;
import org.bedrock.common.ai.model.factory.model.ChatModelCreateParam;
import org.bedrock.common.ai.model.factory.model.EmbeddingModelCreateParam;
import org.bedrock.common.code.util.SpringUtil;
import org.bedrock.common.spi.autoservice.AutoService;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.model.tool.ToolCallingManager;

/**
 * 腾讯混元 大模型构造器
 * 适配 腾讯混元 OpenAI 兼容协议，复用 OpenAiChatModel 实现对话能力
 * 官方文档参考：<a href="https://cloud.tencent.com/document/product/1729/111007#457d3a68-6b8d-4195-b3a5-3dc2ecf7046d">腾讯混元 OpenAI 兼容接口文档</a>
 */
@AutoService(AiModelBuilder.class)
public class HunYuanAiModelBuilder implements AiModelBuilder {

	/**
	 * 腾讯混元 OpenAI 协议默认接口地址
	 */
	public static final String BASE_URL = "https://api.hunyuan.cloud.tencent.com";

	/**
	 * 对话补全接口路径
	 */
	public static final String COMPLETE_PATH = "/v1/chat/completions";

	/**
	 * 默认使用模型：hunyuan-turbos-latest
	 */
	public static final String MODEL_DEFAULT = "hunyuan-turbos-latest";

	/**
	 * 工具调用管理器，用于函数调用能力处理
	 */
	private final ToolCallingManager toolCallingManager;

	/**
	 * 无参构造，从Spring容器获取 ToolCallingManager 单例
	 */
	public HunYuanAiModelBuilder() {
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
	public AiPlatformEnum getAiPlatform() {
		return AiPlatformEnum.HUNYUAN;
	}

}
