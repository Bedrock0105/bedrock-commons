package org.bedrock.common.http.spring.beans;

import org.bedrock.common.http.HttpConfiguration;
import org.springframework.beans.factory.FactoryBean;

/**
 * HTTP客户端工厂Bean
 * 实现Spring FactoryBean接口，用于创建指定HTTP客户端接口的实例
 */
public class HttpFactoryBean implements FactoryBean<Object> {

	/**
	 * HTTP客户端接口类
	 */
	private Class<?> interfaceClass;

	/**
	 * HTTP配置对象
	 */
	private HttpConfiguration config;

	/**
	 * 默认构造方法
	 */
	public HttpFactoryBean() {
	}

	/**
	 * 指定客户端接口类的构造方法
	 *
	 * @param interfaceClass HTTP客户端接口类
	 */
	public HttpFactoryBean(Class<?> interfaceClass) {
		this.interfaceClass = interfaceClass;
	}

	/**
	 * 创建并返回HTTP客户端接口实例
	 *
	 * @return 客户端接口实例
	 * @throws Exception 创建实例异常
	 */
	@Override
	public Object getObject() throws Exception {
		return config.getClient(interfaceClass);
	}

	/**
	 * 返回创建的Bean类型（客户端接口类）
	 *
	 * @return 客户端接口Class对象
	 */
	@Override
	public Class<?> getObjectType() {
		return interfaceClass;
	}

	/**
	 * 设置HTTP客户端接口类
	 *
	 * @param interfaceClass 客户端接口类
	 */
	public void setInterfaceClass(Class<?> interfaceClass) {
		this.interfaceClass = interfaceClass;
	}

	/**
	 * 设置HTTP配置对象
	 *
	 * @param config HTTP配置对象
	 */
	public void setConfig(HttpConfiguration config) {
		this.config = config;
	}
}
