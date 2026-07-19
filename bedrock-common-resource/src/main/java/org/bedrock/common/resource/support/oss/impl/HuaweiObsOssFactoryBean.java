package org.bedrock.common.resource.support.oss.impl;

import com.obs.services.ObsClient;
import org.bedrock.common.resource.enums.OssEnum;
import org.bedrock.common.resource.model.oss.OssProperties;
import org.bedrock.common.resource.service.oss.OssTemplate;
import org.bedrock.common.resource.service.oss.impl.HuaweiObsOssTemplate;
import org.bedrock.common.resource.support.oss.OssFactoryBean;
import org.springframework.lang.NonNull;

/**
 * 华为云 OBS 工厂
 */
public class HuaweiObsOssFactoryBean implements OssFactoryBean {

	@Override
	@NonNull
	public OssEnum getOssEnum() {
		return OssEnum.HUAWEI;
	}

	@Override
	public OssTemplate createOssTemplate(OssProperties properties) {
		return new HuaweiObsOssTemplate(createObsClient(properties), properties);
	}

	/**
	 * 创建华为云 OBS 客户端，子类可覆写以做特殊配置
	 */
	protected ObsClient createObsClient(OssProperties properties) {
		return new ObsClient(
			properties.getAccessKey(),
			properties.getSecretKey(),
			properties.getEndpoint()
		);
	}

}
