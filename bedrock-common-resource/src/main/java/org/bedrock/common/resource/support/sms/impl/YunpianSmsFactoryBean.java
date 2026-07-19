package org.bedrock.common.resource.support.sms.impl;

import com.yunpian.sdk.YunpianClient;
import org.bedrock.common.resource.enums.SmsEnum;
import org.bedrock.common.resource.model.sms.SmsProperties;
import org.bedrock.common.resource.service.sms.SmsTemplate;
import org.bedrock.common.resource.service.sms.impl.YunpianSmsTemplate;
import org.bedrock.common.resource.support.sms.SmsFactoryBean;
import org.springframework.lang.NonNull;

/**
 * 云片短信工厂
 */
public class YunpianSmsFactoryBean implements SmsFactoryBean {

	@Override
	@NonNull
	public SmsEnum getSmsEnum() {
		return SmsEnum.YUNPIAN;
	}

	@Override
	public SmsTemplate createSmsTemplate(SmsProperties properties) {
		return new YunpianSmsTemplate(createYunpianClient(properties), properties);
	}

	/**
	 * 创建云片客户端，子类可覆写以做特殊配置
	 */
	protected YunpianClient createYunpianClient(SmsProperties properties) {
		return new YunpianClient(properties.getApiKey()).init();
	}

}
