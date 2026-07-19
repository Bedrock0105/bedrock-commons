package org.bedrock.common.tenant.props;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Set;

@Getter
@Setter
@ConfigurationProperties(prefix = TenantProperties.PREFIX)
public class TenantProperties {

	public static final String PREFIX = "bedrock.tenant";

	/**
	 * 是否启用
	 */
	private boolean enabled = true;

	/**
	 * 多租户字段
	 */
	private String tenantColumn = "tenant_id";

	/**
	 * 忽略多租户的表
	 */
	private Set<String> excludes = Set.of();
}
