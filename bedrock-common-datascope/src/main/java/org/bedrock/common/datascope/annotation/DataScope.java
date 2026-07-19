package org.bedrock.common.datascope.annotation;

import org.bedrock.common.datascope.enums.DataScopeTypeEnum;

import java.lang.annotation.*;

/**
 * 数据权限过滤注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataScope {

	/**
	 * 数据权限编码：用于标识当前MyBatis SQL操作对应的“数据权限规则”，是权限拦截器匹配权限策略的核心标识
	 *
	 * <p>核心限制与设计原因：
	 * 1. 实现方式限制：由于数据权限通过MyBatis拦截器实现（拦截SQL执行链路），拦截器工作阶段无Spring EL（SpEL）解析环境，
	 *    因此无法通过SpEL表达式动态生成权限编码，仅支持直接配置字符串常量；
	 *
	 * <p>空值处理规则：
	 * 若该字段配置为空字符串（""）或null，权限拦截器会自动获取当前MyBatis操作的「mapperId」作为默认数据权限编码，
	 * 其中mapperId格式为“全限定接口名.方法名”（例：com.bedrock.mapper.UserMapper.selectUserList），确保权限规则能精准匹配到具体SQL操作；
	 *
	 * <p>使用建议：
	 * - 自定义字符串编码：需与权限配置表中的“权限编码”字段一致（如"user:dept:own"表示“仅查看本部门用户”）；
	 * - 依赖mapperId默认值：需确保mapperId的命名规范与权限策略的匹配规则一致（避免因方法名变更导致权限失效）。
	 */
	String mapperId() default "";

	/**
	 * 数据权限类型
	 */
	DataScopeTypeEnum type() default DataScopeTypeEnum.DATA_SCOPE_ALL;

	/**
	 * 数据权限字段
	 */
	String column() default "dept_id";

	/**
	 * 可见字段
	 */
	String field() default "*";

	/**
	 * 附件信息
	 */
	String value() default "";

}
