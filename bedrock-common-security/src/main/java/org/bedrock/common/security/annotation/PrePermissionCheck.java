package org.bedrock.common.security.annotation;

import org.bedrock.common.security.expression.SecurityExpressionExpressionRoot;

import java.lang.annotation.*;

/**
 * 鉴权注解
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface PrePermissionCheck {

	/**
	 * @return 方法执行前需要评估的Spring-EL表达式，用于权限校验
	 *
	 * <p>表达式支持以下用法：</p>
	 * 1. 调用{@link SecurityExpressionExpressionRoot}中的方法（作为SpEL根对象），例如：
	 *    <ul>
	 *      <li>{@code @PrePermissionCheck(value = "hasPermission()")}：调用权限检查方法</li>
	 *      <li>{@code @PrePermissionCheck(value = "hasRole('ADMIN')")}：校验是否拥有ADMIN角色</li>
	 *    </ul>
	 * 2. 引用Spring容器中的Bean（通过{@code @Bean名称}），例如：
	 *    <ul>
	 *      <li>{@code @PrePermissionCheck(value = "@userService.isOwner(#userId)")}：调用容器中userService的isOwner方法</li>
	 *    </ul>
	 * @see SecurityExpressionExpressionRoot 提供的内置权限校验方法
	 */
	String value();
}
