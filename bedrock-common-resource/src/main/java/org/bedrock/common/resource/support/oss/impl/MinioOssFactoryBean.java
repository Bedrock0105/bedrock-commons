package org.bedrock.common.resource.support.oss.impl;

import io.minio.MinioClient;
import org.bedrock.common.resource.enums.OssEnum;
import org.bedrock.common.resource.model.oss.OssProperties;
import org.bedrock.common.resource.service.oss.OssTemplate;
import org.bedrock.common.resource.service.oss.impl.MinioOssTemplate;
import org.bedrock.common.resource.support.oss.OssFactoryBean;
import org.springframework.lang.NonNull;

/**
 * MinIO 对象存储工厂
 */
public class MinioOssFactoryBean implements OssFactoryBean {

	@Override
	@NonNull
	public OssEnum getOssEnum() {
		return OssEnum.MINIO;
	}

	@Override
	public OssTemplate createOssTemplate(OssProperties properties) {
		return new MinioOssTemplate(createMinioClient(properties), properties);
	}

	/**
	 * 创建 MinIO 客户端，子类可覆写以做特殊配置
	 */
	protected MinioClient createMinioClient(OssProperties properties) {
		return MinioClient.builder()
			.endpoint(properties.getEndpoint())
			.credentials(properties.getAccessKey(), properties.getSecretKey())
			.build();
	}

}
