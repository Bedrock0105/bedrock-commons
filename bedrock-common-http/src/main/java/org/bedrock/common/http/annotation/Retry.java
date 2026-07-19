package org.bedrock.common.http.annotation;

import org.bedrock.common.http.HttpResponse;
import org.bedrock.common.http.model.RetryMeta;

import java.lang.annotation.*;
import java.util.function.Predicate;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Retry {

	/**
	 * 最大重试次数
	 */
	int maxRetryCount() default 3;

	/**
	 * 休眠时间
	 */
	long retrySleepMillis() default 1000;

	/**
	 * 重试条件
	 */
	Class<? extends Predicate<HttpResponse>> retryPredicate() default RetryMeta.DefaultRetryPredicate.class;
}
