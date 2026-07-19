package org.bedrock.common.resource.model.sms;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 短信配置模型
 */
@Getter
@Setter
public class SmsProperties implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 短信 API 账号
	 * <p>阿里云：AccessKeyId；腾讯云：SecretId；华为云：AppKey；七牛云：AccessKey；云片：ApiKey</p>
	 */
	private String apiKey;

	/**
	 * 短信 API 密钥
	 * <p>阿里云：AccessKeySecret；腾讯云：SecretKey；华为云：AppSecret；七牛云：SecretKey</p>
	 * <p>云片仅需 apiKey，无需配置此项</p>
	 */
	private String apiSecret;

	/**
	 * 短信签名
	 */
	private String signature;

	/**
	 * 短信模板 ID
	 */
	private String templateId;

	/**
	 * 服务端点（可选，未配置时使用各厂商默认端点）
	 */
	private String endpoint;

	/**
	 * 服务地域（如：cn-hangzhou、ap-guangzhou、cn-north-4）
	 * <p>阿里云、腾讯云、华为云等需要配置</p>
	 */
	private String region;

	/**
	 * 短信应用 AppId
	 * <p>仅腾讯云 TENCENT 使用（SmsSdkAppId）</p>
	 */
	private String appId;

	/**
	 * 短信通道号 / 发送方号码
	 * <p>仅华为云 HUAWEI 使用</p>
	 */
	private String sender;

}
