package org.bedrock.common.code.util;

import org.springframework.util.ClassUtils;

public abstract class ClassUtil extends org.springframework.util.ClassUtils {

	/**
	 * 根据类名获取Class
	 * 获取不到返回空
	 */
	public static Class<?> forName(String className) {
		try {
			return ClassUtils.forName(className, getDefaultClassLoader());
		} catch (Exception e) {
			return null;
		}
	}

}
