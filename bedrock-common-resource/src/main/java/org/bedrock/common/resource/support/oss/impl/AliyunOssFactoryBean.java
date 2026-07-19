package org.bedrock.common.resource.support.oss.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import org.bedrock.common.resource.enums.OssEnum;
import org.bedrock.common.resource.model.oss.OssProperties;
import org.bedrock.common.resource.service.oss.OssTemplate;
import org.bedrock.common.resource.service.oss.impl.AliyunOssTemplate;
import org.bedrock.common.resource.support.oss.OssFactoryBean;
import org.springframework.lang.NonNull;

/**
 * 阿里云 OSS 工厂
 */
public class AliyunOssFactoryBean implements OssFactoryBean {

	@Override
	@NonNull
	public OssEnum getOssEnum() {
		return OssEnum.ALIYUN;
	}

	@Override
	public OssTemplate createOssTemplate(OssProperties properties) {
		return new AliyunOssTemplate(createOssClient(properties), properties);
	}

	/**
	 * 创建阿里云 OSS 客户端，子类可覆写以做特殊配置
	 */
	protected OSS createOssClient(OssProperties properties) {
		return new OSSClientBuilder().build(
			properties.getEndpoint(),
			properties.getAccessKey(),
			properties.getSecretKey()
		);
	}

}
