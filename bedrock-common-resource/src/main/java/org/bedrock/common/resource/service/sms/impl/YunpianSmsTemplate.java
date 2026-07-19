package org.bedrock.common.resource.service.sms.impl;

import com.yunpian.sdk.YunpianClient;
import com.yunpian.sdk.model.Result;
import com.yunpian.sdk.model.SmsSingleSend;
import lombok.extern.slf4j.Slf4j;
import org.bedrock.common.resource.model.sms.SmsProperties;
import org.bedrock.common.resource.model.sms.SmsResponse;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * 云片短信实现
 * <p>
 * 依赖：com.yunpian.sdk:yunpian-java-sdk
 * </p>
 * <p>
 * 配置示例：
 * <pre>
 * apiKey: your-apikey
 * templateId: 1234567
 * </pre>
 * </p>
 * <p>注意：云片仅需 apiKey，无需 apiSecret</p>
 */
@Slf4j
public class YunpianSmsTemplate extends AbstractSmsTemplate {

	private final YunpianClient yunpianClient;

	/**
	 * 通过工厂注入云片客户端
	 */
	public YunpianSmsTemplate(YunpianClient yunpianClient, SmsProperties smsProperties) {
		super(smsProperties);
		this.yunpianClient = yunpianClient;
	}

	@Override
	public SmsResponse send(Map<String, Object> params, Collection<String> phones) {
		try {
			String tplValue = toYunpianTplValue(params);
			for (String phone : phones) {
				Map<String, String> param = new HashMap<>(4);
				param.put("mobile", phone);
				param.put("tpl_id", smsProperties.getTemplateId());
				if (!tplValue.isEmpty()) {
					param.put("tpl_value", tplValue);
				}
				Result<SmsSingleSend> result = yunpianClient.sms().single_send(param);
				if (result.getCode() != 0) {
					log.error("云片短信发送失败：{}", result.getMsg());
					return fail(result.getCode(), result.getMsg());
				}
			}
			return success("发送成功");
		} catch (Exception e) {
			log.error("云片短信发送异常：{}", e.getMessage(), e);
			return fail(500, e.getMessage());
		}
	}

}
