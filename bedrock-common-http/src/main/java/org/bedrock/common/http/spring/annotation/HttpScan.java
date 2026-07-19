package org.bedrock.common.http.spring.annotation;

import org.bedrock.common.http.spring.beans.HttpFactoryBean;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * HTTP客户端扫描注解
 * 用于扫描指定包下的HTTP客户端接口，自动注册为Spring Bean，支持自定义扫描规则、Bean创建逻辑、作用域等配置
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Repeatable(HttpScans.class)
@Import(ClientScannerRegistrar.class)
public @interface HttpScan {

	/**
	 * 扫描的基础包路径（与basePackages互为别名）
	 *
	 * @return 基础包路径数组，默认空数组（扫描当前注解所在包）
	 */
	@AliasFor("basePackages")
	String[] value() default {};

	/**
	 * 扫描的基础包路径（与value互为别名）
	 *
	 * @return 基础包路径数组，默认空数组（扫描当前注解所在包）
	 */
	@AliasFor("value")
	String[] basePackages() default {};

	/**
	 * 是否懒加载HTTP客户端Bean
	 *
	 * @return true=懒加载（首次使用时初始化），false=立即初始化（默认）
	 */
	boolean lazyInitialization() default false;

	/**
	 * 扫描的注解标记类（仅扫描标注了该注解的接口）
	 *
	 * @return 注解类型，默认Annotation.class（扫描所有标注了注解的接口）
	 */
	Class<? extends Annotation> annotationClass() default Annotation.class;

	/**
	 * 扫描的标记接口（仅扫描实现了该接口的类）
	 *
	 * @return 标记接口类型，默认Class.class（不限制标记接口）
	 */
	Class<?> markerInterface() default Class.class;

	/**
	 * HTTP配置的引用名称（用于关联自定义HTTP配置）
	 *
	 * @return 配置引用名称，默认空字符串（使用全局默认HTTP配置）
	 */
	String httpConfigurationRef() default "";

	/**
	 * 创建HTTP客户端Bean的工厂类
	 *
	 * @return 工厂Bean类，默认HttpFactoryBean.class（使用默认工厂创建Bean）
	 */
	Class<? extends HttpFactoryBean> httpFactoryBeanClass() default HttpFactoryBean.class;

	/**
	 * Bean名称生成器（自定义HTTP客户端Bean的命名规则）
	 *
	 * @return 名称生成器类，默认BeanNameGenerator.class（使用Spring默认命名规则）
	 */
	Class<? extends BeanNameGenerator> nameGenerator() default BeanNameGenerator.class;

	/**
	 * HTTP客户端Bean的默认作用域
	 *
	 * @return 作用域字符串，默认AbstractBeanDefinition.SCOPE_DEFAULT（单例作用域）
	 */
	String defaultScope() default AbstractBeanDefinition.SCOPE_DEFAULT;
}
