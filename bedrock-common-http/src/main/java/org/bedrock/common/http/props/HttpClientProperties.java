package org.bedrock.common.http.props;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bedrock.common.http.enums.HttpExecutorEnum;
import org.bedrock.common.http.enums.ProtocolVersion;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ToString
@ConfigurationProperties(prefix = HttpClientProperties.PREFIX)
public class HttpClientProperties {

	public static final String PREFIX = "bedrock.http.client";

	/**
	 * 执行器名称
	 */
	private String httpExecutorName = HttpExecutorEnum.OKHTTP.getName();

	/**
	 * 连接超时时间（毫秒）
	 */
	private int connectTimeout = 5000;

	/**
	 * 读取超时（毫秒）
	 */
	private int readTimeout = 15000;

	/**
	 * 写超时（毫秒）
	 */
	private int writeTimeout = 8000;

	/**
	 * HTTP协议版本
	 */
	private ProtocolVersion protocolVersion = ProtocolVersion.HTTP_1_1;

	/**
	 * 日志配置
	 */
	private Log log = new Log();

	@Getter
	@Setter
	public static class Log {

		/**
		 * 日志总开关（优先级最高）：false时所有日志都不输出
		 */
		private boolean enabled = true;

		/**
		 * 请求日志大类开关：false时，以下requestHeaderLog/requestBodyLog均不生效
		 */
		private boolean requestLog = true;

		/**
		 * 请求头日志（依赖requestLog和enabled为true）
		 */
		private boolean requestHeaderLog = true;

		/**
		 * 请求体日志（依赖requestLog和enabled为true，生产环境建议关闭大报文场景）
		 */
		private boolean requestBodyLog = false;

		/**
		 * 响应日志大类开关：false时，以下responseHeaderLog/responseBodyLog均不生效
		 */
		private boolean responseLog = true;

		/**
		 * 响应头日志（依赖responseLog和enabled为true）
		 */
		private boolean responseHeaderLog = true;

		/**
		 * 响应体日志（依赖responseLog和enabled为true，生产环境建议关闭大报文场景）
		 */
		private boolean responseBodyLog = false;
	}
}
