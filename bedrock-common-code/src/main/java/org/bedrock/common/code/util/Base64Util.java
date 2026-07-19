package org.bedrock.common.code.util;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public abstract class Base64Util {

	/**
	 * Base64编码
	 */
	private final static Base64.Encoder ENCODER = Base64.getEncoder();

	/**
	 * Base64解码
	 */
	private final static Base64.Decoder DECODER = Base64.getDecoder();

	/**
	 * Base64编码
	 */
	public static String encode(String str) {
		return encode(str, StandardCharsets.UTF_8);
	}

	/**
	 * Base64编码
	 */
	public static String encode(String str, Charset charset) {
		return new String(ENCODER.encode(str.getBytes(charset)), charset);
	}

	/**
	 * Base64编码
	 */
	public static String encode(byte[] bytes) {
		return encode(bytes, StandardCharsets.UTF_8);
	}

	/**
	 * Base64编码
	 */
	public static String encode(byte[] bytes, Charset charset) {
		return new String(ENCODER.encode(bytes), charset);
	}

	/**
	 * Base64编码
	 */
	public static byte[] encodeToBytes(String str) {
		return encodeToBytes(str, StandardCharsets.UTF_8);
	}

	/**
	 * Base64编码
	 */
	public static byte[] encodeToBytes(String str, Charset charset) {
		return ENCODER.encode(str.getBytes(charset));
	}

	/**
	 * Base64编码
	 */
	public static byte[] encodeToBytes(byte[] bytes) {
		return ENCODER.encode(bytes);
	}

	/**
	 * Base64解码
	 */
	public static String decode(String str) {
		return decode(str, StandardCharsets.UTF_8);
	}

	/**
	 * Base64解码
	 */
	public static String decode(String str, Charset charset) {
		return new String(DECODER.decode(str), charset);
	}

	/**
	 * Base64解码
	 */
	public static byte[] decodeToBytes(String str) {
		return DECODER.decode(str);
	}

	/**
	 * Base64解码
	 */
	public static byte[] decodeToBytes(byte[] bytes) {
		return DECODER.decode(bytes);
	}
}
