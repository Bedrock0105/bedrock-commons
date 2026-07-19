package org.bedrock.common.resource.config;

import lombok.extern.slf4j.Slf4j;
import org.bedrock.common.resource.factory.OssBeanFactory;
import org.bedrock.common.resource.support.oss.OssFactoryBean;
import org.bedrock.common.resource.support.oss.impl.AliyunOssFactoryBean;
import org.bedrock.common.resource.support.oss.impl.HuaweiObsOssFactoryBean;
import org.bedrock.common.resource.support.oss.impl.LocalOssFactoryBean;
import org.bedrock.common.resource.support.oss.impl.MinioOssFactoryBean;
import org.bedrock.common.resource.support.oss.impl.QiniuOssFactoryBean;
import org.bedrock.common.resource.support.oss.impl.S3OssFactoryBean;
import org.bedrock.common.resource.support.oss.impl.TencentCosOssFactoryBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import java.util.List;

@Slf4j
@AutoConfiguration
public class OssConfiguration {

	/**
	 * 创建本地OSS工厂
	 */
	@Bean(name = "localOssFactoryBean")
	@ConditionalOnMissingBean(name = "localOssFactoryBean")
	public OssFactoryBean localOssFactoryBean() {
		return new LocalOssFactoryBean();
	}

	/**
	 * 创建 MinIO OSS 工厂
	 */
	@Bean(name = "minioOssFactoryBean")
	@ConditionalOnMissingBean(name = "minioOssFactoryBean")
	@ConditionalOnClass(name = "io.minio.MinioClient")
	public OssFactoryBean minioOssFactoryBean() {
		return new MinioOssFactoryBean();
	}

	/**
	 * 创建 AWS S3 工厂
	 */
	@Bean(name = "s3OssFactoryBean")
	@ConditionalOnMissingBean(name = "s3OssFactoryBean")
	@ConditionalOnClass(name = "software.amazon.awssdk.services.s3.S3Client")
	public OssFactoryBean s3OssFactoryBean() {
		return new S3OssFactoryBean();
	}

	/**
	 * 创建阿里云 OSS 工厂
	 */
	@Bean(name = "aliyunOssFactoryBean")
	@ConditionalOnMissingBean(name = "aliyunOssFactoryBean")
	@ConditionalOnClass(name = "com.aliyun.oss.OSS")
	public OssFactoryBean aliyunOssFactoryBean() {
		return new AliyunOssFactoryBean();
	}

	/**
	 * 创建腾讯云 COS 工厂
	 */
	@Bean(name = "tencentCosOssFactoryBean")
	@ConditionalOnMissingBean(name = "tencentCosOssFactoryBean")
	@ConditionalOnClass(name = "com.qcloud.cos.COSClient")
	public OssFactoryBean tencentCosOssFactoryBean() {
		return new TencentCosOssFactoryBean();
	}

	/**
	 * 创建七牛云 Kodo 工厂
	 */
	@Bean(name = "qiniuOssFactoryBean")
	@ConditionalOnMissingBean(name = "qiniuOssFactoryBean")
	@ConditionalOnClass(name = "com.qiniu.storage.UploadManager")
	public OssFactoryBean qiniuOssFactoryBean() {
		return new QiniuOssFactoryBean();
	}

	/**
	 * 创建华为云 OBS 工厂
	 */
	@Bean(name = "huaweiObsOssFactoryBean")
	@ConditionalOnMissingBean(name = "huaweiObsOssFactoryBean")
	@ConditionalOnClass(name = "com.obs.services.ObsClient")
	public OssFactoryBean huaweiObsOssFactoryBean() {
		return new HuaweiObsOssFactoryBean();
	}

	@Bean(name = "ossBeanFactory")
	@ConditionalOnMissingBean(name = "ossBeanFactory")
	public OssBeanFactory ossBeanFactory(ObjectProvider<List<OssFactoryBean>> ossFactoryBeans) {
		return new OssBeanFactory(ossFactoryBeans);
	}
}
