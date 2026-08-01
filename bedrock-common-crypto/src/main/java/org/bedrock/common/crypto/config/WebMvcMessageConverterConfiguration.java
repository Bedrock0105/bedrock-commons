package org.bedrock.common.crypto.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.bedrock.common.crypto.algorithm.CryptoAlgorithmRegistry;
import org.bedrock.common.crypto.props.CryptoProperties;
import org.bedrock.common.crypto.resolver.ApiDecryptParamResolver;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.StreamUtils;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Spring MVC 增强：消息转换器 + Query/Form 参数解密 Resolver。
 * <p>
 * 使用 {@link #extendMessageConverters}：在 Spring Boot 已装配的默认转换器列表上做增量修改，
 * 不会清空内置转换器（与 {@code configureMessageConverters} 不同）。
 * </p>
 * <p>
 * 将默认的 {@link MappingJackson2HttpMessageConverter} 替换为增强实现：
 * 当写出对象为 {@link String} 时直接写原文，避免 Jackson 再包一层双引号
 * （适配 API 加密后密文字符串返回的场景）。
 * </p>
 * <p>
 * {@link ApiDecryptParamResolver} 通过 {@link #addArgumentResolvers} 注册为
 * <strong>custom</strong> 解析器（位于注解型默认解析器之后、catch-all 之前）：
 * {@code @RequestBody} 仍走 Body Advice；显式 {@code @RequestParam} 等会被默认解析器先认领，
 * Query 密文场景需自行控制参数注解（例如不加 {@code @RequestParam}，由本 Resolver 绑定）。
 * </p>
 */
@AutoConfiguration(after = CryptoConfiguration.class)
@RequiredArgsConstructor
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(prefix = CryptoProperties.PREFIX, name = "enabled", havingValue = "true")
@ConditionalOnBean(CryptoAlgorithmRegistry.class)
public class WebMvcMessageConverterConfiguration implements WebMvcConfigurer {

	private final ObjectMapper objectMapper;

	private final CryptoProperties properties;

	private final CryptoAlgorithmRegistry registry;

	/**
	 * 注册 Query/Form 参数解密解析器（custom 段，优先级低于注解型默认解析器）。
	 */
	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
		argumentResolvers.add(new ApiDecryptParamResolver(properties, registry));
	}

	/**
	 * 在默认转换器列表基础上做增强，而不是整表替换。
	 *
	 * @param converters Spring Boot 已注入的转换器列表（可修改）
	 */
	@Override
	public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
		// String 转换器统一 UTF-8，避免平台默认编码
		for (HttpMessageConverter<?> converter : converters) {
			if (converter instanceof StringHttpMessageConverter stringConverter) {
				stringConverter.setDefaultCharset(StandardCharsets.UTF_8);
			}
		}

		// 用增强版 Jackson 替换默认 Jackson（保留原位置，不影响转换器优先级）
		for (int i = 0; i < converters.size(); i++) {
			HttpMessageConverter<?> converter = converters.get(i);
			if (converter instanceof MappingJackson2HttpMessageConverter) {
				converters.set(i, createPlainStringAwareJacksonConverter());
				return;
			}
		}

		// 列表中尚无 Jackson 时再追加（极少见）
		converters.add(createPlainStringAwareJacksonConverter());
	}

	/**
	 * 创建：String 明文写出、其它类型仍走 Jackson 的转换器。
	 */
	private MappingJackson2HttpMessageConverter createPlainStringAwareJacksonConverter() {
		return new MappingJackson2HttpMessageConverter(objectMapper) {
			@Override
			protected void writeInternal(Object object, Type type, HttpOutputMessage outputMessage)
				throws IOException, HttpMessageNotWritableException {
				// 加密密文等已是 String：直接写 body，不再 JSON 序列化成 "xxx"
				if (object instanceof String strVal) {
					Charset defaultCharset = getDefaultCharset();
					Charset charset = defaultCharset == null ? StandardCharsets.UTF_8 : defaultCharset;
					StreamUtils.copy(strVal, charset, outputMessage.getBody());
					return;
				}
				super.writeInternal(object, type, outputMessage);
			}
		};
	}
}
