package org.bedrock.common.resource.model.email;


import java.io.Serial;
import java.io.Serializable;

/**
 * 邮件返回集合
 *
 * @param success 是否成功
 * @param code    状态码
 * @param msg     返回消息
 *
 */
public record EmailResponse(boolean success, Integer code, String msg) implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

}
