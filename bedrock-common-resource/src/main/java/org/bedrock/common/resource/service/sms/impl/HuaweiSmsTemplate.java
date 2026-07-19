package org.bedrock.common.resource.service.sms.impl;

import com.huaweicloud.sdk.smsapi.v1.SMSApiClient;
import com.huaweicloud.sdk.smsapi.v1.model.BatchSendSmsRequest;
import com.huaweicloud.sdk.smsapi.v1.model.BatchSendSmsRequestBody;
import com.huaweicloud.sdk.smsapi.v1.model.BatchSendSmsResponse;
import lombok.extern.slf4j.Slf4j;
import org.bedrock.common.code.util.JsonUtil;
import org.bedrock.common.resource.model.sms.SmsProperties;
import org.bedrock.common.resource.model.sms.SmsResponse;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 华为云短信实现
 * <p>
 * 依赖：com.huaweicloud.sdk:huaweicloud-sdk-smsapi
 * </p>
 * <p>
 * 配置示例：
 * <pre>
 * apiKey: your-app-key
 * apiSecret: your-app-secret
 * sender: 您的通道号
 * signature: 您的签名
 * templateId: your-template-id
 * region: cn-north-4
 * </pre>
 * </p>
 * <p>注意：sender（通道号）和 region 为必填项</p>
 */
@Slf4j
public class HuaweiSmsTemplate extends AbstractSmsTemplate {

	private final SMSApiClient smsApiClient;

	/**
	 * 通过工厂注入华为云短信客户端
	 */
	public HuaweiSmsTemplate(SMSApiClient smsApiClient, SmsProperties smsProperties) {
		super(smsProperties);
		this.smsApiClient = smsApiClient;
	}

	@Override
	public SmsResponse send(Map<String, Object> params, Collection<String> phones) {
		try {
			BatchSendSmsRequest request = new BatchSendSmsRequest();
			request.withBody(new BatchSendSmsRequestBody()
				.withFrom(smsProperties.getSender())
				.withTo(joinPhones(phones))
				.withTemplateId(smsProperties.getTemplateId())
				.withSignature(smsProperties.getSignature())
				.withTemplateParas(toTemplateParas(params)));
			BatchSendSmsResponse response = smsApiClient.batchSendSms(request);
			if (response.getHttpStatusCode() == 200) {
				return success("发送成功");
			}
			log.error("华为云短信发送失败，状态码：{}", response.getHttpStatusCode());
			return fail(response.getHttpStatusCode(), "发送失败");
		} catch (Exception e) {
			log.error("华为云短信发送异常：{}", e.getMessage(), e);
			return fail(500, e.getMessage());
		}
	}

	/**
	 * 将模板参数转为华为云要求的 JSON 数组字符串，如 ["123456","5"]
	 */
	private String toTemplateParas(Map<String, Object> params) {
		if (params == null || params.isEmpty()) {
			return "[]";
		}
		List<String> values = new ArrayList<>(params.size());
		params.values().forEach(value -> values.add(String.valueOf(value)));
		return JsonUtil.toJson(values);
	}

}
