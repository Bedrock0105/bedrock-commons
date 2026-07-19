package org.bedrock.common.log.operation.convert.impl;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.bedrock.common.log.operation.convert.AnalysisClassConvert;
import org.bedrock.common.code.util.CollectionUtil;
import org.bedrock.common.code.util.DateUtil;
import org.bedrock.common.code.util.ObjectUtil;

import java.lang.annotation.Annotation;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class LocalDateTimeAnalysisClassConvert implements AnalysisClassConvert<LocalDateTime> {

	private final Map<String, DateTimeFormatter> map = new HashMap<>();

	@Override
	public Class<LocalDateTime> getAnalysisClass() {
		return LocalDateTime.class;
	}

	@Override
	public String convert(Object obj, Set<Annotation> annotations) {
		if (ObjectUtil.isEmpty(obj)) {
			return null;
		}
		String dataTimeFormat = DateUtil.PATTERN_DATETIME;
		if (CollectionUtil.isNotEmpty(annotations)) {
			for (Annotation annotation : annotations) {
				if (annotation instanceof JsonFormat) {
					dataTimeFormat = ((JsonFormat) annotation).pattern();
					break;
				}
			}
		}
		return ((LocalDateTime) obj).format(map.computeIfAbsent(dataTimeFormat, DateTimeFormatter::ofPattern));

	}

}
