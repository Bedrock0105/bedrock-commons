package org.bedrock.common.log.service;

import org.bedrock.common.log.model.LogOperation;

/**
 * 保存日志
 */
public interface ILogSaveService {

	/**
	 * 保存操作日志
	 */
	void saveOperationLog(LogOperation logOperation);
}
