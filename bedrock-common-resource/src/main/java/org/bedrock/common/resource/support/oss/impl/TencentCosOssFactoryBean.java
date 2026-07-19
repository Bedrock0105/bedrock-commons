package org.bedrock.common.resource.support.oss.impl;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.region.Region;
import org.bedrock.common.resource.enums.OssEnum;
import org.bedrock.common.resource.model.oss.OssProperties;
import org.bedrock.common.resource.service.oss.OssTemplate;
import org.bedrock.common.resource.service.oss.impl.TencentCosOssTemplate;
import org.bedrock.common.resource.support.oss.OssFactoryBean;
import org.springframework.lang.NonNull;

/**
 * 腾讯云 COS 工厂
 */
public class TencentCosOssFactoryBean implements OssFactoryBean {

	@Override
	@NonNull
	public OssEnum getOssEnum() {
		return OssEnum.TENCENT;
	}

	@Override
	public OssTemplate createOssTemplate(OssProperties properties) {
		return new TencentCosOssTemplate(createCosClient(properties), properties);
	}

	/**
	 * 创建腾讯云 COS 客户端，子类可覆写以做特殊配置
	 */
	protected COSClient createCosClient(OssProperties properties) {
		COSCredentials credentials = new BasicCOSCredentials(properties.getAccessKey(), properties.getSecretKey());
		ClientConfig clientConfig = new ClientConfig(new Region(properties.getBucketRegion()));
		return new COSClient(credentials, clientConfig);
	}

}
