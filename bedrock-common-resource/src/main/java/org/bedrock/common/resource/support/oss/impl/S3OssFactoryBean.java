package org.bedrock.common.resource.support.oss.impl;

import org.bedrock.common.code.util.StringUtil;
import org.bedrock.common.resource.enums.OssEnum;
import org.bedrock.common.resource.model.oss.OssProperties;
import org.bedrock.common.resource.service.oss.OssTemplate;
import org.bedrock.common.resource.service.oss.impl.S3OssTemplate;
import org.bedrock.common.resource.support.oss.OssFactoryBean;
import org.springframework.lang.NonNull;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;

import java.net.URI;

/**
 * Amazon S3 工厂（标准 S3 协议）
 */
public class S3OssFactoryBean implements OssFactoryBean {

	@Override
	@NonNull
	public OssEnum getOssEnum() {
		return OssEnum.AWS;
	}

	@Override
	public OssTemplate createOssTemplate(OssProperties properties) {
		return new S3OssTemplate(createS3Client(properties), properties);
	}

	/**
	 * 创建 AWS S3 客户端，子类可覆写以做特殊配置
	 * <p>默认使用 VirtualHost 风格访问，可通过 endpoint 覆盖端点（如 LocalStack）</p>
	 */
	protected S3Client createS3Client(OssProperties properties) {
		AwsBasicCredentials credentials = AwsBasicCredentials.create(
			properties.getAccessKey(),
			properties.getSecretKey()
		);
		S3ClientBuilder builder = S3Client.builder()
			.region(Region.of(properties.getBucketRegion()))
			.credentialsProvider(StaticCredentialsProvider.create(credentials));
		if (StringUtil.isNotBlank(properties.getEndpoint())) {
			builder.endpointOverride(URI.create(properties.getEndpoint()));
		}
		return builder.build();
	}

}
