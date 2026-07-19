package org.bedrock.common.ai.advisor.usage;

import org.bedrock.common.ai.enums.TokenUsageSource;
import org.springframework.ai.chat.metadata.Usage;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Token 用量统计持久化 SPI（Advisor 回调接口，非业务 CRUD Service）。
 * <p>
 * 由 {@link org.bedrock.common.ai.advisor.TokenUsageStatisticsAdvisor} 在每次对话结束后回调，
 * 业务侧实现此接口，将用量、耗时等信息写入数据库或监控系统。
 * </p>
 */
public interface TokenUsageRecorder {

	/**
	 * Advisor 上下文中存放 jtokkit {@link com.knuddels.jtokkit.api.Encoding} 的 key。
	 */
	String CONTEXT_ENCODING_KEY = "chat_encoding";

	/**
	 * 记录一次对话的 Token 用量与耗时指标。
	 */
	void recordTokenUsage(TokenUsageRecord record);

	/**
	 * 单次对话的 Token 用量与耗时快照。
	 *
	 * @param usage                 Token 用量
	 * @param usageSource           用量数据来源
	 * @param stream                是否流式调用（由 Advisor 入口显式标记，非耗时推断）
	 * @param startedAt             请求开始时间
	 * @param totalLatencyMs        端到端总耗时（毫秒）
	 * @param firstTokenLatencyMs   首 token 耗时（毫秒）
	 * @param streamingDurationMs   流式生成耗时（毫秒）：首 token 到最后 token；同步为 0
	 * @param tokensPerSecond       输出吞吐（tokens/s）
	 * @param completedAt           请求结束时间
	 * @param advisorContext        Advisor 链上下文（含 user/assistant 消息 id、会话、模型等）
	 */
	record TokenUsageRecord(Usage usage,
	                        TokenUsageSource usageSource,
	                        boolean stream,
	                        LocalDateTime startedAt,
	                        int totalLatencyMs,
	                        int firstTokenLatencyMs,
	                        int streamingDurationMs,
	                        double tokensPerSecond,
	                        LocalDateTime completedAt,
	                        Map<String, Object> advisorContext) {

		public static double computeTokensPerSecond(int completionTokens,
		                                            int streamingDurationMs,
		                                            int totalLatencyMs) {
			int generationDurationMs = streamingDurationMs > 0 ? streamingDurationMs : totalLatencyMs;
			if (generationDurationMs <= 0 || completionTokens <= 0) {
				return 0.0;
			}
			return completionTokens / (generationDurationMs / 1000.0);
		}

	}

}
