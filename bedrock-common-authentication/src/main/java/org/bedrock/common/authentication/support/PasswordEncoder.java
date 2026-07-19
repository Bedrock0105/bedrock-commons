package org.bedrock.common.authentication.support;

/**
 * 密码编码器
 */
@FunctionalInterface
public interface PasswordEncoder {

	/**
	 * 密码加密
	 *
	 * @param rawPassword 需要加密的密码
	 * @return
	 */
	String encode(String rawPassword);

	/**
	 * 对比密码是否一致
	 *
	 * @param rawPassword     需要对比的密码
	 * @param encodedPassword 原始密码
	 * @return
	 */
	default boolean matches(String rawPassword, String encodedPassword) {
		return encodedPassword.equals(encode(rawPassword));
	}
}
