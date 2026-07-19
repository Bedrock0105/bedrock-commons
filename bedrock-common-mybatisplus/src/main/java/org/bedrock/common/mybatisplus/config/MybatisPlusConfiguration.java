package org.bedrock.common.mybatisplus.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import lombok.RequiredArgsConstructor;
import org.bedrock.common.code.util.JsonUtil;
import org.bedrock.common.mybatisplus.handler.DefaultMetaObjectHandler;
import org.bedrock.common.mybatisplus.plugins.BedrockInnerInterceptor;
import org.bedrock.common.mybatisplus.plugins.MybatisCustomizeInnerInterceptor;
import org.bedrock.common.mybatisplus.props.MybatisPlusProperties;
import org.bedrock.common.code.annotation.BedrockPropertySource;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.List;

@AutoConfiguration
@RequiredArgsConstructor
@EnableConfigurationProperties(MybatisPlusProperties.class)
@MapperScan("org.bedrock.**.mapper.**")
@BedrockPropertySource("classpath:/mybatis-plus.yml")
public class MybatisPlusConfiguration implements InitializingBean {

	private final MybatisPlusProperties properties;

	/**
	 * 添加元数据处理器
	 */
	@Bean(name = "metaObjectHandler")
	@ConditionalOnMissingBean(name = "metaObjectHandler")
	public MetaObjectHandler metaObjectHandler() {
		return new DefaultMetaObjectHandler();
	}

	/**
	 * 添加分页插件
	 */
	@Bean
	public MybatisPlusInterceptor mybatisPlusInterceptor(ObjectProvider<List<BedrockInnerInterceptor>> bedrockInnerInterceptors,
														 ObjectProvider<TenantLineInnerInterceptor> tenantLineInnerInterceptor) {
		MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
		/**
		 * 添加自定义插件
		 */
		bedrockInnerInterceptors.ifAvailable(k -> interceptor.addInnerInterceptor(new MybatisCustomizeInnerInterceptor(k)));

		/**
		 * 添加租户插件
		 */
		tenantLineInnerInterceptor.ifAvailable(interceptor::addInnerInterceptor);

		/**
		 * 添加分页插件
		 */
		PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor();
		paginationInnerInterceptor.setMaxLimit(properties.getMaxLimit());
		paginationInnerInterceptor.setOverflow(properties.isOverflow());
		paginationInnerInterceptor.setOptimizeJoin(properties.isOptimizeJoin());
		interceptor.addInnerInterceptor(paginationInnerInterceptor);
		return interceptor;
	}

	/**
	 * 设置JacksonTypeHandler的ObjectMapper
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		JacksonTypeHandler.setObjectMapper(JsonUtil.getInstance());
	}
}
