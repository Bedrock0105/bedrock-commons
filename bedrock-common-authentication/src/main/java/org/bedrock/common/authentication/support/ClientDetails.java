package org.bedrock.common.authentication.support;

import java.util.Map;
import java.util.Set;

/**
 * Client details for OAuth 2
 */
public interface ClientDetails {

	/**
	 * The client id.
	 *
	 */
	String getClientId();

	/**
	 * 此客户端可以访问的资源。如果为空，则调用者可以忽略。
	 */
	Set<String> getResourceIds();

	/**
	 * 是否需要使用密码来验证此客户端的身份
	 */
	boolean isSecretRequired();

	/**
	 * 客户端密码
	 */
	String getClientSecret();

	/**
	 * 此客户端是否仅限于特定范围。若为否，则认证请求的范围将为被忽略。
	 */
	boolean isScoped();

	/**
	 * 此客户端的范围。如果客户端没有范围限制，则为空
	 */
	Set<String> getScope();

	/**
	 * 此客户端被授权的授权类型
	 *
	 * @return The grant types for which this client is authorized.
	 */
	Set<String> getAuthorizedGrantTypes();

	/**
	 * 此客户端在“authorization_code”访问授权期间使用的预定义重定向URI。请参阅OAuth规范
	 */
	Set<String> getRegisteredRedirectUri();
	/**
	 * 此客户端的访问令牌有效期。若未明确设置，则为空（实现可能会利用这一特性）
	 *
	 * @return the access token validity period
	 */
	Integer getAccessTokenValiditySeconds();

	/**
	 * 此客户端的刷新令牌有效期。若为空，则表示由令牌服务设置的默认值，以及
	 *  对于非过期令牌，值为零或负数
	 */
	Integer getRefreshTokenValiditySeconds();

	/**
	 * 测试客户端是否需要用户针对特定范围进行批准。
	 */
	boolean isAutoApprove(String scope);

	/**
	 * 此客户端的额外信息，虽然不是vanilla OAuth协议所必需的，但可能很有用，例如，
	 */
	Map<String, Object> getAdditionalInformation();
}
