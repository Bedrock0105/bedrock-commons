package org.bedrock.common.code.util;

import org.bedrock.common.code.support.PoolSupport;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public abstract class DateUtil {

	public static final String PATTERN_DATE = "yyyy-MM-dd";

	public static final String PATTERN_TIME = "HH:mm:ss";

	public static final String PATTERN_DATETIME = PATTERN_DATE + " " + PATTERN_TIME;

	/**
	 * java 8 时间格式化
	 */
	public static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern(PATTERN_DATETIME);

	public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(PATTERN_DATE);

	public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern(PATTERN_TIME);

	public static final DateTimeFormatter DATE_FORMATTER_NO_SEPARATION = DateTimeFormatter.ofPattern("yyyyMMdd");

	/**
	 * java date 时间格式化
	 */
	public static final PoolSupport<SimpleDateFormat> SIMPLE_DATETIME_FORMATTER = PoolSupport.builder(() -> new SimpleDateFormat(PATTERN_DATETIME)).maximumCapacity(1000).build();

	public static final PoolSupport<SimpleDateFormat> SIMPLE_DATE_FORMATTER = PoolSupport.builder(() -> new SimpleDateFormat(PATTERN_DATE)).maximumCapacity(100).build();

	public static final PoolSupport<SimpleDateFormat> SIMPLE_TIME_FORMATTER = PoolSupport.builder(() -> new SimpleDateFormat(PATTERN_TIME)).maximumCapacity(100).build();

	/**
	 * 获取当前时间
	 */
	public static Date now() {
		return new Date();
	}

	/**
	 * 格式化时间
	 */
	public static String formatDateTime(Date date) {
		return format(date, SIMPLE_DATETIME_FORMATTER);
	}

	/**
	 * 格式化时间
	 */
	public static String formatDate(Date date) {
		return format(date, SIMPLE_DATE_FORMATTER);
	}

	/**
	 * 格式化时间
	 */
	public static String formatTime(Date date) {
		return format(date, SIMPLE_TIME_FORMATTER);
	}

	/**
	 * 格式化时间
	 */
	public static String format(Date date, String pattern) {
		return new SimpleDateFormat(pattern).format(date);
	}

	/**
	 * 格式化时间
	 */
	public static String format(Date date, PoolSupport<SimpleDateFormat> pattern) {
		return pattern.run(formatter -> formatter.format(date));
	}

	/**
	 * 解析时间
	 */
	public static Date parseDateTime(String date) {
		return parse(date, SIMPLE_DATETIME_FORMATTER);
	}

	/**
	 * 解析时间
	 */
	public static Date parseDate(String date) {
		return parse(date, SIMPLE_DATE_FORMATTER);
	}

	/**
	 * 解析时间
	 */
	public static Date parseTime(String date) {
		return parse(date, SIMPLE_TIME_FORMATTER);
	}

	/**
	 * 解析时间
	 */
	public static Date parse(String date, String pattern) {
		try {
			return new SimpleDateFormat(pattern).parse(date);
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 解析时间
	 */
	public static Date parse(String date, PoolSupport<SimpleDateFormat> formatter) {
		return formatter.run(format -> {
			try {
				return format.parse(date);
			} catch (ParseException e) {
				throw new RuntimeException(e);
			}
		});
	}

	/**
	 * Date 转 LocalDateTime
	 */
	public static LocalDateTime toLocalDateTime(Date date) {
		ZoneId zoneId = ZoneId.systemDefault();
		return LocalDateTime.ofInstant(date.toInstant(), zoneId);
	}

	/**
	 * Date 转 LocalDate
	 */
	public static LocalDate toLocalDate(Date date) {
		ZoneId zoneId = ZoneId.systemDefault();
		return LocalDate.ofInstant(date.toInstant(), zoneId);
	}

	/**
	 * LocalDateTime 转 Date
	 */
	public static Date toDate(LocalDateTime localDateTime) {
		ZoneId zoneId = ZoneId.systemDefault();
		Instant instant = localDateTime.atZone(zoneId).toInstant();
		return Date.from(instant);
	}

	/**
	 * LocalDate 转 Date
	 */
	public static Date toDate(LocalDate localDate) {
		ZoneId zoneId = ZoneId.systemDefault();
		Instant instant = LocalDateTime.of(localDate, LocalTime.of(0, 0, 0)).atZone(zoneId).toInstant();
		return Date.from(instant);
	}
}
