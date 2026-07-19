package org.bedrock.common.ai.factory.model.impl;

import org.bedrock.common.ai.model.factory.model.EmbeddingModelCreateParam;
import org.bedrock.common.code.util.StringUtil;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.retry.RetryUtils;

/**
 * OpenAI 兼容协议嵌入模型构建辅助类。
 */
final class OpenAiCompatibleEmbeddingSupport {

	private static final String DEFAULT_EMBEDDINGS_PATH = "/v1/embeddings";

	private OpenAiCompatibleEmbeddingSupport() {
	}

	static EmbeddingModel create(EmbeddingModelCreateParam param, String defaultBaseUrl) {
		return create(param, defaultBaseUrl, DEFAULT_EMBEDDINGS_PATH);
	}

	static EmbeddingModel create(EmbeddingModelCreateParam param, String defaultBaseUrl, String embeddingsPath) {
		OpenAiApi.Builder apiBuilder = OpenAiApi.builder()
			.apiKey(param.key());

		String resolvedPath = StringUtil.isNotBlank(param.path()) ? param.path() : embeddingsPath;
		if (StringUtil.isNotBlank(resolvedPath)) {
			apiBuilder.embeddingsPath(resolvedPath);
		}
		if (StringUtil.isNotBlank(param.url())) {
			apiBuilder.baseUrl(param.url());
		} else if (StringUtil.isNotBlank(defaultBaseUrl)) {
			apiBuilder.baseUrl(defaultBaseUrl);
		}
		OpenAiEmbeddingOptions options = OpenAiEmbeddingOptions.builder()
			.model(param.model())
			.build();
		return new OpenAiEmbeddingModel(
			apiBuilder.build(),
			MetadataMode.EMBED,
			options,
			RetryUtils.DEFAULT_RETRY_TEMPLATE);
	}

}
