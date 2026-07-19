package org.bedrock.common.log.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.bedrock.common.log.handler.LogTraceIdHandler;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@Aspect
public class LogTraceAspect {

	@Around("@annotation(org.springframework.scheduling.annotation.Async) || @annotation(org.springframework.scheduling.annotation.Scheduled)")
	public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
		try {
			LogTraceIdHandler.insertTraceId();
			return joinPoint.proceed();
		} finally {
			LogTraceIdHandler.removeTraceId();
		}
	}
}
