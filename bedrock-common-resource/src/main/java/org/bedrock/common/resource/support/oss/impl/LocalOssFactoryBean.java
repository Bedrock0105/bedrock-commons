package org.bedrock.common.resource.support.oss.impl;

import org.bedrock.common.resource.enums.OssEnum;
import org.bedrock.common.resource.model.oss.OssProperties;
import org.bedrock.common.resource.service.oss.OssTemplate;
import org.bedrock.common.resource.service.oss.impl.LocalOssTemplate;
import org.bedrock.common.resource.support.oss.OssFactoryBean;
import org.springframework.lang.NonNull;

public class LocalOssFactoryBean implements OssFactoryBean {

	@Override
	@NonNull
	public OssEnum getOssEnum() {
		return OssEnum.LOCAL;
	}

	@Override
	public OssTemplate createOssTemplate(OssProperties properties) {
		return new LocalOssTemplate(properties);
	}
}
