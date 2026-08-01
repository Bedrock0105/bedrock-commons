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
 * 百川智能 大模型构造器
 * 适配 百川智能 OpenAI 兼容协议，复用 OpenAiChatModel 实现对话能力
 * 官方文档参考：<a href="https://platform.baichuan-ai.com/docs/api">百川智能 OpenAI 兼容接口文档</a>
 */
@AutoService(AiModelBuilder.class)
public class BaiChuanAiModelBuilder implements AiModelBuilder {

	/**
	 * 百川智能 OpenAI 协议默认接口地址
	 */
	public static final String BASE_URL = "https://api.baichuan-ai.com";

	/**
	 * 对话补全接口路径
	 */
	public static final String COMPLETE_PATH = "/v1/chat/completions";

	/**
	 * 默认使用模型：Baichuan4-Turbo
	 */
	public static final String MODEL_DEFAULT = "Baichuan4-Turbo";

	/**
	 * 工具调用管理器，用于函数调用能力处理
	 */
	private final ToolCallingManager toolCallingManager;

	/**
	 * 无参构造，从Spring容器获取 ToolCallingManager 单例
	 */
	public BaiChuanAiModelBuilder() {
		this.toolCallingManager = SpringUtil.getBean(ToolCallingManager.class);
	}

	/**
	 * 创建 百川智能 对话模型实例
	 * 1. 优先使用入参自定义接口地址，为空则采用官方默认地址
	 * 2. 基于 OpenAI 兼容协议构建 Api 客户端
	 * 3. 内置默认模型名称、温度等参数配置
	 *
	 * @param param 模型构建参数（平台地址、密钥等）
	 * @return 适配百川智能协议的OpenAiChatModel实例
	 */
	@Override
	public ChatModel createChatModel(ChatModelCreateParam param) {
		return OpenAiCompatibleModelFactory.createOpenAiChatModel(param, BASE_URL, COMPLETE_PATH, MODEL_DEFAULT, toolCallingManager);
	}

	@Override
	public EmbeddingModel createEmbeddingModel(EmbeddingModelCreateParam param) {
		return OpenAiCompatibleModelFactory.create(param, BASE_URL);
	}

	/**
	 * 获取当前构造器绑定的AI平台枚举
	 *
	 * @return AI平台标识 百川智能
	 */
	@Override
	public AiPlatformEnum getAiPlatform() {
		return AiPlatformEnum.BAICHUAN;
	}
}
