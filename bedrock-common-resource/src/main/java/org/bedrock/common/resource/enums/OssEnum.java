package org.bedrock.common.resource.enums;

/**
 * 支持的OSS服务
 */
public enum OssEnum {

	/**
	 * 本地磁盘存储
	 */
	LOCAL,

	/**
	 * MinIO 对象存储
	 * <p>需要引入依赖：</p>
	 * <pre>{@code
	 * <dependency>
	 *     <groupId>io.minio</groupId>
	 *     <artifactId>minio</artifactId>
	 * </dependency>
	 * }</pre>
	 */
	MINIO,

	/**
	 * Amazon S3（标准 S3 协议）
	 * <p>需要引入依赖：</p>
	 * <pre>{@code
	 * <dependency>
	 *     <groupId>software.amazon.awssdk</groupId>
	 *     <artifactId>s3</artifactId>
	 * </dependency>
	 * }</pre>
	 */
	AWS,

	/**
	 * 阿里云 OSS
	 * <p>需要引入依赖：</p>
	 * <pre>{@code
	 * <dependency>
	 *     <groupId>com.aliyun.oss</groupId>
	 *     <artifactId>aliyun-sdk-oss</artifactId>
	 * </dependency>
	 * }</pre>
	 */
	ALIYUN,

	/**
	 * 腾讯云 COS
	 * <p>需要引入依赖：</p>
	 * <pre>{@code
	 * <dependency>
	 *     <groupId>com.qcloud</groupId>
	 *     <artifactId>cos_api</artifactId>
	 * </dependency>
	 * }</pre>
	 */
	TENCENT,

	/**
	 * 七牛云 Kodo
	 * <p>需要引入依赖：</p>
	 * <pre>{@code
	 * <dependency>
	 *     <groupId>com.qiniu</groupId>
	 *     <artifactId>qiniu-java-sdk</artifactId>
	 * </dependency>
	 * }</pre>
	 */
	QINIU,

	/**
	 * 华为云 OBS
	 * <p>需要引入依赖：</p>
	 * <pre>{@code
	 * <dependency>
	 *     <groupId>com.huaweicloud</groupId>
	 *     <artifactId>esdk-obs-java</artifactId>
	 * </dependency>
	 * }</pre>
	 */
	HUAWEI,
	/**
	 * Amazon S3（标准 S3 协议）
	 * <p>需要引入依赖：</p>
	 * <pre>{@code
	 * <dependency>
	 *     <groupId>software.amazon.awssdk</groupId>
	 *     <artifactId>s3</artifactId>
	 * </dependency>
	 * }</pre>
	 */
	RUSTFS

}
