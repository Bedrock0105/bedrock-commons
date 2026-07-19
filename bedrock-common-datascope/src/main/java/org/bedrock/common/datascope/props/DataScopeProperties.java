package org.bedrock.common.datascope.props;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = DataScopeProperties.PREFIX)
public class DataScopeProperties {

	public static final String PREFIX = "bedrock.datascope";

	/**
	 * 是否启用
	 */
	private boolean enabled = true;

}
