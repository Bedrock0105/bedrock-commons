package org.bedrock.common.authentication.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.Setter;
import org.bedrock.common.auth.constant.TokenConstant;
import org.bedrock.common.authentication.support.OauthTokenResultSerializer;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * token结果
 */
@Getter
@Setter
@JsonSerialize(using = OauthTokenResultSerializer.class)
public class OauthTokenResult {

	public static String BEARER_TYPE = TokenConstant.AUTH_BEARER;

	/**
	 * The access token issued by the authorization server. This value is REQUIRED.
	 */
	public static String ACCESS_TOKEN = "access_token";

	/**
	 * The type of the token issued as described in <a
	 * href="https://tools.ietf.org/html/draft-ietf-oauth-v2-22#section-7.1">Section 7.1</a>. Value is case insensitive.
	 * This value is REQUIRED.
	 */
	public static String TOKEN_TYPE = TokenConstant.TOKEN_TYPE;

	/**
	 * The refresh token which can be used to obtain new access tokens using the same authorization grant as described
	 * in <a href="https://tools.ietf.org/html/draft-ietf-oauth-v2-22#section-6">Section 6</a>. This value is OPTIONAL.
	 */
	public static String REFRESH_TOKEN = "refresh_token";

	/**
	 * The scope of the access token as described by <a
	 * href="https://tools.ietf.org/html/draft-ietf-oauth-v2-22#section-3.3">Section 3.3</a>
	 */
	public static String SCOPE = "scope";

	/**
	 * The lifetime in seconds of the access token. For example, the value "3600" denotes that the access token will
	 * * expire in one hour from the time the response was generated. This value is OPTIONAL.
	 */
	public static String EXPIRES_IN = "expires_in";

	/**
	 * The lifetime in seconds of the refresh token. For example, the value "3600" denotes that the refresh token will
	 */
	public static String REFRESH_EXPIRE_IN = "refresh_expire_in";

	/**
	 * 访问令牌
	 */
	private String accessToken;

	/**
	 * 令牌类型
	 */
	private String tokenType = BEARER_TYPE.toLowerCase();

	/**
	 * 刷新token
	 */
	private String refreshToken;

	/**
	 * 过期时间 毫秒
	 */
	private Long expireTime;

	/**
	 * 刷新token过期时间 毫秒
	 */
	private Long refreshExpireTime;

	/**
	 * 访问范围
	 */
	private Set<String> scope;

	/**
	 * 生成token的参数
	 * 需要自己实现{@link org.bedrock.common.authentication.handler.TokenEnhancerHandler}
	 * 进行token增强
	 */
	private Map<String, Object> additionalInformation = new HashMap<>();

	/**
	 * 添加token参数
	 */
	public OauthTokenResult additionalInformation(String key, Object value) {
		this.additionalInformation.put(key, value);
		return this;
	}
}
