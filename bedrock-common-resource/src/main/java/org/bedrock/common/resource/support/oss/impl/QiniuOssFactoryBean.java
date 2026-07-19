package org.bedrock.common.resource.support.oss.impl;

import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.util.Auth;
import org.bedrock.common.code.util.StringUtil;
import org.bedrock.common.resource.enums.OssEnum;
import org.bedrock.common.resource.model.oss.OssProperties;
import org.bedrock.common.resource.service.oss.OssTemplate;
import org.bedrock.common.resource.service.oss.impl.QiniuOssTemplate;
import org.bedrock.common.resource.support.oss.OssFactoryBean;
import org.springframework.lang.NonNull;

/**
 * 七牛云 Kodo 对象存储工厂
 */
public class QiniuOssFactoryBean implements OssFactoryBean {

	@Override
	@NonNull
	public OssEnum getOssEnum() {
		return OssEnum.QINIU;
	}

	@Override
	public OssTemplate createOssTemplate(OssProperties properties) {
		return new QiniuOssTemplate(createAuth(properties), createConfiguration(properties), properties);
	}

	/**
	 * 创建七牛云 Auth，子类可覆写以做特殊配置
	 */
	protected Auth createAuth(OssProperties properties) {
		return Auth.create(properties.getAccessKey(), properties.getSecretKey());
	}

	/**
	 * 创建七牛云 Configuration，子类可覆写以做特殊配置
	 */
	protected Configuration createConfiguration(OssProperties properties) {
		return new Configuration(resolveRegion(properties));
	}

	/**
	 * 根据配置解析七牛云存储区域，未配置时自动探测
	 */
	protected Region resolveRegion(OssProperties properties) {
		if (StringUtil.isBlank(properties.getBucketRegion())) {
			return Region.autoRegion();
		}
		if (StringUtil.isBlank(properties.getBucketRegion())){
			return Region.autoRegion();
		}
		return Region.createWithRegionId(properties.getBucketRegion());
	}

}
