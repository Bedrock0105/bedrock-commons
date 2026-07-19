package org.bedrock.common.ai.advisor.knowledge;

import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * 知识库检索 SPI（Advisor 回调接口，非业务 CRUD Service）：由业务方实现向量库获取，其余步骤可通过 default 方法覆盖。
 * <p>
 * 典型调用链（由 {@link org.bedrock.common.ai.advisor.KnowledgeRetrievalAdvisor} 驱动）：
 * <ol>
 *   <li>{@link #needRetrieval} — 判断是否执行检索（默认：context 中尚无检索结果时执行）</li>
 *   <li>{@link #getSearchRequest} — 构建向量检索请求，可向 context 写入中间参数；返回 {@code null} 表示跳过检索</li>
 *   <li>{@link #retrieve} — 调用 {@link VectorStore#similaritySearch(SearchRequest)} 召回文档</li>
 *   <li>{@link #transformRetrievedDocuments} — 对召回结果做过滤、重排或截断</li>
 * </ol>
 * <p>
 * 各步骤方法均接收本轮用户文本 {@code text} 与历史消息列表 {@code history}
 * （{@code prompt.getInstructions()}，通常含多轮 user / assistant 及 system 消息），
 * 业务方可据此实现：
 * <ul>
 *   <li>结合上下文改写检索 query（如多轮追问「它」指代消解）</li>
 *   <li>根据对话状态决定是否检索、检索哪个知识库</li>
 *   <li>按历史相关性过滤、重排召回结果</li>
 * </ul>
 * <p>
 * 检索结果写入 context 的 {@link #KNOWLEDGE_RETRIEVED_DOCUMENTS} 键，
 * 由后续的「提示词组装 Advisor」统一编排进 prompt，本接口不直接修改用户提示词。
 */
public interface KnowledgeRetriever {

	/**
	 * context 中存放检索结果的键名。
	 * <p>
	 * value 类型为 {@code List<Document>}，由 {@link org.bedrock.common.ai.advisor.KnowledgeRetrievalAdvisor} 写入。
	 */
	String KNOWLEDGE_RETRIEVED_DOCUMENTS = "qa_knowledge_retrieved_documents";

	/**
	 * 当前请求是否需要执行知识库检索。
	 * <p>
	 * 默认实现：context 中已存在 {@link #KNOWLEDGE_RETRIEVED_DOCUMENTS} 时跳过，
	 * 避免同一次调用链中重复检索。
	 * <p>
	 * 业务方可结合 {@code history} 实现更精细的策略，例如：纯寒暄不检索、追问才检索等。
	 *
	 * @param text    本轮用户输入文本
	 * @param history prompt 中的历史消息列表（含本轮及过往多轮对话）
	 * @param context Advisor 共享上下文
	 * @return {@code true} 表示需要检索
	 */
	default boolean needRetrieval(String text, List<Message> history, Map<String, Object> context) {
		return !context.containsKey(KNOWLEDGE_RETRIEVED_DOCUMENTS);
	}

	/**
	 * 构建向量检索请求。
	 * <p>
	 * 默认以 {@code text} 作为 query；文本为空时返回 {@code null}，表示不检索。
	 * 业务方可覆盖此方法以结合 {@code history} 改写 query、设置 topK、相似度阈值、
	 * metadata 过滤等，也可向 context 写入检索相关的中间状态。
	 * <p>
	 * 调用时传入的是已 {@code mutate()} 后的 context 副本，可安全写入。
	 *
	 * @param text    本轮用户输入文本
	 * @param history prompt 中的历史消息列表，可用于 query 改写或上下文理解
	 * @param context Advisor 共享上下文（可变副本）
	 * @return 检索请求；{@code null} 表示跳过检索
	 */
	@Nullable
	default SearchRequest getSearchRequest(String text, List<Message> history, Map<String, Object> context) {
		if (!StringUtils.hasText(text)) {
			return null;
		}
		return SearchRequest.builder()
			.topK(4)
			.similarityThreshold(0.6)
			.query(text).build();
	}

	/**
	 * 执行向量相似度检索。
	 * <p>
	 * 默认从 {@link #getVectorStore(Map)} 获取 VectorStore 并调用 similaritySearch。
	 * {@code history} 在默认实现中未使用，业务方覆盖时可参与检索策略。
	 *
	 * @param request 检索请求
	 * @param text    本轮用户输入文本
	 * @param history prompt 中的历史消息列表
	 * @param context Advisor 共享上下文
	 * @return 召回的文档列表（{@link VectorStore#similaritySearch} 契约保证非 null，可为空列表）
	 */
	default List<Document> retrieve(SearchRequest request, String text, List<Message> history,
	                                Map<String, Object> context) {
		return getVectorStore(context).similaritySearch(request);
	}

	/**
	 * 转换 / 过滤检索结果。
	 * <p>
	 * 默认原样返回。业务方可结合 {@code history} 做去重、按对话相关性截断、
	 * 按分数过滤、拼接 metadata 等处理。
	 *
	 * @param text      本轮用户输入文本
	 * @param history   prompt 中的历史消息列表
	 * @param documents 原始检索结果
	 * @param context   Advisor 共享上下文
	 * @return 处理后的文档列表
	 */
	default List<Document> transformRetrievedDocuments(String text, List<Message> history, List<Document> documents,
	                                                   Map<String, Object> context) {
		return documents;
	}

	/**
	 * 从 context 中解析本次请求应使用的向量库。
	 * <p>
	 * 由业务方实现，通常根据会话绑定的知识库 id 获取对应 VectorStore。
	 *
	 * @param context Advisor 共享上下文
	 * @return 向量库实例，不可为 null
	 */
	@NonNull
	VectorStore getVectorStore(Map<String, Object> context);

}
