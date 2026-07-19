package org.bedrock.common.http.spring.annotation;

import org.bedrock.common.http.spring.beans.HttpFactoryBean;
import org.bedrock.common.http.spring.scan.HttpScannerConfigurer;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * HTTP客户端扫描注册器
 * 实现ImportBeanDefinitionRegistrar，解析@HttpScan注解配置，注册HttpScannerConfigurer Bean，
 * 最终完成指定包下HTTP客户端接口的扫描与Bean注册
 */
public class ClientScannerRegistrar implements ImportBeanDefinitionRegistrar {

	@Override
	public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
		// 解析当前类上的@HttpScan注解属性
		AnnotationAttributes httpScan = AnnotationAttributes
			.fromMap(importingClassMetadata.getAnnotationAttributes(HttpScan.class.getName()));
		if (httpScan == null) {
			return;
		}
		// 生成Bean名称并执行核心注册逻辑
		String beanName = generateBaseBeanName(importingClassMetadata, 0);
		registerBeanDefinitions(importingClassMetadata, httpScan, registry, beanName);
	}

	/**
	 * 注册HttpScannerConfigurer Bean定义（核心逻辑）
	 *
	 * @param annoMeta  注解元数据（@HttpScan所在类的元数据）
	 * @param annoAttrs @HttpScan注解的属性集合
	 * @param registry  Bean定义注册器
	 * @param beanName  要注册的Bean名称
	 */
	void registerBeanDefinitions(AnnotationMetadata annoMeta, AnnotationAttributes annoAttrs,
								 BeanDefinitionRegistry registry, String beanName) {
		// 构建HttpScannerConfigurer Bean定义构建器
		BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(HttpScannerConfigurer.class);

		// 设置扫描过滤：注解类型
		Class<? extends Annotation> annotationClass = annoAttrs.getClass("annotationClass");
		if (!Annotation.class.equals(annotationClass)) {
			builder.addPropertyValue("annotationClass", annotationClass);
		}

		// 设置扫描过滤：标记接口
		Class<?> markerInterface = annoAttrs.getClass("markerInterface");
		if (!Class.class.equals(markerInterface)) {
			builder.addPropertyValue("markerInterface", markerInterface);
		}

		// 设置Bean名称生成器
		Class<? extends BeanNameGenerator> generatorClass = annoAttrs.getClass("nameGenerator");
		if (!BeanNameGenerator.class.equals(generatorClass)) {
			builder.addPropertyValue("nameGenerator", BeanUtils.instantiateClass(generatorClass));
		}

		// 设置HTTP客户端工厂Bean类
		Class<? extends HttpFactoryBean> httpFactoryBeanClass = annoAttrs.getClass("httpFactoryBeanClass");
		if (!HttpFactoryBean.class.equals(httpFactoryBeanClass)) {
			builder.addPropertyValue("httpFactoryBeanClass", httpFactoryBeanClass);
		}

		// 设置HTTP配置Bean引用名称
		String httpConfigurationRef = annoAttrs.getString("httpConfigurationRef");
		if (StringUtils.hasText(httpConfigurationRef)) {
			builder.addPropertyValue("httpConfigurationBeanName", httpConfigurationRef);
		}

		// 处理扫描基础包：为空则使用注解所在类的包作为默认值
		List<String> basePackages = new ArrayList<>(Arrays.stream(annoAttrs.getStringArray("basePackages"))
			.filter(StringUtils::hasText).toList());
		if (basePackages.isEmpty()) {
			basePackages.add(getDefaultBasePackage(annoMeta));
		}

		// 设置懒加载配置
		boolean lazyInitialization = annoAttrs.getBoolean("lazyInitialization");
		builder.addPropertyValue("lazyInitialization", lazyInitialization);

		// 设置Bean默认作用域
		String defaultScope = annoAttrs.getString("defaultScope");
		if (!AbstractBeanDefinition.SCOPE_DEFAULT.equals(defaultScope)) {
			builder.addPropertyValue("defaultScope", defaultScope);
		}

		// 设置扫描基础包（拼接为逗号分隔的字符串）
		builder.addPropertyValue("basePackage", StringUtils.collectionToCommaDelimitedString(basePackages));
		// 标记为基础设施Bean（适配spring-native）
		builder.setRole(AbstractBeanDefinition.ROLE_INFRASTRUCTURE);

		// 注册HttpScannerConfigurer Bean定义到容器
		registry.registerBeanDefinition(beanName, builder.getBeanDefinition());
	}

	/**
	 * 生成HttpScannerConfigurer Bean的基础名称
	 *
	 * @param importingClassMetadata 注解所在类的元数据
	 * @param index                  索引（适配多@HttpScan注解场景）
	 * @return Bean唯一名称
	 */
	private static String generateBaseBeanName(AnnotationMetadata importingClassMetadata, int index) {
		return importingClassMetadata.getClassName() + "#" + ClientScannerRegistrar.class.getSimpleName() + "#" + index;
	}

	/**
	 * 获取默认扫描基础包（@HttpScan注解所在类的包路径）
	 *
	 * @param importingClassMetadata 注解所在类的元数据
	 * @return 默认基础包路径
	 */
	private static String getDefaultBasePackage(AnnotationMetadata importingClassMetadata) {
		return ClassUtils.getPackageName(importingClassMetadata.getClassName());
	}

	/**
	 * 重复注解（@HttpScans）扫描注册器
	 * 处理多个@HttpScan注解的场景，逐个解析并注册HttpScannerConfigurer Bean
	 */
	static class RepeatingRegistrar extends ClientScannerRegistrar {

		@Override
		public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
			// 解析@HttpScans注解（包裹多个@HttpScan注解）
			AnnotationAttributes mapperScansAttrs = AnnotationAttributes
				.fromMap(importingClassMetadata.getAnnotationAttributes(HttpScans.class.getName()));
			if (mapperScansAttrs != null) {
				// 遍历每个@HttpScan注解，逐个生成Bean名称并注册
				AnnotationAttributes[] annotations = mapperScansAttrs.getAnnotationArray("value");
				for (int i = 0; i < annotations.length; i++) {
					registerBeanDefinitions(importingClassMetadata, annotations[i], registry,
						generateBaseBeanName(importingClassMetadata, i));
				}
			}
		}
	}
}
