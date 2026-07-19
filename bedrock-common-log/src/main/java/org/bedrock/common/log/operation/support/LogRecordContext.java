package org.bedrock.common.log.operation.support;


import org.bedrock.common.code.util.ThreadLocalUtil;

import java.util.HashMap;
import java.util.Map;

public class LogRecordContext {

	private static final String LOG_RECORD_CONTEXT_KEY = "logOperationRecordContext";

	private LogRecordContext() {
		throw new IllegalStateException("Utility class");
	}

	public static void putVariable(String name, Object value) {
		Map<String, Object> param = ThreadLocalUtil.get(LOG_RECORD_CONTEXT_KEY);
		if (param == null) {
			param = new HashMap<>();
		}
		param.put(name, value);
		ThreadLocalUtil.set(LOG_RECORD_CONTEXT_KEY, param);

	}

	public static Object getVariable(String key) {
		Map<String, Object> variableMap = ThreadLocalUtil.get(LOG_RECORD_CONTEXT_KEY);
		return variableMap == null ? null : variableMap.get(key);
	}

	public static Map<String, Object> getVariables() {
		return ThreadLocalUtil.get(LOG_RECORD_CONTEXT_KEY);
	}

	public static void clear() {
		ThreadLocalUtil.remove(LOG_RECORD_CONTEXT_KEY);
	}
}
