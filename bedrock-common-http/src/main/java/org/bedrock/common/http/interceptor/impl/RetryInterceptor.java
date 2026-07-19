package org.bedrock.common.http.interceptor.impl;

import lombok.extern.slf4j.Slf4j;
import org.bedrock.common.code.reflects.MethodMeta;
import org.bedrock.common.http.HttpRequest;
import org.bedrock.common.http.HttpResponse;
import org.bedrock.common.http.exception.HttpException;
import org.bedrock.common.http.interceptor.Interceptor;
import org.bedrock.common.http.model.RetryMeta;
import org.bedrock.common.http.support.InterceptorChain;
import org.bedrock.common.spi.autoservice.AutoService;
import org.springframework.core.Ordered;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

/**
 * HTTP请求重试拦截器
 * 核心功能：基于重试配置{RetryMeta}实现请求失败重试，支持配置最大重试次数、重试间隔、自定义重试触发条件
 * 执行优先级：Ordered.LOWEST_PRECEDENCE - 19，在日志拦截器之后执行，保证重试日志可正常打印
 */
@Slf4j
@AutoService(Interceptor.class)
public class RetryInterceptor implements Interceptor {

	/**
	 * 核心拦截方法：执行重试逻辑，按需触发请求重试
	 *
	 * @param request    请求对象，包含重试配置元数据
	 * @param methodMeta 方法元数据
	 * @param chain      拦截器执行链
	 * @return 最终的请求响应结果
	 */
	@Override
	public HttpResponse interceptor(HttpRequest request, MethodMeta methodMeta, InterceptorChain chain) {
		// 获取重试配置，无配置则直接执行请求不重试
		RetryMeta retryMeta = getRetryMeta(request);
		if (retryMeta == null) {
			return chain.doInterceptor(request, methodMeta);
		}

		// 创建适配当前重试配置的重试模板
		RetryTemplate retryTemplate = createRetryTemplate(retryMeta);
		// 执行重试模板逻辑
		return (HttpResponse) retryTemplate.execute(context -> {
			HttpResponse response;
			// 核心规则：首次请求走拦截器链，重试请求直接执行响应（复用原请求参数）
			if (context.getRetryCount() > 0) {
				response = request.executeResponse();
			} else {
				response = chain.doInterceptor(request, methodMeta);
			}

			// 校验是否满足重试条件：不满足则直接返回响应，终止重试
			if (!retryMeta.getRetryPredicate().test(response)) {
				return response;
			}
			context.setAttribute("httpResponse", response);
			// 满足重试条件，打印重试日志并抛出异常触发重试
			int retryCount = context.getRetryCount();
			log.info("【请求触发重试】第[{}]次重试，请求地址: [{}]，响应状态码: [{}]", retryCount, request.url(), response.status());
			throw new HttpException("请求执行结果满足重试条件，触发第[" + retryCount + "]次重试，响应状态码：" + response.status());
		}, context -> context.getAttribute("httpResponse"));
	}

	/**
	 * 获取当前请求的重试配置元数据
	 *
	 * @param request 请求对象
	 * @return 重试配置，无则返回null
	 */
	private RetryMeta getRetryMeta(HttpRequest request) {
		return request.getRetryMeta();
	}

	/**
	 * 根据重试配置构建重试模板
	 * 配置规则：固定间隔重试 + 指定最大重试次数，重试触发条件由外部{RetryPredicate}自定义
	 *
	 * @param retryMeta 重试配置元数据
	 * @return 初始化完成的重试模板
	 */
	private static RetryTemplate createRetryTemplate(RetryMeta retryMeta) {
		RetryTemplate retryTemplate = new RetryTemplate();
		// 配置最大重试次数：包含首次请求，例如配置3次则 首次+2次重试
		SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
		retryPolicy.setMaxAttempts(retryMeta.getMaxRetryCount());
		// 配置固定重试间隔时间（毫秒）
		FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
		backOffPolicy.setBackOffPeriod(retryMeta.getRetrySleepMillis());

		retryTemplate.setRetryPolicy(retryPolicy);
		retryTemplate.setBackOffPolicy(backOffPolicy);
		return retryTemplate;
	}

	/**
	 * 拦截器执行优先级
	 * 优先级说明：低于日志拦截器，保证重试过程的请求/响应日志都能被正常打印
	 */
	@Override
	public int getOrder() {
		return Ordered.LOWEST_PRECEDENCE - 19;
	}
}
