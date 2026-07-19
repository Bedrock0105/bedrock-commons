package org.bedrock.common.code.util;

import org.springframework.lang.Nullable;

import java.util.Collection;
import java.util.Map;

public abstract class CollectionUtil extends org.springframework.util.CollectionUtils {

	public static boolean isNotEmpty(@Nullable Collection<?> collection) {
		return !isEmpty(collection);
	}

	public static boolean isNotEmpty(@Nullable Map<?, ?> map) {
		return !isEmpty(map);
	}

	public static boolean isEmpty(@Nullable Object[] array) {
		return ObjectUtil.isEmpty(array);
	}

	public static boolean isNotEmpty(@Nullable Object[] array) {
		return ObjectUtil.isNotEmpty(array);
	}
}
