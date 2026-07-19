package org.bedrock.common.boot.props;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = RequestContentProperties.PREFIX)
public class RequestContentProperties {

	public static final String PREFIX = "bedrock.content";

	/**
	 * 租户ID
	 */
	private String tenantId = "X-Tenant-Id";

	/**
	 * 账号ID
	 */
	private String accountId = "X-User-Account-Id";

	/**
	 * 进行传递的请求头
	 */
	private List<String> headers = List.of("X-Tenant-Id", "X-User-Account-Id", "Authorization");
}
