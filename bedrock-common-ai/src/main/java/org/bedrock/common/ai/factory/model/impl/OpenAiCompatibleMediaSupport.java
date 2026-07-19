package org.bedrock.common.ai.factory.model.impl;

import org.bedrock.common.code.util.StringUtil;
import org.springframework.ai.audio.transcription.TranscriptionModel;
import org.springframework.ai.audio.tts.TextToSpeechModel;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.openai.OpenAiAudioSpeechModel;
import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.ai.openai.OpenAiImageModel;
import org.springframework.ai.openai.api.OpenAiAudioApi;
import org.springframework.ai.openai.api.OpenAiImageApi;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.support.HttpRequestWrapper;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

/**
 * OpenAI 兼容协议图像 / 语音模型构建辅助类。
 * <p>
 * 复用 Spring AI 的 {@link OpenAiImageModel}、{@link OpenAiAudioSpeechModel}、
 * {@link OpenAiAudioTranscriptionModel}，适配提供 OpenAI 兼容端点的第三方平台。
 * </p>
 */
final class OpenAiCompatibleMediaSupport {

	/** OpenAI / 硅基流动默认图片路径（相对 baseUrl，无前导斜杠） */
	static final String DEFAULT_IMAGES_PATH = "v1/images/generations";

	private OpenAiCompatibleMediaSupport() {
	}

	static ImageModel createImageModel(String url, String key, String defaultBaseUrl) {
		return createImageModel(url, key, defaultBaseUrl, DEFAULT_IMAGES_PATH, null);
	}

	static ImageModel createImageModel(String url, String key, String defaultBaseUrl, String imagesPath) {
		return createImageModel(url, key, defaultBaseUrl, imagesPath, null);
	}

	/**
	 * @param userPath 用户自定义半路径，非空时覆盖 {@code imagesPath}
	 */
	static ImageModel createImageModel(String url, String key, String defaultBaseUrl, String imagesPath,
			String userPath) {
		String resolvedPath = StringUtil.isNotBlank(userPath) ? userPath : imagesPath;
		OpenAiImageApi.Builder builder = OpenAiImageApi.builder()
			.apiKey(key)
			.imagesPath(resolvedPath);
		applyBaseUrl(builder::baseUrl, url, defaultBaseUrl);
		return new OpenAiImageModel(builder.build());
	}

	static TextToSpeechModel createTextToSpeechModel(String url, String key, String defaultBaseUrl) {
		return createTextToSpeechModel(url, key, defaultBaseUrl, null);
	}

	/**
	 * @param audioPathRewrite 将 Spring AI 硬编码的 {@code /v1/audio/*} 路径改写为目标平台路径；不需要时传 {@code null}
	 */
	static TextToSpeechModel createTextToSpeechModel(String url, String key, String defaultBaseUrl,
			UnaryOperator<String> audioPathRewrite) {
		return new OpenAiAudioSpeechModel(buildAudioApi(url, key, defaultBaseUrl, audioPathRewrite));
	}

	static TranscriptionModel createTranscriptionModel(String url, String key, String defaultBaseUrl) {
		return createTranscriptionModel(url, key, defaultBaseUrl, null);
	}

	/**
	 * @param audioPathRewrite 将 Spring AI 硬编码的 {@code /v1/audio/*} 路径改写为目标平台路径；不需要时传 {@code null}
	 */
	static TranscriptionModel createTranscriptionModel(String url, String key, String defaultBaseUrl,
			UnaryOperator<String> audioPathRewrite) {
		return new OpenAiAudioTranscriptionModel(buildAudioApi(url, key, defaultBaseUrl, audioPathRewrite));
	}

	private static OpenAiAudioApi buildAudioApi(String url, String key, String defaultBaseUrl,
			UnaryOperator<String> audioPathRewrite) {
		OpenAiAudioApi.Builder builder = OpenAiAudioApi.builder()
			.apiKey(key);
		applyBaseUrl(builder::baseUrl, url, defaultBaseUrl);
		if (audioPathRewrite != null) {
			builder.restClientBuilder(RestClient.builder()
				.requestInterceptor(new AudioPathRewriteInterceptor(audioPathRewrite)));
			builder.webClientBuilder(WebClient.builder()
				.filter(audioPathRewriteFilter(audioPathRewrite)));
		}
		return builder.build();
	}

	private static void applyBaseUrl(Consumer<String> baseUrlSetter, String url, String defaultBaseUrl) {
		if (StringUtil.isNotBlank(url)) {
			baseUrlSetter.accept(url);
		}
		else if (StringUtil.isNotBlank(defaultBaseUrl)) {
			baseUrlSetter.accept(defaultBaseUrl);
		}
	}

	private static ExchangeFilterFunction audioPathRewriteFilter(UnaryOperator<String> audioPathRewrite) {
		return (request, next) -> {
			URI original = request.url();
			String rewrittenPath = audioPathRewrite.apply(original.getPath());
			if (rewrittenPath == null || rewrittenPath.equals(original.getPath())) {
				return next.exchange(request);
			}
			URI newUri = UriComponentsBuilder.fromUri(original)
				.replacePath(rewrittenPath)
				.build(true)
				.toUri();
			return next.exchange(ClientRequest.from(request).url(newUri).build());
		};
	}

	/**
	 * 将 OpenAiAudioApi 固定写入的 {@code /v1/audio/*} 路径改写为兼容平台实际路径。
	 */
	private static final class AudioPathRewriteInterceptor implements ClientHttpRequestInterceptor {

		private final UnaryOperator<String> audioPathRewrite;

		private AudioPathRewriteInterceptor(UnaryOperator<String> audioPathRewrite) {
			this.audioPathRewrite = audioPathRewrite;
		}

		@Override
		public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
				throws IOException {
			URI original = request.getURI();
			String rewrittenPath = audioPathRewrite.apply(original.getPath());
			if (rewrittenPath == null || rewrittenPath.equals(original.getPath())) {
				return execution.execute(request, body);
			}
			URI newUri = UriComponentsBuilder.fromUri(original)
				.replacePath(rewrittenPath)
				.build(true)
				.toUri();
			return execution.execute(new HttpRequestWrapper(request) {
				@Override
				public URI getURI() {
					return newUri;
				}
			}, body);
		}

	}

}
