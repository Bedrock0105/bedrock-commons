package org.bedrock.common.http.model;

import lombok.Getter;
import lombok.Setter;
import org.bedrock.common.http.HttpResponse;

import java.util.function.Predicate;

@Getter
@Setter
public class RetryMeta {

	/**
	 * 最大重试次数
	 */
	private int maxRetryCount = 3;

	/**
	 * 休眠时间
	 */
	private long retrySleepMillis = 1000;

	/**
	 * 重试条件
	 */
	private Predicate<HttpResponse> retryPredicate = new DefaultRetryPredicate();

	/**
	 * 默认重试条件
	 */
	public static class DefaultRetryPredicate implements Predicate<HttpResponse> {

		@Override
		public boolean test(HttpResponse response) {
			return response.isClientError() || response.isServerError();
		}
	}
}
