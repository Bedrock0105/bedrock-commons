package org.bedrock.common.ai.advisor.prompt;

import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.bedrock.common.ai.advisor.knowledge.KnowledgeRetriever.KNOWLEDGE_RETRIEVED_DOCUMENTS;

/**
 * 提示词增强 SPI（Advisor 回调接口，非业务 CRUD Service）：从 context 收集各检索源的结构化结果，合并后渲染进用户 prompt。
 * <p>
 * 典型调用链（由 {@link org.bedrock.common.ai.advisor.PromptAugmentationAdvisor} 驱动）：
 * <ol>
 *   <li>{@link #needAugmentation} — 判断是否需要增强（默认：context 中已有知识库检索结果时执行）</li>
 *   <li>{@link #documents} — 从 context 收集各来源的文档分片（知识库、Web 等）</li>
 *   <li>{@link #join} — 合并多来源文档列表</li>
 *   <li>{@link #augment} — 生成 {@link AugmentResult}（上下文文本 + 是否模板改写 UserMessage）</li>
 * </ol>
 * <p>
 * 与 {@link org.bedrock.common.ai.advisor.KnowledgeRetrievalAdvisor} 配合：
 * 检索 Advisor 负责召回并写入 {@link org.bedrock.common.ai.advisor.knowledge.KnowledgeRetriever#KNOWLEDGE_RETRIEVED_DOCUMENTS}，
 * 本接口负责读取并编排进最终 prompt，二者职责分离。
 * <p>
 * 业务方可覆盖各步骤方法以支持多源增强、自定义合并策略与模板变量。
 */
public interface PromptAugmenter {

	/**
	 * context 中存放用户原始问题的键名。
	 * <p>
	 * 由 {@link org.bedrock.common.ai.advisor.PromptAugmentationAdvisor} 在增强前写入，
	 * 便于后续 Advisor 或日志获取未改写前的用户输入。
	 */
	String QA_CONTEXT_ORIGINAL_USER_QUERY = "qa_original_user_query";

	/**
	 * 当前请求是否需要执行提示词增强。
	 * <p>
	 * 默认实现：仅当 context 中已存在 {@link org.bedrock.common.ai.advisor.knowledge.KnowledgeRetriever#KNOWLEDGE_RETRIEVED_DOCUMENTS} 时增强，
	 * 即知识库检索 Advisor 已执行过（无论召回是否为空）。
	 * 避免在未挂载知识库的普通对话中误改写用户 prompt。
	 * <p>
	 * 业务方可覆盖：例如存在 Web 检索结果时也返回 {@code true}。
	 *
	 * @param prompt  prompt 消息
	 * @param context Advisor 共享上下文
	 * @return {@code true} 表示需要增强
	 */
	default boolean needAugmentation(Prompt prompt, Map<String, Object> context) {
		return context.containsKey(KNOWLEDGE_RETRIEVED_DOCUMENTS);
	}

	/**
	 * 从 context 收集各增强来源的文档分片。
	 * <p>
	 * 默认实现读取 {@link org.bedrock.common.ai.advisor.knowledge.KnowledgeRetriever#KNOWLEDGE_RETRIEVED_DOCUMENTS}（由
	 * {@link org.bedrock.common.ai.advisor.KnowledgeRetrievalAdvisor} 写入）。
	 * 返回值为「分来源」的二维列表，便于 {@link #join} 按来源做优先级或去重。
	 * <p>
	 * 业务方可追加 Web 检索、协作文档等来源，每个来源一个 {@code List<Document>}。
	 *
	 * @param prompt  prompt 消息
	 * @param context Advisor 共享上下文
	 * @return 各来源文档分片列表，外层每个元素代表一个来源
	 */
	default List<List<Document>> documents(Prompt prompt, Map<String, Object> context) {
		List<List<Document>> documents = new ArrayList<>();
		Object raw = context.get(KNOWLEDGE_RETRIEVED_DOCUMENTS);
		if (raw instanceof List<?> documentsList) {
			List<Document> knowledgeDocs = documentsList.stream()
				.filter(Document.class::isInstance)
				.map(Document.class::cast)
				.toList();
			documents.add(knowledgeDocs);
		}
		return documents;
	}

	/**
	 * 合并多来源文档为单一列表。
	 * <p>
	 * 默认实现按来源顺序扁平化。业务方可覆盖以实现去重、按分数排序、token 截断等。
	 *
	 * @param documents 各来源文档分片（{@link #documents} 的返回值）
	 * @param prompt    prompt 消息
	 * @param context   Advisor 共享上下文
	 * @return 合并后的文档列表
	 */
	default List<Document> join(List<List<Document>> documents, Prompt prompt, Map<String, Object> context) {
		return documents.stream()
			.flatMap(List::stream)
			.toList();
	}

	/**
	 * 将文档列表转为增强结果（上下文字符串 + 是否仍由 Advisor 模板改写 UserMessage）。
	 * <p>
	 * 默认以系统换行符拼接各文档 {@link Document#getText()}，并 {@code applyUserTemplate=true}。
	 * 业务方可覆盖：若已自行改写 prompt 消息列表，可返回 {@code applyUserTemplate=false} 跳过模板注入。
	 *
	 * @param documents 合并后的文档列表（{@link #join} 的返回值）
	 * @param prompt    prompt 消息
	 * @param context   Advisor 共享上下文
	 * @return 增强结果；{@link AugmentResult#contextText()} 为空时使用 {@code emptyContextPromptTemplate}
	 */
	default AugmentResult augment(List<Document> documents, Prompt prompt, Map<String, Object> context) {
		return new AugmentResult(documents.stream()
			.map(Document::getText)
			.filter(text -> text != null && !text.isBlank())
			.collect(Collectors.joining(System.lineSeparator())));
	}

	/**
	 * 提示词增强结果。
	 *
	 * @param applyUserTemplate {@code true}：由 {@link org.bedrock.common.ai.advisor.PromptAugmentationAdvisor}
	 *                          用模板把 {@code contextText} 注入 UserMessage；
	 *                          {@code false}：业务已自行改写消息列表，Advisor 跳过模板改写
	 * @param contextText       检索/文档拼接后的上下文字符串（模板变量 {@code context}）
	 */
	record AugmentResult(boolean applyUserTemplate, String contextText) {

		/**
		 * 默认需要模板改写 UserMessage
		 */
		public AugmentResult(String contextText) {
			this(true, contextText);
		}

		/**
		 * 默认不应用上下文，不改写 UserMessage
		 */
		public AugmentResult() {
			this(false, null);
		}
	}

}
