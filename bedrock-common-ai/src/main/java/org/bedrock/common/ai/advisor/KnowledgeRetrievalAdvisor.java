package org.bedrock.common.ai.advisor;

import org.bedrock.common.ai.advisor.knowledge.KnowledgeRetriever;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.List;

/**
 * 知识库检索 Advisor：仅负责向量召回，不直接修改 prompt。
 * <p>
 * <b>为何不在这里改 prompt？</b>
 * 一次对话请求往往有多种增强来源（知识库、Web 检索、协作文档等），各自 Advisor 只负责
 * 「召回 / 拉取」并把结果写入共享 {@code context}，保留原始用户提示词不动。
 * 后续由 {@link PromptAugmentationAdvisor} 从 context 读取检索结果，
 * 按模板统一增强用户 prompt（本 Advisor 不直接改 prompt）。这样：
 * <ul>
 *   <li>各检索 Advisor 职责单一，可独立开关、排序、复用</li>
 *   <li>组装层可决定引用格式、优先级、token 预算，而不污染检索逻辑</li>
 *   <li>其他 Advisor 若需原始用户文本（如改写 query、审计日志），仍可从 prompt 直接读取</li>
 * </ul>
 * <p>
 * <b>历史消息传递：</b>
 * {@link #before} 会将本轮用户文本（{@code text}）及 prompt 中的历史消息列表
 * （{@code prompt.getInstructions()}，通常由 {@link ChatHistoryAdvisor} 注入的多轮对话）
 * 一并传给 {@link KnowledgeRetriever}，便于业务方基于上下文做 query 改写、
 * 多轮追问补全、按会话状态决定是否检索等扩展。
 * <p>
 * 工作流程：
 * <ol>
 *   <li>{@link #before} — 提取 {@code text} 与 {@code history}；判断是否需要检索；
 *       {@code mutate()} 拷贝 context；构建 SearchRequest；召回并转换文档；
 *       结果写入 context 的 {@link KnowledgeRetriever#KNOWLEDGE_RETRIEVED_DOCUMENTS}</li>
 *   <li>模型调用 — 由 {@link PromptAugmentationAdvisor} 消费 context 中的检索结果并增强 prompt</li>
 *   <li>{@link #after} — 将检索结果附加到 {@link ChatResponse} metadata，便于日志与观测</li>
 * </ol>
 * <p>
 * 设计要点：
 * <ul>
 *   <li>确认需要检索后立即 {@code mutate()}，因 {@link KnowledgeRetriever#getSearchRequest}
 *       可能向 context 写入中间状态（过滤条件、改写后的 query 等）</li>
 *   <li>{@link KnowledgeRetriever#needRetrieval} 默认防重：context 已有检索结果时跳过</li>
 *   <li>检索结果以 {@code List<Document>} 写入 context，由 {@link PromptAugmentationAdvisor} 统一编排进 prompt</li>
 *   <li>推荐链顺序：{@link ChatHistoryAdvisor} → {@link KnowledgeRetrievalAdvisor} → {@link PromptAugmentationAdvisor}</li>
 * </ul>
 *
 * @see KnowledgeRetriever
 * @see PromptAugmentationAdvisor
 * @see ChatHistoryAdvisor
 */
public class KnowledgeRetrievalAdvisor implements BaseAdvisor {

	/**
	 * 知识库检索服务，由业务方实现向量库获取及可选的步骤覆盖
	 */
	private final KnowledgeRetriever knowledgeRetriever;

	/**
	 * Advisor 在链中的执行顺序，数值越小越先执行 before，越后执行 after
	 */
	private final int order;

	public KnowledgeRetrievalAdvisor(KnowledgeRetriever knowledgeRetriever, int order) {
		Assert.notNull(knowledgeRetriever, "knowledgeRetriever cannot be null");
		this.knowledgeRetriever = knowledgeRetriever;
		this.order = order;
	}

	/**
	 * 请求前置处理：执行知识库向量检索，将结果写入 context。
	 * <p>
	 * 执行步骤：
	 * <ol>
	 *   <li>从 prompt 提取本轮用户文本 {@code text} 及历史消息列表 {@code history}
	 *       （{@code prompt.getInstructions()}，含 system / user / assistant 等）</li>
	 *   <li>{@link KnowledgeRetriever#needRetrieval} 判断是否需要检索</li>
	 *   <li>{@code mutate()} 拷贝 context，供后续步骤安全写入</li>
	 *   <li>{@link KnowledgeRetriever#getSearchRequest} 构建检索请求（可向 context 写入中间数据），
	 *       返回 {@code null} 则跳过后续检索</li>
	 *   <li>{@link KnowledgeRetriever#retrieve} 召回文档</li>
	 *   <li>{@link KnowledgeRetriever#transformRetrievedDocuments} 过滤 / 转换结果</li>
	 *   <li>将最终结果写入 context（{@link KnowledgeRetriever#KNOWLEDGE_RETRIEVED_DOCUMENTS}）</li>
	 * </ol>
	 */
	@Override
	public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {
		String text = chatClientRequest.prompt().getUserMessage().getText();
		List<Message> messageList = chatClientRequest.prompt().getInstructions();
		if (!knowledgeRetriever.needRetrieval(text, messageList, chatClientRequest.context())) {
			return chatClientRequest;
		}
		ChatClientRequest clientRequest = ChatClientRequest.builder()
			.prompt(chatClientRequest.prompt())
			.context(new HashMap<>(chatClientRequest.context()))
			.build();
		SearchRequest searchRequest = knowledgeRetriever.getSearchRequest(text, messageList, clientRequest.context());
		if (searchRequest == null) {
			return clientRequest;
		}
		List<Document> documents = knowledgeRetriever.retrieve(searchRequest, text, messageList, clientRequest.context());
		documents = knowledgeRetriever.transformRetrievedDocuments(text, messageList, documents, clientRequest.context());
		clientRequest.context().put(KnowledgeRetriever.KNOWLEDGE_RETRIEVED_DOCUMENTS, documents);
		return clientRequest;
	}

	/**
	 * 响应后置处理：将 context 中的检索结果写入 ChatResponse metadata。
	 * <p>
	 * 仅当 context 中存在检索结果时才写入 metadata。
	 */
	@Override
	public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
		Object retrievedDocuments = chatClientResponse.context()
			.get(KnowledgeRetriever.KNOWLEDGE_RETRIEVED_DOCUMENTS);
		if (retrievedDocuments == null) {
			return chatClientResponse;
		}
		ChatResponse.Builder chatResponseBuilder;
		if (chatClientResponse.chatResponse() == null) {
			chatResponseBuilder = ChatResponse.builder();
		} else {
			chatResponseBuilder = ChatResponse.builder().from(chatClientResponse.chatResponse());
		}
		chatResponseBuilder.metadata(KnowledgeRetriever.KNOWLEDGE_RETRIEVED_DOCUMENTS, retrievedDocuments);
		return ChatClientResponse.builder()
			.chatResponse(chatResponseBuilder.build())
			.context(chatClientResponse.context())
			.build();
	}

	@Override
	public int getOrder() {
		return order;
	}

	/**
	 * 创建 Builder 实例。
	 *
	 * @param knowledgeRetriever 知识库检索服务实现，不能为空
	 */
	public static KnowledgeRetrievalAdvisor.Builder builder(KnowledgeRetriever knowledgeRetriever) {
		return new KnowledgeRetrievalAdvisor.Builder(knowledgeRetriever);
	}

	/**
	 * {@link KnowledgeRetrievalAdvisor} 构建器
	 */
	public static final class Builder {

		/**
		 * 默认在 ChatHistoryAdvisor 之后、{@link PromptAugmentationAdvisor} 之前执行（order 更小）
		 */
		private int order = Advisor.DEFAULT_CHAT_MEMORY_PRECEDENCE_ORDER - 350;

		private final KnowledgeRetriever knowledgeRetriever;

		private Builder(KnowledgeRetriever knowledgeRetriever) {
			Assert.notNull(knowledgeRetriever, "knowledgeRetriever cannot be null");
			this.knowledgeRetriever = knowledgeRetriever;
		}

		/**
		 * 设置 Advisor 在链中的执行顺序。
		 * <p>
		 * 默认值为 {@link Advisor#DEFAULT_CHAT_MEMORY_PRECEDENCE_ORDER} - 350，
		 * 应小于 {@link PromptAugmentationAdvisor} 的 order，确保检索先于 prompt 增强。
		 */
		public KnowledgeRetrievalAdvisor.Builder order(int order) {
			this.order = order;
			return this;
		}

		/**
		 * 构建 {@link KnowledgeRetrievalAdvisor} 实例
		 */
		public KnowledgeRetrievalAdvisor build() {
			return new KnowledgeRetrievalAdvisor(knowledgeRetriever, order);
		}

	}

}
