package org.bedrock.common.log.operation.convert;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.util.Set;

public interface AnalysisClassConvert<T> {

	/**
	 * 获取需要转换的类
	 *
	 * @return
	 */
	Class<T> getAnalysisClass();

	/**
	 * 对象转字符串
	 *
	 * @param obj         对象
	 * @param annotations 字段上面的注解
	 * @return
	 */
	String convert(Object obj, @Nullable Set<Annotation> annotations);

}
