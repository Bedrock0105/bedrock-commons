package org.bedrock.common.log.feign;

import org.bedrock.common.log.model.LogOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "bedrock-log")
public interface ILogSaveFeign {

	String SAVE_OPERATION_LOG_URL = "/feign/log/save-operation";

	@PostMapping(SAVE_OPERATION_LOG_URL)
	void save(@RequestBody LogOperation logOperation);

}
