package org.bedrock.common.code.util;

import org.springframework.util.StringUtils;

import java.util.Properties;

public class NacosLauncherServiceUtil {

	/**
	 * 获取nacos配置的数量
	 */
	public static int nacosSharedConfigs(Properties props) {
		int i = 2;
		while (true) {
			if (StringUtils.isEmpty(props.getProperty(String.format("spring.cloud.nacos.config.shared-configs[%d].data-id", i)))) {
				return i;
			}
			i++;
		}
	}

	/**
	 * 获取nacos配置的数量
	 */
	public static int nacosConfigImport(Properties props) {
		int i = 3;
		while (true) {
			if (StringUtils.isEmpty(props.getProperty(String.format("spring.config.import[%d]", i)))) {
				return i;
			}
			i++;
		}
	}
}
