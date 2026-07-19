package org.bedrock.common.resource.factory;

import jakarta.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.bedrock.common.resource.enums.SmsEnum;
import org.bedrock.common.resource.model.sms.SmsProperties;
import org.bedrock.common.resource.service.sms.SmsTemplate;
import org.bedrock.common.resource.support.sms.SmsFactoryBean;
import org.springframework.beans.factory.ObjectProvider;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * 生成资源上传模板
 */
@Slf4j
public class SmsBeanFactory {

	private final Map<SmsEnum, SmsFactoryBean> smsFactoryBeanMap;

	/**
	 * 获取资源上传模板
	 */
	@Nullable
	public SmsTemplate getSmsTemplate(SmsEnum smsEnum, SmsProperties properties) {
		SmsFactoryBean smsFactoryBean = smsFactoryBeanMap.get(smsEnum);
		if (smsFactoryBean == null) {
			log.error("未找到对应的短信发送模板{},请检测是否引用了相关依赖 SmsEnum 枚举类型上面有标注", smsEnum);
			throw new IllegalArgumentException("未找到对应的资源上传模板");
		}
		return smsFactoryBean.createSmsTemplate(properties);
	}

	/**
	 * 构造函数
	 * 生成bean 工厂
	 */
	public SmsBeanFactory(ObjectProvider<List<SmsFactoryBean>> smsFactoryBeans) {
		smsFactoryBeanMap = new EnumMap<>(SmsEnum.class);
		smsFactoryBeans.ifAvailable(list -> list
			.forEach(bean -> smsFactoryBeanMap
				.put(bean.getSmsEnum(), bean)));

	}
}
