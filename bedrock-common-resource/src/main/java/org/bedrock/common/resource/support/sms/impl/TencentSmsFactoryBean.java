package org.bedrock.common.resource.support.sms.impl;

import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.sms.v20210111.SmsClient;
import org.bedrock.common.code.util.StringUtil;
import org.bedrock.common.resource.enums.SmsEnum;
import org.bedrock.common.resource.model.sms.SmsProperties;
import org.bedrock.common.resource.service.sms.SmsTemplate;
import org.bedrock.common.resource.service.sms.impl.TencentSmsTemplate;
import org.bedrock.common.resource.support.sms.SmsFactoryBean;
import org.springframework.lang.NonNull;

/**
 * 腾讯云短信工厂
 */
public class TencentSmsFactoryBean implements SmsFactoryBean {

	private static final String DEFAULT_ENDPOINT = "sms.tencentcloudapi.com";

	@Override
	@NonNull
	public SmsEnum getSmsEnum() {
		return SmsEnum.TENCENT;
	}

	@Override
	public SmsTemplate createSmsTemplate(SmsProperties properties) {
		return new TencentSmsTemplate(createSmsClient(properties), properties);
	}

	/**
	 * 创建腾讯云短信客户端，子类可覆写以做特殊配置
	 */
	protected SmsClient createSmsClient(SmsProperties properties) {
		Credential credential = new Credential(properties.getApiKey(), properties.getApiSecret());
		HttpProfile httpProfile = new HttpProfile();
		httpProfile.setEndpoint(StringUtil.isBlank(properties.getEndpoint()) ? DEFAULT_ENDPOINT : properties.getEndpoint());
		ClientProfile clientProfile = new ClientProfile();
		clientProfile.setHttpProfile(httpProfile);
		return new SmsClient(credential, properties.getRegion(), clientProfile);
	}

}
