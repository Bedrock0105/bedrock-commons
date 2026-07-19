package org.bedrock.common.code.util;

import org.springframework.util.DigestUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 摘要工具类
 * org.apache.commons.codec.digest.DigestUtil
 */
public abstract class DigestUtil extends org.springframework.util.DigestUtils {

	/**
	 * The MD2 message digest algorithm defined in RFC 1319.
	 */
	public static final String MD2 = "MD2";

	/**
	 * The SHA-1 hash algorithm defined in the FIPS PUB 180-2.
	 */
	public static final String SHA_1 = "SHA-1";

	/**
	 * The SHA-224 hash algorithm defined in the FIPS PUB 180-3.
	 */
	public static final String SHA_224 = "SHA-224";

	/**
	 * The SHA-256 hash algorithm defined in the FIPS PUB 180-2.
	 */
	public static final String SHA_256 = "SHA-256";

	/**
	 * The SHA-384 hash algorithm defined in the FIPS PUB 180-2.
	 */
	public static final String SHA_384 = "SHA-384";

	/**
	 * The SHA-512 hash algorithm defined in the FIPS PUB 180-2.
	 */
	public static final String SHA_512 = "SHA-512";

	/**
	 * The SHA-512 hash algorithm defined in the FIPS PUB 180-4.
	 */
	public static final String SHA_512_224 = "SHA-512/224";

	/**
	 * The SHA-512 hash algorithm defined in the FIPS PUB 180-4.
	 */
	public static final String SHA_512_256 = "SHA-512/256";

	/**
	 * The SHA3-224 hash algorithm defined in the FIPS PUB 202.
	 */
	public static final String SHA3_224 = "SHA3-224";

	/**
	 * The SHA3-256 hash algorithm defined in the FIPS PUB 202.
	 */
	public static final String SHA3_256 = "SHA3-256";

	/**
	 * The SHA3-384 hash algorithm defined in the FIPS PUB 202.
	 */
	public static final String SHA3_384 = "SHA3-384";

	/**
	 * The SHA3-512 hash algorithm defined in the FIPS PUB 202.
	 */
	public static final String SHA3_512 = "SHA3-512";

	/**
	 * Calculates the MD2 digest and returns the value as a 16 element {@code byte[]}.
	 */
	public static byte[] md2(final byte[] data) {
		return getDigest(MD2).digest(data);
	}

	/**
	 * Calculates the MD2 digest and returns the value as a 16 element {@code byte[]}.
	 */
	public static byte[] md2(final String data) {
		return md2(data.getBytes(HexUtil.DEFAULT_CHARSET));
	}

	/**
	 * Calculates the MD2 digest and returns the value as a 32 character hex string.
	 */
	public static String md2Hex(final byte[] data) {
		return HexUtil.encodeHexString(md2(data));
	}

	/**
	 * Calculates the MD2 digest and returns the value as a 32 character hex string.
	 */
	public static String md2Hex(final String data) {
		return HexUtil.encodeHexString(md2(data));
	}

	/**
	 * Return a hexadecimal bytes representation of the MD5 digest of the given bytes.
	 */
	public static byte[] md5(final byte[] data) {
		return DigestUtils.md5Digest(data);
	}

	/**
	 * Return a hexadecimal bytes representation of the MD5 digest of the given string.
	 */
	public static byte[] md5(final String data) {
		return md5(data.getBytes(HexUtil.DEFAULT_CHARSET));
	}

	/**
	 * Return a hexadecimal string representation of the MD5 digest of the given bytes.
	 */
	public static String md5Hex(final byte[] data) {
		return DigestUtils.md5DigestAsHex(data);
	}

	/**
	 * Return a hexadecimal string representation of the MD5 digest of the given string.
	 */
	public static String md5Hex(final String data) {
		return md5Hex(data.getBytes(HexUtil.DEFAULT_CHARSET));
	}

	/**
	 * Calculates the SHA-1 digest and returns the value as a {@code byte[]}.
	 */
	public static byte[] sha1(final byte[] data) {
		return getDigest(SHA_1).digest(data);
	}

	/**
	 * Calculates the SHA-1 digest and returns the value as a {@code byte[]}.
	 */
	public static byte[] sha1(final String data) {
		return sha1(data.getBytes(HexUtil.DEFAULT_CHARSET));
	}

	/**
	 * Calculates the SHA-1 digest and returns the value as a hex string.
	 */
	public static String sha1Hex(final byte[] data) {
		return HexUtil.encodeHexString(sha1(data));
	}

	/**
	 * Calculates the SHA-1 digest and returns the value as a hex string.
	 */
	public static String sha1Hex(final String data) {
		return HexUtil.encodeHexString(sha1(data));
	}

	/**
	 * Calculates the SHA_224 digest and returns the value as a {@code byte[]}.
	 */
	public static byte[] sha224(final byte[] data) {
		return getDigest(SHA_224).digest(data);
	}

	/**
	 * Calculates the SHA_224 digest and returns the value as a {@code byte[]}.
	 */
	public static byte[] sha224(final String data) {
		return sha224(data.getBytes(HexUtil.DEFAULT_CHARSET));
	}

	/**
	 * Calculates the SHA_224 digest and returns the value as a hex string.
	 */
	public static String sha224Hex(final byte[] data) {
		return HexUtil.encodeHexString(sha256(data));
	}

	/**
	 * Calculates the SHA_224 digest and returns the value as a hex string.
	 */
	public static String sha224Hex(final String data) {
		return HexUtil.encodeHexString(sha256(data));
	}

	/**
	 * Calculates the SHA-256 digest and returns the value as a {@code byte[]}.
	 */
	public static byte[] sha256(final byte[] data) {
		return getDigest(SHA_256).digest(data);
	}

	/**
	 * Calculates the SHA-256 digest and returns the value as a {@code byte[]}.
	 */
	public static byte[] sha256(final String data) {
		return sha256(data.getBytes(HexUtil.DEFAULT_CHARSET));
	}

	/**
	 * Calculates the SHA-256 digest and returns the value as a hex string.
	 */
	public static String sha256Hex(final byte[] data) {
		return HexUtil.encodeHexString(sha256(data));
	}

	/**
	 * Calculates the SHA-256 digest and returns the value as a hex string.
	 */
	public static String sha256Hex(final String data) {
		return HexUtil.encodeHexString(sha256(data));
	}

	/**
	 * Calculates the SHA-384 digest and returns the value as a {@code byte[]}.
	 */
	public static byte[] sha384(final byte[] data) {
		return getDigest(SHA_384).digest(data);
	}

	/**
	 * Calculates the SHA-384 digest and returns the value as a {@code byte[]}.
	 */
	public static byte[] sha384(final String data) {
		return sha384(data.getBytes(HexUtil.DEFAULT_CHARSET));
	}

	/**
	 * Calculates the SHA-384 digest and returns the value as a hex string.
	 */
	public static String sha384Hex(final byte[] data) {
		return HexUtil.encodeHexString(sha384(data));
	}

	/**
	 * Calculates the SHA-384 digest and returns the value as a hex string.
	 */
	public static String sha384Hex(final String data) {
		return HexUtil.encodeHexString(sha384(data));
	}

	/**
	 * Calculates the SHA-512 digest and returns the value as a {@code byte[]}.
	 */
	public static byte[] sha512(final byte[] data) {
		return getDigest(SHA_512).digest(data);
	}

	/**
	 * Calculates the SHA-512 digest and returns the value as a {@code byte[]}.
	 */
	public static byte[] sha512(final String data) {
		return sha512(data.getBytes(HexUtil.DEFAULT_CHARSET));
	}

	/**
	 * Calculates the SHA-512 digest and returns the value as a hex string.
	 */
	public static String sha512Hex(final byte[] data) {
		return HexUtil.encodeHexString(sha512(data));
	}

	/**
	 * Calculates the SHA-512 digest and returns the value as a hex string.
	 */
	public static String sha512Hex(final String data) {
		return HexUtil.encodeHexString(sha512(data));
	}

	/**
	 * Calculates the SHA-512/224 digest and returns the value as a {@code byte[]}.
	 */
	public static byte[] sha512224(final byte[] data) {
		return getDigest(SHA_512_224).digest(data);
	}

	/**
	 * Calculates the SHA-512/224 digest and returns the value as a {@code byte[]}.
	 */
	public static byte[] sha512224(final String data) {
		return sha512224(data.getBytes(HexUtil.DEFAULT_CHARSET));
	}

	/**
	 * Calculates the SHA-512/224 digest and returns the value as a hex string.
	 */
	public static String sha512_224Hex(final byte[] data) {
		return HexUtil.encodeHexString(sha512224(data));
	}

	/**
	 * Calculates the SHA-512/224 digest and returns the value as a hex string.
	 */
	public static String sha512224Hex(final String data) {
		return HexUtil.encodeHexString(sha512224(data));
	}

	/**
	 * Calculates the SHA-512/256 digest and returns the value as a {@code byte[]}.
	 */
	public static byte[] sha512256(final byte[] data) {
		return getDigest(SHA_512_256).digest(data);
	}

	/**
	 * Calculates the SHA-512/256 digest and returns the value as a {@code byte[]}.
	 */
	public static byte[] sha512256(final String data) {
		return sha512256(data.getBytes(HexUtil.DEFAULT_CHARSET));
	}

	/**
	 * Calculates the SHA-512/256 digest and returns the value as a hex string.
	 */
	public static String sha512_256Hex(final byte[] data) {
		return HexUtil.encodeHexString(sha512256(data));
	}

	/**
	 * Calculates the SHA-512/256 digest and returns the value as a hex string.
	 */
	public static String sha512256Hex(final String data) {
		return HexUtil.encodeHexString(sha512256(data));
	}

	/**
	 * Calculates the SHA3-224 digest and returns the value as a {@code byte[]}.
	 */
	public static byte[] sha3224(final byte[] data) {
		return getDigest(SHA3_224).digest(data);
	}

	/**
	 * Calculates the SHA3-224 digest and returns the value as a {@code byte[]}.
	 */
	public static byte[] sha3224(final String data) {
		return sha3224(data.getBytes(HexUtil.DEFAULT_CHARSET));
	}

	/**
	 * Calculates the SHA3-224 digest and returns the value as a hex string.
	 *
	 * @param data Data to digest
	 * @return SHA3-224 digest as a hex string
	 * @since 1.12
	 */
	public static String sha3224Hex(final byte[] data) {
		return HexUtil.encodeHexString(sha3224(data));
	}

	/**
	 * Calculates the SHA3-224 digest and returns the value as a hex string.
	 */
	public static String sha3224Hex(final String data) {
		return HexUtil.encodeHexString(sha3224(data));
	}

	/**
	 * Calculates the SHA3-256 digest and returns the value as a {@code byte[]}.
	 */
	public static byte[] sha3256(final byte[] data) {
		return getDigest(SHA3_256).digest(data);
	}

	/**
	 * Calculates the SHA3-256 digest and returns the value as a {@code byte[]}.
	 */
	public static byte[] sha3256(final String data) {
		return sha3256(data.getBytes(HexUtil.DEFAULT_CHARSET));
	}

	/**
	 * Calculates the SHA3-256 digest and returns the value as a hex string.
	 */
	public static String sha3_256Hex(final byte[] data) {
		return HexUtil.encodeHexString(sha3256(data));
	}

	/**
	 * Calculates the SHA3-256 digest and returns the value as a hex string.
	 */
	public static String sha3256Hex(final String data) {
		return HexUtil.encodeHexString(sha3256(data));
	}

	/**
	 * Calculates the SHA3-384 digest and returns the value as a {@code byte[]}.
	 */
	public static byte[] sha3384(final byte[] data) {
		return getDigest(SHA3_384).digest(data);
	}

	/**
	 * Calculates the SHA3-384 digest and returns the value as a {@code byte[]}.
	 */
	public static byte[] sha3384(final String data) {
		return sha3384(data.getBytes(HexUtil.DEFAULT_CHARSET));
	}

	/**
	 * Calculates the SHA3-384 digest and returns the value as a hex string.
	 */
	public static String sha3384Hex(final byte[] data) {
		return HexUtil.encodeHexString(sha3384(data));
	}

	/**
	 * Calculates the SHA3-384 digest and returns the value as a hex string.
	 */
	public static String sha3384Hex(final String data) {
		return HexUtil.encodeHexString(sha3384(data));
	}

	/**
	 * Calculates the SHA3-512 digest and returns the value as a {@code byte[]}.
	 */
	public static byte[] sha3512(final byte[] data) {
		return getDigest(SHA3_512).digest(data);
	}

	/**
	 * Calculates the SHA3-512 digest and returns the value as a {@code byte[]}.
	 */
	public static byte[] sha3512(final String data) {
		return sha3512(data.getBytes(HexUtil.DEFAULT_CHARSET));
	}

	/**
	 * Calculates the SHA3-512 digest and returns the value as a hex string.
	 */
	public static String sha3512Hex(final byte[] data) {
		return HexUtil.encodeHexString(sha3512(data));
	}

	/**
	 * Calculates the SHA3-512 digest and returns the value as a hex string.
	 */
	public static String sha3512Hex(final String data) {
		return HexUtil.encodeHexString(sha3512(data));
	}

	public static MessageDigest getDigest(final String algorithm) {
		try {
			return MessageDigest.getInstance(algorithm);
		} catch (final NoSuchAlgorithmException e) {
			throw new IllegalArgumentException(e);
		}
	}
}
