package org.bedrock.common.resource.support.sms.impl;

import com.huaweicloud.sdk.core.auth.BasicCredentials;
import com.huaweicloud.sdk.core.auth.ICredential;
import com.huaweicloud.sdk.smsapi.v1.SMSApiClient;
import com.huaweicloud.sdk.smsapi.v1.region.SMSApiRegion;
import org.bedrock.common.resource.enums.SmsEnum;
import org.bedrock.common.resource.model.sms.SmsProperties;
import org.bedrock.common.resource.service.sms.SmsTemplate;
import org.bedrock.common.resource.service.sms.impl.HuaweiSmsTemplate;
import org.bedrock.common.resource.support.sms.SmsFactoryBean;
import org.springframework.lang.NonNull;

/**
 * 华为云短信工厂
 */
public class HuaweiSmsFactoryBean implements SmsFactoryBean {

	@Override
	@NonNull
	public SmsEnum getSmsEnum() {
		return SmsEnum.HUAWEI;
	}

	@Override
	public SmsTemplate createSmsTemplate(SmsProperties properties) {
		return new HuaweiSmsTemplate(createSmsApiClient(properties), properties);
	}

	/**
	 * 创建华为云短信客户端，子类可覆写以做特殊配置
	 */
	protected SMSApiClient createSmsApiClient(SmsProperties properties) {
		ICredential credential = new BasicCredentials()
			.withAk(properties.getApiKey())
			.withSk(properties.getApiSecret());
		return SMSApiClient.newBuilder()
			.withCredential(credential)
			.withRegion(SMSApiRegion.valueOf(properties.getRegion()))
			.build();
	}

}
