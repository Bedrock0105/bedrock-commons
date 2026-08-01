package org.bedrock.common.code.util;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Signature;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * 国密 SM2 非对称加密工具类。
 * <p>
 * 基于 BouncyCastle，使用标准曲线 {@code sm2p256v1}。
 * 公钥加密、私钥解密；签名算法为 {@code SM3withSM2}。
 */
public abstract class Sm2Util {

	/**
	 * 算法名称
	 */
	public static final String ALGORITHM = "EC";

	/**
	 * 加解密算法名
	 */
	public static final String CIPHER_ALGORITHM = "SM2";

	/**
	 * 签名算法
	 */
	public static final String SIGNATURE_ALGORITHM = "SM3withSM2";

	/**
	 * SM2 标准曲线
	 */
	public static final String CURVE_NAME = "sm2p256v1";

	private static final String BC_PROVIDER = "BC";

	private static final SecureRandom SECURE_RANDOM = new SecureRandom();

	static {
		if (Security.getProvider(BC_PROVIDER) == null) {
			Security.addProvider(new BouncyCastleProvider());
		}
	}

	private Sm2Util() {
	}

	/**
	 * 生成 SM2 密钥对。
	 */
	public static KeyPair generateKeyPair() {
		try {
			ECGenParameterSpec sm2Spec = new ECGenParameterSpec(CURVE_NAME);
			KeyPairGenerator generator = KeyPairGenerator.getInstance(ALGORITHM, BC_PROVIDER);
			generator.initialize(sm2Spec, SECURE_RANDOM);
			return generator.generateKeyPair();
		} catch (Exception e) {
			throw new IllegalStateException("生成 SM2 密钥对失败", e);
		}
	}

	/**
	 * 从密钥对导出公钥 Hex。
	 */
	public static String generatePublicKeyHex(KeyPair keyPair) {
		return HexUtil.encodeHexString(keyPair.getPublic().getEncoded());
	}

	/**
	 * 从密钥对导出私钥 Hex。
	 */
	public static String generatePrivateKeyHex(KeyPair keyPair) {
		return HexUtil.encodeHexString(keyPair.getPrivate().getEncoded());
	}

	/**
	 * 从 Hex 还原公钥。
	 */
	public static PublicKey getPublicKeyFromHex(String publicKeyHex) {
		try {
			return KeyFactory.getInstance(ALGORITHM, BC_PROVIDER)
				.generatePublic(new X509EncodedKeySpec(HexUtil.decodeHex(publicKeyHex)));
		} catch (Exception e) {
			throw new IllegalArgumentException("解析 SM2 公钥失败", e);
		}
	}

	/**
	 * 从 Hex 还原私钥。
	 */
	public static PrivateKey getPrivateKeyFromHex(String privateKeyHex) {
		try {
			return KeyFactory.getInstance(ALGORITHM, BC_PROVIDER)
				.generatePrivate(new PKCS8EncodedKeySpec(HexUtil.decodeHex(privateKeyHex)));
		} catch (Exception e) {
			throw new IllegalArgumentException("解析 SM2 私钥失败", e);
		}
	}

	/**
	 * 公钥加密。
	 *
	 * @param data         明文
	 * @param publicKeyHex 公钥（Hex）
	 */
	public static byte[] encrypt(byte[] data, String publicKeyHex) {
		try {
			Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM, BC_PROVIDER);
			cipher.init(Cipher.ENCRYPT_MODE, getPublicKeyFromHex(publicKeyHex), SECURE_RANDOM);
			return cipher.doFinal(data);
		} catch (Exception e) {
			throw new IllegalArgumentException("SM2 加密失败", e);
		}
	}

	/**
	 * 公钥加密。
	 */
	public static byte[] encrypt(String data, String publicKeyHex) {
		return encrypt(data.getBytes(HexUtil.DEFAULT_CHARSET), publicKeyHex);
	}

	/**
	 * 公钥加密，输出 Base64。
	 */
	public static String encryptToBase64(byte[] data, String publicKeyHex) {
		return Base64Util.encode(encrypt(data, publicKeyHex));
	}

	/**
	 * 公钥加密，输出 Base64。
	 */
	public static String encryptToBase64(String data, String publicKeyHex) {
		return encryptToBase64(data.getBytes(HexUtil.DEFAULT_CHARSET), publicKeyHex);
	}

	/**
	 * 公钥加密，输出 Hex。
	 */
	public static String encryptToHex(byte[] data, String publicKeyHex) {
		return HexUtil.encodeHexString(encrypt(data, publicKeyHex));
	}

	/**
	 * 公钥加密，输出 Hex。
	 */
	public static String encryptToHex(String data, String publicKeyHex) {
		return encryptToHex(data.getBytes(HexUtil.DEFAULT_CHARSET), publicKeyHex);
	}

	/**
	 * 私钥解密。
	 *
	 * @param data          密文
	 * @param privateKeyHex 私钥（Hex）
	 */
	public static byte[] decrypt(byte[] data, String privateKeyHex) {
		try {
			Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM, BC_PROVIDER);
			cipher.init(Cipher.DECRYPT_MODE, getPrivateKeyFromHex(privateKeyHex));
			return cipher.doFinal(data);
		} catch (Exception e) {
			throw new IllegalArgumentException("SM2 解密失败", e);
		}
	}

	/**
	 * 私钥解密为字符串。
	 */
	public static String decryptToString(byte[] data, String privateKeyHex) {
		return new String(decrypt(data, privateKeyHex), HexUtil.DEFAULT_CHARSET);
	}

	/**
	 * 从 Base64 密文私钥解密。
	 */
	public static byte[] decryptFromBase64(String dataBase64, String privateKeyHex) {
		return decrypt(Base64Util.decodeToBytes(dataBase64), privateKeyHex);
	}

	/**
	 * 从 Base64 密文私钥解密为字符串。
	 */
	public static String decryptFromBase64ToString(String dataBase64, String privateKeyHex) {
		return new String(decryptFromBase64(dataBase64, privateKeyHex), HexUtil.DEFAULT_CHARSET);
	}

	/**
	 * 从 Hex 密文私钥解密。
	 */
	public static byte[] decryptFromHex(String dataHex, String privateKeyHex) {
		return decrypt(HexUtil.decodeHex(dataHex), privateKeyHex);
	}

	/**
	 * 从 Hex 密文私钥解密为字符串。
	 */
	public static String decryptFromHexToString(String dataHex, String privateKeyHex) {
		return new String(decryptFromHex(dataHex, privateKeyHex), HexUtil.DEFAULT_CHARSET);
	}

	/**
	 * 私钥签名。
	 */
	public static byte[] sign(byte[] data, String privateKeyHex) {
		try {
			Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM, BC_PROVIDER);
			signature.initSign(getPrivateKeyFromHex(privateKeyHex), SECURE_RANDOM);
			signature.update(data);
			return signature.sign();
		} catch (Exception e) {
			throw new IllegalArgumentException("SM2 签名失败", e);
		}
	}

	/**
	 * 私钥签名。
	 */
	public static byte[] sign(String data, String privateKeyHex) {
		return sign(data.getBytes(HexUtil.DEFAULT_CHARSET), privateKeyHex);
	}

	/**
	 * 私钥签名，输出 Base64。
	 */
	public static String signToBase64(byte[] data, String privateKeyHex) {
		return Base64Util.encode(sign(data, privateKeyHex));
	}

	/**
	 * 私钥签名，输出 Base64。
	 */
	public static String signToBase64(String data, String privateKeyHex) {
		return signToBase64(data.getBytes(HexUtil.DEFAULT_CHARSET), privateKeyHex);
	}

	/**
	 * 私钥签名，输出 Hex。
	 */
	public static String signToHex(byte[] data, String privateKeyHex) {
		return HexUtil.encodeHexString(sign(data, privateKeyHex));
	}

	/**
	 * 私钥签名，输出 Hex。
	 */
	public static String signToHex(String data, String privateKeyHex) {
		return signToHex(data.getBytes(HexUtil.DEFAULT_CHARSET), privateKeyHex);
	}

	/**
	 * 公钥验签。
	 *
	 * @param data         原文
	 * @param sign         签名
	 * @param publicKeyHex 公钥（Hex）
	 */
	public static boolean verify(byte[] data, byte[] sign, String publicKeyHex) {
		try {
			Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM, BC_PROVIDER);
			signature.initVerify(getPublicKeyFromHex(publicKeyHex));
			signature.update(data);
			return signature.verify(sign);
		} catch (Exception e) {
			throw new IllegalArgumentException("SM2 验签失败", e);
		}
	}

	/**
	 * 公钥验签。
	 */
	public static boolean verify(String data, byte[] sign, String publicKeyHex) {
		return verify(data.getBytes(HexUtil.DEFAULT_CHARSET), sign, publicKeyHex);
	}

	/**
	 * 公钥验签（签名为 Base64）。
	 */
	public static boolean verifyFromBase64(byte[] data, String signBase64, String publicKeyHex) {
		return verify(data, Base64Util.decodeToBytes(signBase64), publicKeyHex);
	}

	/**
	 * 公钥验签（签名为 Base64）。
	 */
	public static boolean verifyFromBase64(String data, String signBase64, String publicKeyHex) {
		return verifyFromBase64(data.getBytes(HexUtil.DEFAULT_CHARSET), signBase64, publicKeyHex);
	}

	/**
	 * 公钥验签（签名为 Hex）。
	 */
	public static boolean verifyFromHex(byte[] data, String signHex, String publicKeyHex) {
		return verify(data, HexUtil.decodeHex(signHex), publicKeyHex);
	}

	/**
	 * 公钥验签（签名为 Hex）。
	 */
	public static boolean verifyFromHex(String data, String signHex, String publicKeyHex) {
		return verifyFromHex(data.getBytes(HexUtil.DEFAULT_CHARSET), signHex, publicKeyHex);
	}
}
