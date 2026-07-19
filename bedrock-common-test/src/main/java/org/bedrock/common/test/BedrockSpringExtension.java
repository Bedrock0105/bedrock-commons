package org.bedrock.common.test;

import org.bedrock.common.code.constant.NacosConstant;
import org.bedrock.common.code.start.StartConfiguration;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.*;

public class BedrockSpringExtension extends SpringExtension {

	@Override
	public void beforeAll(ExtensionContext context) throws Exception {
		super.beforeAll(context);
		Class<?> testClass = context.getRequiredTestClass();
		BedrockTest bedrockTest = AnnotatedElementUtils.getMergedAnnotation(testClass, BedrockTest.class);
		String appName = bedrockTest.appName();
		Properties properties = System.getProperties();
		properties.setProperty("spring.main.allow-bean-definition-overriding", "true");
		properties.setProperty("spring.cloud.nacos.config.file-extension", NacosConstant.NACOS_CONFIG_FORMAT);
		properties.setProperty("spring.cloud.nacos.config.shared-configs[0].data-id", NacosConstant.defaultDataId(bedrockTest.profile()));
		properties.setProperty("spring.cloud.nacos.config.shared-configs[0].group", NacosConstant.NACOS_CONFIG_GROUP);
		properties.setProperty("spring.cloud.nacos.config.shared-configs[0].refresh", NacosConstant.NACOS_CONFIG_REFRESH);
		properties.setProperty("spring.config.import[0]", NacosConstant.importDataId(appName));
		properties.setProperty("spring.config.import[1]", NacosConstant.defaultImportDataId(bedrockTest.profile()));

		properties.setProperty("spring.application.name", appName);
		properties.setProperty("spring.profiles.active", bedrockTest.profile());
		if (bedrockTest.enableStartConfiguration()) {
			List<StartConfiguration> launcherList = new ArrayList<>();
			ServiceLoader.load(StartConfiguration.class).forEach(launcherList::add);
			SpringApplicationBuilder builder = new SpringApplicationBuilder(testClass);
			launcherList.stream()
				.sorted(Comparator.comparing(StartConfiguration::getOrder))
				.forEach(startConfiguration -> startConfiguration.configure(appName, properties, builder, bedrockTest.profile()));
		}
	}
}
