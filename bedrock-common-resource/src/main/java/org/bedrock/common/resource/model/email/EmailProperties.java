package org.bedrock.common.resource.model.email;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 邮件配置模型
 */
@Getter
@Setter
public class EmailProperties implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 协议
	 */
	private String protocol;

	/**
	 * SMTP服务器
	 */
	private String smtpServer;

	/**
	 * SMTP端口号
	 */
	private Integer smtpPort;

	/**
	 * 加密类型(1.SSL 2.TLS )
	 */
	private Integer encyType;

	/**
	 * 账户验证(0.否 1.是)
	 */
	private Integer accountAuth;

	/**
	 * 设置用户名
	 */
	private String username;

	/**
	 * 设置密码（记得替换为你实际的密码、授权码
	 */
	private String password;
}
