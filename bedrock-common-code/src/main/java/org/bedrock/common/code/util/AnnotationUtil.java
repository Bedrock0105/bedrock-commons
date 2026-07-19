package org.bedrock.common.code.util;

import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.lang.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public abstract class AnnotationUtil extends AnnotationUtils {

	/**
	 * 获取方法注解
	 * 方法上面没有，再去类上面找
	 */
	@Nullable
	public static <A extends Annotation> A findAnnotation(Method method, Class<A> annotationType) {
		A annotation = AnnotationUtils.findAnnotation(method, annotationType);
		if (annotation != null) {
			return annotation;
		}
		return AnnotatedElementUtils.findMergedAnnotation(method.getDeclaringClass(), annotationType);
	}
}
