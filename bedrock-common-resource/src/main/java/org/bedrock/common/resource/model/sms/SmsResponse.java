package org.bedrock.common.resource.model.sms;

import java.io.Serial;
import java.io.Serializable;

/**
 * 短信返回
 *
 * @param success 是否成功
 * @param code    状态码
 * @param msg     返回消息
 *
 */
public record SmsResponse(boolean success, Integer code, String msg) implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;
}
