package org.bedrock.common.dynamic.config;

import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.dynamic.datasource.aop.DynamicDataSourceAnnotationAdvisor;
import com.baomidou.dynamic.datasource.creator.DataSourceProperty;
import com.baomidou.dynamic.datasource.creator.DefaultDataSourceCreator;
import com.baomidou.dynamic.datasource.processor.DsProcessor;
import com.baomidou.dynamic.datasource.provider.DynamicDataSourceProvider;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.*;
import lombok.RequiredArgsConstructor;
import org.bedrock.common.code.util.StringUtil;
import org.bedrock.common.dynamic.aspect.DynamicDataSourceAnnotationInterceptor;
import org.bedrock.common.dynamic.provider.DefaultAbstractJdbcDataSourceProvider;
import org.bedrock.common.dynamic.service.DataSourceDynamicLoad;
import org.bedrock.common.dynamic.service.DataSourceStartLoad;
import org.bedrock.common.dynamic.support.DataSourceDynamicLoadSupport;
import org.bedrock.common.dynamic.support.DataSourceStartLoadSupport;
import org.springframework.aop.Advisor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Role;
import org.springframework.core.annotation.Order;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.List;

@AutoConfiguration(before = DynamicDataSourceAutoConfiguration.class, beforeName = "com.alibaba.druid.spring.boot3.autoconfigure.DruidDataSourceAutoConfigure")
@RequiredArgsConstructor
@Import({DruidDynamicDataSourceConfiguration.class, DynamicDataSourceCreatorAutoConfiguration.class, DynamicDataSourceAopConfiguration.class, DynamicDataSourceAssistConfiguration.class})
@EnableConfigurationProperties({DataSourceProperties.class})
public class DynamicAutoConfiguration implements InitializingBean {

	private final DynamicDataSourceProperties properties;

	private final DataSourceProperties dataSourceProperties;



	@Bean
	@ConditionalOnMissingBean
	public DataSource dataSource(List<DynamicDataSourceProvider> providers) {
		DynamicRoutingDataSource dataSource = new DynamicRoutingDataSource(providers);
		dataSource.setPrimary(properties.getPrimary());
		dataSource.setStrict(properties.getStrict());
		dataSource.setStrategy(properties.getStrategy());
		dataSource.setP6spy(properties.getP6spy());
		dataSource.setSeata(properties.getSeata());
		dataSource.setGraceDestroy(properties.getGraceDestroy());
		return dataSource;
	}

	@Bean
	@ConditionalOnMissingBean
	public DataSourceDynamicLoadSupport dataSourceDynamicLoadSupport(DataSource dataSource,
																	 ObjectProvider<List<DataSourceDynamicLoad>> dataSourceDynamicLoads) {
		return new DataSourceDynamicLoadSupport(dataSourceDynamicLoads.getIfAvailable(Collections::emptyList), dataSource);
	}

	@Bean
	@ConditionalOnMissingBean
	public DataSourceStartLoadSupport dataSourceStartLoadSupport(ObjectProvider<List<DataSourceStartLoad>> DataSourceStartLoads) {
		return new DataSourceStartLoadSupport(DataSourceStartLoads.getIfAvailable(Collections::emptyList));
	}

	@Bean
	@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
	@ConditionalOnProperty(prefix = DynamicDataSourceProperties.PREFIX + ".aop", name = "enabled", havingValue = "true", matchIfMissing = true)
	public Advisor dynamicDatasourceAnnotationAdvisor(DsProcessor dsProcessor, DataSourceDynamicLoadSupport dataSourceDynamicLoadSupport) {
		DynamicDatasourceAopProperties aopProperties = properties.getAop();
		DynamicDataSourceAnnotationInterceptor interceptor = new DynamicDataSourceAnnotationInterceptor(aopProperties.getAllowedPublicOnly(), dsProcessor, dataSourceDynamicLoadSupport);
		DynamicDataSourceAnnotationAdvisor advisor = new DynamicDataSourceAnnotationAdvisor(interceptor, DS.class);
		advisor.setOrder(aopProperties.getOrder());
		return advisor;
	}

	@Bean
	@Order(1)
	public DynamicDataSourceProvider defaultAbstractJdbcDataSourceProvider(DefaultDataSourceCreator defaultDataSourceCreator,
																		   DataSourceStartLoadSupport support) {
		return new DefaultAbstractJdbcDataSourceProvider(defaultDataSourceCreator, support, properties);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		properties.getDatasource().computeIfAbsent(properties.getPrimary(),
			k -> {
				if (dataSourceProperties == null || StringUtil.isBlank(dataSourceProperties.getUrl())) {
					return null;
				}
				DataSourceProperty dataSourceProperty = new DataSourceProperty();
				dataSourceProperty.setUrl(dataSourceProperties.getUrl());
				dataSourceProperty.setUsername(dataSourceProperties.getUsername());
				dataSourceProperty.setPassword(dataSourceProperties.getPassword());
				dataSourceProperty.setDriverClassName(dataSourceProperties.getDriverClassName());
				dataSourceProperty.setP6spy(properties.getP6spy());
				dataSourceProperty.setSeata(properties.getSeata());
				dataSourceProperty.setLazy(properties.getLazy());
				dataSourceProperty.setPublicKey(properties.getPublicKey());
				return dataSourceProperty;
			});
	}
}
