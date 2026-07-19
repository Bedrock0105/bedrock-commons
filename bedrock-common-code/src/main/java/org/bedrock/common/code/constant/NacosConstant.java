package org.bedrock.common.code.constant;

/**
 * Nacos常量.
 */
public interface NacosConstant {

	/**
	 * nacos 配置前缀
	 */
	String NACOS_CONFIG_PREFIX = "application";
	/**
	 * nacos 配置文件类型
	 */
	String NACOS_CONFIG_FORMAT = "yml";
	/**
	 * nacos 是否刷新
	 */
	String NACOS_CONFIG_REFRESH = "true";
	/**
	 * nacos 分组
	 */
	String NACOS_CONFIG_GROUP = "DEFAULT_GROUP";

	/**
	 * 服务默认加载的配置
	 *
	 * @param profile 环境变量
	 * @return sharedDataIds
	 */
	static String defaultDataId(String profile) {
		return NACOS_CONFIG_PREFIX + "-" + profile + "." + NACOS_CONFIG_FORMAT;
	}

	/**
	 * 服务默认加载的配置
	 *
	 * @param profile 环境变量
	 * @return defaultImportDataId
	 */
	static String defaultImportDataId(String profile) {
		return "optional:nacos:"
			.concat(NACOS_CONFIG_PREFIX)
			.concat("-")
			.concat(profile)
			.concat(".")
			.concat(NacosConstant.NACOS_CONFIG_FORMAT)
			.concat("?group=")
			.concat(NacosConstant.NACOS_CONFIG_GROUP)
			.concat("&refreshEnabled=")
			.concat(NacosConstant.NACOS_CONFIG_REFRESH);
	}

	/**
	 * 服务默认加载的配置
	 *
	 * @param applicationName 应用名称
	 * @return defaultImportDataId
	 */
	static String importDataId(String applicationName) {
		return "optional:nacos:"
			.concat(applicationName)
			.concat(".")
			.concat(NacosConstant.NACOS_CONFIG_FORMAT)
			.concat("?group=")
			.concat(NacosConstant.NACOS_CONFIG_GROUP)
			.concat("&refreshEnabled=")
			.concat(NacosConstant.NACOS_CONFIG_REFRESH);
	}

}
