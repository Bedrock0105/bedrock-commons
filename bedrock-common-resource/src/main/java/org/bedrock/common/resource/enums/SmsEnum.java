package org.bedrock.common.resource.enums;

/**
 * 支持的短信服务
 */
public enum SmsEnum {

	/**
	 * 阿里云短信
	 * <p>需要引入依赖：</p>
	 * <pre>{@code
	 * <dependency>
	 *     <groupId>com.aliyun</groupId>
	 *     <artifactId>dysmsapi20170525</artifactId>
	 * </dependency>
	 * }</pre>
	 */
	ALIYUN,

	/**
	 * 腾讯云短信
	 * <p>需要引入依赖：</p>
	 * <pre>{@code
	 * <dependency>
	 *     <groupId>com.tencentcloudapi</groupId>
	 *     <artifactId>tencentcloud-sdk-java-sms</artifactId>
	 * </dependency>
	 * }</pre>
	 */
	TENCENT,

	/**
	 * 云片短信
	 * <p>需要引入依赖：</p>
	 * <pre>{@code
	 * <dependency>
	 *     <groupId>com.yunpian.sdk</groupId>
	 *     <artifactId>yunpian-java-sdk</artifactId>
	 * </dependency>
	 * }</pre>
	 */
	YUNPIAN,

	/**
	 * 华为云短信
	 * <p>需要引入依赖：</p>
	 * <pre>{@code
	 * <dependency>
	 *     <groupId>com.huaweicloud.sdk</groupId>
	 *     <artifactId>huaweicloud-sdk-smsapi</artifactId>
	 * </dependency>
	 * }</pre>
	 */
	HUAWEI,

	/**
	 * 七牛云短信
	 * <p>需要引入依赖：</p>
	 * <pre>{@code
	 * <dependency>
	 *     <groupId>com.qiniu</groupId>
	 *     <artifactId>qiniu-java-sdk</artifactId>
	 * </dependency>
	 * }</pre>
	 */
	QINIU

}
