package org.bedrock.common.log.operation.convert;

import org.bedrock.common.log.operation.convert.impl.DateAnalysisClassConvert;
import org.bedrock.common.log.operation.convert.impl.LocalDateAnalysisClassConvert;
import org.bedrock.common.log.operation.convert.impl.LocalDateTimeAnalysisClassConvert;
import org.bedrock.common.log.operation.convert.impl.LocalTimeAnalysisClassConvert;

import java.util.HashMap;
import java.util.Map;

public class AnalysisClassConvertManager {

	/**
	 * 参数转换器
	 */
	private static final Map<Class<?>, AnalysisClassConvert<?>> ANALYSIS_CLASS_CONVERT_MAP = new HashMap<>();

	/**
	 * 获取一个参数转换器
	 *
	 * @param clazz
	 * @param <T>
	 * @return
	 */
	public static <T> AnalysisClassConvert<T> getAnalysisClassConvert(Class<T> clazz) {
		return (AnalysisClassConvert<T>) ANALYSIS_CLASS_CONVERT_MAP.get(clazz);
	}

	/**
	 * 注册一个参数转换器
	 *
	 * @param analysisClassConvert
	 */
	public static void registerAnalysisClassConvert(AnalysisClassConvert<?> analysisClassConvert) {
		ANALYSIS_CLASS_CONVERT_MAP.put(analysisClassConvert.getAnalysisClass(), analysisClassConvert);
	}

	static {
		registerAnalysisClassConvert(new LocalDateTimeAnalysisClassConvert());
		registerAnalysisClassConvert(new LocalTimeAnalysisClassConvert());
		registerAnalysisClassConvert(new LocalDateAnalysisClassConvert());
		registerAnalysisClassConvert(new DateAnalysisClassConvert());
	}
}
