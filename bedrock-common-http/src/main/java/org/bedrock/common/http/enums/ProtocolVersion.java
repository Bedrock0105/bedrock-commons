package org.bedrock.common.http.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.Comparator;

/**
 * HTTP协议版本枚举
 */
@Getter
public enum ProtocolVersion {
	NONE("none", 0, 0),// 未指定
	HTTP_1_0("HTTP/1.0", 1, 0),
	HTTP_1_1("HTTP/1.1", 1, 1),
	HTTP_2("HTTP/2", 2, 0),
	HTTP_3("HTTP/3", 3, 0);

	private final String text;

	private final int major;

	private final int minor;

	private static final ProtocolVersion DEFAULT = HTTP_1_1;

	private static final ProtocolVersion LATEST = HTTP_3;

	ProtocolVersion(String text, int major, int minor) {
		this.text = text;
		this.major = major;
		this.minor = minor;
	}

	/**
	 * 获取默认HTTP版本
	 */
	public static ProtocolVersion getDefault() {
		return DEFAULT;
	}

	/**
	 * 获取最新HTTP版本
	 */
	public static ProtocolVersion getLatest() {
		return LATEST;
	}

	/**
	 * 从字符串解析HTTP版本
	 */
	public static ProtocolVersion parse(String version) {
		if (version == null || version.trim().isEmpty()) {
			return DEFAULT;
		}

		String normalized = version.trim().toUpperCase();

		// 直接匹配枚举值
		for (ProtocolVersion v : values()) {
			if (v.text.equalsIgnoreCase(normalized)) {
				return v;
			}
		}

		// 尝试解析版本号
		try {
			String[] parts = normalized.replace("HTTP/", "").split("\\.");
			int major = Integer.parseInt(parts[0]);
			int minor = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;

			return findClosest(major, minor);
		} catch (Exception e) {
			return DEFAULT;
		}
	}

	/**
	 * 是否支持流水线
	 */
	public boolean supportsPipelining() {
		return this == HTTP_1_1 || this == HTTP_2 || this == HTTP_3;
	}

	/**
	 * 是否支持多路复用
	 */
	public boolean supportsMultiplexing() {
		return this == HTTP_2 || this == HTTP_3;
	}

	private static ProtocolVersion findClosest(int major, int minor) {
		return Arrays.stream(values())
			.filter(v -> v.major == major && v.minor == minor)
			.findFirst()
			.orElseGet(() -> Arrays.stream(values())
				.filter(v -> v.major == major)
				.min(Comparator.comparingInt(v -> Math.abs(v.minor - minor)))
				.orElse(DEFAULT));
	}

	@Override
	public String toString() {
		return text;
	}
}
