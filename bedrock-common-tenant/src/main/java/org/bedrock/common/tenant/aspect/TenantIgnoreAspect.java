package org.bedrock.common.tenant.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.bedrock.common.tenant.handler.TenantHandler;
import org.springframework.core.annotation.Order;

@Slf4j
@Aspect
@Order
public class TenantIgnoreAspect {

	@Around("@within(org.bedrock.common.tenant.annotation.TenantIgnore) || @annotation(org.bedrock.common.tenant.annotation.TenantIgnore)")
	public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
		boolean ignore = TenantHandler.isIgnore();
		TenantHandler.setIgnore(true);
		try {
			return joinPoint.proceed();
		} finally {
			TenantHandler.setIgnore(ignore);
		}
	}
}
