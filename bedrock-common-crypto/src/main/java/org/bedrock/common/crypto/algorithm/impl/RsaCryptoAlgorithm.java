package org.bedrock.common.crypto.algorithm.impl;

import org.bedrock.common.code.util.RsaUtil;
import org.bedrock.common.code.util.StringUtil;
import org.bedrock.common.crypto.algorithm.CryptoAlgorithm;
import org.bedrock.common.crypto.exception.CryptoException;
import org.bedrock.common.crypto.props.CryptoProperties;
import org.bedrock.common.spi.autoservice.AutoService;

/**
 * RSA 非对称加解密实现，委托 {@link RsaUtil}。
 * <p>加密使用公钥，解密使用私钥。通过 {@link AutoService} 注册到 SPI。</p>
 */
@AutoService(CryptoAlgorithm.class)
public class RsaCryptoAlgorithm implements CryptoAlgorithm {

	private CryptoProperties properties;

	@Override
	public void init(CryptoProperties cryptoProperties) {
		this.properties = cryptoProperties;
	}

	@Override
	public String encrypt(byte[] plainText, String secretKey, String publicKey, String privateKey) {
		try {
			return RsaUtil.encryptToBase64(plainText, requirePublicKey(publicKey));
		} catch (Exception e) {
			throw new CryptoException("RSA 加密失败", e);
		}
	}

	@Override
	public byte[] decrypt(String cipherText, String secretKey, String publicKey, String privateKey) {
		try {
			return RsaUtil.decryptFromBase64(cipherText, requirePrivateKey(privateKey));
		} catch (Exception e) {
			throw new CryptoException("RSA 解密失败", e);
		}
	}

	private String requirePublicKey(String publicKey) {
		if (StringUtil.isBlank(publicKey)) {
			publicKey = properties.getPublicKey();
		}
		if (StringUtil.isBlank(publicKey)) {
			throw new CryptoException("RSA 公钥 publicKey 不能为空");
		}
		return publicKey;
	}

	private String requirePrivateKey(String privateKey) {
		if (StringUtil.isBlank(privateKey)) {
			privateKey = properties.getPrivateKey();
		}
		if (StringUtil.isBlank(privateKey)) {
			throw new CryptoException("RSA 私钥 privateKey 不能为空");
		}
		return privateKey;
	}
}
