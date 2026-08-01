package org.bedrock.common.code.util;

import javax.crypto.Cipher;
import java.io.ByteArrayOutputStream;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.interfaces.RSAKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * RSA 非对称加密工具类。
 * <p>
 * 默认变换：{@code RSA/ECB/PKCS1Padding}；默认密钥长度 2048 位。
 * 公钥加密、私钥解密；签名算法为 {@code SHA256withRSA}。超长数据按密钥模长自动分块加解密。
 */
public abstract class RsaUtil {

	/**
	 * 算法名称
	 */
	public static final String ALGORITHM = "RSA";

	/**
	 * 加解密变换
	 */
	public static final String TRANSFORMATION = "RSA/ECB/PKCS1Padding";

	/**
	 * 签名算法
	 */
	public static final String SIGNATURE_ALGORITHM = "SHA256withRSA";

	/**
	 * 默认密钥长度（位）
	 */
	public static final int DEFAULT_KEY_SIZE = 2048;

	private RsaUtil() {
	}

	/**
	 * 生成默认长度（2048 位）的 RSA 密钥对。
	 */
	public static KeyPair generateKeyPair() {
		return generateKeyPair(DEFAULT_KEY_SIZE);
	}

	/**
	 * 生成指定长度的 RSA 密钥对。
	 *
	 * @param keySize 密钥长度，常用 1024 / 2048 / 4096
	 */
	public static KeyPair generateKeyPair(int keySize) {
		try {
			KeyPairGenerator generator = KeyPairGenerator.getInstance(ALGORITHM);
			generator.initialize(keySize);
			return generator.generateKeyPair();
		} catch (Exception e) {
			throw new IllegalStateException("生成 RSA 密钥对失败", e);
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
			return KeyFactory.getInstance(ALGORITHM)
				.generatePublic(new X509EncodedKeySpec(HexUtil.decodeHex(publicKeyHex)));
		} catch (Exception e) {
			throw new IllegalArgumentException("解析 RSA 公钥失败", e);
		}
	}

	/**
	 * 从 Hex 还原私钥。
	 */
	public static PrivateKey getPrivateKeyFromHex(String privateKeyHex) {
		try {
			return KeyFactory.getInstance(ALGORITHM)
				.generatePrivate(new PKCS8EncodedKeySpec(HexUtil.decodeHex(privateKeyHex)));
		} catch (Exception e) {
			throw new IllegalArgumentException("解析 RSA 私钥失败", e);
		}
	}

	/**
	 * 公钥加密。
	 *
	 * @param data         明文
	 * @param publicKeyHex 公钥（Hex）
	 */
	public static byte[] encrypt(byte[] data, String publicKeyHex) {
		return doCipher(Cipher.ENCRYPT_MODE, data, getPublicKeyFromHex(publicKeyHex), true);
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
		return doCipher(Cipher.DECRYPT_MODE, data, getPrivateKeyFromHex(privateKeyHex), false);
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
			Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
			signature.initSign(getPrivateKeyFromHex(privateKeyHex));
			signature.update(data);
			return signature.sign();
		} catch (Exception e) {
			throw new IllegalArgumentException("RSA 签名失败", e);
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
			Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
			signature.initVerify(getPublicKeyFromHex(publicKeyHex));
			signature.update(data);
			return signature.verify(sign);
		} catch (Exception e) {
			throw new IllegalArgumentException("RSA 验签失败", e);
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

	private static byte[] doCipher(int mode, byte[] data, java.security.Key key, boolean encrypt) {
		try {
			Cipher cipher = Cipher.getInstance(TRANSFORMATION);
			cipher.init(mode, key);
			int keySize = ((RSAKey) key).getModulus().bitLength();
			int maxBlock = encrypt ? (keySize / 8 - 11) : (keySize / 8);
			return doFinalByBlock(cipher, data, maxBlock);
		} catch (Exception e) {
			throw new IllegalArgumentException("RSA 加解密失败", e);
		}
	}

	private static byte[] doFinalByBlock(Cipher cipher, byte[] data, int maxBlock) throws Exception {
		int inputLen = data.length;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int offset = 0;
		while (inputLen - offset > 0) {
			int blockSize = Math.min(inputLen - offset, maxBlock);
			out.write(cipher.doFinal(data, offset, blockSize));
			offset += blockSize;
		}
		return out.toByteArray();
	}
}
