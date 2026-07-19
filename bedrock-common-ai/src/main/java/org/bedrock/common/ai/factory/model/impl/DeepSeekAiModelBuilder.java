package org.bedrock.common.ai.factory.model.impl;

import org.bedrock.common.ai.enums.AiPlatformEnum;
import org.bedrock.common.ai.factory.model.AiModelBuilder;
import org.bedrock.common.ai.model.factory.model.ChatModelCreateParam;
import org.bedrock.common.code.util.SpringUtil;
import org.bedrock.common.code.util.StringUtil;
import org.bedrock.common.spi.autoservice.AutoService;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.deepseek.api.DeepSeekApi;
import org.springframework.ai.model.tool.ToolCallingManager;

/**
 * 深度求索 DeepSeek
 */
@AutoService(AiModelBuilder.class)
public class DeepSeekAiModelBuilder implements AiModelBuilder {

	private final ToolCallingManager toolCallingManager;

	public DeepSeekAiModelBuilder() {
		this.toolCallingManager = SpringUtil.getBean(ToolCallingManager.class);
	}

	@Override
	public ChatModel createChatModel(ChatModelCreateParam param) {
		DeepSeekApi.Builder builder = DeepSeekApi
			.builder()
			.apiKey(param.key());
		if (StringUtil.isNotBlank(param.url())) {
			builder.baseUrl(param.url());
		}
		if (StringUtil.isNotBlank(param.path())) {
			builder.completionsPath(param.path());
		}
		return DeepSeekChatModel.builder()
			.deepSeekApi(builder.build())
			.toolCallingManager(toolCallingManager)
			.build();
	}


	@Override
	public AiPlatformEnum getAiPlatform() {
		return AiPlatformEnum.DEEP_SEEK;
	}

}
