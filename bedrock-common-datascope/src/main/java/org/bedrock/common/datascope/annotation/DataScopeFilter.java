package org.bedrock.common.datascope.annotation;

import org.bedrock.common.datascope.enums.DataScopeTypeEnum;

import java.lang.annotation.*;

/**
 * 数据权限定义：用于管控数据源过滤器（如Druid）的SQL拦截权限，实现“按权限过滤数据”的核心配置载体
 *
 * <p>核心定位：
 * 作为数据权限的配置入口，定义权限开启、数据范围模型设置的规则，配合数据源拦截器实现“SQL执行前自动追加数据范围条件”，
 * 最终实现“不同用户/角色仅能查询其权限范围内数据”的业务目标。
 *
 * <p>核心工作机制：
 * <ul>
 *   <li>1. 权限控制核心类：依赖 {@link org.bedrock.common.datascope.handler.DataScopeAuthHolder} 管理权限状态与模型；
 *   <li>2. 权限开启：通过 {@code DataScopeAuthHolder.setIsAuth(true)} 启用数据权限拦截；
 *   <li>3. 模型配置：通过 {@code DataScopeAuthHolder.setDataScopeModel(DataScopeModel)} 设置具体数据范围（如部门ID、用户可见范围）；
 *   <li>4. SQL拦截与解析：由 {@link org.bedrock.common.datascope.plugins.DruidDataScopeAuthInterceptor}（Druid过滤器）执行拦截，
 *       读取 {@code DataScopeAuthHolder} 中ThreadLocal存储的权限模型，动态向SQL追加数据范围过滤条件（如 WHERE dept_id IN (1,2,3)）。
 * </ul>
 *
 * <p>关键特性：
 * <ul>
 *   <li>状态一致性：方法执行完毕后，自动恢复至执行前的权限状态（避免线程复用导致的权限残留）；
 *   <li>线程隔离：权限模型通过ThreadLocal存储，仅对当前线程生效，子线程默认不继承（避免权限跨线程扩散，保障数据安全）；
 *   <li>非侵入性：无需修改原始SQL，通过拦截器动态加工，降低业务代码耦合度。
 * </ul>
 *
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataScopeFilter {

	/**
	 * 计算mapperId的值：动态生成MyBatis操作的唯一标识（mapperId），支持通过Spring EL（SpEL）表达式动态解析生成
	 *
	 * <p>核心功能：
	 * 相较于固定字符串或默认mapperId的生成方式，本方法允许通过SpEL表达式动态计算mapperId，
	 * 可基于当前上下文参数（如方法入参、用户信息、请求上下文等）生成灵活的标识，适配复杂业务场景中“动态权限匹配”的需求。
	 *
	 * <p>SpEL支持说明：
	 * 1. 解析环境：依托Spring的SpEL表达式解析器（EvaluationContext），可访问方法参数（通过#{#paramName}引用）、
	 * 2. 表达式示例：
	 *    - 固定前缀+动态参数：#{ 'user:query:' + #deptId }（根据部门ID动态生成，如"user:query:100"）
	 *    - 引用Bean方法：#{ @mapperIdGenerator.generate(#methodName, #params) }（调用自定义Bean生成mapperId）
	 *    - 复杂逻辑：#{ #role == 'ADMIN' ? 'user:all' : 'user:own:' + #userId }（根据角色动态切换规则）
	 *
	 * <p>计算逻辑：
	 * 1. 解析输入的SpEL表达式，结合当前执行上下文（如方法参数、Spring容器Bean）进行求值；
	 * 2. 求值结果自动转换为字符串作为最终mapperId（若表达式结果为null，会触发默认值逻辑，通常为全限定方法名）；
	 * 3. 生成的mapperId将用于数据权限匹配、SQL拦截规则定位等场景。
	 *
	 * <p>注意事项：
	 * - 表达式语法：需遵循SpEL规范，避免使用未定义的变量或Bean（会导致解析异常）；
	 * - 性能影响：复杂表达式可能增加解析耗时，建议缓存高频使用的表达式解析结果；
	 * - 上下文依赖：若表达式依赖方法参数，需确保参数名在编译时保留（如使用-parameters编译参数），否则需通过#{#p0}（索引）引用。
	 */
	String mapperId();

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
