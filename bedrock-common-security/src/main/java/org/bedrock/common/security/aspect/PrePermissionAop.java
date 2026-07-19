package org.bedrock.common.security.aspect;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.bedrock.common.security.annotation.PrePermissionCheck;
import org.bedrock.common.security.exception.PermissionDeniedException;
import org.bedrock.common.security.expression.SecurityExpressionExpressionRoot;
import org.bedrock.common.code.util.AnnotationUtil;
import org.bedrock.common.code.util.SpELParserUtil;
import org.bedrock.common.code.util.WebUtil;
import org.springframework.core.annotation.Order;

import java.lang.reflect.Method;

@Slf4j
@Aspect
@Order(-1)
@RequiredArgsConstructor
public class PrePermissionAop {

	private final SecurityExpressionExpressionRoot securityExpressionExpressionRoot;

	@Around("@within(org.bedrock.common.security.annotation.PrePermissionCheck) || @annotation(org.bedrock.common.security.annotation.PrePermissionCheck)")
	public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
		MethodSignature signature = (MethodSignature) joinPoint.getSignature();
		Method method = signature.getMethod();
		PrePermissionCheck permissionCheck = AnnotationUtil.findAnnotation(method, PrePermissionCheck.class);
		if (permissionCheck == null) {
			return joinPoint.proceed();
		}
		if (WebUtil.getRequest() == null) {
			return joinPoint.proceed();
		}
		boolean parsed = SpELParserUtil.parseBoolean(permissionCheck.value(), method, joinPoint.getArgs(), securityExpressionExpressionRoot);
		if (!parsed) {
			throw new PermissionDeniedException();
		}
		return joinPoint.proceed();
	}
}
