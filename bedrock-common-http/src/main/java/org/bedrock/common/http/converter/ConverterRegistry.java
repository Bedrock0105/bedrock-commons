package org.bedrock.common.http.converter;

import lombok.Getter;
import org.bedrock.common.code.util.SpringUtil;
import org.bedrock.common.http.converter.impl.DefaultConverter;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.ObjectProvider;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 参数转换器注册中心
 * 管理系统默认转换器列表，支持SPI自动加载和手动维护，保证线程安全
 */
@Getter
public class ConverterRegistry {

	/**
	 * 系统默认参数转换器集合，线程安全
	 */
	private Converter defaultConverter = new DefaultConverter();

	/**
	 * 转换器类型与实例的映射表，并发安全，用于快速获取指定类型转换器
	 */
	private final Map<Class<? extends Converter>, Converter> converterMap = new ConcurrentHashMap<>();

	/**
	 * 手动设置默认转换器线程安全
	 */
	public void setDefaultConverters(Converter converter) {
		this.defaultConverter = converter;
	}

	/**
	 * 获取指定类型的转换器实例，不存在则创建
	 *
	 * @param clazz 转换器类型
	 * @return 对应类型的转换器实例
	 */
	public Converter getConverter(Class<? extends Converter> clazz) {
		return converterMap.computeIfAbsent(clazz, this::createConverter);
	}

	/**
	 * 创建转换器实例，优先从Spring容器获取，兜底反射实例化
	 *
	 * @param clazz 转换器类型
	 * @return 转换器实例
	 */
	private Converter createConverter(Class<? extends Converter> clazz) {
		if (SpringUtil.getApplicationContext() != null) {
			ObjectProvider<? extends Converter> beanProvider = SpringUtil.getApplicationContext().getBeanProvider(clazz);
			Converter springBean = beanProvider.getIfAvailable();
			if (springBean != null) {
				return springBean;
			}
		}
		return BeanUtils.instantiateClass(clazz);
	}
}
