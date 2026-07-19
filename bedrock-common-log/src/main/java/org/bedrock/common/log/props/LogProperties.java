package org.bedrock.common.log.props;

import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = LogProperties.PREFIX)
public class LogProperties {

	public static final String PREFIX = "bedrock.log.request";

	/**
	 * 是否启用
	 */
	private boolean enabled = true;

	/**
	 * 日志级别
	 */
	private LogLevel level = LogLevel.INFO;

	/**
	 * 每行的最大长度
	 */
	private Integer maxLength = Integer.MAX_VALUE;

	/**
	 * 不打印日志的接口
	 */
	private List<String> exclude = List.of();

	/**
	 * 不打印日志的Handler
	 */
	private String[] excludedHandlers = new String[0];

	/**
	 * 是否打印Handler的日志
	 */
	private boolean handlersLog = true;

	/**
	 * 是否打印方法参数日志
	 */
	private boolean methodParamsLog = true;

	/**
	 * 是否打印方法返回日志
	 */
	private boolean methodReturnLog = true;

	/**
	 * 是否打印方法返回值日志
	 */
	private boolean methodReturnMsgLog = true;

	/**
	 * 日志级别
	 */
	public enum LogLevel {
		TRACE() {
			@Override
			public void log(Logger logger, String message, Object... args) {
				if (logger.isTraceEnabled()) {
					logger.trace(message, args);
				}
			}
		},

		DEBUG() {
			@Override
			public void log(Logger logger, String message, Object... args) {
				if (logger.isDebugEnabled()) {
					logger.debug(message, args);
				}
			}
		},

		INFO() {
			@Override
			public void log(Logger logger, String message, Object... args) {
				if (logger.isInfoEnabled()) {
					logger.info(message, args);
				}
			}
		},

		WARN() {
			@Override
			public void log(Logger logger, String message, Object... args) {
				if (logger.isWarnEnabled()) {
					logger.warn(message, args);
				}
			}
		},

		ERROR() {
			@Override
			public void log(Logger logger, String message, Object... args) {
				if (logger.isErrorEnabled()) {
					logger.error(message, args);
				}
			}
		},
		OFF();

		public void log(Logger logger, String message, Object... args) {
		}
	}
}
