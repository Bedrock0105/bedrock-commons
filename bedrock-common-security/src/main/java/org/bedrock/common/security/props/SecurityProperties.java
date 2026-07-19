package org.bedrock.common.security.props;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = SecurityProperties.PREFIX)
public class SecurityProperties {

	public static final String PREFIX = "bedrock.security";

	/**
	 * 是否启用
	 * 登录校验
	 */
	private boolean enabled = false;

	/**
	 * 忽略的url
	 */
	private List<String> ignoreUrls = new ArrayList<>();

}
