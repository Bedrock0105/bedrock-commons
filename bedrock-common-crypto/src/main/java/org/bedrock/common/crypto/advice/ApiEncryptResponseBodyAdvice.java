package org.bedrock.common.crypto.advice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bedrock.common.code.util.AnnotationUtil;
import org.bedrock.common.code.util.JsonUtil;
import org.bedrock.common.crypto.algorithm.CryptoAlgorithm;
import org.bedrock.common.crypto.algorithm.CryptoAlgorithmRegistry;
import org.bedrock.common.crypto.annotation.ApiEncrypt;
import org.bedrock.common.crypto.annotation.ApiExcludeCrypto;
import org.bedrock.common.crypto.enums.CryptoAlgorithmEnum;
import org.bedrock.common.crypto.props.CryptoProperties;
import org.bedrock.common.spi.configuration.AutoIgnore;
import org.springframework.core.MethodParameter;
import org.springframework.core.Ordered;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * 响应体加密 Advice。
 * <p>
 * 对带有 {@link ResponseBody} / {@link RestController} 的返回值生效：当方法/类存在
 * {@link ApiEncrypt}（含 {@code @ApiCrypto}），且未被 {@link ApiExcludeCrypto} 排除时，
 * 将响应对象序列化为 JSON 后加密，以 Base64 密文字符串写出。
 * </p>
 *
 * @see ResponseBodyAdvice
 */
@Slf4j
@AutoIgnore
@ControllerAdvice
@RequiredArgsConstructor
public class ApiEncryptResponseBodyAdvice implements ResponseBodyAdvice<Object>, Ordered {

	private final CryptoProperties properties;

	private final CryptoAlgorithmRegistry registry;

	@Override
	public boolean supports(MethodParameter returnType, @NonNull Class converterType) {
		return needEncrypt(returnType);
	}

	@Nullable
	@Override
	public Object beforeBodyWrite(Object body, @NonNull MethodParameter returnType,
								  @NonNull MediaType selectedContentType, @NonNull Class selectedConverterType,
								  @NonNull ServerHttpRequest request, @NonNull ServerHttpResponse response) {
		if (body == null) {
			return null;
		}
		ApiEncrypt annotation = AnnotationUtil.findElementAnnotation(returnType.getMethod(), ApiEncrypt.class);
		if (annotation == null) {
			return body;
		}
		Class<? extends CryptoAlgorithm> algorithmClass = resolveAlgorithmClass(annotation);
		CryptoAlgorithm algorithm = registry.get(algorithmClass);
		String encrypt = algorithm.encrypt(JsonUtil.toJsonBytes(body), annotation.key(), annotation.publicKey(),
			annotation.privateKey());
		response.getHeaders().setContentType(MediaType.TEXT_PLAIN);
		return encrypt;
	}

	@Override
	public int getOrder() {
		return properties.getOrder();
	}

	/**
	 * 判断当前方法是否需要加密响应。
	 *
	 * @param returnType 返回值参数描述
	 * @return {@code true} 表示需要加密
	 */
	public boolean needEncrypt(MethodParameter returnType) {
		if (AnnotationUtil.isAnnotation(returnType.getMethod(), ApiExcludeCrypto.class)) {
			return false;
		}
		return AnnotationUtil.isElementAnnotation(returnType.getMethod(), ApiEncrypt.class);
	}

	/**
	 * 解析算法实现 Class：注解 handler &gt; 注解 algorithm 枚举 &gt; 配置。
	 *
	 * @param annotation 加密注解
	 * @return 算法实现类型
	 */
	private Class<? extends CryptoAlgorithm> resolveAlgorithmClass(ApiEncrypt annotation) {
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
}
