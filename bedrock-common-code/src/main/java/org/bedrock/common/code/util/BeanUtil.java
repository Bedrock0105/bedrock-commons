package org.bedrock.common.code.util;

import org.springframework.cglib.beans.BeanMap;

import java.util.Map;

public abstract class BeanUtil extends org.springframework.beans.BeanUtils {

	/**
	 * Bean 转 Map
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, Object> beanToMap(Object bean) {
		return BeanMap.create(bean);
	}

	/**
	 * 复制属性
	 */
	public static <T> T copyProperties(Object source, Class<T> clazz) {
		T instantiateClass = BeanUtil.instantiateClass(clazz);
		BeanUtil.copyProperties(source, instantiateClass);
		return instantiateClass;
	}
}
