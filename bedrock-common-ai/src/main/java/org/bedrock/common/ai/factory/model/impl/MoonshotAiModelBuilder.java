package org.bedrock.common.ai.factory.model.impl;

import org.bedrock.common.ai.enums.AiPlatformEnum;
import org.bedrock.common.ai.factory.model.AiModelBuilder;
import org.bedrock.common.ai.model.factory.model.ChatModelCreateParam;
import org.bedrock.common.code.util.SpringUtil;
import org.bedrock.common.code.util.StringUtil;
import org.bedrock.common.spi.autoservice.AutoService;
import org.springaicommunity.moonshot.MoonshotChatModel;
import org.springaicommunity.moonshot.api.MoonshotApi;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.model.tool.ToolCallingManager;

/**
 * 月之暗面 Kimi
 */
@AutoService(AiModelBuilder.class)
public class MoonshotAiModelBuilder implements AiModelBuilder {

	private final ToolCallingManager toolCallingManager;

	public MoonshotAiModelBuilder() {
		this.toolCallingManager = SpringUtil.getBean(ToolCallingManager.class);
	}

	@Override
	public ChatModel createChatModel(ChatModelCreateParam param) {
		MoonshotApi.Builder builder = MoonshotApi.builder()
			.apiKey(param.key());
		if (StringUtil.isNotBlank(param.url())) {
			builder.baseUrl(param.url());
		}
		if (StringUtil.isNotBlank(param.path())) {
			builder.completionsPath(param.path());
		}
		return MoonshotChatModel.builder()
			.moonshotApi(builder.build())
			.toolCallingManager(toolCallingManager)
			.build();
	}

	@Override
	public AiPlatformEnum getAiPlatform() {
		return AiPlatformEnum.MOONSHOT;
	}

}
