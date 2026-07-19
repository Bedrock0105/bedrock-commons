package org.bedrock.common.resource.service.sms.impl;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.aliyun.dysmsapi20170525.models.SendSmsResponseBody;
import lombok.extern.slf4j.Slf4j;
import org.bedrock.common.resource.model.sms.SmsProperties;
import org.bedrock.common.resource.model.sms.SmsResponse;

import java.util.Collection;
import java.util.Map;

/**
 * 阿里云短信实现
 * <p>
 * 依赖：com.aliyun:dysmsapi20170525
 * </p>
 * <p>
 * 配置示例：
 * <pre>
 * apiKey: your-access-key-id
 * apiSecret: your-access-key-secret
 * signature: 您的签名
 * templateId: SMS_123456789
 * region: cn-hangzhou
 * </pre>
 * </p>
 */
@Slf4j
public class AliyunSmsTemplate extends AbstractSmsTemplate {

	private final Client client;

	/**
	 * 通过工厂注入阿里云短信客户端
	 */
	public AliyunSmsTemplate(Client client, SmsProperties smsProperties) {
		super(smsProperties);
		this.client = client;
	}

	@Override
	public SmsResponse send(Map<String, Object> params, Collection<String> phones) {
		try {
			SendSmsRequest request = new SendSmsRequest()
				.setPhoneNumbers(joinPhones(phones))
				.setSignName(smsProperties.getSignature())
				.setTemplateCode(smsProperties.getTemplateId())
				.setTemplateParam(toJsonParams(params));
			SendSmsResponse response = client.sendSms(request);
			SendSmsResponseBody body = response.getBody();
			if (body != null && "OK".equalsIgnoreCase(body.getCode())) {
				return success(body.getMessage());
			}
			String msg = body != null ? body.getMessage() : "发送失败";
			log.error("阿里云短信发送失败：{}", msg);
			return fail(500, msg);
		} catch (Exception e) {
			log.error("阿里云短信发送异常：{}", e.getMessage(), e);
			return fail(500, e.getMessage());
		}
	}

}
