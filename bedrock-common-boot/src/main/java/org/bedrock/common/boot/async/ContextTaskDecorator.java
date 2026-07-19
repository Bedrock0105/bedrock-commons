package org.bedrock.common.boot.async;

import org.bedrock.common.code.util.ThreadLocalUtil;
import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;

import java.util.Map;

public class ContextTaskDecorator implements TaskDecorator {

	@Override
	public Runnable decorate(Runnable runnable) {
		Map<String, String> contextMap = MDC.getCopyOfContextMap();
		Map<String, Object> all = ThreadLocalUtil.get();
		return () -> {
			try {
				if (contextMap != null) {
					MDC.setContextMap(contextMap);
				}
				ThreadLocalUtil.getIgnoredThreadLocalKeysForTransfer().forEach(all::remove);
				ThreadLocalUtil.set(all);
				runnable.run();
			} finally {
				MDC.clear();
				ThreadLocalUtil.clear();
			}
		};
	}
}
