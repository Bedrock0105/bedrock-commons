package org.bedrock.common.resource.support.email.impl;

import org.bedrock.common.resource.constant.MailConstant;
import org.bedrock.common.resource.enums.EmailEnum;
import org.bedrock.common.resource.model.email.EmailProperties;
import org.bedrock.common.resource.service.email.EmailTemplate;
import org.bedrock.common.resource.service.email.impl.SpringEmailTemplate;
import org.bedrock.common.resource.support.email.EmailFactoryBean;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

public class SpringEmailFactoryBean implements EmailFactoryBean {

	@Override
	@NonNull
	public EmailEnum getEmailEnum() {
		return EmailEnum.SPRING;
	}

	@Nullable
	@Override
	public EmailTemplate createEmailTemplate(EmailProperties properties) {
		JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
		javaMailSender.setDefaultEncoding("utf-8");
		// 设置邮箱服务器
		javaMailSender.setHost(properties.getSmtpServer());
		// 设置端口
		javaMailSender.setPort(properties.getSmtpPort());
		// 设置用户名
		javaMailSender.setUsername(properties.getUsername());
		// 设置密码（记得替换为你实际的密码、授权码）
		javaMailSender.setPassword(properties.getPassword());
		// 设置协议
		javaMailSender.setProtocol(properties.getProtocol());
		// 配置项
		Properties defaultProperties = new Properties();
		defaultProperties.put("mail.smtp.connectiontimeout", 5000);
		defaultProperties.put("mail.smtp.timeout", 3000);
		defaultProperties.put("mail.smtp.writetimeout", "5000");

		Integer encyType = properties.getEncyType();
		Integer accountAuth = properties.getAccountAuth();
		if (MailConstant.ACCOUNT_AUTH_YES.equals(accountAuth)) {
			defaultProperties.put("mail.smtp.auth", "true");
		}
		if (MailConstant.ENCY_TYPE_SSL.equals(encyType)) {
			defaultProperties.put("mail.smtp.ssl.enable", "true");  // 设置是否使用ssl安全连接 ---一般都使用
		} else if (MailConstant.ENCY_TYPE_TLS.equals(encyType)) {
			defaultProperties.put("mail.smtp.starttls.enable", "true");
		} else {
			defaultProperties.put("mail.smtp.ssl.enable", "true");//默认ssl
		}
		// 设置配置项
		javaMailSender.setJavaMailProperties(defaultProperties);
		return new SpringEmailTemplate(properties, javaMailSender);
	}
}
