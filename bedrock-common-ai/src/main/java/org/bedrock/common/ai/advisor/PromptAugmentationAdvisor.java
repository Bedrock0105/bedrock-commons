package org.bedrock.common.ai.advisor;

import org.bedrock.common.ai.advisor.prompt.PromptAugmenter;
import org.bedrock.common.code.util.StringUtil;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Map;

/**
 * 提示词增强 Advisor：从 context 读取各检索源结果，按模板改写用户 prompt。
 * <p>
 * 与 {@link KnowledgeRetrievalAdvisor} 配合构成完整 RAG 链路：
 * <ul>
 *   <li>{@link KnowledgeRetrievalAdvisor} — 向量召回，结果写入 context，不改 prompt</li>
 *   <li>{@link PromptAugmentationAdvisor} — 读取 context，合并多源文档，渲染模板增强用户消息</li>
 * </ul>
 * <p>
 * 工作流程：
 * <ol>
 *   <li>{@link #before} — 判断是否需要增强；保存原始用户问题到 context；
 *       调用 {@link PromptAugmenter} 收集 / 合并 / 格式化文档；
 *       用 {@link PromptTemplate} 渲染并 {@code augmentUserMessage}</li>
 *   <li>模型调用 — 使用增强后的 prompt</li>
 *   <li>{@link #after} — 默认透传（可按需扩展）</li>
 * </ol>
 * <p>
 * <b>推荐 Advisor 链顺序</b>（{@code order} 越小，{@code before} 越早执行）：
 * <pre>
 * ChatHistoryAdvisor（注入多轮历史）
 *   → KnowledgeRetrievalAdvisor（向量召回 → context）
 *   → PromptAugmentationAdvisor（context → prompt 增强）
 *   → 模型调用
 * </pre>
 * 建议 order 示例：ChatHistory -400，KnowledgeRetrieval -350，PromptAugmentation -300。
 * <p>
 * 设计要点：
 * <ul>
 *   <li>未传入 {@link PromptAugmenter} 时使用内置默认实现（接口 default 方法，读取知识库检索结果）</li>
 *   <li>增强前 {@code mutate()} 拷贝 context，并保存 {@link PromptAugmenter#QA_CONTEXT_ORIGINAL_USER_QUERY}</li>
 *   <li>有检索上下文（{@code augment} 非空）时使用 {@code promptTemplate}（变量 {@code context}、{@code query}）</li>
 *   <li>无检索结果时使用 {@code emptyContextPromptTemplate}，默认引导模型礼貌拒答；
 *       也可通过 Builder 配置为 {@code {query}} 以原样传递用户原始问题</li>
 * </ul>
 *
 * @see PromptAugmenter
 * @see KnowledgeRetrievalAdvisor
 * @see ChatHistoryAdvisor
 */
public class PromptAugmentationAdvisor implements BaseAdvisor {

	private static final PromptTemplate DEFAULT_PROMPT_TEMPLATE = new PromptTemplate("""
		以下是与问题相关的参考信息：

		---------------------
		{context}
		---------------------

		请根据以上参考信息回答，不要使用参考信息以外的知识。

		请注意：
		1. 如果参考信息里找不到答案，直接说不知道，不要猜测或编造。
		2. 回答要简洁准确，直接回应问题。
		3. 不要说「根据上下文」「根据提供的信息」这类套话。

		用户问题：{query}

		请回答：
		""");

	private static final PromptTemplate DEFAULT_EMPTY_CONTEXT_PROMPT_TEMPLATE = new PromptTemplate("""
		知识库里没有找到相关内容。
		请礼貌告诉用户，这个问题超出了知识库范围，无法回答。不要使用通用知识作答。
		""");

	/**
	 * 内置默认增强服务：直接使用 {@link PromptAugmenter} 的 default 方法，
	 * 从 context 读取 {@link org.bedrock.common.ai.advisor.knowledge.KnowledgeRetriever#KNOWLEDGE_RETRIEVED_DOCUMENTS}。
	 * Builder 未指定 {@code promptAugmenter} 时启用。
	 */
	private static final PromptAugmenter DEFAULT_PROMPT_AUGMENTER = new PromptAugmenter() {
	};

	/**
	 * 提示词增强服务，由业务方实现或覆盖各增强步骤
	 */
	private final PromptAugmenter promptAugmenter;

	/**
	 * 有检索上下文时使用的 prompt 模板（变量：{@code context}、{@code query}）
	 */
	private final PromptTemplate promptTemplate;

	/**
	 * 无检索上下文时使用的 prompt 模板。
	 * <p>
	 * 默认为拒答引导文案（不含变量）。业务方可通过 Builder 自定义，例如：
	 * {@code new PromptTemplate("{query}")} — RAG 无召回时原样传递用户原始问题。
	 */
	private final PromptTemplate emptyContextPromptTemplate;

	/**
	 * Advisor 在链中的执行顺序，数值越小越先执行 before，越后执行 after
	 */
	private final int order;

	public PromptAugmentationAdvisor(int order,
	                                 @Nullable PromptAugmenter promptAugmenter,
	                                 @Nullable PromptTemplate promptTemplate,
	                                 @Nullable PromptTemplate emptyContextPromptTemplate) {
		this.promptAugmenter = promptAugmenter == null ? DEFAULT_PROMPT_AUGMENTER
			: promptAugmenter;
		this.promptTemplate = promptTemplate == null ? DEFAULT_PROMPT_TEMPLATE : promptTemplate;
		this.emptyContextPromptTemplate = emptyContextPromptTemplate == null ? DEFAULT_EMPTY_CONTEXT_PROMPT_TEMPLATE
			: emptyContextPromptTemplate;
		this.order = order;
	}

	/**
	 * 请求前置处理：收集 context 中的检索结果，按模板增强用户 prompt。
	 * <p>
	 * 执行步骤：
	 * <ol>
	 *   <li>{@link PromptAugmenter#needAugmentation} 判断是否需要增强</li>
	 *   <li>{@code mutate()} 拷贝 context</li>
	 *   <li>保存原始用户问题到 {@link PromptAugmenter#QA_CONTEXT_ORIGINAL_USER_QUERY}</li>
	 *   <li>{@link PromptAugmenter#documents} → {@link PromptAugmenter#join}
	 *       → {@link PromptAugmenter#augment} 生成 {@link PromptAugmenter.AugmentResult}</li>
	 *   <li>若 {@code applyUserTemplate=false} 则跳过模板改写（业务已自行处理消息列表）</li>
	 *   <li>否则选择模板：{@code contextText} 非空 → {@code promptTemplate}；否则 → {@code emptyContextPromptTemplate}</li>
	 *   <li>{@code augmentUserMessage} 渲染模板并改写用户消息</li>
	 * </ol>
	 */
	@Override
	public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {
		Prompt prompt = chatClientRequest.prompt();

		if (!promptAugmenter.needAugmentation(prompt, chatClientRequest.context())) {
			return chatClientRequest;
		}
		chatClientRequest = chatClientRequest.mutate().build();
		prompt = chatClientRequest.prompt();
		chatClientRequest.context().put(PromptAugmenter.QA_CONTEXT_ORIGINAL_USER_QUERY, prompt.getUserMessage().getText());
		List<List<Document>> documents = promptAugmenter.documents(prompt, chatClientRequest.context());
		List<Document> join = promptAugmenter.join(documents, prompt, chatClientRequest.context());
		PromptAugmenter.AugmentResult augmentResult = promptAugmenter.augment(join, prompt, chatClientRequest.context());
		// 业务已自行改写消息列表时，不再套用 {context}/{query} 模板
		if (!augmentResult.applyUserTemplate()) {
			return chatClientRequest;
		}
		String content = augmentResult.contextText();
		Map<String, Object> context = Map.of("context", content, "query", prompt.getUserMessage().getText());
		PromptTemplate promptTemplate = StringUtil.isNotBlank(content) ? this.promptTemplate : emptyContextPromptTemplate;
		return ChatClientRequest.builder()
			.prompt(chatClientRequest.prompt().augmentUserMessage(promptTemplate.render(context)))
			.context(chatClientRequest.context())
			.build();
	}

	/**
	 * 响应后置处理：默认透传，可按需扩展（如记录增强后的 prompt 到 metadata）。
	 */
	@Override
	public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
		return chatClientResponse;
	}

	@Override
	public int getOrder() {
		return order;
	}

	/**
	 * 创建 Builder 实例。
	 * <p>
	 * 未调用 {@link Builder#promptAugmenter(PromptAugmenter)} 时使用内置默认实现。
	 */
	public static PromptAugmentationAdvisor.Builder builder() {
		return new PromptAugmentationAdvisor.Builder();
	}

	/**
	 * {@link PromptAugmentationAdvisor} 构建器
	 */
	public static final class Builder {

		private int order = 0;

		private PromptAugmenter promptAugmenter;

		private PromptTemplate promptTemplate;

		private PromptTemplate emptyContextPromptTemplate;

		private Builder() {
		}

		/**
		 * 设置提示词增强服务实现；不传则使用内置默认（{@link PromptAugmenter} default 方法）。
		 */
		public PromptAugmentationAdvisor.Builder promptAugmenter(PromptAugmenter promptAugmenter) {
			this.promptAugmenter = promptAugmenter;
			return this;
		}

		/**
		 * 设置 Advisor 在链中的执行顺序。
		 * <p>
		 * 默认值为 {@link Advisor#DEFAULT_CHAT_MEMORY_PRECEDENCE_ORDER} - 300。
		 * 应大于 {@link KnowledgeRetrievalAdvisor} 的 order，确保检索先于增强执行。
		 */
		public PromptAugmentationAdvisor.Builder order(int order) {
			this.order = order;
			return this;
		}

		/**
		 * 设置有检索上下文时使用的 prompt 模板。
		 * 变量：{@code context}、{@code query}
		 */
		public PromptAugmentationAdvisor.Builder promptTemplate(PromptTemplate promptTemplate) {
			this.promptTemplate = promptTemplate;
			return this;
		}

		/**
		 * 设置无检索上下文时使用的 prompt 模板。
		 * <p>
		 * 默认引导模型礼貌拒答；若需无召回时原样发送用户问题，可传入 {@code new PromptTemplate("{query}")}。
		 * 渲染时统一传入 {@code context}、{@code query}，模板未使用的变量会被忽略。
		 */
		public PromptAugmentationAdvisor.Builder emptyContextPromptTemplate(PromptTemplate emptyContextPromptTemplate) {
			this.emptyContextPromptTemplate = emptyContextPromptTemplate;
			return this;
		}

		/**
		 * 构建 {@link PromptAugmentationAdvisor} 实例
		 */
		public PromptAugmentationAdvisor build() {
			return new PromptAugmentationAdvisor(order, promptAugmenter, promptTemplate,
				emptyContextPromptTemplate);
		}

	}

}
