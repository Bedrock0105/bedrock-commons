package org.bedrock.common.ai.config;

import org.bedrock.common.ai.factory.model.AiModelFactory;
import org.bedrock.common.ai.factory.model.DefaultAiModelFactoryImpl;
import org.bedrock.common.ai.factory.vectorstore.DefaultVectorStoreFactoryImpl;
import org.bedrock.common.ai.factory.vectorstore.VectorStoreFactory;
import org.bedrock.common.ai.mcp.McpManager;
import org.bedrock.common.ai.tool.ToolManager;
import org.springframework.ai.mcp.annotation.spring.ClientMcpSyncHandlersRegistry;
import org.springframework.ai.tool.resolution.ToolCallbackResolver;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * AI 公共自动配置。
 * <p>
 * {@link McpManager} 通过 {@link ObjectProvider} 可选注入 Spring AI 同步 MCP Handler Registry。
 */
@AutoConfiguration
public class AiAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public AiModelFactory aiModelFactory() {
		return new DefaultAiModelFactoryImpl();
	}

	@Bean
	@ConditionalOnMissingBean
	public VectorStoreFactory vectorStoreFactory() {
		return new DefaultVectorStoreFactoryImpl();
	}

	@Bean
	@ConditionalOnMissingBean
	public ToolManager toolManager(ToolCallbackResolver toolCallbackResolver) {
		return new ToolManager(toolCallbackResolver);
	}

	/** 动态 MCP 管理器（同步） */
	@Bean(destroyMethod = "close")
	@ConditionalOnMissingBean
	public McpManager mcpManager(ObjectProvider<ClientMcpSyncHandlersRegistry> syncHandlersRegistry) {
		return new McpManager(syncHandlersRegistry);
	}
}
