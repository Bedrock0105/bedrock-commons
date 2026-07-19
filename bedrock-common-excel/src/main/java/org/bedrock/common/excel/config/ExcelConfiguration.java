package org.bedrock.common.excel.config;

import cn.idev.excel.converters.Converter;
import cn.idev.excel.util.ClassUtils;
import lombok.RequiredArgsConstructor;
import org.bedrock.common.excel.converter.DefaultConverter;
import org.bedrock.common.excel.support.ExcelConverterResolver;
import org.bedrock.common.excel.support.HandlerExcelConverterResolver;
import org.bedrock.common.excel.support.RequestExcelArgumentResolver;
import org.bedrock.common.excel.support.ResponseReturnValueHandler;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@AutoConfiguration
@RequiredArgsConstructor
public class ExcelConfiguration implements InitializingBean {

	private final RequestMappingHandlerAdapter requestMappingHandlerAdapter;

	@Bean
	public ExcelConverterResolver excelConverterResolver() {
		return new ExcelConverterResolver();
	}

	/**
	 * 默认的转换器
	 */
	@Bean
	@Primary
	@ConditionalOnMissingBean(name = "defaultConverter")
	public Converter<Object> defaultConverter(ObjectProvider<List<HandlerExcelConverterResolver>> excelConvertToJavaData) {
		DefaultConverter defaultConverter = new DefaultConverter(excelConvertToJavaData.getIfAvailable(Collections::emptyList));
		ClassUtils.setDefaultConverter(defaultConverter);
		return defaultConverter;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		List<HandlerMethodArgumentResolver> argumentResolvers = requestMappingHandlerAdapter.getArgumentResolvers();
		List<HandlerMethodArgumentResolver> newArgumentResolvers = new ArrayList<>();
		newArgumentResolvers.add(new RequestExcelArgumentResolver());
		if (argumentResolvers != null) {
			newArgumentResolvers.addAll(argumentResolvers);
		}
		requestMappingHandlerAdapter.setArgumentResolvers(newArgumentResolvers);
		List<HandlerMethodReturnValueHandler> returnValueHandlers = requestMappingHandlerAdapter.getReturnValueHandlers();
		List<HandlerMethodReturnValueHandler> newReturnValueHandlers = new ArrayList<>();
		newReturnValueHandlers.add(new ResponseReturnValueHandler());
		if (returnValueHandlers != null) {
			newReturnValueHandlers.addAll(returnValueHandlers);
		}
		requestMappingHandlerAdapter.setReturnValueHandlers(newReturnValueHandlers);

	}
}
