package org.bedrock.common.code.util;

import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.lang.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.springframework.core.BridgeMethodResolver;

import java.lang.reflect.Parameter;
import java.util.function.Consumer;

/**
 * 注解工具类，扩展Spring原生{@link AnnotationUtils}能力
 *
 * <p>两组查询API区别重点：
 * <ul>
 *     <li>{@link AnnotationUtils}：仅查询元素【直接声明注解】，<b>不追溯元注解/组合注解</b>，可处理{@code @AliasFor}</li>
 *     <li>{@link AnnotatedElementUtils}：支持<b>递归查找元注解、组合注解，合并{@code @AliasFor}属性</b>（SpringMVC/@Component/@RequestMapping场景首选）</li>
 * </ul>
 */
public abstract class AnnotationUtil extends AnnotationUtils {

	/**
	 * 查找注解【原生模式】
	 * <p>规则：优先获取方法上直接声明的注解，方法无注解则查询当前类上直接声明的注解
	 * <p>限制：<b>不支持元注解、组合注解识别</b>；推荐仅用于查询直接标注的原生注解
	 *
	 * @param method         目标方法
	 * @param annotationType 注解类型
	 * @return 注解实例，找不到返回null
	 */
	@Nullable
	public static <A extends Annotation> A findAnnotation(Method method, Class<A> annotationType) {
		A annotation = AnnotationUtils.findAnnotation(method, annotationType);
		if (annotation != null) {
			return annotation;
		}
		return AnnotationUtils.findAnnotation(method.getDeclaringClass(), annotationType);
	}

	/**
	 * 判断是否存在注解【原生模式】
	 * <p>规则：优先判断方法上直接声明的注解，方法无注解则判断当前类上直接声明的注解
	 * <p>限制：<b>不支持元注解、组合注解识别</b>；推荐仅用于查询直接标注的原生注解
	 *
	 * @param method         目标方法
	 * @param annotationType 注解类型
	 * @return true 存在直接标注的目标注解
	 */
	public static <A extends Annotation> boolean isAnnotation(Method method, Class<A> annotationType) {
		return findAnnotation(method, annotationType) != null;
	}

	/**
	 * 查找注解【合并模式｜推荐绝大多数业务场景使用】
	 * <p>规则：优先方法，方法无注解则查询当前类；支持元注解、组合注解、@AliasFor属性合并
	 * <p>示例：可识别{@code @RestController}上的元注解{@code @Controller}、自定义组合注解
	 *
	 * @param method         目标方法
	 * @param annotationType 注解类型
	 * @return 合并后的注解实例，找不到返回null
	 */
	@Nullable
	public static <A extends Annotation> A findElementAnnotation(Method method, Class<A> annotationType) {
		// 解析泛型桥方法，避免泛型实现桥方法丢失注解问题
		Method targetMethod = BridgeMethodResolver.findBridgedMethod(method);
		A annotation = AnnotatedElementUtils.findMergedAnnotation(targetMethod, annotationType);
		if (annotation != null) {
			return annotation;
		}
		return AnnotatedElementUtils.findMergedAnnotation(targetMethod.getDeclaringClass(), annotationType);
	}

	/**
	 * 判断元素是否存在注解【合并模式】
	 * <p>规则：优先方法，方法无注解则查询当前类；支持元注解、组合注解识别
	 *
	 * @param method         目标方法
	 * @param annotationType 注解类型
	 * @return true:存在目标注解
	 */
	public static <A extends Annotation> boolean isElementAnnotation(Method method, Class<A> annotationType) {
		Method targetMethod = BridgeMethodResolver.findBridgedMethod(method);
		if (AnnotatedElementUtils.isAnnotated(targetMethod, annotationType)) {
			return true;
		}
		return AnnotatedElementUtils.isAnnotated(targetMethod.getDeclaringClass(), annotationType);
	}

	/**
	 * 类上查找注解【合并模式，不递归父类】
	 *
	 * @param clazz          目标类
	 * @param annotationType 注解类型
	 * @return 注解实例
	 */
	@Nullable
	public static <A extends Annotation> A findClassAnnotation(Class<?> clazz, Class<A> annotationType) {
		return AnnotatedElementUtils.findMergedAnnotation(clazz, annotationType);
	}

	/**
	 * 获取方法参数上的合并注解
	 *
	 * @param parameter      方法入参
	 * @param annotationType 注解类型
	 */
	@Nullable
	public static <A extends Annotation> A findParameterAnnotation(Parameter parameter, Class<A> annotationType) {
		return AnnotatedElementUtils.findMergedAnnotation(parameter, annotationType);
	}

	/**
	 * 判断方法入参是否存在目标注解
	 */
	public static <A extends Annotation> boolean isParameterAnnotation(Parameter parameter, Class<A> annotationType) {
		return AnnotatedElementUtils.isAnnotated(parameter, annotationType);
	}

	/**
	 * 获取合并注解，如果不为null执行消费逻辑
	 *
	 * @param method         目标方法
	 * @param annotationType 注解类型
	 * @param consumer       注解回调
	 */
	public static <A extends Annotation> void ifPresentElementAnnotation(Method method, Class<A> annotationType, Consumer<A> consumer) {
		A anno = findElementAnnotation(method, annotationType);
		if (anno != null) {
			consumer.accept(anno);
		}
	}
}
