package org.bedrock.common.resource.support.sms;

import org.bedrock.common.resource.enums.SmsEnum;
import org.bedrock.common.resource.model.sms.SmsProperties;
import org.bedrock.common.resource.service.sms.SmsTemplate;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/**
 * 创建短信指定类型模板工厂
 */
public interface SmsFactoryBean {

	/**
	 * 获取短信枚举
	 */
	@NonNull
	SmsEnum getSmsEnum();

	/**
	 * 获取短信模板
	 */
	@Nullable
	SmsTemplate createSmsTemplate(SmsProperties properties);

	default boolean isSupport(@NonNull SmsEnum smsEnum) {
		return smsEnum == getSmsEnum();
	}
}
