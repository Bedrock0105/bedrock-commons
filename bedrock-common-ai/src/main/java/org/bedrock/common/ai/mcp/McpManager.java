package org.bedrock.common.ai.mcp;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import io.modelcontextprotocol.client.transport.customizer.McpSyncHttpClientRequestCustomizer;
import io.modelcontextprotocol.json.jackson2.JacksonMcpJsonMapper;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.extern.slf4j.Slf4j;
import org.bedrock.common.code.util.CollectionUtil;
import org.bedrock.common.code.util.JsonUtil;
import org.bedrock.common.code.util.StringUtil;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.mcp.annotation.spring.ClientMcpSyncHandlersRegistry;
import org.springframework.ai.mcp.client.common.autoconfigure.NamedClientMcpTransport;
import org.springframework.ai.mcp.client.common.autoconfigure.properties.McpStdioClientProperties;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.util.Assert;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * MCP（Model Context Protocol）客户端动态管理器（同步）。
 * <p>
 * 负责按配置动态创建传输层（SSE / Streamable HTTP / STDIO）、初始化 {@link McpSyncClient}，
 * 并将其包装为 {@link ToolCallbackProvider} 注册到本管理器，供对话链路按名称拉取工具。
 * <p>
 * Bean 销毁由 Spring {@code destroyMethod = "close"} 触发
 * <p>
 * <b>典型用法：</b>
 * <ol>
 *   <li>{@link #listTools(McpClientParameters)} — 临时建连拉工具列表，用于测试 MCP 配置是否可用</li>
 *   <li>{@link #register(String, McpClientParameters)} — 长期注册（registrationId 与业务 name 分离）</li>
 *   <li>{@link #getCallbacks(String)} — 运行时按注册 ID 取 {@link ToolCallback}</li>
 *   <li>{@link #unregister(String)} / {@link #close()} — 释放连接</li>
 * </ol>
 */
@Slf4j
public class McpManager {

	/**
	 * 默认 MCP client name（写入 {@link McpSchema.Implementation}）
	 */
	private static final String DEFAULT_CLIENT_NAME = "spring-ai-mcp-client";

	/**
	 * 默认客户端版本号
	 */
	private static final String DEFAULT_VERSION = "1.0.0";

	/**
	 * 默认请求超时（秒）
	 */
	private static final int DEFAULT_REQUEST_TIMEOUT_SECONDS = 30;

	/**
	 * SSE 默认 endpoint
	 */
	private static final String DEFAULT_SSE_ENDPOINT = "/sse";

	/**
	 * Streamable HTTP 默认 endpoint
	 */
	private static final String DEFAULT_STREAMABLE_HTTP_ENDPOINT = "/mcp";

	/**
	 * Claude Desktop / Spring AI stdio 配置根键
	 */
	private static final String MCP_SERVERS_KEY = "mcpServers";

	private static final JacksonMcpJsonMapper JACKSON_MCP_JSON_MAPPER =
		new JacksonMcpJsonMapper(JsonUtil.getInstance());

	/**
	 * Spring AI 注解回调注册表（同步），可选依赖。
	 * 未启用 MCP annotation 时 {@link ObjectProvider#getIfAvailable()} 为 null。
	 */
	private final ObjectProvider<ClientMcpSyncHandlersRegistry> clientMcpSyncHandlersRegistry;

	/**
	 * 已注册的 MCP 工具提供者。
	 * <p>
	 * key 为注册 ID（如数据库主键字符串），与 {@link McpClientParameters#name()} 业务名分离：
	 * 业务名用于 NamedTransport / Implementation 展示，注册 ID 用于稳定查找与生命周期管理。
	 */
	private final Map<String, McpRegistration> registrations = new ConcurrentHashMap<>();

	/**
	 * 构造函数。
	 */
	public McpManager(ObjectProvider<ClientMcpSyncHandlersRegistry> clientMcpSyncHandlersRegistry) {
		this.clientMcpSyncHandlersRegistry = clientMcpSyncHandlersRegistry;
	}

	// -------------------------------------------------------------------------
	// 对外 API
	// -------------------------------------------------------------------------

	/**
	 * 按注册 ID 获取工具回调列表。
	 *
	 * @param registrationId 注册时指定的 ID（通常为 DB 主键字符串）
	 * @return 工具回调；未注册时返回空列表（非 null）
	 */
	public List<ToolCallback> getCallbacks(String registrationId) {
		McpRegistration registration = registrations.get(registrationId);
		if (registration == null) {
			return Collections.emptyList();
		}
		ToolCallback[] callbacks = registration.provider().getToolCallbacks();
		if (callbacks.length == 0) {
			return Collections.emptyList();
		}
		return List.of(callbacks);
	}

	/**
	 * 临时连接 MCP Server 并拉取工具列表，用于配置连通性测试。
	 * <p>
	 * 无论成功失败都会关闭临时 Client，不会写入 {@link #registrations}。
	 */
	public List<McpSchema.Tool> listTools(McpClientParameters parameters) {
		validate(parameters);
		List<NamedClientMcpTransport> transports = buildTransports(parameters);
		List<McpSyncClient> clients = createSyncClients(parameters, transports, null);
		try {
			return clients.stream()
					.map(client -> client.listTools().tools())
					.flatMap(List::stream)
					.collect(Collectors.toList());
		} finally {
			closeSyncClientsQuietly(clients);
		}
	}

	/**
	 * 注册长期存活的 MCP 工具提供者。
	 * <p>
	 * {@code registrationId} 作为本管理器 Map 的 key；{@link McpClientParameters#name()} 仍用于
	 * NamedTransport 名与 Client Implementation 展示名。
	 * <p>
	 * 若同一 registrationId 已存在，会先关闭并移除旧注册，再写入新注册（避免连接泄漏）。
	 * 远端 tools 列表变更时会清空 {@link SyncMcpToolCallbackProvider} 本地缓存。
	 *
	 * @param registrationId 稳定注册 ID（如 DB 主键字符串）
	 * @param parameters     连接参数（其中 {@code name} 为业务名，不可用雪花 id）
	 */
	public void register(String registrationId, McpClientParameters parameters) {
		Assert.hasText(registrationId, "registrationId must not be blank");
		validate(parameters);
		CachingToolCallbackProvider cachingProvider = new CachingToolCallbackProvider();
		Consumer<List<McpSchema.Tool>> onToolsChange = tools -> cachingProvider.invalidateCache();

		List<McpSyncClient> clients = createSyncClients(parameters, buildTransports(parameters), onToolsChange);
		SyncMcpToolCallbackProvider provider = SyncMcpToolCallbackProvider.builder()
			.mcpClients(clients)
			.build();
		cachingProvider.setDelegate(provider);

		AutoCloseable closer = () -> closeSyncClientsQuietly(clients);
		replaceRegistration(registrationId, new McpRegistration(registrationId, cachingProvider, closer));
	}

	/**
	 * 仅把外部已构建好的 {@link ToolCallbackProvider} 挂到管理器（不负责关闭底层连接）。
	 * <p>
	 * 若需要统一释放连接，请改用 {@link #register(String, McpClientParameters)}。
	 */
	public void registerToolCallbackProvider(String registrationId, ToolCallbackProvider provider) {
		Assert.hasText(registrationId, "registrationId must not be blank");
		Assert.notNull(provider, "provider must not be null");
		replaceRegistration(registrationId, new McpRegistration(registrationId, provider, null));
	}

	/**
	 * 注销指定注册 ID，并关闭其底层 Client（若有）。
	 *
	 * @param registrationId 注册 ID；不存在时静默忽略
	 */
	public void unregister(String registrationId) {
		McpRegistration removed = registrations.remove(registrationId);
		if (removed == null) {
			return;
		}
		closeRegistration(removed);
	}

	/**
	 * 关闭全部已注册 Client。由 Spring Bean {@code destroyMethod = "close"} 调用。
	 */
	public void close() {
		List<McpRegistration> snapshot = new ArrayList<>(registrations.values());
		registrations.clear();
		for (McpRegistration registration : snapshot) {
			closeRegistration(registration);
		}
	}

	// -------------------------------------------------------------------------
	// Client 创建
	// -------------------------------------------------------------------------

	/**
	 * 为每个 NamedTransport 创建并 initialize 同步 MCP Client。
	 *
	 * @param onToolsChange 额外的 tools 变更回调（可与 Spring Handler 注册表链式执行），可为 null
	 */
	public List<McpSyncClient> createSyncClients(McpClientParameters parameters,
	                                             List<NamedClientMcpTransport> namedTransports,
	                                             Consumer<List<McpSchema.Tool>> onToolsChange) {
		List<McpSyncClient> clients = new ArrayList<>(namedTransports.size());
		for (NamedClientMcpTransport namedTransport : namedTransports) {
			McpSchema.Implementation clientInfo = new McpSchema.Implementation(
				connectedClientName(parameters.resolvedClientName(), namedTransport.name()),
				parameters.resolvedVersion());

			McpClient.SyncSpec spec = McpClient.sync(namedTransport.transport())
				.clientInfo(clientInfo)
				.requestTimeout(Duration.ofSeconds(parameters.resolvedRequestTimeoutSeconds()));

			applySyncHandlers(spec, namedTransport.name(), onToolsChange);

			McpSyncClient client = spec.build();
			client.initialize();
			clients.add(client);
		}
		return clients;
	}

	/**
	 * 挂载同步 Handler 注册表，并链式合并额外 toolsChange 回调。
	 * <p>
	 * 注意：{@code toolsChangeConsumer} 是覆盖式 API，故注册表回调与清缓存回调需合并后一次设置。
	 */
	private void applySyncHandlers(McpClient.SyncSpec spec,
	                               String transportName,
	                               Consumer<List<McpSchema.Tool>> onToolsChange) {
		ClientMcpSyncHandlersRegistry syncRegistry = clientMcpSyncHandlersRegistry.getIfAvailable();
		if (syncRegistry != null) {
			spec.sampling(request -> syncRegistry.handleSampling(transportName, request))
				.elicitation(request -> syncRegistry.handleElicitation(transportName, request))
				.loggingConsumer(notification -> syncRegistry.handleLogging(transportName, notification))
				.progressConsumer(notification -> syncRegistry.handleProgress(transportName, notification))
				.promptsChangeConsumer(prompts -> syncRegistry.handlePromptListChanged(transportName, prompts))
				.resourcesChangeConsumer(resources -> syncRegistry.handleResourceListChanged(transportName, resources))
				.capabilities(syncRegistry.getCapabilities(transportName));
		}

		if (syncRegistry != null || onToolsChange != null) {
			spec.toolsChangeConsumer(tools -> {
				if (syncRegistry != null) {
					syncRegistry.handleToolListChanged(transportName, tools);
				}
				if (onToolsChange != null) {
					onToolsChange.accept(tools);
				}
			});
		}
	}

	// -------------------------------------------------------------------------
	// Transport 构建
	// -------------------------------------------------------------------------

	/**
	 * 按传输类型构建 NamedTransport 列表。
	 * <ul>
	 *   <li>SSE / STREAMABLE_HTTP：通常只有 1 个 transport</li>
	 *   <li>STDIO：Claude Desktop JSON 里可配置多个 server，对应多个 transport</li>
	 * </ul>
	 */
	private List<NamedClientMcpTransport> buildTransports(McpClientParameters parameters) {
		return switch (parameters.type()) {
			case SSE -> List.of(buildSseTransport(parameters));
			case STREAMABLE_HTTP -> List.of(buildStreamableHttpTransport(parameters));
			case STDIO -> buildStdioTransports(parameters.stdioServersJson());
		};
	}

	/**
	 * 构建 SSE HTTP Transport。
	 * <p>
	 * {@code url} 为 baseUrl，{@code endpoint} 默认为 {@code /sse}。
	 */
	private NamedClientMcpTransport buildSseTransport(McpClientParameters parameters) {
		String endpoint = StringUtil.isNotBlank(parameters.endpoint())
			? parameters.endpoint()
			: DEFAULT_SSE_ENDPOINT;

		HttpClientSseClientTransport.Builder builder = HttpClientSseClientTransport.builder(parameters.url())
			.sseEndpoint(endpoint)
			.clientBuilder(HttpClient.newBuilder())
			.jsonMapper(JACKSON_MCP_JSON_MAPPER);

		applyHttpHeaders(builder::httpRequestCustomizer, parameters.headers());
		return new NamedClientMcpTransport(parameters.name(), builder.build());
	}

	/**
	 * 构建 Streamable HTTP Transport。
	 * <p>
	 * 参数形态与 SSE 接近：{@code url} + {@code endpoint}（默认 {@code /mcp}）+ optional headers。
	 */
	private NamedClientMcpTransport buildStreamableHttpTransport(McpClientParameters parameters) {
		String endpoint = StringUtil.isNotBlank(parameters.endpoint())
			? parameters.endpoint()
			: DEFAULT_STREAMABLE_HTTP_ENDPOINT;

		HttpClientStreamableHttpTransport.Builder builder = HttpClientStreamableHttpTransport.builder(parameters.url())
			.endpoint(endpoint)
			.clientBuilder(HttpClient.newBuilder())
			.jsonMapper(JACKSON_MCP_JSON_MAPPER);

		applyHttpHeaders(builder::httpRequestCustomizer, parameters.headers());
		return new NamedClientMcpTransport(parameters.name(), builder.build());
	}

	/**
	 * 将自定义 Header 挂到 HTTP Transport 请求上。
	 */
	private void applyHttpHeaders(Consumer<McpSyncHttpClientRequestCustomizer> customizerSetter,
	                              Map<String, String> headers) {
		if (CollectionUtil.isEmpty(headers)) {
			return;
		}
		Map<String, String> snapshot = Map.copyOf(headers);
		customizerSetter.accept((request, method, url, body, context) ->
			snapshot.forEach(request::header));
	}

	/**
	 * 解析 Claude Desktop 格式 JSON，为每个 server 创建 STDIO Transport。
	 * <p>
	 * 支持两种入参形态：
	 * <pre>{@code
	 * // 1）完整 Claude Desktop / mcpServers 根对象
	 * {
	 *   "mcpServers": {
	 *     "filesystem": {
	 *       "command": "npx",
	 *       "args": ["-y", "@modelcontextprotocol/server-filesystem", "/tmp"],
	 *       "env": { "FOO": "bar" }
	 *     }
	 *   }
	 * }
	 *
	 * // 2）直接传 servers 映射（省略 mcpServers 外壳）
	 * {
	 *   "filesystem": {
	 *     "command": "npx",
	 *     "args": ["-y", "@modelcontextprotocol/server-filesystem", "/tmp"]
	 *   }
	 * }
	 * }</pre>
	 */
	private List<NamedClientMcpTransport> buildStdioTransports(String stdioServersJson) {
		Map<String, ServerParameters> serverParametersMap = parseStdioServerParameters(stdioServersJson);
		if (serverParametersMap.isEmpty()) {
			throw new IllegalArgumentException("STDIO config is empty: no mcpServers found");
		}
		List<NamedClientMcpTransport> transports = new ArrayList<>(serverParametersMap.size());
		for (Map.Entry<String, ServerParameters> entry : serverParametersMap.entrySet()) {
			StdioClientTransport transport = new StdioClientTransport(entry.getValue(), JACKSON_MCP_JSON_MAPPER);
			transports.add(new NamedClientMcpTransport(entry.getKey(), transport));
		}
		return transports;
	}

	/**
	 * 将 Claude Desktop JSON 转为 {@code serverName -> ServerParameters}。
	 * <p>
	 * 内部复用 Spring AI {@link McpStdioClientProperties.Parameters#toServerParameters()}，
	 * 与官方 {@code spring.ai.mcp.client.stdio.servers-configuration} 解析口径一致。
	 */
	private Map<String, ServerParameters> parseStdioServerParameters(String stdioServersJson) {
		try {
			JsonNode root = JsonUtil.parseJsonNode(stdioServersJson);
			if (root == null || root.isNull() || root.isMissingNode()) {
				throw new IllegalArgumentException("STDIO JSON is empty or invalid");
			}
			JsonNode serversNode = root.has(MCP_SERVERS_KEY) ? root.get(MCP_SERVERS_KEY) : root;
			if (serversNode == null || !serversNode.isObject()) {
				throw new IllegalArgumentException(
					"STDIO JSON must be an object, optionally wrapped by \"" + MCP_SERVERS_KEY + "\"");
			}

			Map<String, McpStdioClientProperties.Parameters> serverConfigs =
				JsonUtil.convertValue(serversNode, new TypeReference<>() {
				});
			if (serverConfigs == null || serverConfigs.isEmpty()) {
				return Map.of();
			}
			Map<String, ServerParameters> result = new LinkedHashMap<>();
			for (Map.Entry<String, McpStdioClientProperties.Parameters> entry : serverConfigs.entrySet()) {
				if (entry.getValue() == null || StringUtil.isBlank(entry.getValue().command())) {
					throw new IllegalArgumentException(
						"STDIO server \"" + entry.getKey() + "\" missing required field: command");
				}
				result.put(entry.getKey(), entry.getValue().toServerParameters());
			}
			return result;
		} catch (IllegalArgumentException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new IllegalArgumentException("Failed to parse Claude Desktop STDIO JSON", ex);
		}
	}

	// -------------------------------------------------------------------------
	// 内部辅助
	// -------------------------------------------------------------------------

	private String connectedClientName(String clientName, String serverConnectionName) {
		return clientName + " - " + serverConnectionName;
	}

	private void validate(McpClientParameters parameters) {
		Assert.notNull(parameters, "parameters must not be null");
		Assert.hasText(parameters.name(), "parameters.name must not be blank");
		Assert.notNull(parameters.type(), "parameters.type must not be null");

		switch (parameters.type()) {
			case SSE, STREAMABLE_HTTP -> Assert.hasText(parameters.url(),
				"parameters.url is required for " + parameters.type());
			case STDIO -> Assert.hasText(parameters.stdioServersJson(),
				"parameters.stdioServersJson is required for STDIO (Claude Desktop JSON)");
		}
	}

	/**
	 * 先卸旧注册（若有），再放入新注册，避免同名重复导致连接泄漏。
	 */
	private void replaceRegistration(String name, McpRegistration registration) {
		McpRegistration previous = registrations.put(name, registration);
		if (previous != null) {
			log.info("Replacing existing MCP registration: {}", name);
			closeRegistration(previous);
		}
	}

	private void closeRegistration(McpRegistration registration) {
		if (registration == null || registration.closer() == null) {
			return;
		}
		try {
			registration.closer().close();
		} catch (Exception ex) {
			log.error("Error closing MCP registration: {}", registration.name(), ex);
		}
	}

	/**
	 * 关闭同步 Client；单连接失败不影响其余连接关闭。
	 */
	private void closeSyncClientsQuietly(List<McpSyncClient> clients) {
		if (clients == null) {
			return;
		}
		for (McpSyncClient client : clients) {
			try {
				client.close();
			} catch (Exception ex) {
				log.warn("Error closing sync MCP client", ex);
			}
		}
	}

	// -------------------------------------------------------------------------
	// 参数 / 注册模型
	// -------------------------------------------------------------------------

	/**
	 * MCP 连接参数（同步 Client）。
	 * <p>
	 * 字段按用途分组：
	 * <ul>
	 *   <li><b>公共</b>：{@code name} / {@code clientName} / {@code version} / {@code requestTimeoutSeconds} / {@code type}</li>
	 *   <li><b>HTTP（SSE、STREAMABLE_HTTP）</b>：{@code url} / {@code endpoint} / {@code headers}</li>
	 *   <li><b>STDIO</b>：{@code stdioServersJson}（Claude Desktop 格式）</li>
	 * </ul>
	 * SSE 与 Streamable HTTP 参数形态基本一致，只是默认 endpoint 不同（{@code /sse} vs {@code /mcp}）。
	 * <p>
	 * 推荐用静态工厂 {@link #sse} / {@link #streamableHttp} / {@link #stdio} 创建。
	 *
	 * @param name                  业务名（HTTP 场景为 NamedTransport 名，并参与 Implementation 展示）；不作为管理器 Map key
	 * @param clientName            MCP client 展示名，空则用默认 {@code spring-ai-mcp-client}
	 * @param version               客户端版本，空则 {@code 1.0.0}
	 * @param requestTimeoutSeconds 请求超时秒数；&lt;=0 时用默认 30
	 * @param type                  传输类型
	 * @param url                   HTTP baseUrl（SSE / StreamableHTTP 必填）
	 * @param endpoint              HTTP path；SSE 默认 {@code /sse}，StreamableHTTP 默认 {@code /mcp}
	 * @param headers               可选 HTTP 请求头（如 Authorization）
	 * @param stdioServersJson      STDIO 专用：Claude Desktop JSON 字符串
	 */
	public record McpClientParameters(
	                                   String name,
		String clientName,
		String version,
		int requestTimeoutSeconds,
		TransportType type,
	                                   String url,
		String endpoint,
		Map<String, String> headers,
		String stdioServersJson) {

		/**
		 * 传输类型。
		 * <ul>
		 *   <li>{@link #SSE} — HTTP + Server-Sent Events</li>
		 *   <li>{@link #STREAMABLE_HTTP} — HTTP Streamable</li>
		 *   <li>{@link #STDIO} — 本地子进程标准输入输出（Claude Desktop 同款）</li>
		 * </ul>
		 */
		public enum TransportType {
			SSE,
			STREAMABLE_HTTP,
			STDIO
		}

		/**
		 * 解析后的 clientName（空则默认值）
		 */
		public String resolvedClientName() {
			return StringUtil.isNotBlank(clientName) ? clientName : DEFAULT_CLIENT_NAME;
		}

		/**
		 * 解析后的版本号
		 */
		public String resolvedVersion() {
			return StringUtil.isNotBlank(version) ? version : DEFAULT_VERSION;
		}

		/**
		 * 解析后的超时秒数
		 */
		public int resolvedRequestTimeoutSeconds() {
			return requestTimeoutSeconds > 0 ? requestTimeoutSeconds : DEFAULT_REQUEST_TIMEOUT_SECONDS;
		}

		/**
		 * SSE 快捷构造
		 */
		public static McpClientParameters sse(String name, String url) {
			return sse(name, url, null, null, null, 0, null);
		}

		/**
		 * SSE 完整构造。
		 *
		 * @param endpoint 可空，默认 {@code /sse}
		 * @param headers  可空
		 */
		public static McpClientParameters sse(String name,
		                                      String url,
		                                      String endpoint,
		                                      Map<String, String> headers,
		                                      String clientName,
		                                      int requestTimeoutSeconds,
		                                      String version) {
			return new McpClientParameters(
				name, clientName, version, requestTimeoutSeconds, TransportType.SSE,
				url, endpoint, headers, null);
		}

		/**
		 * Streamable HTTP 快捷构造
		 */
		public static McpClientParameters streamableHttp(String name, String url) {
			return streamableHttp(name, url, null, null, null, 0, null);
		}

		/**
		 * Streamable HTTP 完整构造。
		 *
		 * @param endpoint 可空，默认 {@code /mcp}
		 */
		public static McpClientParameters streamableHttp(String name,
		                                                 String url,
		                                                 String endpoint,
		                                                 Map<String, String> headers,
		                                                 String clientName,
		                                                 int requestTimeoutSeconds,
		                                                 String version) {
			return new McpClientParameters(
				name, clientName, version, requestTimeoutSeconds, TransportType.STREAMABLE_HTTP,
				url, endpoint, headers, null);
		}

		/**
		 * STDIO 快捷构造。
		 *
		 * @param stdioServersJson Claude Desktop 格式 JSON（含或不含 {@code mcpServers} 外壳均可）
		 */
		public static McpClientParameters stdio(String name, String stdioServersJson) {
			return stdio(name, stdioServersJson, null, 0, null);
		}

		/**
		 * STDIO 完整构造
		 */
		public static McpClientParameters stdio(String name,
		                                        String stdioServersJson,
		                                        String clientName,
		                                        int requestTimeoutSeconds,
		                                        String version) {
			return new McpClientParameters(
				name, clientName, version, requestTimeoutSeconds, TransportType.STDIO,
				null, null, null, stdioServersJson);
		}
	}

	/**
	 * 注册条目：注册 ID + 工具提供者 + 可选关闭回调（关闭底层 MCP Client）。
	 *
	 * @param name     注册 ID（与 {@link #registrations} 的 key 一致）
	 * @param provider 工具回调提供者（可能是带缓存包装的代理）
	 * @param closer   关闭底层连接；外部直接注入的 Provider 可为 null
	 */
	public record McpRegistration(String name, ToolCallbackProvider provider, AutoCloseable closer) {

	}

	/**
	 * 带缓存失效能力的 {@link ToolCallbackProvider} 代理。
	 * <p>
	 * Spring AI 的 Sync Provider 在工具列表变化后需要 {@code invalidateCache()}，
	 * 否则对话侧会一直用旧的 Tool 元数据。本类把真实 Provider 包一层，在 MCP toolsChange
	 * 事件里统一清缓存。
	 */
	public static final class CachingToolCallbackProvider implements ToolCallbackProvider {

		private volatile ToolCallbackProvider delegate;

		@Override
		public ToolCallback[] getToolCallbacks() {
			return delegate == null ? new ToolCallback[0] : delegate.getToolCallbacks();
		}

		public void setDelegate(ToolCallbackProvider delegate) {
			this.delegate = delegate;
		}

		/**
		 * 在远端 tools 列表变更时调用，丢弃本地缓存
		 */
		public void invalidateCache() {
			ToolCallbackProvider current = delegate;
			if (current instanceof SyncMcpToolCallbackProvider syncProvider) {
				syncProvider.invalidateCache();
			}
		}
	}
}
