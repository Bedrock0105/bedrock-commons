package org.bedrock.common.log.service.impl;

import lombok.RequiredArgsConstructor;
import org.bedrock.common.log.feign.ILogSaveFeign;
import org.bedrock.common.log.model.LogOperation;
import org.bedrock.common.log.service.ILogSaveService;
import org.springframework.scheduling.annotation.Async;

@RequiredArgsConstructor
public class DefaultLogSaveServiceImpl implements ILogSaveService {

	private final ILogSaveFeign ilogSaveFeign;
	@Async
	@Override
	public void saveOperationLog(LogOperation logOperation) {
		ilogSaveFeign.save(logOperation);
	}
}
