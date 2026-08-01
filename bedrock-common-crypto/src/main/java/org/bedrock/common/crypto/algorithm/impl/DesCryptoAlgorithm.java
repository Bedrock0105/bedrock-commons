package org.bedrock.common.crypto.algorithm.impl;

import org.bedrock.common.code.util.DesUtil;
import org.bedrock.common.code.util.StringUtil;
import org.bedrock.common.crypto.algorithm.CryptoAlgorithm;
import org.bedrock.common.crypto.exception.CryptoException;
import org.bedrock.common.crypto.props.CryptoProperties;
import org.bedrock.common.spi.autoservice.AutoService;

/**
 * DES 对称加解密实现，委托 {@link DesUtil}。
 * <p>通过 {@link AutoService} 注册到 SPI，由 {@code ServiceLoader} 发现。</p>
 */
@AutoService(CryptoAlgorithm.class)
public class DesCryptoAlgorithm implements CryptoAlgorithm {

	private CryptoProperties properties;

	@Override
	public void init(CryptoProperties cryptoProperties) {
		this.properties = cryptoProperties;
	}

	@Override
	public String encrypt(byte[] plainText, String secretKey, String publicKey, String privateKey) {
		try {
			return DesUtil.encryptIvToBase64(plainText, requireSecretKey(secretKey));
		} catch (Exception e) {
			throw new CryptoException("DES 加密失败", e);
		}
	}

	@Override
	public byte[] decrypt(String cipherText, String secretKey, String publicKey, String privateKey) {
		try {
			return DesUtil.decryptIvFromBase64(cipherText, requireSecretKey(secretKey));
		} catch (Exception e) {
			throw new CryptoException("DES 解密失败", e);
		}
	}

	private String requireSecretKey(String secretKey) {
		if (StringUtil.isBlank(secretKey)) {
			secretKey = properties.getKey();
		}
		if (StringUtil.isBlank(secretKey)) {
			throw new CryptoException("DES 对称密钥 secretKey 不能为空");
		}
		return secretKey;
	}
}
