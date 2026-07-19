
package org.bedrock.common.http.spring.scan;

import java.lang.annotation.Annotation;

import lombok.Getter;
import lombok.Setter;
import org.bedrock.common.http.spring.beans.HttpFactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.StringUtils;

import org.springframework.beans.BeansException;
import org.springframework.util.Assert;

/**
 * HTTP客户端扫描配置器
 * 实现Spring后置处理器接口，用于配置并启动HttpClientScanner，完成指定包下HTTP客户端接口的Bean定义注册
 */
@Getter
@Setter
public class HttpScannerConfigurer
	implements BeanDefinitionRegistryPostProcessor, InitializingBean, ApplicationContextAware {

	/**
	 * 扫描的基础包路径（多个包用分隔符分隔）
	 */
	private String basePackage;

	/**
	 * 客户端Bean是否懒加载（首次使用时初始化）
	 */
	private boolean lazyInitialization;

	/**
	 * 扫描过滤：仅标注该注解的接口会被扫描
	 */
	private Class<? extends Annotation> annotationClass;

	/**
	 * 扫描过滤：仅实现该标记接口的类会被扫描
	 */
	private Class<?> markerInterface;

	/**
	 * HTTP配置Bean的名称（用于注入自定义HTTP配置）
	 */
	private String httpConfigurationBeanName;

	/**
	 * 创建客户端Bean的工厂类
	 */
	private Class<? extends HttpFactoryBean> httpFactoryBeanClass;

	/**
	 * Spring应用上下文
	 */
	private ApplicationContext applicationContext;

	/**
	 * Bean名称生成器（自定义客户端Bean命名规则）
	 */
	private BeanNameGenerator nameGenerator;

	/**
	 * 客户端Bean的默认作用域
	 */
	private String defaultScope;

	/**
	 * 注入Spring应用上下文
	 *
	 * @param applicationContext 应用上下文实例
	 * @throws BeansException 上下文注入异常
	 */
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	/**
	 * 初始化后校验：确保基础包路径非空
	 *
	 * @throws Exception 校验失败抛出异常
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(this.basePackage, "Property 'basePackage' is required");
	}

	/**
	 * 后置处理Bean定义注册器：创建并配置扫描器，执行HTTP客户端接口扫描
	 *
	 * @param registry Bean定义注册器
	 */
	@Override
	public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) {
		// 创建HTTP客户端扫描器
		HttpClientScanner scanner = new HttpClientScanner(registry);
		// 配置扫描器核心属性
		scanner.setAnnotationClass(this.annotationClass);
		scanner.setMarkerInterface(this.markerInterface);
		scanner.setHttpConfigurationBeanName(this.httpConfigurationBeanName);
		scanner.setResourceLoader(this.applicationContext);
		scanner.setBeanNameGenerator(this.nameGenerator);
		scanner.setHttpFactoryBeanClass(this.httpFactoryBeanClass);
		scanner.setLazyInitialization(lazyInitialization);
		// 配置默认作用域
		if (StringUtils.hasText(defaultScope)) {
			scanner.setDefaultScope(defaultScope);
		}
		// 注册扫描过滤规则
		scanner.registerFilters();
		// 执行扫描（解析多包路径分隔符）
		scanner.scan(
			StringUtils.tokenizeToStringArray(this.basePackage, ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS));
	}

}
