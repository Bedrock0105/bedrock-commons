package org.bedrock.common.ai.factory.model.impl;

import org.bedrock.common.ai.enums.AiPlatformEnum;
import org.bedrock.common.ai.factory.model.AiModelBuilder;
import org.bedrock.common.ai.model.factory.model.ChatModelCreateParam;
import org.bedrock.common.code.util.SpringUtil;
import org.bedrock.common.code.util.StringUtil;
import org.bedrock.common.spi.autoservice.AutoService;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.anthropic.api.AnthropicApi;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.model.tool.ToolCallingManager;

/**
 * Anthropic
 */
@AutoService(AiModelBuilder.class)
public class AnthropicAiModelBuilder implements AiModelBuilder {

	private final ToolCallingManager toolCallingManager;

	public AnthropicAiModelBuilder() {
		this.toolCallingManager = SpringUtil.getBean(ToolCallingManager.class);
	}

	@Override
	public ChatModel createChatModel(ChatModelCreateParam param) {
		AnthropicApi.Builder builder = AnthropicApi
			.builder()
			.apiKey(param.key());
		if (StringUtil.isNotBlank(param.url())) {
			builder.baseUrl(param.url());
		}
		if (StringUtil.isNotBlank(param.path())) {
			builder.completionsPath(param.path());
		}
		return AnthropicChatModel.builder()
			.anthropicApi(builder.build())
			.toolCallingManager(toolCallingManager)
			.build();
	}

	@Override
	public AiPlatformEnum getAiPlatform() {
		return AiPlatformEnum.ANTHROPIC;
	}

}
