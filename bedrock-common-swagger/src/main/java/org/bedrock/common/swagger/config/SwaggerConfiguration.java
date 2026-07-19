package org.bedrock.common.swagger.config;

import com.github.xiaoymin.knife4j.spring.configuration.Knife4jAutoConfiguration;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.bedrock.common.code.annotation.BedrockPropertySource;
import org.bedrock.common.swagger.props.SwaggerProperties;
import org.springdoc.core.configuration.SpringDocConfiguration;
import org.springdoc.core.configuration.SpringDocPageableConfiguration;
import org.springdoc.core.configuration.SpringDocSortConfiguration;
import org.springdoc.core.customizers.DataRestDelegatingMethodParameterCustomizer;
import org.springdoc.core.customizers.DelegatingMethodParameterCustomizer;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springdoc.core.extractor.DelegatingMethodParameter;
import org.springdoc.core.models.GroupedOpenApi;
import org.springdoc.core.providers.RepositoryRestConfigurationProvider;
import org.springdoc.core.providers.SpringDataWebPropertiesProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.HttpHeaders;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Optional;

@Slf4j
@AutoConfiguration(before = {SpringDocConfiguration.class, Knife4jAutoConfiguration.class, SpringDocPageableConfiguration.class, SpringDocSortConfiguration.class})
@BedrockPropertySource("classpath:/swagger.yml")
@RequiredArgsConstructor
@EnableConfigurationProperties(SwaggerProperties.class)
@ConditionalOnProperty(prefix = SwaggerProperties.PREFIX, value = "enabled", havingValue = "true", matchIfMissing = true)
public class SwaggerConfiguration {

	private final SwaggerProperties swaggerProperties;

	private static final String TENANT_ID_HEADER_NAME = "X-Tenant-ID";

	private static final String USER_ACCOUNT_ID_HEADER_NAME = "X-User-Account-ID";

	@Bean
	@ConditionalOnMissingBean
	public GlobalOpenApiCustomizer globalOpenApiCustomizer() {
		return openApi -> {
			// 全局添加鉴权参数
			if (openApi.getPaths() != null) {
				openApi.getPaths().forEach((s, pathItem) -> {
					// 接口添加鉴权参数
					pathItem.readOperations()
						.forEach(operation ->
							operation.addSecurityItem(new SecurityRequirement()
								.addList(HttpHeaders.AUTHORIZATION)
								.addList(TENANT_ID_HEADER_NAME)
								.addList(USER_ACCOUNT_ID_HEADER_NAME))
						);
				});
			}
		};
	}

	@Bean
	@ConditionalOnMissingBean
	public GroupedOpenApi publicApi() {
		return GroupedOpenApi.builder()
			.group("default")
			.pathsToMatch(swaggerProperties.getPathsToMatch().toArray(new String[0]))
			.pathsToExclude(swaggerProperties.getPathsToExclude().toArray(new String[0]))
			.packagesToScan(swaggerProperties.getPackagesToScan().toArray(new String[0]))
			.packagesToExclude(swaggerProperties.getPackagesToExclude().toArray(new String[0]))
			.build();
	}

	@Bean
	@ConditionalOnMissingBean(OpenAPI.class)
	public OpenAPI customOpenAPI() {
		return new OpenAPI()
			.info(new Info()
				.title(swaggerProperties.getInfo().getTitle())
				.version(swaggerProperties.getInfo().getVersion())
				.description(swaggerProperties.getInfo().getDescription())
				.termsOfService(swaggerProperties.getInfo().getTermsOfService())
				.contact(new Contact().email(swaggerProperties.getInfo().getContact().getEmail())
					.url(swaggerProperties.getInfo().getContact().getUrl())
					.name(swaggerProperties.getInfo().getContact().getName()))
				.license(new License().name(swaggerProperties.getInfo().getLicense().getName())
					.url(swaggerProperties.getInfo().getLicense().getUrl())))
			.components(new Components()
				.addSecuritySchemes(HttpHeaders.AUTHORIZATION,
					new SecurityScheme()
						.name(HttpHeaders.AUTHORIZATION)
						.type(SecurityScheme.Type.APIKEY)
						.in(SecurityScheme.In.HEADER)
						.scheme("Bearer")
						.bearerFormat("JWT")
				)
				.addSecuritySchemes(TENANT_ID_HEADER_NAME, new SecurityScheme()
					.name(TENANT_ID_HEADER_NAME)
					.type(SecurityScheme.Type.APIKEY)
					.in(SecurityScheme.In.HEADER))
				.addSecuritySchemes(USER_ACCOUNT_ID_HEADER_NAME, new SecurityScheme()
					.name(USER_ACCOUNT_ID_HEADER_NAME)
					.type(SecurityScheme.Type.APIKEY)
					.in(SecurityScheme.In.HEADER))

			);
	}

	/**
	 * 此类为了实现@Parameter(hidden = true) 在参数是个对象前面优先级低的问题
	 * 要是有更好的方法请替换掉
	 */
	@Bean
	@ConditionalOnMissingBean
	public DelegatingMethodParameterCustomizer delegatingMethodParameterCustomizer(Optional<SpringDataWebPropertiesProvider> optionalSpringDataWebPropertiesProvider,
																				   Optional<RepositoryRestConfigurationProvider> optionalRepositoryRestConfiguration) {
		return new DataRestDelegatingMethodParameterCustomizer(optionalSpringDataWebPropertiesProvider, optionalRepositoryRestConfiguration) {
			@Override
			public void customize(MethodParameter originalParameter, MethodParameter methodParameter) {
				io.swagger.v3.oas.annotations.Parameter parameterDoc = AnnotatedElementUtils.findMergedAnnotation(
					AnnotatedElementUtils.forAnnotations(originalParameter.getParameterAnnotations()),
					io.swagger.v3.oas.annotations.Parameter.class);
				if (parameterDoc != null && parameterDoc.hidden()) {
					try {
						Field field = FieldUtils.getDeclaredField(DelegatingMethodParameter.class, "additionalParameterAnnotations", true);
						field.set(methodParameter, new Annotation[]{parameterDoc});
						return;
					} catch (IllegalAccessException e) {
						log.warn("Failed to set hidden parameter annotation: {}", e.getMessage(), e);
					}

				}
				super.customize(originalParameter, methodParameter);

			}
		};
	}
}
