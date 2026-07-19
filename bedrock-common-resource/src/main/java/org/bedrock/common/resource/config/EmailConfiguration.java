package org.bedrock.common.resource.config;

import lombok.extern.slf4j.Slf4j;
import org.bedrock.common.resource.factory.EmailBeanFactory;
import org.bedrock.common.resource.support.email.EmailFactoryBean;
import org.bedrock.common.resource.support.email.impl.SpringEmailFactoryBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import java.util.List;

@Slf4j
@AutoConfiguration
public class EmailConfiguration {

	@Bean(name = "springEmailFactoryBean")
	@ConditionalOnMissingBean(name = "springEmailFactoryBean")
	@ConditionalOnClass(name = "org.springframework.mail.javamail.JavaMailSenderImpl")
	public EmailFactoryBean springEmailFactoryBean() {
		return new SpringEmailFactoryBean();
	}

	@Bean(name = "emailBeanFactory")
	@ConditionalOnMissingBean(name = "emailBeanFactory")
	public EmailBeanFactory emailBeanFactory(ObjectProvider<List<EmailFactoryBean>> emailFactoryBeans) {
		return new EmailBeanFactory(emailFactoryBeans);
	}
}
