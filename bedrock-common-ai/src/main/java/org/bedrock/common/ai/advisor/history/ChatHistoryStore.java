package org.bedrock.common.ai.advisor.history;

import lombok.extern.slf4j.Slf4j;
import org.bedrock.common.code.util.JsonUtil;
import org.bedrock.common.code.util.StringUtil;
import org.springframework.ai.chat.messages.*;
import org.springframework.ai.content.Media;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Map;

/**
 * 聊天历史持久化 SPI（Advisor 回调接口，非业务 CRUD Service）。
 * <p>
 * 由 {@link org.bedrock.common.ai.advisor.ChatHistoryAdvisor} 在 Advisor 链中调用，
 * 负责按会话维度读取和写入消息记录。具体存储方式（数据库、Redis 等）由实现类决定。
 * <p>
 * 持久化策略（预插入 → 更新 / 删除）：
 * <ol>
 *   <li>{@code before} — 预插入本轮输入（user 或 {@link org.springframework.ai.chat.messages.ToolResponseMessage}）
 *       和 assistant 占位记录，ID 写入 context</li>
 *   <li>{@code after} — 有 assistant 内容则 {@link #update}，否则 {@link #delete} 回滚</li>
 * </ol>
 * <p>
 * 消息类型支持：
 * <ul>
 *   <li>{@code user} — 普通用户消息，content 为文本</li>
 *   <li>{@code assistant} — 模型回复，content 为文本，reasoning 为推理内容，toolCalls 为工具调用列表</li>
 *   <li>{@code tool} — 工具响应，content 为 {@link ToolEntry} 的 JSON 序列化</li>
 * </ul>
 * <p>
 * 调用方需在 ChatClient 的 context 中传入：
 * <ul>
 *   <li>{@link #CONVERSATION_ID} — 会话 ID，必填</li>
 *   <li>{@link #MAX_MESSAGES} — 注入的历史消息条数上限，选填，默认 10</li>
 * </ul>
 * <p>
 * 其他 Advisor（附件、知识库、Web 检索等）可在 {@code before} 阶段通过
 * {@link #USER_MESSAGE_ID} 或 {@link #ASSISTANT_MESSAGE_ID} 关联扩展数据，
 * 无需等待模型响应完成。
 *
 * @see org.bedrock.common.ai.advisor.ChatHistoryAdvisor
 */
public interface ChatHistoryStore {

	/**
	 * 从 ChatClient context 中获取会话 ID 的键名。
	 * <p>
	 * 值为 {@link String} 或可 {@code toString()} 的对象，不能为空。
	 * Advisor 的 {@code before} 阶段通过此 ID 定位会话并加载历史消息。
	 */
	String CONVERSATION_ID = "chat_memory_conversation_id";

	/**
	 * 从 ChatClient context 中获取最大历史消息条数的键名。
	 * <p>
	 * 值为 {@link Number} 或可解析为整数的字符串；未传时 Advisor 默认取 20 条。
	 * 控制拼接到 prompt 前的历史消息数量，避免上下文过长。
	 */
	String MAX_MESSAGES = "chat_memory_max_messages";

	/**
	 * 本轮输入消息 ID 在 context 中的键名（user 或 tool 响应）。
	 * <p>
	 * 由 {@link org.bedrock.common.ai.advisor.ChatHistoryAdvisor#before} 预插入后写入。
	 * Agent 场景下可能是 tool 响应记录的 ID，命名保留 USER_MESSAGE_ID 以兼容现有调用方。
	 * 其他 Advisor 可通过此 ID 关联附件、知识库、Web 检索等扩展数据。
	 * <p>
	 * 使用示例：
	 * <pre>{@code
	 * String userMessageId = (String) request.context().get(USER_MESSAGE_ID);
	 * attachmentService.bind(userMessageId, files);
	 * }</pre>
	 */
	String USER_MESSAGE_ID = "chat_history_user_message_id";

	/**
	 * assistant 消息 ID 在 context 中的键名。
	 * <p>
	 * 由 {@link org.bedrock.common.ai.advisor.ChatHistoryAdvisor#before} 预插入空 assistant 占位记录后写入。
	 * {@code after} 阶段通过 {@link #update} 回填 content / reasoning，
	 * 或模型无有效回复时通过 {@link #delete} 与 user 记录一并删除。
	 */
	String ASSISTANT_MESSAGE_ID = "chat_history_assistant_message_id";

	/**
	 * 获取指定会话的历史消息。
	 * <p>
	 * 返回的消息由 Advisor 拼接到本次 prompt 之前，作为多轮对话上下文。
	 * 实现类应保证：
	 * <ul>
	 *   <li>返回顺序与对话时序一致（通常按时间升序）</li>
	 *   <li>只返回已完成的消息（assistant 的 content / reasoning / toolCalls 至少一项非空）</li>
	 *   <li>最多返回 {@code maxMessages} 条</li>
	 * </ul>
	 *
	 * @param conversationId 会话 ID
	 * @param maxMessages    最多返回的消息条数
	 * @param context        调用上下文，可用于传递租户、用户等扩展信息
	 * @return 历史消息列表，无历史时返回空列表（非 null）
	 */
	List<ChatHistoryStore.ChatEntry> get(String conversationId, int maxMessages, Map<String, Object> context);

	/**
	 * 预插入一条消息记录。
	 * <p>
	 * {@code before} 阶段调用两次：
	 * <ol>
	 *   <li>插入 user 消息（含 content）</li>
	 *   <li>插入 assistant 占位记录（content、reasoning 为空，parentId 指向 user 记录 ID）</li>
	 * </ol>
	 *
	 * @param chatEntry 消息数据
	 * @return 持久化后的记录 ID，供 context 写入及后续 update / delete 使用
	 */
	String add(ChatEntry chatEntry);

	/**
	 * 回填 assistant 消息内容。
	 * <p>
	 * 模型返回有效内容后，将 content、reasoning、toolCalls 等写入 {@code messageId} 对应的占位记录。
	 * 流式场景下可能是部分内容（cancel / error 兜底时）。
	 *
	 * @param messageId assistant 记录 ID，对应 context 中的 {@link #ASSISTANT_MESSAGE_ID}
	 * @param chatEntry assistant 数据，{@link ChatEntry#parentId()} 为 user 记录 ID
	 */
	void update(String messageId, ChatEntry chatEntry);

	/**
	 * 删除一条预插入的消息记录。
	 * <p>
	 * {@code after} 阶段无有效 assistant 内容时，Advisor 会先后调用两次：
	 * 先 delete assistant 记录，再 delete user 记录，回滚整轮预插入数据。
	 *
	 * @param messageId 待删除的记录 ID（user 或 assistant）
	 */
	void delete(String messageId);

	/**
	 * 单条聊天历史记录，对应持久化层的一行数据。
	 *
	 * @param conversationId 会话 ID，与 {@link #CONVERSATION_ID} 对应
	 * @param parentId       父消息 ID；
	 *                       user 预插入时为 {@code null}，
	 *                       assistant 预插入 / 更新时为 user 记录 ID
	 * @param role           消息角色：{@code user}、{@code assistant}、{@code tool}
	 * @param content        消息正文；tool 类型时为 {@link ToolEntry} 的 JSON
	 * @param reasoning      推理/思考过程内容（DeepSeek 等 reasoning 模型），可为空
	 * @param toolCalls      assistant 工具调用列表，与 content 独立存储；仅 assistant 角色使用
	 * @param media          用户附件 Media（仅内存 / 历史还原用，不落库；落库走消息表 attachments）
	 * @param context        调用上下文快照，便于实现类获取用户 ID、租户等扩展信息
	 */
	@Slf4j
	record ChatEntry(
		String conversationId,
		@Nullable String parentId,
		String role,
		@Nullable String content,
		@Nullable String reasoning,
		@Nullable String toolCalls,
		List<Media> media,
		Map<String, Object> context
	) {

		/**
		 * 兼容旧 7 参调用：media 固定为空列表（Advisor 预插入等场景无需附件）。
		 */
		public ChatEntry(String conversationId, @Nullable String parentId, String role,
		                 @Nullable String content, @Nullable String reasoning, @Nullable String toolCalls,
		                 Map<String, Object> context) {
			this(conversationId, parentId, role, content, reasoning, toolCalls, List.of(), context);
		}

		/**
		 * 将持久化记录还原为 Spring AI {@link Message}。
		 * <p>
		 * 按 role 分支处理：
		 * <ul>
		 *   <li>{@code user} → {@link UserMessage}（含 media）</li>
		 *   <li>{@code assistant} → {@link AssistantMessage}（含 toolCalls 还原）</li>
		 *   <li>{@code system} → {@link SystemMessage}</li>
		 *   <li>{@code tool} → 从 content JSON 反序列化为 {@link ToolResponseMessage}</li>
		 * </ul>
		 */
		public Message toChatMessage() {
			MessageType messageType = MessageType.fromValue(role);
			String content = this.content;
			if (content == null) {
				content = "";
			}
			return switch (messageType) {
				case USER -> UserMessage.builder()
					.text(content)
					.media(media)
					.build();
				case ASSISTANT -> {
					AssistantMessage.Builder builder = AssistantMessage.builder().content(content);
					if (StringUtil.isNotBlank(toolCalls)) {
						List<AssistantMessage.ToolCall> calls = JsonUtil.parseList(toolCalls, AssistantMessage.ToolCall.class);
						if (calls != null) {
							builder.toolCalls(calls);
						}
					}
					yield builder.build();
				}
				case SYSTEM -> new SystemMessage(content);
				case TOOL -> {
					ToolEntry toolEntry = JsonUtil.parse(content, ToolEntry.class);
					if (toolEntry == null) {
						log.warn("Invalid tool entry: {}", content);
						yield ToolResponseMessage.builder().build();
					}
					yield ToolResponseMessage.builder()
						.metadata(toolEntry.metadata)
						.responses(toolEntry.responses)
						.build();
				}
			};
		}
	}

	/**
	 * Tool 响应序列化载体，用于 {@link ToolResponseMessage} 的 JSON 持久化与还原。
	 *
	 * @param responses 工具执行结果列表
	 * @param metadata  工具响应 metadata
	 */
	record ToolEntry(List<ToolResponseMessage.ToolResponse> responses, Map<String, Object> metadata) {

	}
}
