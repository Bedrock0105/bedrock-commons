package org.bedrock.common.code.start;

import org.bedrock.common.code.constant.NacosConstant;
import org.bedrock.common.code.server.ServerInfo;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.SimpleCommandLinePropertySource;
import org.springframework.core.env.StandardEnvironment;

import java.util.*;

import static org.bedrock.common.code.constant.EnvConstant.DEV_CODE;

/**
 * 应用启动入口，统一注入应用名、激活环境以及 Nacos 共享配置。
 */
public final class BedrockApplication {

	private BedrockApplication() {
	}

	/**
	 * 按应用名和主配置类启动 Spring 应用。
	 */
	public static ApplicationContext run(String applicationName, Class<?> primarySource, String... args) {
		return createBuilder(applicationName, primarySource, args).run(args);
	}

	/**
	 * 创建已注入公共启动配置的 {@link SpringApplicationBuilder}。
	 */
	public static SpringApplicationBuilder createBuilder(String applicationName, Class<?> source, String... args) {
		ServerInfo.setStartTime(System.currentTimeMillis());
		SpringApplicationBuilder builder = new SpringApplicationBuilder(source);
		Properties props = System.getProperties();
		String profile = getProfile(args);
		props.setProperty("spring.application.name", applicationName);
		props.setProperty("spring.profiles.active", profile);
		Properties properties = getProperties(profile, applicationName);
		List<StartConfiguration> launcherList = new ArrayList<>();
		ServiceLoader.load(StartConfiguration.class).forEach(launcherList::add);
		launcherList.stream()
			.sorted(Comparator.comparing(StartConfiguration::getOrder))
			.forEach(startConfiguration -> startConfiguration.configure(applicationName, properties, builder, profile));
		builder.properties(properties);
		return builder;
	}

	/**
	 * 按环境和应用名组装 Nacos 共享配置与配置导入项。
	 */
	private static Properties getProperties(String profile, String applicationName) {
		Properties properties = new Properties();
		properties.setProperty("spring.main.allow-bean-definition-overriding", "true");
		properties.setProperty("spring.cloud.nacos.config.file-extension", NacosConstant.NACOS_CONFIG_FORMAT);
		properties.setProperty("spring.cloud.nacos.config.shared-configs[0].data-id", NacosConstant.defaultDataId(profile));
		properties.setProperty("spring.cloud.nacos.config.shared-configs[0].group", NacosConstant.NACOS_CONFIG_GROUP);
		properties.setProperty("spring.cloud.nacos.config.shared-configs[0].refresh", NacosConstant.NACOS_CONFIG_REFRESH);
		properties.setProperty("spring.config.import[0]", NacosConstant.importDataId(applicationName));
		properties.setProperty("spring.config.import[1]", NacosConstant.defaultImportDataId(profile));
		return properties;
	}

	/**
	 * 解析激活环境。优先读取命令行、系统属性和环境变量中的 {@code spring.profiles.active}，都没有时返回开发环境。
	 */
	public static String getProfile(String... args) {
		StandardEnvironment environment = new StandardEnvironment();
		environment.getPropertySources().addFirst(new SimpleCommandLinePropertySource(args));
		String activeProfiles = environment.getProperty(AbstractEnvironment.ACTIVE_PROFILES_PROPERTY_NAME);
		if (activeProfiles == null || activeProfiles.isBlank()) {
			return DEV_CODE;
		}
		return activeProfiles;
	}
}
