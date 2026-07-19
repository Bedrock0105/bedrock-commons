package org.bedrock.common.http.spring.scan;

import lombok.Getter;
import lombok.Setter;
import org.bedrock.common.code.util.ClassUtil;
import org.bedrock.common.http.spring.beans.HttpFactoryBean;
import org.springframework.aop.scope.ScopedProxyFactoryBean;
import org.springframework.aop.scope.ScopedProxyUtils;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.util.Optional;
import java.util.Set;

/**
 * HTTP客户端接口扫描器
 * 继承Spring ClassPathBeanDefinitionScanner，扫描指定包下的HTTP客户端接口，
 * 配置扫描过滤规则并将接口注册为HttpFactoryBean类型的Spring Bean
 */
@Getter
@Setter
public class HttpClientScanner extends ClassPathBeanDefinitionScanner {

	/**
	 * 是否懒加载客户端Bean（首次使用时初始化）
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
	 * 客户端Bean的默认作用域
	 */
	private String defaultScope;

	/**
	 * 创建客户端Bean的工厂类（默认HttpFactoryBean）
	 */
	private Class<? extends HttpFactoryBean> httpFactoryBeanClass = HttpFactoryBean.class;

	/**
	 * 设置HTTP工厂Bean类（兜底默认值）
	 *
	 * @param httpFactoryBeanClass 工厂Bean类，为null时使用默认HttpFactoryBean
	 */
	public void setHttpFactoryBeanClass(Class<? extends HttpFactoryBean> httpFactoryBeanClass) {
		this.httpFactoryBeanClass = httpFactoryBeanClass == null ? HttpFactoryBean.class : httpFactoryBeanClass;
	}

	/**
	 * 构造扫描器
	 *
	 * @param registry Bean定义注册器
	 */
	public HttpClientScanner(BeanDefinitionRegistry registry) {
		// 父类构造：不使用Spring默认的类型过滤器
		super(registry, false);
	}

	/**
	 * 注册扫描过滤规则
	 * 1. 按注解/标记接口设置包含过滤器
	 * 2. 排除package-info.java文件
	 */
	public void registerFilters() {
		boolean acceptAllInterfaces = true;

		// 配置注解过滤规则
		if (this.annotationClass != null) {
			addIncludeFilter(new AnnotationTypeFilter(this.annotationClass));
			acceptAllInterfaces = false;
		}

		// 配置标记接口过滤规则（忽略标记接口本身）
		if (this.markerInterface != null) {
			addIncludeFilter(new AssignableTypeFilter(this.markerInterface) {
				@Override
				protected boolean matchClassName(String className) {
					return false;
				}
			});
			acceptAllInterfaces = false;
		}

		// 无过滤规则时，接受所有接口
		if (acceptAllInterfaces) {
			addIncludeFilter((metadataReader, metadataReaderFactory) -> true);
		}

		// 排除package-info.java
		addExcludeFilter((metadataReader, metadataReaderFactory) -> {
			String className = metadataReader.getClassMetadata().getClassName();
			return className.endsWith("package-info");
		});
	}

	/**
	 * 重写扫描方法：扫描指定包并处理Bean定义
	 *
	 * @param basePackages 扫描的基础包路径
	 * @return 扫描到的Bean定义持有者集合
	 */
	@Override
	public Set<BeanDefinitionHolder> doScan(String... basePackages) {
		Set<BeanDefinitionHolder> beanDefinitions = super.doScan(basePackages);
		if (!beanDefinitions.isEmpty()) {
			// 处理扫描到的客户端接口Bean定义
			processBeanDefinitions(beanDefinitions);
		}
		return beanDefinitions;
	}

	/**
	 * 处理扫描到的Bean定义：配置HTTP工厂Bean、作用域、懒加载等
	 *
	 * @param beanDefinitions 扫描到的Bean定义持有者集合
	 */
	private void processBeanDefinitions(Set<BeanDefinitionHolder> beanDefinitions) {
		AbstractBeanDefinition definition;
		BeanDefinitionRegistry registry = getRegistry();
		for (BeanDefinitionHolder holder : beanDefinitions) {
			definition = (AbstractBeanDefinition) holder.getBeanDefinition();
			boolean scopedProxy = false;

			// 处理作用域代理Bean
			if (ScopedProxyFactoryBean.class.getName().equals(definition.getBeanClassName())) {
				definition = (AbstractBeanDefinition) Optional
					.ofNullable(((RootBeanDefinition) definition).getDecoratedDefinition())
					.map(BeanDefinitionHolder::getBeanDefinition)
					.orElseThrow(() -> new IllegalStateException(
						"The target bean definition of scoped proxy bean not found. Root bean definition[" + holder + "]"));
				scopedProxy = true;
			}

			String beanClassName = definition.getBeanClassName();
			// 向工厂Bean构造参数添加接口类名
			definition.getConstructorArgumentValues().addGenericArgumentValue(beanClassName);
			// 设置工厂Bean的interfaceClass属性
			Class<?> aClass = ClassUtil.forName(beanClassName);
			if (aClass != null) {
				definition.getPropertyValues().add("interfaceClass", aClass);
				// 标记Bean的实际类型为客户端接口
				definition.setAttribute(FactoryBean.OBJECT_TYPE_ATTRIBUTE, aClass);
			}
			// 设置Bean类型为HTTP工厂Bean
			definition.setBeanClass(this.httpFactoryBeanClass);

			boolean explicitFactoryUsed = false;
			// 注入自定义HTTP配置
			if (StringUtils.hasText(this.httpConfigurationBeanName)) {
				definition.getPropertyValues().add("config", new RuntimeBeanReference(this.httpConfigurationBeanName));
				explicitFactoryUsed = true;
			}
			// 未指定配置时，按类型自动注入
			if (!explicitFactoryUsed) {
				definition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
			}

			// 设置懒加载
			definition.setLazyInit(lazyInitialization);
			if (scopedProxy) {
				continue;
			}

			// 设置默认作用域
			if (ConfigurableBeanFactory.SCOPE_SINGLETON.equals(definition.getScope()) && defaultScope != null) {
				definition.setScope(defaultScope);
			}

			// 非单例Bean创建作用域代理
			if (!definition.isSingleton()) {
				BeanDefinitionHolder proxyHolder = ScopedProxyUtils.createScopedProxy(holder, registry, true);
				if (registry.containsBeanDefinition(proxyHolder.getBeanName())) {
					registry.removeBeanDefinition(proxyHolder.getBeanName());
				}
				registry.registerBeanDefinition(proxyHolder.getBeanName(), proxyHolder.getBeanDefinition());
			}
		}
	}

	/**
	 * 重写候选组件判断：仅识别独立的接口（排除内部类/嵌套类）
	 *
	 * @param beanDefinition 注解Bean定义
	 * @return true=是候选组件，false=非候选组件
	 */
	@Override
	protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
		return beanDefinition.getMetadata().isInterface() && beanDefinition.getMetadata().isIndependent();
	}
}
