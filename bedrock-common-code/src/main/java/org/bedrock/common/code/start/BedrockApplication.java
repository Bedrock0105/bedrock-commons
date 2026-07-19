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

public final class BedrockApplication {

	public static ApplicationContext run(String applicationName, Class<?> primarySource, String... args) {
		return createSpringApplicationBuilder(applicationName, primarySource, args).run(args);
	}

	public static SpringApplicationBuilder createSpringApplicationBuilder(String applicationName, Class<?> source, String... args) {
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

	public static String getProfile(String... args) {
		// 创建标准环境对象
		StandardEnvironment environment = new StandardEnvironment();
		// 添加命令行参数源（优先级最高）
		SimpleCommandLinePropertySource commandLineSource = new SimpleCommandLinePropertySource(args);
		environment.getPropertySources().addFirst(commandLineSource);
		// 1. 从命令行参数获取
		String activeProfiles = environment.getProperty(AbstractEnvironment.ACTIVE_PROFILES_PROPERTY_NAME);
		// 2. 从环境变量获取
		if (activeProfiles == null) {
			activeProfiles = System.getenv(AbstractEnvironment.ACTIVE_PROFILES_PROPERTY_NAME.toUpperCase());
		}
		// 3. 从系统属性获取
		if (activeProfiles == null) {
			activeProfiles = System.getProperty(AbstractEnvironment.ACTIVE_PROFILES_PROPERTY_NAME);
		}
		// 4. 使用默认环境
		if (activeProfiles == null || activeProfiles.trim().isEmpty()) {
			activeProfiles = DEV_CODE;
		}
		return activeProfiles;
	}
}
