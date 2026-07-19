package org.bedrock.common.code.util;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.io.IOException;
import java.io.InputStream;

public abstract class IoUtil extends org.springframework.util.StreamUtils {

	public static final byte[] EMPTY_CONTENT = new byte[0];

	/**
	 * 读取输入流 转byte数组
	 */
	@NonNull
	public static byte[] copyToByteArray(@Nullable InputStream inputStream) {
		try {
			return org.springframework.util.StreamUtils.copyToByteArray(inputStream);
		} catch (IOException e) {
			return EMPTY_CONTENT;
		}
	}
}
