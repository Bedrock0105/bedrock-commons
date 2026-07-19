package org.bedrock.common.log.operation.convert.impl;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.bedrock.common.log.operation.convert.AnalysisClassConvert;
import org.bedrock.common.code.support.PoolSupport;
import org.bedrock.common.code.util.CollectionUtil;
import org.bedrock.common.code.util.DateUtil;
import org.bedrock.common.code.util.ObjectUtil;

import java.lang.annotation.Annotation;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DateAnalysisClassConvert implements AnalysisClassConvert<Date> {

	private final Map<String, PoolSupport<SimpleDateFormat>> map = new HashMap<>();

	@Override
	public Class<Date> getAnalysisClass() {
		return Date.class;
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
		PoolSupport<SimpleDateFormat> suppers = map.computeIfAbsent(dataTimeFormat,
			format -> PoolSupport.builder(() -> new SimpleDateFormat(format)).threadSafe(true)
				.maximumCapacity(10).build()
		);
		return suppers.run(format -> format.format((Date) obj));
	}

}
