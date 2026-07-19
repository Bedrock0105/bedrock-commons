package org.bedrock.common.resource.support.sms.impl;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.teaopenapi.models.Config;
import org.bedrock.common.code.util.StringUtil;
import org.bedrock.common.resource.enums.SmsEnum;
import org.bedrock.common.resource.model.sms.SmsProperties;
import org.bedrock.common.resource.service.sms.SmsTemplate;
import org.bedrock.common.resource.service.sms.impl.AliyunSmsTemplate;
import org.bedrock.common.resource.support.sms.SmsFactoryBean;
import org.springframework.lang.NonNull;

/**
 * 阿里云短信工厂
 */
public class AliyunSmsFactoryBean implements SmsFactoryBean {

	private static final String DEFAULT_ENDPOINT = "dysmsapi.aliyuncs.com";

	@Override
	@NonNull
	public SmsEnum getSmsEnum() {
		return SmsEnum.ALIYUN;
	}

	@Override
	public SmsTemplate createSmsTemplate(SmsProperties properties) {
		try {
			return new AliyunSmsTemplate(createClient(properties), properties);
		} catch (Exception e) {
			throw new RuntimeException("创建阿里云短信客户端失败", e);
		}
	}

	/**
	 * 创建阿里云短信客户端，子类可覆写以做特殊配置
	 */
	protected Client createClient(SmsProperties properties) throws Exception {
		Config config = new Config()
			.setAccessKeyId(properties.getApiKey())
			.setAccessKeySecret(properties.getApiSecret());
		config.endpoint = StringUtil.isBlank(properties.getEndpoint()) ? DEFAULT_ENDPOINT : properties.getEndpoint();
		return new Client(config);
	}

}
