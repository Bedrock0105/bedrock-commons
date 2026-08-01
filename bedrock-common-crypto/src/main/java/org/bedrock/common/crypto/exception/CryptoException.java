package org.bedrock.common.crypto.exception;

/**
 * API 加解密过程中的运行时异常。
 */
public class CryptoException extends RuntimeException {

	/**
	 * @param message 错误信息
	 */
	public CryptoException(String message) {
		super(message);
	}

	/**
	 * @param message 错误信息
	 * @param cause   原因
	 */
	public CryptoException(String message, Throwable cause) {
		super(message, cause);
	}
}
