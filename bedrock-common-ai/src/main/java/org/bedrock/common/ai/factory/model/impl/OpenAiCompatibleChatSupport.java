package org.bedrock.common.ai.factory.model.impl;

import org.bedrock.common.code.util.StringUtil;
import org.springframework.ai.openai.api.OpenAiApi;

/**
 * OpenAI 兼容协议 Chat API 构建辅助。
 */
final class OpenAiCompatibleChatSupport {

	private OpenAiCompatibleChatSupport() {
	}

	static void applyBaseUrl(OpenAiApi.Builder builder, String url, String defaultBaseUrl) {
		if (StringUtil.isNotBlank(url)) {
			builder.baseUrl(url);
		}
		else if (StringUtil.isNotBlank(defaultBaseUrl)) {
			builder.baseUrl(defaultBaseUrl);
		}
	}

	/**
	 * 用户填写的半路径优先，否则使用平台默认 completionsPath。
	 */
	static void applyCompletionsPath(OpenAiApi.Builder builder, String path, String defaultPath) {
		if (StringUtil.isNotBlank(path)) {
			builder.completionsPath(path);
		}
		else if (StringUtil.isNotBlank(defaultPath)) {
			builder.completionsPath(defaultPath);
		}
	}

}
