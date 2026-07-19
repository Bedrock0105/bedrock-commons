package org.bedrock.common.log.config;

import lombok.RequiredArgsConstructor;
import org.bedrock.common.log.aspect.OperationLogAspect;
import org.bedrock.common.log.aspect.RequestLogAspect;
import org.bedrock.common.log.feign.ILogSaveFeign;
import org.bedrock.common.log.operation.support.*;
import org.bedrock.common.log.props.LogProperties;
import org.bedrock.common.log.service.ILogSaveService;
import org.bedrock.common.log.service.impl.DefaultLogSaveServiceImpl;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.Collections;
import java.util.List;

@AutoConfiguration
@RequiredArgsConstructor
@EnableConfigurationProperties(LogProperties.class)
public class LogConfiguration implements SmartInitializingSingleton {

	private final LogProperties logProperties;

	private final ObjectProvider<List<IParseFunction>> iParseFunctions;

	private final ObjectProvider<List<IDeffParseFunction>> iDeffParseFunctions;

	@Bean
	public RequestLogAspect requestLogAspect() {
		return new RequestLogAspect(logProperties);
	}

	@Bean
	public OperationLogAspect operationLogAspect() {
		return new OperationLogAspect();
	}

	@Bean
	@ConditionalOnBean(ILogSaveFeign.class)
	@ConditionalOnMissingBean(ILogSaveService.class)
	public ILogSaveService logUnifiedProcessingSupport(ILogSaveFeign iLogSaveFeign) {
		return new DefaultLogSaveServiceImpl(iLogSaveFeign);
	}

	@Override
	public void afterSingletonsInstantiated() {
		iParseFunctions.ifAvailable(OperationLogCodeSupport::registerFunction);
		iDeffParseFunctions.ifAvailable(OperationLogCodeSupport::registerDeffFunction);
		OperationLogCodeSupport.registerDeffFunction(Collections.singletonList(new DefaultDeffParseFunction()));
		OperationLogCodeSupport.registerFunction(Collections.singletonList(new JsonParseFunction()));
	}

}
