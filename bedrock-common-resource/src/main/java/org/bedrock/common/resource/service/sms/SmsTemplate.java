package org.bedrock.common.resource.service.sms;

import org.bedrock.common.resource.model.sms.SmsResponse;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 短信服务
 */
public interface SmsTemplate {

	/**
	 * 发送短信
	 *
	 * @param params 参数
	 * @param phone  手机号
	 * @return 短信发送结果
	 */
	default SmsResponse send(Map<String, Object> params, String phone) {
		return send(params, List.of(phone));
	}

	/**
	 * 批量发送短信
	 *
	 * @param params 参数
	 * @param phones 手机号
	 * @return 短信发送结果
	 */
	SmsResponse send(Map<String, Object> params, Collection<String> phones);
}
