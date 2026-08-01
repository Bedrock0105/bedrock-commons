package org.bedrock.common.crypto.algorithm.impl;

import org.bedrock.common.code.util.Sm4Util;
import org.bedrock.common.code.util.StringUtil;
import org.bedrock.common.crypto.algorithm.CryptoAlgorithm;
import org.bedrock.common.crypto.exception.CryptoException;
import org.bedrock.common.crypto.props.CryptoProperties;
import org.bedrock.common.spi.autoservice.AutoService;

/**
 * 国密 SM4 对称加解密实现，委托 {@link Sm4Util}。
 * <p>通过 {@link AutoService} 注册到 SPI，由 {@code ServiceLoader} 发现。</p>
 */
@AutoService(CryptoAlgorithm.class)
public class Sm4CryptoAlgorithm implements CryptoAlgorithm {

	private CryptoProperties properties;

	@Override
	public void init(CryptoProperties cryptoProperties) {
		this.properties = cryptoProperties;
	}

	@Override
	public String encrypt(byte[] plainText, String secretKey, String publicKey, String privateKey) {
		try {
			return Sm4Util.encryptIvToBase64(plainText, requireSecretKey(secretKey));
		} catch (Exception e) {
			throw new CryptoException("SM4 加密失败", e);
		}
	}

	@Override
	public byte[] decrypt(String cipherText, String secretKey, String publicKey, String privateKey) {
		try {
			return Sm4Util.decryptIvFromBase64(cipherText, requireSecretKey(secretKey));
		} catch (Exception e) {
			throw new CryptoException("SM4 解密失败", e);
		}
	}

	private String requireSecretKey(String secretKey) {
		if (StringUtil.isBlank(secretKey)) {
			secretKey = properties.getKey();
		}
		if (StringUtil.isBlank(secretKey)) {
			throw new CryptoException("SM4 对称密钥 secretKey 不能为空");
		}
		return secretKey;
	}
}
