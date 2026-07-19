package org.bedrock.common.code.util;

import lombok.Getter;

import java.util.concurrent.ThreadLocalRandom;

public abstract class RandomUtil {

	private static ThreadLocalRandom random() {
		return ThreadLocalRandom.current();
	}

	/**
	 * 获取指定字符数包括数字、字母（不分大小写）
	 */
	public static String random(int count) {
		return random(count, RandomTypeEnum.ALL);
	}

	/**
	 * 获取指定字符数
	 * 指定生成的字符类型
	 */
	public static String random(int count, RandomTypeEnum randomType) {
		return random(count, randomType.getChars().toCharArray());
	}

	/**
	 * 获取指定字符数
	 * 指定生成的字符类型
	 */
	public static String random(int count, char[] chars) {
		if (chars == null || chars.length == 0) {
			return null;
		}
		char[] charsResult = new char[count];
		for (int i = 0; i < count; i++) {
			charsResult[i] = chars[random().nextInt(chars.length)];
		}
		return new String(charsResult);
	}

	@Getter
	public enum RandomTypeEnum {
		NUMBER("0123456789"),
		STRING("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"),
		ALL("0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ");

		private final String chars;

		RandomTypeEnum(String chars) {
			this.chars = chars;
		}
	}

}
