package org.bedrock.common.code.util;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Arrays;

/**
 * 国密 SM4 对称加密工具类。
 * <p>
 * 默认变换：{@code SM4/ECB/PKCS5Padding}（无 IV，BouncyCastle）。
 * {@code *Iv*} 方法使用 {@code SM4/CBC/PKCS5Padding}，加密时随机生成 IV 并拼到密文前（IV + 密文），解密时自动拆分。
 */
public abstract class Sm4Util {

	/**
	 * 算法名称
	 */
	public static final String ALGORITHM = "SM4";

	/**
	 * ECB 变换（无 IV）
	 */
	public static final String TRANSFORMATION = "SM4/ECB/PKCS5Padding";

	/**
	 * CBC 变换（需 IV）
	 */
	public static final String TRANSFORMATION_CBC = "SM4/CBC/PKCS5Padding";

	/**
	 * 默认密钥长度（位），SM4 固定 128
	 */
	public static final int DEFAULT_KEY_SIZE = 128;

	/**
	 * IV 长度（字节）
	 */
	public static final int IV_LENGTH = 16;

	private static final String BC_PROVIDER = "BC";

	private static final SecureRandom SECURE_RANDOM = new SecureRandom();

	static {
		if (Security.getProvider(BC_PROVIDER) == null) {
			Security.addProvider(new BouncyCastleProvider());
		}
	}

	private Sm4Util() {
	}

	/**
	 * 生成默认长度（128 位）密钥的 Hex 字符串。
	 */
	public static String generateKeyHex() {
		return HexUtil.encodeHexString(generateKey(DEFAULT_KEY_SIZE));
	}

	/**
	 * 生成指定长度密钥的 Hex 字符串。
	 *
	 * @param keySize 密钥长度，通常为 128
	 */
	public static String generateKeyHex(int keySize) {
		return HexUtil.encodeHexString(generateKey(keySize));
	}

	/**
	 * ECB 加密。
	 *
	 * @param data   明文
	 * @param keyHex 密钥（Hex）
	 */
	public static byte[] encrypt(byte[] data, String keyHex) {
		return doCipher(Cipher.ENCRYPT_MODE, data, keyHex);
	}

	/**
	 * ECB 加密。
	 *
	 * @param data   明文
	 * @param keyHex 密钥（Hex）
	 */
	public static byte[] encrypt(String data, String keyHex) {
		return encrypt(data.getBytes(HexUtil.DEFAULT_CHARSET), keyHex);
	}

	/**
	 * ECB 加密，输出 Base64。
	 */
	public static String encryptToBase64(byte[] data, String keyHex) {
		return Base64Util.encode(encrypt(data, keyHex));
	}

	/**
	 * ECB 加密，输出 Base64。
	 */
	public static String encryptToBase64(String data, String keyHex) {
		return encryptToBase64(data.getBytes(HexUtil.DEFAULT_CHARSET), keyHex);
	}

	/**
	 * ECB 加密，输出 Hex。
	 */
	public static String encryptToHex(byte[] data, String keyHex) {
		return HexUtil.encodeHexString(encrypt(data, keyHex));
	}

	/**
	 * ECB 加密，输出 Hex。
	 */
	public static String encryptToHex(String data, String keyHex) {
		return encryptToHex(data.getBytes(HexUtil.DEFAULT_CHARSET), keyHex);
	}

	/**
	 * ECB 解密。
	 *
	 * @param data   密文
	 * @param keyHex 密钥（Hex）
	 */
	public static byte[] decrypt(byte[] data, String keyHex) {
		return doCipher(Cipher.DECRYPT_MODE, data, keyHex);
	}

	/**
	 * ECB 解密为字符串。
	 */
	public static String decryptToString(byte[] data, String keyHex) {
		return new String(decrypt(data, keyHex), HexUtil.DEFAULT_CHARSET);
	}

	/**
	 * 从 Base64 密文 ECB 解密。
	 */
	public static byte[] decryptFromBase64(String dataBase64, String keyHex) {
		return decrypt(Base64Util.decodeToBytes(dataBase64), keyHex);
	}

	/**
	 * 从 Base64 密文 ECB 解密为字符串。
	 */
	public static String decryptFromBase64ToString(String dataBase64, String keyHex) {
		return new String(decryptFromBase64(dataBase64, keyHex), HexUtil.DEFAULT_CHARSET);
	}

	/**
	 * 从 Hex 密文 ECB 解密。
	 */
	public static byte[] decryptFromHex(String dataHex, String keyHex) {
		return decrypt(HexUtil.decodeHex(dataHex), keyHex);
	}

	/**
	 * 从 Hex 密文 ECB 解密为字符串。
	 */
	public static String decryptFromHexToString(String dataHex, String keyHex) {
		return new String(decryptFromHex(dataHex, keyHex), HexUtil.DEFAULT_CHARSET);
	}

	// ==================== CBC + 自动拼接 IV ====================

	/**
	 * CBC 加密：内部生成 IV，返回 {@code IV + 密文}。
	 *
	 * @param data   明文
	 * @param keyHex 密钥（Hex）
	 */
	public static byte[] encryptIv(byte[] data, String keyHex) {
		byte[] iv = generateIv();
		return concat(iv, doCipherCbc(Cipher.ENCRYPT_MODE, data, keyHex, iv));
	}

	/**
	 * CBC 加密：内部生成 IV，返回 {@code IV + 密文}。
	 */
	public static byte[] encryptIv(String data, String keyHex) {
		return encryptIv(data.getBytes(HexUtil.DEFAULT_CHARSET), keyHex);
	}

	/**
	 * CBC 加密（IV 已拼接），输出 Base64。
	 */
	public static String encryptIvToBase64(byte[] data, String keyHex) {
		return Base64Util.encode(encryptIv(data, keyHex));
	}

	/**
	 * CBC 加密（IV 已拼接），输出 Base64。
	 */
	public static String encryptIvToBase64(String data, String keyHex) {
		return encryptIvToBase64(data.getBytes(HexUtil.DEFAULT_CHARSET), keyHex);
	}

	/**
	 * CBC 加密（IV 已拼接），输出 Hex。
	 */
	public static String encryptIvToHex(byte[] data, String keyHex) {
		return HexUtil.encodeHexString(encryptIv(data, keyHex));
	}

	/**
	 * CBC 加密（IV 已拼接），输出 Hex。
	 */
	public static String encryptIvToHex(String data, String keyHex) {
		return encryptIvToHex(data.getBytes(HexUtil.DEFAULT_CHARSET), keyHex);
	}

	/**
	 * CBC 解密：从 {@code IV + 密文} 中拆出 IV 再解密。
	 *
	 * @param combined IV + 密文
	 * @param keyHex   密钥（Hex）
	 */
	public static byte[] decryptIv(byte[] combined, String keyHex) {
		byte[] iv = Arrays.copyOfRange(combined, 0, IV_LENGTH);
		byte[] cipherText = Arrays.copyOfRange(combined, IV_LENGTH, combined.length);
		return doCipherCbc(Cipher.DECRYPT_MODE, cipherText, keyHex, iv);
	}

	/**
	 * CBC 解密为字符串。
	 */
	public static String decryptIvToString(byte[] combined, String keyHex) {
		return new String(decryptIv(combined, keyHex), HexUtil.DEFAULT_CHARSET);
	}

	/**
	 * 从 Base64（IV + 密文）CBC 解密。
	 */
	public static byte[] decryptIvFromBase64(String combinedBase64, String keyHex) {
		return decryptIv(Base64Util.decodeToBytes(combinedBase64), keyHex);
	}

	/**
	 * 从 Base64（IV + 密文）CBC 解密为字符串。
	 */
	public static String decryptIvFromBase64ToString(String combinedBase64, String keyHex) {
		return new String(decryptIvFromBase64(combinedBase64, keyHex), HexUtil.DEFAULT_CHARSET);
	}

	/**
	 * 从 Hex（IV + 密文）CBC 解密。
	 */
	public static byte[] decryptIvFromHex(String combinedHex, String keyHex) {
		return decryptIv(HexUtil.decodeHex(combinedHex), keyHex);
	}

	/**
	 * 从 Hex（IV + 密文）CBC 解密为字符串。
	 */
	public static String decryptIvFromHexToString(String combinedHex, String keyHex) {
		return new String(decryptIvFromHex(combinedHex, keyHex), HexUtil.DEFAULT_CHARSET);
	}

	private static byte[] generateKey(int keySize) {
		try {
			KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM, BC_PROVIDER);
			keyGenerator.init(keySize, SECURE_RANDOM);
			return keyGenerator.generateKey().getEncoded();
		} catch (Exception e) {
			throw new IllegalStateException("生成 SM4 密钥失败", e);
		}
	}

	private static byte[] generateIv() {
		byte[] iv = new byte[IV_LENGTH];
		SECURE_RANDOM.nextBytes(iv);
		return iv;
	}

	private static byte[] doCipher(int mode, byte[] data, String keyHex) {
		try {
			Cipher cipher = Cipher.getInstance(TRANSFORMATION, BC_PROVIDER);
			cipher.init(mode, new SecretKeySpec(HexUtil.decodeHex(keyHex), ALGORITHM));
			return cipher.doFinal(data);
		} catch (Exception e) {
			throw new IllegalArgumentException("SM4 加解密失败", e);
		}
	}

	private static byte[] doCipherCbc(int mode, byte[] data, String keyHex, byte[] iv) {
		try {
			Cipher cipher = Cipher.getInstance(TRANSFORMATION_CBC, BC_PROVIDER);
			cipher.init(mode, new SecretKeySpec(HexUtil.decodeHex(keyHex), ALGORITHM), new IvParameterSpec(iv));
			return cipher.doFinal(data);
		} catch (Exception e) {
			throw new IllegalArgumentException("SM4 CBC 加解密失败", e);
		}
	}

	private static byte[] concat(byte[] first, byte[] second) {
		byte[] result = new byte[first.length + second.length];
		System.arraycopy(first, 0, result, 0, first.length);
		System.arraycopy(second, 0, result, first.length, second.length);
		return result;
	}
}
