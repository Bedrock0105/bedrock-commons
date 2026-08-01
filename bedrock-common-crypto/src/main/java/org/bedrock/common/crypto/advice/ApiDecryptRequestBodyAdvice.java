package org.bedrock.common.crypto.advice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bedrock.common.code.util.AnnotationUtil;
import org.bedrock.common.code.util.IoUtil;
import org.bedrock.common.crypto.algorithm.CryptoAlgorithm;
import org.bedrock.common.crypto.algorithm.CryptoAlgorithmRegistry;
import org.bedrock.common.crypto.annotation.ApiDecrypt;
import org.bedrock.common.crypto.annotation.ApiExcludeCrypto;
import org.bedrock.common.crypto.enums.CryptoAlgorithmEnum;
import org.bedrock.common.crypto.props.CryptoProperties;
import org.bedrock.common.spi.configuration.AutoIgnore;
import org.springframework.core.MethodParameter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdvice;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

/**
 * 请求体解密 Advice。
 * <p>
 * 对带有 {@link RequestBody} 的控制器入参生效：当方法/类存在 {@link ApiDecrypt}
 *（含 {@code @ApiCrypto}），且未被 {@link ApiExcludeCrypto} 排除时，将密文请求体解密为明文后再反序列化。
 * </p>
 *
 * @see RequestBodyAdvice
 */
@Slf4j
@AutoIgnore
@ControllerAdvice
@RequiredArgsConstructor
public class ApiDecryptRequestBodyAdvice implements RequestBodyAdvice, Ordered {

	private final CryptoProperties properties;

	private final CryptoAlgorithmRegistry registry;

	@Override
	public boolean supports(MethodParameter methodParameter, @NonNull Type targetType,
							@NonNull Class<? extends HttpMessageConverter<?>> converterType) {
		return needDecrypt(methodParameter);
	}

	@Override
	public Object handleEmptyBody(Object body, @NonNull HttpInputMessage inputMessage,
								  @NonNull MethodParameter parameter, @NonNull Type targetType,
								  @NonNull Class<? extends HttpMessageConverter<?>> converterType) {
		return body;
	}

	@NonNull
	@Override
	public HttpInputMessage beforeBodyRead(HttpInputMessage inputMessage, @NonNull MethodParameter parameter,
										   @NonNull Type targetType,
										   @NonNull Class<? extends HttpMessageConverter<?>> converterType)
		throws IOException {
		InputStream messageBody = inputMessage.getBody();
		ApiDecrypt annotation = AnnotationUtil.findElementAnnotation(parameter.getMethod(), ApiDecrypt.class);
		if (annotation == null) {
			return inputMessage;
		}
		Class<? extends CryptoAlgorithm> algorithmClass = resolveAlgorithmClass(annotation);
		CryptoAlgorithm algorithm = registry.get(algorithmClass);
		String copyToString = IoUtil.copyToString(messageBody, StandardCharsets.UTF_8);
		byte[] decryptedBody = algorithm.decrypt(copyToString, annotation.key(), annotation.publicKey(),
			annotation.privateKey());
		return new DecryptedHttpInputMessage(inputMessage.getHeaders(), new ByteArrayInputStream(decryptedBody));
	}

	@NonNull
	@Override
	public Object afterBodyRead(@NonNull Object body, @NonNull HttpInputMessage inputMessage,
							   @NonNull MethodParameter parameter, @NonNull Type targetType,
							   @NonNull Class<? extends HttpMessageConverter<?>> converterType) {
		return body;
	}

	@Override
	public int getOrder() {
		return properties.getOrder();
	}

	/**
	 * 判断当前方法是否需要解密请求体。
	 *
	 * @param parameter 方法参数
	 * @return {@code true} 表示需要解密
	 */
	public boolean needDecrypt(MethodParameter parameter) {
		if (AnnotationUtil.isAnnotation(parameter.getMethod(), ApiExcludeCrypto.class)) {
			return false;
		}
		return AnnotationUtil.isElementAnnotation(parameter.getMethod(), ApiDecrypt.class);
	}

	/**
	 * 解析算法实现 Class：注解 handler &gt; 注解 algorithm 枚举 &gt; 配置。
	 *
	 * @param annotation 解密注解
	 * @return 算法实现类型
	 */
	private Class<? extends CryptoAlgorithm> resolveAlgorithmClass(ApiDecrypt annotation) {
		Class<? extends CryptoAlgorithm> handler = annotation.handler();
		if (handler != CryptoAlgorithm.class) {
			return handler;
		}
		CryptoAlgorithmEnum algorithm = annotation.algorithm();
		handler = algorithm.getAlgorithmClass();
		if (handler != CryptoAlgorithm.class) {
			return handler;
		}
		return properties.getAlgorithmClass();
	}

	/**
	 * 解密后的请求体包装。
	 *
	 * @param headers 原始请求头
	 * @param body    解密后的明文流
	 */
	private record DecryptedHttpInputMessage(HttpHeaders headers, InputStream body) implements HttpInputMessage {

		@Override
		public InputStream getBody() {
			return body;
		}

		@Override
		public HttpHeaders getHeaders() {
			return headers;
		}
	}
}
