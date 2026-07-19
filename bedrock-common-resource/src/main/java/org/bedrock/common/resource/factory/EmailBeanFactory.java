package org.bedrock.common.resource.factory;

import jakarta.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.bedrock.common.resource.enums.EmailEnum;
import org.bedrock.common.resource.model.email.EmailProperties;
import org.bedrock.common.resource.service.email.EmailTemplate;
import org.bedrock.common.resource.support.email.EmailFactoryBean;
import org.springframework.beans.factory.ObjectProvider;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * 生成邮件模板
 */
@Slf4j
public class EmailBeanFactory {

	private final Map<EmailEnum, EmailFactoryBean> emailFactoryBeanMap;

	/**
	 * 获取资源上传模板
	 */
	@Nullable
	public EmailTemplate getEmailTemplate(EmailEnum emailEnum, EmailProperties properties) {
		EmailFactoryBean emailFactoryBean = emailFactoryBeanMap.get(emailEnum);
		if (emailFactoryBean == null) {
			log.error("未找到对应的邮件发送模板{},请检测是否引用了相关依赖 EmailEnum 枚举类型上面有标注", emailEnum);
			throw new IllegalArgumentException("未找到对应的资源上传模板");
		}
		return emailFactoryBean.createEmailTemplate(properties);
	}

	/**
	 * 构造函数
	 * 生成bean 工厂
	 */
	public EmailBeanFactory(ObjectProvider<List<EmailFactoryBean>> emailFactoryBeans) {
		emailFactoryBeanMap = new EnumMap<>(EmailEnum.class);
		emailFactoryBeans.ifAvailable(list -> list
			.forEach(bean -> emailFactoryBeanMap
				.put(bean.getEmailEnum(), bean)));

	}
}
