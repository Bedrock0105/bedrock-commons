package org.bedrock.common.code.util;

import org.springframework.lang.Nullable;

public abstract class ObjectUtil extends org.springframework.util.ObjectUtils {

	/**
	 * 判断对象是否不是空
	 */
	public static boolean isNotEmpty(@Nullable Object obj) {
		return !isEmpty(obj);
	}
}
