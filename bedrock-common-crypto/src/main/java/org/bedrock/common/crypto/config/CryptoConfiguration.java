package org.bedrock.common.crypto.config;

import lombok.RequiredArgsConstructor;
import org.bedrock.common.crypto.advice.ApiDecryptRequestBodyAdvice;
import org.bedrock.common.crypto.advice.ApiEncryptResponseBodyAdvice;
import org.bedrock.common.crypto.algorithm.CryptoAlgorithmRegistry;
import org.bedrock.common.crypto.props.CryptoProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * API 加解密自动配置。
 * <p>
 * 注册算法注册中心、请求体解密 / 响应体加密 Advice。
 * 通过 {@code bedrock.crypto.enabled} 控制开关（默认开启）。
 * </p>
 */
@AutoConfiguration
@RequiredArgsConstructor
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@EnableConfigurationProperties(CryptoProperties.class)
@ConditionalOnProperty(prefix = CryptoProperties.PREFIX, name = "enabled", havingValue = "true")
public class CryptoConfiguration {

	private final CryptoProperties properties;

	/**
	 * SPI 算法注册中心。
	 *
	 * @return 注册中心
	 */
	@Bean
	@ConditionalOnMissingBean
	public CryptoAlgorithmRegistry cryptoAlgorithmRegistry() {
		return new CryptoAlgorithmRegistry(properties);
	}

	/**
	 * 响应体加密 Advice。
	 *
	 * @param registry 算法注册中心
	 * @return Advice
	 */
	@Bean
	public ApiEncryptResponseBodyAdvice apiEncryptResponseBodyAdvice(CryptoAlgorithmRegistry registry) {
		return new ApiEncryptResponseBodyAdvice(properties, registry);
	}

	/**
	 * 请求体解密 Advice。
	 *
	 * @param registry 算法注册中心
	 * @return Advice
	 */
	@Bean
	public ApiDecryptRequestBodyAdvice apiDecryptRequestBodyAdvice(CryptoAlgorithmRegistry registry) {
		return new ApiDecryptRequestBodyAdvice(properties, registry);
	}
}
