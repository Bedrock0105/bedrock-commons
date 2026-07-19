package org.bedrock.common.log.handler;

import org.bedrock.common.code.util.StringUtil;
import org.slf4j.MDC;

public class LogTraceIdHandler {

	private static final String TRACE_ID = "TRACE_ID";

	public static boolean insertTraceId() {
		MDC.put(TRACE_ID, StringUtil.randomUUID());
		return true;
	}

	/**
	 * 移除TraceId
	 */
	public static void removeTraceId() {
		MDC.remove(TRACE_ID);
	}
}
