package org.bedrock.common.ai.advisor;

import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.Encoding;
import com.knuddels.jtokkit.api.EncodingRegistry;
import com.knuddels.jtokkit.api.EncodingType;
import lombok.Getter;
import org.bedrock.common.ai.advisor.usage.TokenUsageRecorder;
import org.bedrock.common.ai.enums.TokenUsageSource;
import org.bedrock.common.code.util.StringUtil;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.*;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.metadata.*;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.core.Ordered;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * Token 用量统计 Advisor：在对话链路结束后采集用量与耗时，并委托 {@link TokenUsageRecorder} 持久化。
 * <p>
 * 工作流程（同步 {@link #adviseCall}）：
 * <ol>
 *   <li>记录请求开始时间与耗时起点</li>
 *   <li>执行下游 Advisor 链，完成模型调用</li>
 *   <li>从 {@link ChatResponse} 元数据读取 {@link Usage}</li>
 *   <li>若模型未返回用量（{@link EmptyUsage}），使用 jtokkit 对请求/响应文本做本地估算</li>
 *   <li>组装 {@link TokenUsageRecorder.TokenUsageRecord} 并回调 {@link TokenUsageRecorder#recordTokenUsage}</li>
 * </ol>
 * <p>
 * Token 估算策略：
 * <ul>
 *   <li>优先使用模型响应中的 {@link Usage}</li>
 *   <li>缺失时：对 {@link UserMessage} 文本计 prompt tokens，对 assistant 输出文本计 completion tokens</li>
 *   <li>Encoding 优先从 Advisor {@code context}（key = {@link TokenUsageRecorder#CONTEXT_ENCODING_KEY}）获取，
 *       否则使用构建时配置的 {@code fallbackEncoding}（默认 {@link EncodingType#CL100K_BASE}）</li>
 * </ul>
 * <p>
 * 工作流程（流式 {@link #adviseStream}）：
 * <ol>
 *   <li>记录请求开始时间与耗时起点</li>
 *   <li>透传下游 Advisor 链的流式分片（不修改 Flux 元素，不影响其他 Advisor）</li>
 *   <li>{@code doOnNext} 累积正文、捕获首 token 时间戳与模型 Usage</li>
 *   <li>在 {@code complete / cancel / error} 时通过 {@code saveOnce} 组装记录并回调 {@link TokenUsageRecorder#recordTokenUsage}</li>
 * </ol>
 * <p>
 * 与其他 Advisor 的关系：
 * <ul>
 *   <li>仅挂载 {@code doOnNext / doOnComplete / doOnCancel / doOnError} 副作用，不重写、不替换流式分片</li>
 *   <li>{@code responseContextRef} 在 {@code doOnNext} 中跟踪最新 {@code response.context()}，供终止时读取会话/用户等扩展字段</li>
 *   <li>建议 order 大于 {@link ChatHistoryAdvisor}（更靠近模型），以便 context 已包含历史 Advisor 写入的数据</li>
 * </ul>
 *
 * @see TokenUsageRecorder
 */
public class TokenUsageStatisticsAdvisor implements CallAdvisor, StreamAdvisor {

	public static final EncodingRegistry ENCODING_REGISTRY = Encodings.newLazyEncodingRegistry();

	private static final Usage EMPTY_USAGE = new EmptyUsage();

	/**
	 * 模型未返回 Usage 且 context 中无自定义 Encoding 时，用于本地 token 估算的默认编码器
	 */
	private final Encoding fallbackEncoding;

	/**
	 * Advisor 在链中的执行顺序，数值越小越先执行
	 */
	private final int order;

	private final TokenUsageRecorder tokenUsageRecorder;

	/**
	 * 流式请求的处理调度器，before 阶段在此调度器上执行
	 */
	@Getter
	private final Scheduler scheduler;

	private TokenUsageStatisticsAdvisor(TokenUsageRecorder tokenUsageRecorder,
	                                    int order,
	                                    Scheduler scheduler,
	                                    Encoding fallbackEncoding) {
		Assert.notNull(tokenUsageRecorder, "tokenUsageRecorder cannot be null");
		Assert.notNull(scheduler, "scheduler cannot be null");
		Assert.notNull(fallbackEncoding, "fallbackEncoding cannot be null");
		this.tokenUsageRecorder = tokenUsageRecorder;
		this.order = order;
		this.scheduler = scheduler;
		this.fallbackEncoding = fallbackEncoding;
	}

	@Override
	public int getOrder() {
		return order;
	}

	/**
	 * 同步对话：调用完成后统计 Token 用量与端到端耗时。
	 */
	@Override
	public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain callAdvisorChain) {
		Assert.notNull(chatClientRequest, "chatClientRequest cannot be null");
		Assert.notNull(callAdvisorChain, "callAdvisorChain cannot be null");

		// 耗时统计起点（毫秒时间戳，用于计算 totalLatencyMs）
		long startTimeMillis = System.currentTimeMillis();
		// 请求开始时间（调用模型前记录，写入 startedAt）
		LocalDateTime startedAt = LocalDateTime.now();

		ChatClientResponse chatClientResponse = callAdvisorChain.nextCall(chatClientRequest);

		// 端到端总耗时（毫秒）：从发起模型调用到收到完整响应
		int totalLatencyMs = (int) (System.currentTimeMillis() - startTimeMillis);
		// 请求结束时间（收到完整响应后记录，写入 completedAt）
		LocalDateTime completedAt = LocalDateTime.now();

		// Token 用量及数据来源：优先取模型元数据，缺失时用 jtokkit 本地估算
		ResolvedUsage resolvedUsage = resolveUsage(chatClientRequest, chatClientResponse);
		// 流式生成耗时：同步场景无流式阶段，固定为 0
		int streamingDurationMs = 0;
		// 输出吞吐（tokens/s）：completionTokens / 生成耗时
		double tokensPerSecond = TokenUsageRecorder.TokenUsageRecord.computeTokensPerSecond(
			resolvedUsage.usage().getCompletionTokens(), streamingDurationMs, totalLatencyMs);

		tokenUsageRecorder.recordTokenUsage(new TokenUsageRecorder.TokenUsageRecord(
			resolvedUsage.usage(),
			resolvedUsage.usageSource(),
			false,
			startedAt,
			totalLatencyMs,
			totalLatencyMs,
			streamingDurationMs,
			tokensPerSecond,
			completedAt,
			chatClientResponse.context()));
		return chatClientResponse;
	}

	/**
	 * 流式对话：透传流式分片，在 complete / cancel / error 时统计 Token 用量与耗时。
	 * <p>
	 * 执行流程：
	 * <pre>
	 * flatMapMany(nextStream) → 执行后续 Advisor 并调用模型流式接口
	 * doOnNext              → 累积正文、记录首 token 时间戳、捕获 Usage
	 * doOnComplete          → saveOnce → recordTokenUsage
	 * doOnCancel / doOnError → 同上（部分响应也统计）
	 * </pre>
	 * <p>
	 * 下游订阅者仍收到原始分片；本 Advisor 不做二次聚合，不影响其他 Advisor 的处理逻辑。
	 */
	@Override
	public Flux<ChatClientResponse> adviseStream(ChatClientRequest chatClientRequest, StreamAdvisorChain streamAdvisorChain) {
		Assert.notNull(chatClientRequest, "chatClientRequest cannot be null");
		Assert.notNull(streamAdvisorChain, "streamAdvisorChain cannot be null");
		Assert.notNull(getScheduler(), "scheduler cannot be null");

		// 耗时统计起点（毫秒时间戳，用于计算 totalLatencyMs / firstTokenLatencyMs）
		long startTimeMillis = System.currentTimeMillis();
		// 请求开始时间（调用模型前记录，写入 startedAt）
		LocalDateTime startedAt = LocalDateTime.now();

		// 流式正文累积器，doOnNext 逐片 append，终止时交给 buildPartialResponse
		StringBuilder contentBuilder = new StringBuilder();
		// 模型上报的 Usage（通常在末片或非 EmptyUsage 的分片到达）
		AtomicReference<Usage> responseUsageRef = new AtomicReference<>(EMPTY_USAGE);
		// 首 token 到达时的毫秒时间戳，0 表示尚未收到任何分片
		AtomicLong firstTokenTimeMillisRef = new AtomicLong(0);
		// 标记本轮是否已记录，compareAndSet 保证 complete / cancel / error 只写入一次
		AtomicBoolean saved = new AtomicBoolean(false);
		/**
		 * 流式 context 快照，供 complete / cancel / error 使用。
		 * 初始为请求 context（兜底首包前 cancel）；doOnNext 每帧覆盖为 response.context()。
		 */
		AtomicReference<Map<String, Object>> responseContextRef = new AtomicReference<>(chatClientRequest.context());

		Consumer<ChatClientResponse> saveOnce = response -> {
			if (saved.compareAndSet(false, true)) {
				long endTimeMillis = System.currentTimeMillis();
				// 端到端总耗时（毫秒）：从发起模型调用到流终止
				int totalLatencyMs = (int) (endTimeMillis - startTimeMillis);
				// 请求结束时间（流终止时记录，写入 completedAt）
				LocalDateTime completedAt = LocalDateTime.now();

				long firstTokenTimeMillis = firstTokenTimeMillisRef.get();
				// 首 token 耗时（毫秒）：首片到达时间 - 请求开始时间；无分片时为 0
				int firstTokenLatencyMs = firstTokenTimeMillis != 0L
					? (int) (firstTokenTimeMillis - startTimeMillis)
					: 0;
				// 流式生成耗时（毫秒）：末 token 时间 - 首 token 时间；无分片时为 0
				int streamingDurationMs = firstTokenTimeMillis != 0L
					? (int) (endTimeMillis - firstTokenTimeMillis)
					: 0;

				// Token 用量及数据来源：优先取模型元数据，缺失时用 jtokkit 对累积正文估算
				ResolvedUsage resolvedUsage = resolveUsage(chatClientRequest, response);
				// 输出吞吐（tokens/s）：completionTokens / streamingDurationMs（无流式阶段时回退 totalLatencyMs）
				double tokensPerSecond = TokenUsageRecorder.TokenUsageRecord.computeTokensPerSecond(
					resolvedUsage.usage().getCompletionTokens(), streamingDurationMs, totalLatencyMs);

				tokenUsageRecorder.recordTokenUsage(new TokenUsageRecorder.TokenUsageRecord(
					resolvedUsage.usage(),
					resolvedUsage.usageSource(),
					true,
					startedAt,
					totalLatencyMs,
					firstTokenLatencyMs,
					streamingDurationMs,
					tokensPerSecond,
					completedAt,
					response.context()));
			}
		};

		return Mono.just(chatClientRequest)
			.publishOn(getScheduler())
			.flatMapMany(streamAdvisorChain::nextStream)
			.doOnNext(response -> {
				// 每帧更新 context（含其他 Advisor 写入的会话 id、用户信息等）
				responseContextRef.set(response.context());
				// 记录首 token 时间戳（仅写入一次）
				firstTokenTimeMillisRef.compareAndSet(0, System.currentTimeMillis());

				ChatResponse chatResponse = response.chatResponse();
				if (chatResponse == null) {
					return;
				}
				AssistantMessage output = chatResponse.getResult().getOutput();
				if (output.getText() != null) {
					contentBuilder.append(output.getText());
				}
				if (!(chatResponse.getMetadata().getUsage() instanceof EmptyUsage)) {
					responseUsageRef.set(chatResponse.getMetadata().getUsage());
				}
			})
			.doOnComplete(() -> saveOnce.accept(
				buildPartialResponse(responseContextRef.get(), responseUsageRef.get(), contentBuilder)))
			.doOnCancel(() -> saveOnce.accept(
				buildPartialResponse(responseContextRef.get(), responseUsageRef.get(), contentBuilder)))
			.doOnError(ex -> saveOnce.accept(
				buildPartialResponse(responseContextRef.get(), responseUsageRef.get(), contentBuilder)));
	}

	/**
	 * 根据流式累积内容构造完整/部分响应，供终止时解析 Usage 与估算 token。
	 */
	private ChatClientResponse buildPartialResponse(Map<String, Object> context,
	                                                Usage usage,
	                                                StringBuilder contentBuilder) {

		AssistantMessage.Builder assistantBuilder = AssistantMessage.builder()
			.content(contentBuilder.toString());
		AssistantMessage assistantMessage = assistantBuilder.build();

		return ChatClientResponse.builder()
			.chatResponse(ChatResponse.builder()
				.generations(List.of(new Generation(assistantMessage)))
				.metadata(ChatResponseMetadata.builder().usage(usage).build())
				.build())
			.context(context)
			.build();
	}

	/**
	 * 解析本次对话的 Token 用量及数据来源。
	 */
	private ResolvedUsage resolveUsage(ChatClientRequest chatClientRequest, ChatClientResponse chatClientResponse) {
		ChatResponse chatResponse = chatClientResponse.chatResponse();
		if (chatResponse == null) {
			return new ResolvedUsage(EMPTY_USAGE, TokenUsageSource.LOCAL_ESTIMATED);
		}

		Usage usage = chatResponse.getMetadata().getUsage();
		if (!(usage instanceof EmptyUsage)) {
			return new ResolvedUsage(usage, TokenUsageSource.MODEL_REPORTED);
		}

		return new ResolvedUsage(
			estimateUsage(chatClientRequest, chatClientResponse.context(), chatResponse),
			TokenUsageSource.LOCAL_ESTIMATED);
	}

	/**
	 * Token 用量解析结果。
	 *
	 * @param usage       用量
	 * @param usageSource 数据来源
	 */
	private record ResolvedUsage(Usage usage, TokenUsageSource usageSource) {

	}

	/**
	 * 使用 jtokkit 对请求与响应文本做本地 Token 估算。
	 */
	private Usage estimateUsage(ChatClientRequest chatClientRequest,
	                            Map<String, Object> advisorContext,
	                            ChatResponse chatResponse) {
		Encoding encoding = resolveEncoding(advisorContext);

		int promptTokens = 0;
		Message requestMessage = chatClientRequest.prompt().getLastUserOrToolResponseMessage();
		if (requestMessage instanceof UserMessage userMessage) {
			promptTokens = encoding.countTokens(userMessage.getText());
		}

		int completionTokens = 0;
		String assistantText = chatResponse.getResult().getOutput().getText();
		if (StringUtil.isNotBlank(assistantText)) {
			completionTokens = encoding.countTokens(assistantText);
		}

		return new DefaultUsage(promptTokens, completionTokens, promptTokens + completionTokens);
	}

	/**
	 * 从 Advisor 上下文解析 Encoding，未配置时回退到 {@link #fallbackEncoding}。
	 */
	public Encoding resolveEncoding(Map<String, Object> advisorContext) {
		if (advisorContext.get(TokenUsageRecorder.CONTEXT_ENCODING_KEY) instanceof Encoding encoding) {
			return encoding;
		}
		return fallbackEncoding;
	}

	public static Builder builder(TokenUsageRecorder tokenUsageRecorder) {
		return new Builder(tokenUsageRecorder);
	}

	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}

	/**
	 * {@link TokenUsageStatisticsAdvisor} 构建器。
	 */
	public static final class Builder {

		private int order = Ordered.LOWEST_PRECEDENCE - 100;

		private Scheduler scheduler = BaseAdvisor.DEFAULT_SCHEDULER;

		private final TokenUsageRecorder tokenUsageRecorder;

		private Encoding fallbackEncoding = ENCODING_REGISTRY.getEncoding(EncodingType.CL100K_BASE);

		private Builder(TokenUsageRecorder tokenUsageRecorder) {
			this.tokenUsageRecorder = tokenUsageRecorder;
		}

		/**
		 * 设置 Advisor 在链中的执行顺序。
		 * <p>默认值为 {@code Ordered.LOWEST_PRECEDENCE - 100}。</p>
		 */
		public Builder order(int order) {
			this.order = order;
			return this;
		}

		/**
		 * 设置流式处理使用的调度器。
		 * <p>默认值为 {@link BaseAdvisor#DEFAULT_SCHEDULER}。</p>
		 */
		public Builder scheduler(@NonNull Scheduler scheduler) {
			this.scheduler = scheduler;
			return this;
		}

		/**
		 * 设置本地 Token 估算使用的默认 Encoding。
		 * <p>当模型未返回 Usage 且 context 中未指定 {@link TokenUsageRecorder#CONTEXT_ENCODING_KEY} 时生效。
		 * 默认值为 {@link EncodingType#CL100K_BASE}。</p>
		 */
		public Builder fallbackEncoding(@NonNull Encoding fallbackEncoding) {
			this.fallbackEncoding = fallbackEncoding;
			return this;
		}

		public TokenUsageStatisticsAdvisor build() {
			return new TokenUsageStatisticsAdvisor(tokenUsageRecorder, order, scheduler, fallbackEncoding);
		}
	}
}
