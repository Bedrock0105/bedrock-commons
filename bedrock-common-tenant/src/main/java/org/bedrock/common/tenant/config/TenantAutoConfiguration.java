package org.bedrock.common.tenant.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import org.bedrock.common.mybatisplus.config.MybatisPlusConfiguration;
import org.bedrock.common.tenant.aspect.TenantIgnoreAspect;
import org.bedrock.common.tenant.handler.DefaultTenantHandler;
import org.bedrock.common.tenant.handler.TenantMetaObjectHandler;
import org.bedrock.common.tenant.props.TenantProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration(before = MybatisPlusConfiguration.class)
@EnableConfigurationProperties(TenantProperties.class)
public class TenantAutoConfiguration {

	/**
	 * 添加元数据处理器
	 */
	@Bean(name = "metaObjectHandler")
	@ConditionalOnMissingBean(name = "metaObjectHandler")
	public MetaObjectHandler metaObjectHandler() {
		return new TenantMetaObjectHandler();
	}

	@Bean
	@ConditionalOnMissingBean
	public TenantLineHandler tenantHandler(TenantProperties tenantProperties) {
		return new DefaultTenantHandler(tenantProperties);
	}

	@Bean
	@ConditionalOnMissingBean
	public TenantLineInnerInterceptor tenantLineInnerInterceptor(TenantLineHandler tenantLineHandler) {
		return new TenantLineInnerInterceptor(tenantLineHandler);
	}

	/**
	 * 添加忽略租户注解切面
	 */
	@Bean
	public TenantIgnoreAspect tenantIgnoreAspect() {
		return new TenantIgnoreAspect();
	}
}
