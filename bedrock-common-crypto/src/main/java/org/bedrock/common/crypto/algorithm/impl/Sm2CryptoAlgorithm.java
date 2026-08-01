package org.bedrock.common.crypto.algorithm.impl;

import org.bedrock.common.code.util.Sm2Util;
import org.bedrock.common.code.util.StringUtil;
import org.bedrock.common.crypto.algorithm.CryptoAlgorithm;
import org.bedrock.common.crypto.exception.CryptoException;
import org.bedrock.common.crypto.props.CryptoProperties;
import org.bedrock.common.spi.autoservice.AutoService;

/**
 * 国密 SM2 非对称加解密实现，委托 {@link Sm2Util}。
 * <p>加密使用公钥，解密使用私钥。通过 {@link AutoService} 注册到 SPI。</p>
 */
@AutoService(CryptoAlgorithm.class)
public class Sm2CryptoAlgorithm implements CryptoAlgorithm {

	private CryptoProperties properties;

	@Override
	public void init(CryptoProperties cryptoProperties) {
		this.properties = cryptoProperties;
	}

	@Override
	public String encrypt(byte[] plainText, String secretKey, String publicKey, String privateKey) {
		try {
			return Sm2Util.encryptToBase64(plainText, requirePublicKey(publicKey));
		} catch (Exception e) {
			throw new CryptoException("SM2 加密失败", e);
		}
	}

	@Override
	public byte[] decrypt(String cipherText, String secretKey, String publicKey, String privateKey) {
		try {
			return Sm2Util.decryptFromBase64(cipherText, requirePrivateKey(privateKey));
		} catch (Exception e) {
			throw new CryptoException("SM2 解密失败", e);
		}
	}

	private String requirePublicKey(String publicKey) {
		if (StringUtil.isBlank(publicKey)) {
			publicKey = properties.getPublicKey();
		}
		if (StringUtil.isBlank(publicKey)) {
			throw new CryptoException("SM2 公钥 publicKey 不能为空");
		}
		return publicKey;
	}

	private String requirePrivateKey(String privateKey) {
		if (StringUtil.isBlank(privateKey)) {
			privateKey = properties.getPrivateKey();
		}
		if (StringUtil.isBlank(privateKey)) {
			throw new CryptoException("SM2 私钥 privateKey 不能为空");
		}
		return privateKey;
	}
}
