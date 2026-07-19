package org.bedrock.common.code.util;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 参考 org.apache.commons.codec.binary.Hex
 */
public abstract class HexUtil {

	/**
	 * Default charset is {@link StandardCharsets#UTF_8}.
	 *
	 * @since 1.7
	 */
	public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

	/**
	 * Default charset name is {@link StandardCharsets#UTF_8}.
	 */
	public static final String DEFAULT_CHARSET_NAME = StandardCharsets.UTF_8.name();

	/**
	 * Used to build output as hex.
	 */
	private static final char[] DIGITS_LOWER = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd',
		'e', 'f'};

	/**
	 * Used to build output as hex.
	 */
	private static final char[] DIGITS_UPPER = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D',
		'E', 'F'};

	/**
	 * Decodes a hexadecimal string into an array of bytes.
	 */
	public static byte[] decodeHex(final String data) {
		return decodeHex(data.getBytes(DEFAULT_CHARSET));
	}

	/**
	 * Decodes a hexadecimal string into a string.
	 */
	public static String decodeHexToString(final byte[] data, Charset charset) {
		return new String(decodeHex(data), charset);
	}

	/**
	 * Decodes a hexadecimal string into a string.
	 */
	public static String decodeHexToString(final byte[] data) {
		return new String(decodeHex(data), DEFAULT_CHARSET);
	}

	/**
	 * Decodes a hexadecimal string into a string.
	 */
	public static String decodeHexToString(final String data) {
		return decodeHexToString(data, DEFAULT_CHARSET);
	}

	/**
	 * Decodes a hexadecimal string into a string.
	 */
	public static String decodeHexToString(final String data, Charset charset) {
		return new String(decodeHex(data.getBytes(charset)), charset);
	}

	/**
	 * Encodes a byte[] to a hexadecimal string.
	 */
	public static String encodeHexString(final byte[] data) {
		return new String(encodeHex(data, true));
	}

	/**
	 * Encodes a byte[] to a hexadecimal string.
	 */
	public static String encodeHexString(final byte[] data, final boolean toLowerCase) {
		return new String(encodeHex(data, toLowerCase));
	}

	/**
	 * Encodes a byte[] to a hexadecimal string.
	 */
	public static String encodeHexString(final ByteBuffer data) {
		return new String(encodeHex(data, true));
	}

	/**
	 * Encodes a byte[] to a hexadecimal string.
	 */
	public static String encodeHexString(final ByteBuffer data, final boolean toLowerCase) {
		return new String(encodeHex(data, toLowerCase));
	}

	/**
	 * Encodes a byte[] to a hexadecimal string.
	 */
	public static char[] encodeHex(final ByteBuffer data, boolean toLowerCase) {
		return encodeHex(toByteArray(data), toLowerCase);
	}

	/**
	 * Encodes a byte[] to a hexadecimal string.
	 */
	public static char[] encodeHex(final byte[] data, boolean toLowerCase) {
		return encodeHex(data, toLowerCase ? DIGITS_LOWER : DIGITS_UPPER);
	}

	/**
	 * Encodes a byte[] to a hexadecimal string.
	 */
	private static char[] encodeHex(byte[] data, char[] HEX_CHARS) {
		int l = data.length;
		char[] out = new char[l << 1];
		for (int i = 0, j = 0; i < l; i++) {
			out[j++] = HEX_CHARS[(0xF0 & data[i]) >>> 4];
			out[j++] = HEX_CHARS[0x0F & data[i]];
		}
		return out;
	}

	/**
	 * Decodes a hexadecimal string into an array of bytes.
	 */
	public static byte[] decodeHex(final byte[] data) {
		final int len = data.length;
		byte[] out = new byte[len >> 1];
		// two characters form the hex value.
		for (int i = 0, j = 0; j < len; i++) {
			int f = toDigit(data[j], j) << 4;
			j++;
			f = f | toDigit(data[j], j);
			j++;
			out[i] = (byte) (f & 0xFF);
		}
		return out;
	}

	/**
	 * Converts a hexadecimal character to an integer.
	 */
	protected static int toDigit(final byte ch, final int index) {
		final int digit = Character.digit(ch, 16);
		if (digit == -1) {
			throw new IllegalArgumentException("Illegal hexadecimal character " + ch + " at index " + index);
		}
		return digit;
	}

	/**
	 * Converts a ByteBuffer to a byte[]
	 */
	private static byte[] toByteArray(final ByteBuffer byteBuffer) {
		final int remaining = byteBuffer.remaining();
		// Use the underlying buffer if possible
		if (byteBuffer.hasArray()) {
			final byte[] byteArray = byteBuffer.array();
			if (remaining == byteArray.length) {
				byteBuffer.position(remaining);
				return byteArray;
			}
		}
		// Copy the bytes
		final byte[] byteArray = new byte[remaining];
		byteBuffer.get(byteArray);
		return byteArray;
	}
}
