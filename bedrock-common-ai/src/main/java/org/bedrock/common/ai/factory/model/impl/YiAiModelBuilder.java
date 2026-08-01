package org.bedrock.common.ai.factory.model.impl;

import org.bedrock.common.ai.enums.AiPlatformEnum;
import org.bedrock.common.ai.factory.model.AiModelBuilder;
import org.bedrock.common.ai.model.factory.model.ChatModelCreateParam;
import org.bedrock.common.code.util.SpringUtil;
import org.bedrock.common.spi.autoservice.AutoService;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.model.tool.ToolCallingManager;

/**
 * 零一万物 大模型构造器
 * 适配 零一万物 OpenAI 兼容协议，复用 OpenAiChatModel 实现对话能力
 * 官方文档参考：<a href="https://platform.lingyiwanwu.com/docs">零一万物 OpenAI 兼容接口文档</a>
 */
@AutoService(AiModelBuilder.class)
public class YiAiModelBuilder implements AiModelBuilder {

	/**
	 * 零一万物 OpenAI 协议默认接口地址
	 */
	public static final String BASE_URL = "https://api.lingyiwanwu.com";

	/**
	 * 对话补全接口路径
	 */
	public static final String COMPLETE_PATH = "/v1/chat/completions";

	/**
	 * 默认使用模型：yi-lightning
	 */
	public static final String MODEL_DEFAULT = "yi-lightning";

	/**
	 * 工具调用管理器，用于函数调用能力处理
	 */
	private final ToolCallingManager toolCallingManager;

	/**
	 * 无参构造，从Spring容器获取 ToolCallingManager 单例
	 */
	public YiAiModelBuilder() {
		this.toolCallingManager = SpringUtil.getBean(ToolCallingManager.class);
	}

	/**
	 * 创建 零一万物 对话模型实例
	 * 1. 优先使用入参自定义接口地址，为空则采用官方默认地址
	 * 2. 基于 OpenAI 兼容协议构建 Api 客户端
	 * 3. 内置默认模型名称、温度等参数配置
	 *
	 * @param param 模型构建参数（平台地址、密钥等）
	 * @return 适配零一万物协议的OpenAiChatModel实例
	 */
	@Override
	public ChatModel createChatModel(ChatModelCreateParam param) {
		return OpenAiCompatibleModelFactory.createOpenAiChatModel(param, BASE_URL, COMPLETE_PATH, MODEL_DEFAULT, toolCallingManager);
	}

	/**
	 * 获取当前构造器绑定的AI平台枚举
	 *
	 * @return AI平台标识 零一万物
	 */
	@Override
	public AiPlatformEnum getAiPlatform() {
		return AiPlatformEnum.YI;
	}
}
