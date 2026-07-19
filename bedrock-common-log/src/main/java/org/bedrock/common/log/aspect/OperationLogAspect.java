package org.bedrock.common.log.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.bedrock.common.log.operation.support.OperationLogCodeSupport;
import org.springframework.core.annotation.Order;

@Aspect
@Order
public class OperationLogAspect {

	@Pointcut("@annotation(org.bedrock.common.log.annotation.OperationLog)")
	public void operationLogCut() {
	}

	@Pointcut("@annotation(org.bedrock.common.log.annotation.OperationLogs)")
	public void operationLogsCut() {
	}

	@Around("operationLogCut() || operationLogsCut()")
	public Object around(ProceedingJoinPoint point) throws Throwable {
		return OperationLogCodeSupport.proceed(point);
	}
}
