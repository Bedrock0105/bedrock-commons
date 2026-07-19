package org.bedrock.common.ai.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记 Spring Bean 为 AI 工具提供者。
 * <p>
 * 启动时由 {@link org.bedrock.common.ai.tool.ToolManager} 扫描并注册，
 * 可通过 {@code name} 获取对应的 {@link org.springframework.ai.tool.ToolCallback} 列表。
 * </p>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ToolProvider {

	/**
	 * 工具组唯一标识，用于按名称获取工具回调。
	 * <p>为空时默认使用 Spring Bean 名称。</p>
	 */
	String name() default "";

	/**
	 * 工具组展示名称，供前端下拉等场景使用。
	 */
	String label() default "";

	/**
	 * 工具回调的构建方式。
	 */
	Kind kind();

	/**
	 * 工具回调构建策略。
	 */
	enum Kind {
		/**
		 * 通过 {@link org.springframework.ai.tool.resolution.ToolCallbackResolver} 解析 Function 工具。
		 */
		FUNCTION,
		/**
		 * 扫描 Bean 中 {@link org.springframework.ai.tool.annotation.Tool} 标注的方法。
		 */
		METHOD
	}

}
