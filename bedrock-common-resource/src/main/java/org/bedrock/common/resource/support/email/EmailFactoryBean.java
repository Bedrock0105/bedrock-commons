package org.bedrock.common.resource.support.email;

import org.bedrock.common.resource.enums.EmailEnum;
import org.bedrock.common.resource.model.email.EmailProperties;
import org.bedrock.common.resource.service.email.EmailTemplate;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/**
 * 创建邮件指定类型模板工厂
 */
public interface EmailFactoryBean {

	/**
	 * 获取OSS枚举
	 */
	@NonNull
	EmailEnum getEmailEnum();

	/**
	 * 获取OSS模板
	 */
	@Nullable
	EmailTemplate createEmailTemplate(EmailProperties properties);

	default boolean isSupport(@NonNull EmailEnum emailEnum) {
		return emailEnum == getEmailEnum();
	}
}
