package org.bedrock.common.resource.service.sms.impl;

import org.bedrock.common.code.constant.StringPool;
import org.bedrock.common.code.util.JsonUtil;
import org.bedrock.common.resource.model.sms.SmsProperties;
import org.bedrock.common.resource.model.sms.SmsResponse;
import org.bedrock.common.resource.service.sms.SmsTemplate;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 短信模板抽象基类
 * <p>封装模板参数转换等通用逻辑</p>
 */
public abstract class AbstractSmsTemplate implements SmsTemplate {

	protected final SmsProperties smsProperties;

	protected AbstractSmsTemplate(SmsProperties smsProperties) {
		this.smsProperties = smsProperties;
	}

	/**
	 * 将模板参数转为 JSON 字符串（阿里云、腾讯云、华为云、七牛云通用）
	 */
	protected String toJsonParams(Map<String, Object> params) {
		return JsonUtil.toJson(params);
	}

	/**
	 * 将模板参数转为云片 tpl_value 格式：#code#=1234&#name#=张三
	 */
	protected String toYunpianTplValue(Map<String, Object> params) {
		if (params == null || params.isEmpty()) {
			return StringPool.EMPTY;
		}
		return params.entrySet().stream()
			.map(entry -> StringPool.HASH + entry.getKey() + StringPool.HASH + StringPool.EQUALS + entry.getValue())
			.collect(Collectors.joining(StringPool.AMPERSAND));
	}

	/**
	 * 将手机号集合转为逗号分隔字符串
	 */
	protected String joinPhones(Collection<String> phones) {
		return String.join(StringPool.COMMA, phones);
	}

	protected SmsResponse success(String msg) {
		return new SmsResponse(true, 200, msg);
	}

	protected SmsResponse fail(Integer code, String msg) {
		return new SmsResponse(false, code, msg);
	}

}
