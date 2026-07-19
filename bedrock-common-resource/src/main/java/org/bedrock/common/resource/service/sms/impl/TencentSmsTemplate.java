package org.bedrock.common.resource.service.sms.impl;

import com.tencentcloudapi.sms.v20210111.SmsClient;
import com.tencentcloudapi.sms.v20210111.models.SendSmsRequest;
import com.tencentcloudapi.sms.v20210111.models.SendSmsResponse;
import com.tencentcloudapi.sms.v20210111.models.SendStatus;
import lombok.extern.slf4j.Slf4j;
import org.bedrock.common.resource.model.sms.SmsProperties;
import org.bedrock.common.resource.model.sms.SmsResponse;

import java.util.Collection;
import java.util.Map;

/**
 * 腾讯云短信实现
 * <p>
 * 依赖：com.tencentcloudapi:tencentcloud-sdk-java-sms
 * </p>
 * <p>
 * 配置示例：
 * <pre>
 * apiKey: your-secret-id
 * apiSecret: your-secret-key
 * appId: 1400000000
 * signature: 您的签名
 * templateId: 123456
 * region: ap-guangzhou
 * </pre>
 * </p>
 * <p>注意：appId（SmsSdkAppId）和 region 为必填项</p>
 */
@Slf4j
public class TencentSmsTemplate extends AbstractSmsTemplate {

	private final SmsClient smsClient;

	/**
	 * 通过工厂注入腾讯云短信客户端
	 */
	public TencentSmsTemplate(SmsClient smsClient, SmsProperties smsProperties) {
		super(smsProperties);
		this.smsClient = smsClient;
	}

	@Override
	public SmsResponse send(Map<String, Object> params, Collection<String> phones) {
		try {
			SendSmsRequest request = new SendSmsRequest();
			request.setSmsSdkAppId(smsProperties.getAppId());
			request.setSignName(smsProperties.getSignature());
			request.setTemplateId(smsProperties.getTemplateId());
			request.setPhoneNumberSet(phones.toArray(new String[0]));
			if (params != null && !params.isEmpty()) {
				request.setTemplateParamSet(params.values().stream().map(String::valueOf).toArray(String[]::new));
			}
			SendSmsResponse response = smsClient.SendSms(request);
			SendStatus[] statusSet = response.getSendStatusSet();
			if (statusSet != null && statusSet.length > 0) {
				SendStatus status = statusSet[0];
				if ("Ok".equalsIgnoreCase(status.getCode())) {
					return success(status.getMessage());
				}
				log.error("腾讯云短信发送失败：{}", status.getMessage());
				return fail(500, status.getMessage());
			}
			return fail(500, "发送失败");
		} catch (Exception e) {
			log.error("腾讯云短信发送异常：{}", e.getMessage(), e);
			return fail(500, e.getMessage());
		}
	}

}
