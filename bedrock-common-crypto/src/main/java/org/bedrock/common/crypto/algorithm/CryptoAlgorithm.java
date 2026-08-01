package org.bedrock.common.crypto.algorithm;

import org.bedrock.common.crypto.props.CryptoProperties;

/**
 * API 加解密策略接口。
 * <p>
 * 不同加密算法通过实现本接口完成加解密；内置实现需标注
 * {@code @AutoService(CryptoAlgorithm.class)}，由 {@link CryptoAlgorithmRegistry}
 * 通过 {@link java.util.ServiceLoader} 加载。
 * </p>
 */
public interface CryptoAlgorithm {

	/**
	 * 初始化。
	 *
	 * @param cryptoProperties 配置
	 */
	void init(CryptoProperties cryptoProperties);

	/**
	 * 加密明文。
	 *
	 * @param plainText  明文字节（通常为 JSON）
	 * @param secretKey  对称密钥（Hex），用于 AES / DES / SM4；为空则使用配置
	 * @param publicKey  非对称公钥（Hex），用于 RSA / SM2 加密；为空则使用配置
	 * @param privateKey 非对称私钥（Hex）；对称算法可忽略
	 * @return Base64 密文
	 */
	String encrypt(byte[] plainText, String secretKey, String publicKey, String privateKey);

	/**
	 * 解密密文。
	 *
	 * @param cipherText Base64 密文
	 * @param secretKey  对称密钥（Hex），用于 AES / DES / SM4；为空则使用配置
	 * @param publicKey  非对称公钥（Hex）；对称算法可忽略
	 * @param privateKey 非对称私钥（Hex），用于 RSA / SM2 解密；为空则使用配置
	 * @return 明文字节
	 */
	byte[] decrypt(String cipherText, String secretKey, String publicKey, String privateKey);
}
