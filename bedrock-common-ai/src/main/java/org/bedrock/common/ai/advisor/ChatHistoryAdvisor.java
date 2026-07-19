package org.bedrock.common.ai.advisor;

import lombok.extern.slf4j.Slf4j;
import org.bedrock.common.ai.advisor.history.ChatHistoryStore;
import org.bedrock.common.code.util.JsonUtil;
import org.bedrock.common.code.util.NumberUtil;
import org.bedrock.common.code.util.StringUtil;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.messages.*;
import org.springframework.ai.chat.metadata.ChatGenerationMetadata;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * 聊天历史 Advisor：注入历史上下文，并按「预插入 user + assistant → 更新 / 删除」策略持久化。
 * <p>
 * 工作流程：
 * <ol>
 *   <li>{@link #before} — 加载历史、拼接 prompt；通过 {@code getLastUserOrToolResponseMessage()} 获取本轮输入
 *       （支持 {@link org.springframework.ai.chat.messages.UserMessage} 和 {@link ToolResponseMessage}），
 *       预插入输入记录 + assistant 占位记录，ID 写入 context</li>
 *   <li>模型调用 — 同步走 {@code call()}，流式走 {@link #adviseStream}</li>
 *   <li>{@link #after} — 有 assistant 内容则 {@link ChatHistoryStore#update} 回填，否则 {@link ChatHistoryStore#delete} 删除两条预插入记录</li>
 * </ol>
 * <p>
 * Tool 调用支持：
 * <ul>
 *   <li>Agent 多轮场景中，prompt 最后一条可能是 {@link ToolResponseMessage} 而非 {@link org.springframework.ai.chat.messages.UserMessage}</li>
 *   <li>Tool 响应序列化为 JSON（{@link ChatHistoryStore.ToolEntry}）存入 content，role 为 {@code tool}</li>
 *   <li>加载历史时通过 {@link ChatHistoryStore.ChatEntry#toChatMessage()} 反序列化还原为 {@link ToolResponseMessage}</li>
 *   <li>assistant 的 toolCalls 单独字段持久化，与 content 独立；仅 toolCalls 无文本时也视为有效回复</li>
 *   <li>与 Spring AI 官方 {@code MessageChatMemoryAdvisor} 策略一致（#5174）</li>
 * </ul>
 * <p>
 * 设计要点：
 * <ul>
 *   <li>before 阶段即产生 user / assistant 两条记录 ID，其他 Advisor 可立即通过 context 关联扩展数据</li>
 *   <li>assistant 在 before 预插入占位记录，after 回填 content / reasoning / toolCalls；失败则 delete 整轮</li>
 *   <li>流式场景在 {@link #adviseStream} 中自行累积 token 片段，不再使用 {@code ChatClientMessageAggregator} 二次聚合</li>
 *   <li>流式正常结束走 {@code doOnComplete}，cancel / error 走对应钩子，均通过 {@code saveOnce} 触发 {@link #after}</li>
 *   <li>{@code responseContextRef} 在 {@code map(before)} 时写入（兜底首包前 cancel），在 {@code doOnNext} 时更新（含 order 更大的 Advisor 数据）</li>
 *   <li>通过 {@link AtomicBoolean} 保证每轮只持久化一次，防止 complete / cancel / error 重复写入</li>
 * </ul>
 * <p>
 * 使用示例：
 * <pre>{@code
 * chatClient.prompt()
 *     .user("你好")
 *     .advisors(a -> a.param(CONVERSATION_ID, "session-123")
 *                     .param(MAX_MESSAGES, 20))
 *     .call();
 * }</pre>
 *
 * @see ChatHistoryStore
 */
@Slf4j
public class ChatHistoryAdvisor implements BaseAdvisor {

	/**
	 * Spring AI 消息 metadata 中推理内容的键名。
	 * <p>
	 * DeepSeek 等 reasoning 模型会将 {@code reasoning_content} 映射为此字段，
	 * 流式场景下需逐片累积后写入 {@link ChatHistoryStore.ChatEntry#reasoning()}。
	 */
	private static final String REASONING_CONTENT_KEY = "reasoningContent";

	/**
	 * 历史读写服务，由业务方实现具体存储逻辑（数据库、Redis 等）
	 */
	private final ChatHistoryStore chatHistoryStore;

	/**
	 * Advisor 在链中的执行顺序，数值越小越先执行 before，越后执行 after
	 */
	private final int order;

	/**
	 * 流式请求的处理调度器，before 阶段在此调度器上执行
	 */
	private final Scheduler scheduler;

	private ChatHistoryAdvisor(ChatHistoryStore chatHistoryStore, int order, Scheduler scheduler) {
		Assert.notNull(chatHistoryStore, "chatHistoryStore cannot be null");
		Assert.notNull(scheduler, "scheduler cannot be null");
		this.chatHistoryStore = chatHistoryStore;
		this.order = order;
		this.scheduler = scheduler;
	}

	/**
	 * 请求前置处理：注入历史上下文，预插入 user 和 assistant 占位记录。
	 * <p>
	 * 执行步骤：
	 * <ol>
	 *   <li>从 {@link ChatHistoryStore} 加载历史，通过 {@link ChatHistoryStore.ChatEntry#toChatMessage()} 还原消息</li>
	 *   <li>历史消息 + 本次 prompt instructions 组成完整上下文</li>
	 *   <li>通过 {@code getLastUserOrToolResponseMessage()} 获取本轮输入（user 或 tool 响应）</li>
	 *   <li>预插入输入记录 + assistant 占位记录，ID 写入 context</li>
	 * </ol>
	 * <p>
	 * Tool 响应处理：{@link ToolResponseMessage} 的 responses 和 metadata 序列化为 JSON 存入 content，
	 * role 保持 {@code tool}，加载历史时由 {@link ChatHistoryStore.ChatEntry#toChatMessage()} 反序列化还原。
	 * <p>
	 * 注意：{@code getLastUserOrToolResponseMessage()} 找不到消息时返回空的 {@code UserMessage}（非 null），
	 * 不会抛异常，但可能插入 content 为空的记录。
	 *
	 * @param chatClientRequest 原始请求
	 * @param advisorChain      Advisor 链（本方法未使用）
	 * @return 包含历史消息和新 context 的请求
	 */
	@Override
	public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {
		String conversationId = getConversationId(chatClientRequest.context());
		int maxMessages = getMaxMessages(chatClientRequest.context());

		// 1. 加载历史消息，ChatEntry → Spring AI Message（含 ToolResponseMessage 反序列化）
		List<ChatHistoryStore.ChatEntry> memoryMessagesChat = chatHistoryStore.get(conversationId, maxMessages, chatClientRequest.context());
		List<Message> processedMessages = memoryMessagesChat
			.stream()
			.map(ChatHistoryStore.ChatEntry::toChatMessage)
			.collect(Collectors.toList());

		// 2. 历史消息在前，本次 prompt instructions 在后
		processedMessages.addAll(chatClientRequest.prompt().getInstructions());
		// 2.1. Ensure system message, if present, appears first in the list.
		for (int i = 0; i < processedMessages.size(); i++) {
			if (processedMessages.get(i) instanceof SystemMessage) {
				Message systemMessage = processedMessages.remove(i);
				processedMessages.add(0, systemMessage);
				break;
			}
		}
		// 3. 获取本轮输入：最后一条 user 或 tool 响应（Agent 场景下可能是 ToolResponseMessage）
		Map<String, Object> advisorContext = new HashMap<>(chatClientRequest.context());
		Message message = chatClientRequest.prompt().getLastUserOrToolResponseMessage();
		String content = message.getText();
		if (message instanceof ToolResponseMessage toolResponseMessage) {
			// Tool 响应序列化为 JSON 持久化，加载时通过 toChatMessage() 还原
			ChatHistoryStore.ToolEntry toolEntry = new ChatHistoryStore.ToolEntry(toolResponseMessage.getResponses(), toolResponseMessage.getMetadata());
			content = JsonUtil.toJson(toolEntry);
		}
		// 3.1 预插入本轮输入记录（user 或 tool）
		String messageId = StringUtil.toStr(advisorContext.get(ChatHistoryStore.USER_MESSAGE_ID), null);
		messageId = chatHistoryStore.add(new ChatHistoryStore.ChatEntry(conversationId,
			messageId, message.getMessageType().getValue(), content, null, null, advisorContext));
		// 3.2 预插入 assistant 占位记录（content / reasoning / toolCalls 为空，parentId 关联输入记录）
		String assistantMessageId = chatHistoryStore.add(new ChatHistoryStore.ChatEntry(conversationId,
			messageId, MessageType.ASSISTANT.getValue(), null, null, null, advisorContext));
		advisorContext.put(ChatHistoryStore.USER_MESSAGE_ID, messageId);
		advisorContext.put(ChatHistoryStore.ASSISTANT_MESSAGE_ID, assistantMessageId);

		return chatClientRequest.mutate()
			.prompt(chatClientRequest.prompt().mutate().messages(processedMessages).build())
			.context(advisorContext)
			.build();
	}

	/**
	 * 响应后置处理：回填 assistant 内容，或删除本轮预插入记录。
	 * <p>
	 * 从 context 读取 {@link ChatHistoryStore#ASSISTANT_MESSAGE_ID}：
	 * <ul>
	 *   <li>有有效 assistant 内容（content、reasoning 或 toolCalls 任一非空）→ {@link ChatHistoryStore#update} 回填</li>
	 *   <li>无有效内容 → {@link ChatHistoryStore#delete} 先删 assistant，再删 user，回滚整轮</li>
	 * </ul>
	 *
	 * @param chatClientResponse 模型响应
	 * @param advisorChain       Advisor 链（本方法未使用）
	 * @return update 成功时返回携带 context 的新响应；delete 或无 ID 时原样返回
	 */
	@Override
	public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
		Map<String, Object> context = new HashMap<>(chatClientResponse.context());
		String userMessageId = StringUtil.toStr(context.get(ChatHistoryStore.USER_MESSAGE_ID), null);
		String assistantMessageId = StringUtil.toStr(context.get(ChatHistoryStore.ASSISTANT_MESSAGE_ID), null);
		if (assistantMessageId == null) {
			return chatClientResponse;
		}

		ChatHistoryStore.ChatEntry assistantEntry = extractAssistantEntry(chatClientResponse, context);
		if (assistantEntry == null) {
			// 无有效 assistant 内容，回滚整轮预插入记录
			chatHistoryStore.delete(assistantMessageId);
			if (userMessageId != null) {
				chatHistoryStore.delete(userMessageId);
			}
			return chatClientResponse;
		}

		chatHistoryStore.update(assistantMessageId, assistantEntry);
		return ChatClientResponse.builder()
			.chatResponse(chatClientResponse.chatResponse())
			.context(context)
			.build();
	}

	/**
	 * 流式请求处理。
	 * <p>
	 * 不再使用 {@code ChatClientMessageAggregator}，避免与 {@code doOnNext} 中的 {@code StringBuilder}
	 * 重复聚合同一份流式数据；下游订阅者仍收到原始分片，持久化在 complete / cancel / error 时一次性完成。
	 * <p>
	 * 执行流程：
	 * <pre>
	 * map(before)           → 注入历史、预插入记录，responseContextRef 写入 before.context()（兜底 MESSAGE_ID）
	 * flatMapMany(nextStream) → 执行后续 Advisor 的 before，再调用模型流式接口
	 * doOnNext              → 每帧更新 responseContextRef，累积 content / reasoning
	 * doOnComplete          → 正常结束，saveOnce → after(update)
	 * doOnCancel / doOnError → 中断或异常，saveOnce → after(update 部分内容 或 delete)
	 * </pre>
	 * <p>
	 * context 捕获策略：
	 * <ul>
	 *   <li>{@code map} 阶段：拿到本 Advisor 的 before.context()（含 MESSAGE_ID，以及 order 更小的 Advisor 数据）</li>
	 *   <li>{@code doOnNext} 阶段：拿到 {@code response.context()}（含 order 更大的 Advisor 在 before 中写入的数据）</li>
	 *   <li>首包前 cancel：仅有 map 阶段的 context，缺少后续 Advisor 的扩展数据，但 MESSAGE_ID 可用于 delete 回滚</li>
	 * </ul>
	 * <p>
	 * {@code saveOnce} 通过 {@code saved.compareAndSet(false, true)} 保证 complete / cancel / error 三条路径只触发一次 {@link #after}。
	 * {@code doOnComplete} 与其他 Advisor 或订阅方的 {@code doOnComplete} 互不排斥，同一订阅链上会依次执行。
	 *
	 * @param chatClientRequest  原始请求
	 * @param streamAdvisorChain 流式 Advisor 链
	 * @return 原始流式分片 Flux（不做二次聚合）
	 */
	@Override
	public Flux<ChatClientResponse> adviseStream(ChatClientRequest chatClientRequest,
	                                             StreamAdvisorChain streamAdvisorChain) {
		// 流式正文累积器，doOnNext 逐片 append，complete / cancel / error 时交给 buildPartialResponse
		StringBuilder contentBuilder = new StringBuilder();
		// 流式 reasoning 累积器（DeepSeek 等模型）
		StringBuilder reasoningBuilder = new StringBuilder();
		// 流式 toolCalls 快照，通常在最后一片到达
		AtomicReference<List<AssistantMessage.ToolCall>> toolCallsRef = new AtomicReference<>();
		// 最后一片的 generation 元数据，构造部分响应时使用
		AtomicReference<ChatGenerationMetadata> generationMetadataRef = new AtomicReference<>(
			ChatGenerationMetadata.NULL);
		// 最后一片的 response 元数据
		AtomicReference<ChatResponseMetadata> responseMetadataRef = new AtomicReference<>();
		// 标记本轮是否已持久化，compareAndSet 保证 complete / cancel / error 只执行一次 after
		AtomicBoolean saved = new AtomicBoolean(false);
		/**
		 * 流式 context 快照，供 complete / cancel / error 兜底时使用。
		 * map 阶段写入 before.context()（兜底首包前 cancel）；
		 * doOnNext 每帧覆盖为 response.context()（含后续 Advisor 写入的数据）。
		 */
		AtomicReference<Map<String, Object>> responseContextRef = new AtomicReference<>();
		/**
		 * 流式 assistant 消息 metadata 累积器，逐帧 merge 各分片的 metadata。
		 * 供 {@link #buildPartialResponse} 构造完整 AssistantMessage 时使用。
		 */
		AtomicReference<Map<String, Object>> messageMetadataMapRef = new AtomicReference<>(new HashMap<>());
		// 统一持久化入口，防止三条终止路径重复调用 after
		Consumer<ChatClientResponse> saveOnce = response -> {
			if (saved.compareAndSet(false, true)) {
				// after 返回值在 side-effect 中不会作为 Flux 元素下发给订阅方
				this.after(response, streamAdvisorChain);
			}
		};

		return Mono.just(chatClientRequest)
			.publishOn(getScheduler())
			.map(request -> {
				ChatClientRequest before = this.before(request, streamAdvisorChain);
				// 兜底：首包前 cancel 时 doOnNext 未执行，此处 context 至少含 MESSAGE_ID
				responseContextRef.set(before.context());
				return before;
			})
			.flatMapMany(streamAdvisorChain::nextStream)
			.doOnNext(response -> {
				// 每帧更新：拿到 order 更大的 Advisor 在 before 中写入的完整 context
				responseContextRef.set(response.context());
				ChatResponse chatResponse = response.chatResponse();
				if (chatResponse == null) {
					return;
				}
				// Spring AI Generation.getOutput() 契约上非 null，需判空的是 getText()
				AssistantMessage output = chatResponse.getResult().getOutput();
				// 累积正文 token
				if (output.getText() != null) {
					contentBuilder.append(output.getText());
				}
				// 累积各分片的 metadata（含 reasoningContent 等）
				messageMetadataMapRef.get().putAll(output.getMetadata());
				// 累积 reasoning token
				appendReasoning(reasoningBuilder, output);
				// toolCalls 通常在末片一次性到达，每帧覆盖为最新非空列表
				List<AssistantMessage.ToolCall> toolCalls = output.getToolCalls();
				if (!toolCalls.isEmpty()) {
					toolCallsRef.set(toolCalls);
				}
				if (chatResponse.getResult().getMetadata() != ChatGenerationMetadata.NULL) {
					generationMetadataRef.set(chatResponse.getResult().getMetadata());
				}
				responseMetadataRef.set(chatResponse.getMetadata());
			})
			// 流式正常结束：用累积内容构造响应并持久化
			.doOnComplete(() -> saveOnce.accept(buildPartialResponse(responseContextRef.get(), messageMetadataMapRef.get(), contentBuilder,
				reasoningBuilder, toolCallsRef.get(), generationMetadataRef.get(), responseMetadataRef.get())
			))
			// 客户端主动断开：保存已生成的部分内容，无内容则 after 内 delete 回滚
			.doOnCancel(() -> saveOnce.accept(buildPartialResponse(responseContextRef.get(), messageMetadataMapRef.get(), contentBuilder,
				reasoningBuilder, toolCallsRef.get(), generationMetadataRef.get(), responseMetadataRef.get())))
			// 流式传输异常：同上
			.doOnError(ex -> saveOnce.accept(buildPartialResponse(responseContextRef.get(), messageMetadataMapRef.get(), contentBuilder,
				reasoningBuilder, toolCallsRef.get(), generationMetadataRef.get(), responseMetadataRef.get())));
	}

	@Override
	public int getOrder() {
		return order;
	}

	@Override
	public Scheduler getScheduler() {
		return scheduler;
	}

	/**
	 * 根据流式累积内容构造完整/部分响应，供 complete / cancel / error 时交给 {@link #after} 处理。
	 * <p>
	 * context 来自 {@code responseContextRef}：
	 * <ul>
	 *   <li>有 chunk 时：doOnNext 写入的 {@code response.context()}（最完整）</li>
	 *   <li>首包前 cancel：map 写入的 {@code before.context()}（至少含 MESSAGE_ID）</li>
	 * </ul>
	 *
	 * @param context            流式 context 快照
	 * @param metadata           累积的 assistant metadata（含 reasoningContent）
	 * @param contentBuilder     已累积的正文
	 * @param reasoningBuilder   已累积的 reasoning
	 * @param toolCalls          流式末片携带的 toolCalls，可为 null
	 * @param generationMetadata 最后一片的 generation 元数据
	 * @param responseMetadata   最后一片的 response 元数据
	 * @return 构造的 ChatClientResponse，供 {@link #after} 处理
	 */
	private ChatClientResponse buildPartialResponse(Map<String, Object> context,
	                                                Map<String, Object> metadata,
	                                                StringBuilder contentBuilder,
	                                                StringBuilder reasoningBuilder,
	                                                List<AssistantMessage.ToolCall> toolCalls,
	                                                ChatGenerationMetadata generationMetadata,
	                                                ChatResponseMetadata responseMetadata) {
		if (StringUtil.isNotBlank(reasoningBuilder)) {
			metadata.put(REASONING_CONTENT_KEY, reasoningBuilder.toString());
		}

		AssistantMessage.Builder assistantBuilder = AssistantMessage.builder()
			.content(contentBuilder.toString())
			.properties(metadata);
		if (toolCalls != null && !toolCalls.isEmpty()) {
			assistantBuilder.toolCalls(toolCalls);
		}
		AssistantMessage assistantMessage = assistantBuilder.build();

		return ChatClientResponse.builder()
			.chatResponse(ChatResponse.builder()
				.generations(List.of(new Generation(assistantMessage, generationMetadata)))
				.metadata(responseMetadata)
				.build())
			.context(context)
			.build();
	}

	/**
	 * 从响应中提取 assistant 数据，用于 {@link ChatHistoryStore#update}。
	 * <p>
	 * content、reasoning、toolCalls 均为空时返回 null，触发 {@link ChatHistoryStore#delete} 回滚整轮。
	 *
	 * @param chatClientResponse 模型响应（同步完整响应或流式构造的部分响应）
	 * @param context            请求上下文，含 {@link ChatHistoryStore#USER_MESSAGE_ID}
	 * @return assistant ChatEntry；无有效内容时返回 null
	 */
	private ChatHistoryStore.ChatEntry extractAssistantEntry(ChatClientResponse chatClientResponse,
	                                                           Map<String, Object> context) {
		if (chatClientResponse.chatResponse() == null) {
			return null;
		}
		AssistantMessage assistantMessage = chatClientResponse.chatResponse().getResult().getOutput();
		String content = assistantMessage.getText();
		String reasoning = extractReasoning(assistantMessage);
		List<AssistantMessage.ToolCall> toolCalls = assistantMessage.getToolCalls();
		String toolCallsStr = toolCalls.isEmpty() ? null : JsonUtil.toJson(toolCalls);
		if (StringUtil.isBlank(content) && StringUtil.isBlank(reasoning) && StringUtil.isBlank(toolCallsStr)) {
			return null;
		}
		String userMessageId = StringUtil.toStr(context.get(ChatHistoryStore.USER_MESSAGE_ID), null);
		return new ChatHistoryStore.ChatEntry(getConversationId(context), userMessageId,
			assistantMessage.getMessageType().getValue(), StringUtil.isBlank(content) ? null : content, reasoning, toolCallsStr, context);
	}

	/**
	 * 将单条消息中的 reasoning 片段追加到累积器。
	 * <p>
	 * 流式场景下 reasoning 分片到达，需逐片拼接成完整思考过程。
	 */
	private void appendReasoning(StringBuilder reasoningBuilder, Message message) {
		String reasoning = extractReasoning(message);
		if (reasoning != null) {
			reasoningBuilder.append(reasoning);
		}
	}

	/**
	 * 从消息 metadata 提取 reasoning 内容。
	 * <p>
	 * Spring AI 将 DeepSeek 等模型的 {@code reasoning_content} 映射为 metadata 中的 {@code reasoningContent}。
	 *
	 * @return reasoning 文本，不存在或为空时返回 {@code null}
	 */
	private String extractReasoning(Message message) {
		if (message.getMetadata() == null) {
			return null;
		}
		Object reasoning = message.getMetadata().get(REASONING_CONTENT_KEY);
		if (reasoning == null) {
			return null;
		}
		String text = reasoning.toString();
		return text.isEmpty() ? null : text;
	}

	/**
	 * 从 context 提取会话 ID。
	 *
	 * @throws IllegalArgumentException context 为空或缺少 conversationId 时
	 */
	private String getConversationId(Map<String, Object> context) {
		Assert.notNull(context, "context cannot be null");
		Assert.notNull(context.get(ChatHistoryStore.CONVERSATION_ID), "conversationId cannot be null");
		return context.get(ChatHistoryStore.CONVERSATION_ID).toString();
	}

	/**
	 * 从 context 提取最大历史消息条数。
	 * <p>
	 * 未传 {@link ChatHistoryStore#MAX_MESSAGES} 时默认 20 条。
	 */
	private int getMaxMessages(Map<String, Object> context) {
		Assert.notNull(context, "context cannot be null");
		return NumberUtil.toInt(context.get(ChatHistoryStore.MAX_MESSAGES), 20);
	}

	/**
	 * 创建 Builder 实例。
	 *
	 * @param chatHistoryStore 历史读写服务实现，不能为空
	 */
	public static Builder builder(ChatHistoryStore chatHistoryStore) {
		return new Builder(chatHistoryStore);
	}

	/**
	 * {@link ChatHistoryAdvisor} 构建器
	 */
	public static final class Builder {

		private int order = Advisor.DEFAULT_CHAT_MEMORY_PRECEDENCE_ORDER - 200;

		private Scheduler scheduler = BaseAdvisor.DEFAULT_SCHEDULER;

		private final ChatHistoryStore chatHistoryStore;

		private Builder(ChatHistoryStore chatHistoryStore) {
			this.chatHistoryStore = chatHistoryStore;
		}

		/**
		 * 设置 Advisor 在链中的执行顺序。
		 * <p>
		 * 默认值为 {@link Advisor#DEFAULT_CHAT_MEMORY_PRECEDENCE_ORDER}。
		 */
		public Builder order(int order) {
			this.order = order;
			return this;
		}

		/**
		 * 设置流式处理使用的调度器。
		 * <p>
		 * 默认值为 {@link BaseAdvisor#DEFAULT_SCHEDULER}。
		 */
		public Builder scheduler(@NonNull Scheduler scheduler) {
			this.scheduler = scheduler;
			return this;
		}

		/**
		 * 构建 {@link ChatHistoryAdvisor} 实例
		 */
		public ChatHistoryAdvisor build() {
			return new ChatHistoryAdvisor(chatHistoryStore, order, scheduler);
		}
	}
}
