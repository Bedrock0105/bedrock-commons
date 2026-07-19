package org.bedrock.common.resource.config;

import lombok.extern.slf4j.Slf4j;
import org.bedrock.common.resource.factory.SmsBeanFactory;
import org.bedrock.common.resource.support.sms.SmsFactoryBean;
import org.bedrock.common.resource.support.sms.impl.AliyunSmsFactoryBean;
import org.bedrock.common.resource.support.sms.impl.HuaweiSmsFactoryBean;
import org.bedrock.common.resource.support.sms.impl.QiniuSmsFactoryBean;
import org.bedrock.common.resource.support.sms.impl.TencentSmsFactoryBean;
import org.bedrock.common.resource.support.sms.impl.YunpianSmsFactoryBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import java.util.List;

@Slf4j
@AutoConfiguration
public class SmsConfiguration {

	/**
	 * 创建阿里云短信工厂
	 */
	@Bean(name = "aliyunSmsFactoryBean")
	@ConditionalOnMissingBean(name = "aliyunSmsFactoryBean")
	@ConditionalOnClass(name = "com.aliyun.dysmsapi20170525.Client")
	public SmsFactoryBean aliyunSmsFactoryBean() {
		return new AliyunSmsFactoryBean();
	}

	/**
	 * 创建腾讯云短信工厂
	 */
	@Bean(name = "tencentSmsFactoryBean")
	@ConditionalOnMissingBean(name = "tencentSmsFactoryBean")
	@ConditionalOnClass(name = "com.tencentcloudapi.sms.v20210111.SmsClient")
	public SmsFactoryBean tencentSmsFactoryBean() {
		return new TencentSmsFactoryBean();
	}

	/**
	 * 创建云片短信工厂
	 */
	@Bean(name = "yunpianSmsFactoryBean")
	@ConditionalOnMissingBean(name = "yunpianSmsFactoryBean")
	@ConditionalOnClass(name = "com.yunpian.sdk.YunpianClient")
	public SmsFactoryBean yunpianSmsFactoryBean() {
		return new YunpianSmsFactoryBean();
	}

	/**
	 * 创建华为云短信工厂
	 */
	@Bean(name = "huaweiSmsFactoryBean")
	@ConditionalOnMissingBean(name = "huaweiSmsFactoryBean")
	@ConditionalOnClass(name = "com.huaweicloud.sdk.smsapi.v1.SMSApiClient")
	public SmsFactoryBean huaweiSmsFactoryBean() {
		return new HuaweiSmsFactoryBean();
	}

	/**
	 * 创建七牛云短信工厂
	 */
	@Bean(name = "qiniuSmsFactoryBean")
	@ConditionalOnMissingBean(name = "qiniuSmsFactoryBean")
	@ConditionalOnClass(name = "com.qiniu.util.Auth")
	public SmsFactoryBean qiniuSmsFactoryBean() {
		return new QiniuSmsFactoryBean();
	}

	@Bean(name = "smsBeanFactory")
	@ConditionalOnMissingBean(name = "smsBeanFactory")
	public SmsBeanFactory smsBeanFactory(ObjectProvider<List<SmsFactoryBean>> smsFactoryBeans) {
		return new SmsBeanFactory(smsFactoryBeans);
	}
}
