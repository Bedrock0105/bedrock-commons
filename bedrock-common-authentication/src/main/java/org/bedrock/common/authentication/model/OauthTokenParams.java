package org.bedrock.common.authentication.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

/**
 * 登录参数
 */
@Getter
@Setter
public class OauthTokenParams implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(description = "账号")
	private String username;

	@Schema(description = "密码")
	private String password;

	@Schema(description = "验证方式")
	private String grantType;

	@Schema(description = "授权范围")
	private String scope;

	@Schema(description = "续期的token")
	private String refreshToken;

	@Schema(description = "其他参数")
	private Map<String, Object> params;
}
