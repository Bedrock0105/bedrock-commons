package org.bedrock.common.code.util;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.*;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.lang.Nullable;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class SpELParserUtil {

	private static final ExpressionParser PARSER = new SpelExpressionParser();

	private static final ParameterNameDiscoverer PARAM_DISCOVERER = new DefaultParameterNameDiscoverer();

	// Spring Bean解析器：支持在SpEL中引用Spring容器中的Bean（如@userService.check()）
	private static volatile BeanResolver beanResolver;

	/**
	 * SpEL表达式缓存（避免重复解析相同表达式字符串，提升性能）
	 */
	private static final Map<String, Expression> EXPRESSION_CACHE = new ConcurrentHashMap<>(128);

	/**
	 * 初始化BeanResolver（需传入Spring的BeanFactory，通常在AOP切面中初始化）
	 */
	public static void initBeanResolver(BeanFactory beanFactory) {
		if (beanResolver == null) {
			beanResolver = new BeanFactoryResolver(beanFactory);
		}
	}

	/**
	 * 解析方法上的SpEL表达式
	 *
	 * @param expressionStr SpEL表达式字符串（如"#userId == @userService.getCurrentUserId()"）
	 * @param method        目标方法
	 * @param args          方法参数值数组
	 * @return 表达式解析结果（布尔值，用于权限校验等场景）
	 */
	public static boolean parseBoolean(String expressionStr, Method method, Object[] args, @Nullable Object rootBean) {
		return Boolean.TRUE.equals(parse(expressionStr, method, args, Boolean.class, rootBean));
	}

	/**
	 * 解析方法上的SpEL表达式
	 *
	 * @param expressionStr SpEL表达式字符串（如"#userId == @userService.getCurrentUserId()"）
	 * @return 表达式解析结果（布尔值，用于权限校验等场景）
	 */
	public static boolean parseBoolean(String expressionStr, Map<String, Object> variables, @Nullable Object rootBean) {
		return Boolean.TRUE.equals(parse(expressionStr, variables, Boolean.class, rootBean));
	}

	/**
	 * 通用解析方法（返回任意类型结果）
	 */
	public static <T> T parse(String expressionStr, Method method, Object[] args, Class<T> resultType) {
		return parse(expressionStr, method, args, resultType, null);
	}

	/**
	 * 通用解析方法（返回任意类型结果）
	 */
	public static <T> T parse(String expressionStr, Method method, Object[] args, Class<T> resultType, @Nullable Object rootObject) {
		Expression expression = getCachedExpression(expressionStr);
		EvaluationContext context = buildEvaluationContext(method, args, rootObject);
		return expression.getValue(context, resultType);
	}

	/**
	 * 通用解析方法（返回任意类型结果）
	 */
	public static <T> T parse(String expressionStr, Map<String, Object> variables, Class<T> resultType) {
		return parse(expressionStr, variables, resultType, null);
	}

	/**
	 * 通用解析方法（返回任意类型结果）
	 */
	public static <T> T parse(String expressionStr, Map<String, Object> variables, Class<T> resultType, @Nullable Object rootObject) {
		Expression expression = getCachedExpression(expressionStr);
		EvaluationContext context = buildEvaluationContext(variables, rootObject);
		return expression.getValue(context, resultType);
	}

	/**
	 * 通用解析方法（返回任意类型结果）
	 */
	public static <T> T parse(String expressionStr, EvaluationContext context, Class<T> resultType) {
		Expression expression = getCachedExpression(expressionStr);
		return expression.getValue(context, resultType);
	}

	/**
	 * 通用解析方法（返回任意类型结果）
	 */
	public static <T> T parse(String expressionStr, Method method, Object[] args, Class<T> resultType, @Nullable Object rootObject, ParserContext parserContext) {
		Expression expression = PARSER.parseExpression(expressionStr, parserContext);
		EvaluationContext context = buildEvaluationContext(method, args, rootObject);
		return expression.getValue(context, resultType);
	}

	/**
	 * 通用解析方法（返回任意类型结果）
	 */
	public static <T> T parse(String expressionStr, Map<String, Object> variables, Class<T> resultType, @Nullable Object rootObject, ParserContext parserContext) {
		Expression expression = PARSER.parseExpression(expressionStr, parserContext);
		EvaluationContext context = buildEvaluationContext(variables, rootObject);
		return expression.getValue(context, resultType);
	}

	/**
	 * 通用解析方法（返回任意类型结果）
	 */
	public static <T> T parse(String expressionStr, EvaluationContext context, Class<T> resultType, ParserContext parserContext) {
		Expression expression = PARSER.parseExpression(expressionStr, parserContext);
		return expression.getValue(context, resultType);
	}

	/**
	 * 构建SpEL表达式上下文（包含方法参数和Bean解析器）
	 */
	public static EvaluationContext buildEvaluationContext(Method method, Object[] args, @Nullable Object rootBean) {
		return new MethodBasedEvaluationContext(rootBean, method, args, PARAM_DISCOVERER);
	}

	/**
	 * 构建SpEL表达式上下文（包含方法参数和Bean解析器）
	 */
	public static EvaluationContext buildEvaluationContext(Map<String, Object> variables, @Nullable Object rootBean) {
		StandardEvaluationContext context = rootBean == null ? new StandardEvaluationContext() : new StandardEvaluationContext(rootBean);
		if (CollectionUtil.isNotEmpty(variables)) {
			variables.forEach(context::setVariable);
		}
		if (beanResolver != null) {
			context.setBeanResolver(beanResolver);
		}
		return context;
	}

	/**
	 * 获取缓存的Expression对象（无缓存则解析并缓存）
	 *
	 * @param expressionStr SpEL表达式字符串
	 * @return 解析后的Expression对象
	 */
	private static Expression getCachedExpression(String expressionStr) {
		return EXPRESSION_CACHE.computeIfAbsent(expressionStr, PARSER::parseExpression);
	}
}
