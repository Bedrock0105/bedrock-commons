package org.bedrock.common.code.util;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;

/**
 * 国密 SM3 摘要工具类。
 * <p>
 * 基于 BouncyCastle，风格对齐 {@link DigestUtil}，提供 byte[] / String 及 Hex / Base64 便捷方法。
 */
public abstract class Sm3Util {

	/**
	 * SM3 算法名称
	 */
	public static final String SM3 = "SM3";

	private static final String BC_PROVIDER = "BC";

	static {
		if (Security.getProvider(BC_PROVIDER) == null) {
			Security.addProvider(new BouncyCastleProvider());
		}
	}

	private Sm3Util() {
	}

	/**
	 * 计算 SM3 摘要，返回字节数组。
	 *
	 * @param data 待摘要数据
	 */
	public static byte[] sm3(final byte[] data) {
		return getDigest().digest(data);
	}

	/**
	 * 计算 SM3 摘要，返回字节数组。
	 *
	 * @param data 待摘要字符串（UTF-8）
	 */
	public static byte[] sm3(final String data) {
		return sm3(data.getBytes(HexUtil.DEFAULT_CHARSET));
	}

	/**
	 * 计算 SM3 摘要，返回 Hex 字符串。
	 *
	 * @param data 待摘要数据
	 */
	public static String sm3Hex(final byte[] data) {
		return HexUtil.encodeHexString(sm3(data));
	}

	/**
	 * 计算 SM3 摘要，返回 Hex 字符串。
	 *
	 * @param data 待摘要字符串（UTF-8）
	 */
	public static String sm3Hex(final String data) {
		return HexUtil.encodeHexString(sm3(data));
	}

	/**
	 * 计算 SM3 摘要，返回 Base64 字符串。
	 *
	 * @param data 待摘要数据
	 */
	public static String sm3Base64(final byte[] data) {
		return Base64Util.encode(sm3(data));
	}

	/**
	 * 计算 SM3 摘要，返回 Base64 字符串。
	 *
	 * @param data 待摘要字符串（UTF-8）
	 */
	public static String sm3Base64(final String data) {
		return Base64Util.encode(sm3(data));
	}

	/**
	 * 获取 SM3 MessageDigest 实例。
	 */
	public static MessageDigest getDigest() {
		try {
			return MessageDigest.getInstance(SM3, BC_PROVIDER);
		} catch (final NoSuchAlgorithmException | NoSuchProviderException e) {
			throw new IllegalArgumentException(e);
		}
	}
}
