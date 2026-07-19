package org.bedrock.common.resource.support.sms.impl;

import com.qiniu.util.Auth;
import org.bedrock.common.resource.enums.SmsEnum;
import org.bedrock.common.resource.model.sms.SmsProperties;
import org.bedrock.common.resource.service.sms.SmsTemplate;
import org.bedrock.common.resource.service.sms.impl.QiniuSmsTemplate;
import org.bedrock.common.resource.support.sms.SmsFactoryBean;
import org.springframework.lang.NonNull;

/**
 * 七牛云短信工厂
 */
public class QiniuSmsFactoryBean implements SmsFactoryBean {

	@Override
	@NonNull
	public SmsEnum getSmsEnum() {
		return SmsEnum.QINIU;
	}

	@Override
	public SmsTemplate createSmsTemplate(SmsProperties properties) {
		return new QiniuSmsTemplate(createAuth(properties), properties);
	}

	/**
	 * 创建七牛云 Auth，子类可覆写以做特殊配置
	 */
	protected Auth createAuth(SmsProperties properties) {
		return Auth.create(properties.getApiKey(), properties.getApiSecret());
	}

}
