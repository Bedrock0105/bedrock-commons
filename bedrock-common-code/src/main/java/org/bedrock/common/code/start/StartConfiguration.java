package org.bedrock.common.code.start;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.core.Ordered;
import org.springframework.util.StringUtils;

import java.util.Properties;

/**
 * 启动配置器接口 - SPI扩展点
 *
 * <p>该接口是Bedrock框架的核心SPI扩展机制，允许在应用启动时通过编程方式注入自定义配置。
 * 主要用途是在公共模块中统一配置基础设施（如Nacos地址、数据源、Redis等），避免每个项目重复配置。</p>
 *
 * <h2>核心特性</h2>
 * <ul>
 *   <li><b>统一配置管理</b> - 在公共starter模块中集中配置，所有引入的项目自动继承</li>
 *   <li><b>动态配置注入</b> - 根据应用名和环境profile动态生成配置</li>
 *   <li><b>优先级控制</b> - 通过getOrder()控制多个配置器的执行顺序</li>
 *   <li><b>安全覆盖保护</b> - setProperty方法只在配置不存在时设置，尊重用户自定义配置</li>
 * </ul>
 *
 * <h2>使用步骤</h2>
 *
 * <h3>推荐方式：使用@AutoService注解（零配置）</h3>
 * <ol>
 *   <li>实现StartConfiguration接口</li>
 *   <li>在类上添加 {@code @AutoService(StartConfiguration.class)} 注解</li>
 *   <li>编译时自动生成SPI配置文件，无需手动创建</li>
 *   <li>BedrockApplication启动时会自动加载并执行所有配置器</li>
 * </ol>
 *
 * <h2>典型应用场景</h2>
 *
 * <h3>场景1：统一配置Nacos地址</h3>
 * <pre>{@code
 * public class NacosStartConfiguration implements StartConfiguration {
 *
 *     @Override
 *     public int getOrder() {
 *         return Ordered.HIGHEST_PRECEDENCE + 100; // 高优先级
 *     }
 *
 *     @Override
 *     public void configure(String appName, Properties properties,
 *                          SpringApplicationBuilder builder, String profile) {
 *         // 统一配置Nacos服务器地址
 *         setProperty(properties, "spring.cloud.nacos.server-addr",
 *                    "nacos.example.com:8848");
 *
 *         // 统一配置命名空间
 *         setProperty(properties, "spring.cloud.nacos.config.namespace",
 *                    "prod-namespace-id");
 *
 *         // 应用配置
 *         builder.properties(properties);
 *     }
 * }
 * }</pre>
 *
 * <h3>场景2：多环境差异化配置</h3>
 * <pre>{@code
 * public class EnvironmentStartConfiguration implements StartConfiguration {
 *
 *     @Override
 *     public void configure(String appName, Properties properties,
 *                          SpringApplicationBuilder builder, String profile) {
 *         switch (profile) {
 *             case "dev":
 *                 setProperty(properties, "server.port", "8080");
 *                 setProperty(properties, "logging.level.root", "DEBUG");
 *                 break;
 *             case "prod":
 *                 setProperty(properties, "server.port", "8080");
 *                 setProperty(properties, "logging.level.root", "WARN");
 *                 break;
 *         }
 *         builder.properties(properties);
 *     }
 * }
 * }</pre>
 *
 * <h3>场景3：数据库连接池统一配置</h3>
 * <pre>{@code
 * public class DataSourceStartConfiguration implements StartConfiguration {
 *
 *     @Override
 *     public void configure(String appName, Properties properties,
 *                          SpringApplicationBuilder builder, String profile) {
 *         // 统一配置HikariCP连接池
 *         setProperty(properties, "spring.datasource.hikari.minimum-idle", "5");
 *         setProperty(properties, "spring.datasource.hikari.maximum-pool-size", "20");
 *         setProperty(properties, "spring.datasource.hikari.connection-timeout", "30000");
 *
 *         builder.properties(properties);
 *     }
 * }
 * }</pre>
 *
 * <h2>SPI注册</h2>
 *
 * <h3>推荐方式：使用@AutoService注解</h3>
 * <pre>{@code
 * import org.bedrock.common.spi.autoservice.AutoService;
 *
 * @AutoService(StartConfiguration.class)
 * public class NacosStartConfiguration implements StartConfiguration {
 *     // 编译时自动生成SPI配置文件
 * }
 * }</pre>
 *
 * <h3>传统方式：手动创建SPI文件（不推荐）</h3>
 * <p>在 {@code META-INF/services/org.bedrock.common.code.start.StartConfiguration} 文件中添加：</p>
 * <pre>
 * com.example.config.NacosStartConfiguration
 * com.example.config.DataSourceStartConfiguration
 * </pre>
 *
 * <h2>执行顺序</h2>
 * <p>多个配置器按getOrder()返回值从小到大排序执行：</p>
 * <ul>
 *   <li>Ordered.HIGHEST_PRECEDENCE (Integer.MIN_VALUE) - 最高优先级，最先执行</li>
 *   <li>0 - 中等优先级</li>
 *   <li>Ordered.LOWEST_PRECEDENCE (Integer.MAX_VALUE) - 最低优先级，最后执行</li>
 * </ul>
 *
 * <h2>最佳实践</h2>
 * <ul>
 *   <li><b>单一职责</b> - 每个配置器只负责一类配置（如Nacos、数据源、Redis分开）</li>
 *   <li><b>防御性编程</b> - 使用setProperty避免覆盖用户自定义配置</li>
 *   <li><b>合理优先级</b> - 基础配置高优先级，业务配置低优先级</li>
 *   <li><b>环境敏感</b> - 根据profile提供不同的配置值</li>
 *   <li><b>环境变量支持</b> - 敏感信息使用${ENV_VAR:default}格式</li>
 * </ul>
 *
 * <h2>注意事项</h2>
 * <ul>
 *   <li>configure方法在Spring容器创建之前执行，此时无法注入Bean</li>
 *   <li>setProperty只会设置不存在的配置，已存在的配置不会被覆盖</li>
 *   <li>如果需要强制覆盖，可以直接使用properties.setProperty(key, value)</li>
 *   <li>配置器应该轻量快速，避免耗时操作</li>
 * </ul>
 *
 * @see BedrockApplication
 * @see java.util.ServiceLoader
 * @since 1.0.0
 */
public interface StartConfiguration extends Ordered {

	/**
	 * 配置启动 - 在应用启动时注入自定义配置
	 *
	 * <p>该方法会在Spring Boot应用启动过程中被BedrockApplication自动调用，
	 * 允许通过编程方式设置应用属性、配置SpringApplicationBuilder等。</p>
	 *
	 * @param appName 应用名称，来自BedrockApplication.run()的第一个参数
	 * @param defaultProperties 默认属性集合，可以在此添加或修改配置
	 * @param builder SpringApplicationBuilder，用于配置Spring Boot应用的构建器
	 * @param profile 当前激活的环境profile（如dev、test、prod）
	 *
	 * @example
	 * <pre>{@code
	 * @Override
	 * public void configure(String appName, Properties properties,
	 *                      SpringApplicationBuilder builder, String profile) {
	 *     // 1. 设置配置属性
	 *     setProperty(properties, "server.port", "8080");
	 *     setProperty(properties, "spring.cloud.nacos.server-addr", "localhost:8848");
	 *
	 *     // 2. 根据环境差异化配置
	 *     if ("prod".equals(profile)) {
	 *         setProperty(properties, "logging.level.root", "WARN");
	 *     } else {
	 *         setProperty(properties, "logging.level.root", "DEBUG");
	 *     }
	 *
	 * }
	 * }</pre>
	 */
	void configure(String appName, Properties defaultProperties, SpringApplicationBuilder builder, String profile);

	/**
	 * 获取配置器的执行优先级
	 *
	 * <p>数字越小优先级越高，越先执行。默认优先级为LOWEST_PRECEDENCE - 100。</p>
	 *
	 * <p>常见优先级设置：</p>
	 * <ul>
	 *   <li>Ordered.HIGHEST_PRECEDENCE (Integer.MIN_VALUE) - 最高优先级</li>
	 *   <li>Ordered.HIGHEST_PRECEDENCE + 100 - 高优先级（基础配置）</li>
	 *   <li>0 - 中等优先级</li>
	 *   <li>Ordered.LOWEST_PRECEDENCE - 100 - 低优先级（默认值）</li>
	 *   <li>Ordered.LOWEST_PRECEDENCE (Integer.MAX_VALUE) - 最低优先级</li>
	 * </ul>
	 *
	 * @return 优先级值，数字越小越先执行
	 *
	 * @example
	 * <pre>{@code
	 * @Override
	 * public int getOrder() {
	 *     // Nacos配置需要优先执行
	 *     return Ordered.HIGHEST_PRECEDENCE + 100;
	 * }
	 * }</pre>
	 */
	@Override
	default int getOrder() {
		return Ordered.LOWEST_PRECEDENCE - 100;
	}

	/**
	 * 设置配置值（已存在则跳过）- 防御性配置方法
	 *
	 * <p>该方法只在配置项不存在时才设置新值，如果用户已经在application.yml中配置了该项，
	 * 则不会覆盖用户的配置。这保证了框架配置的灵活性，用户可以随时覆盖默认配置。</p>
	 *
	 * <p>链式调用支持：返回this，支持流畅的链式调用风格。</p>
	 *
	 * @param properties 属性集合
	 * @param key 配置键
	 * @param value 配置值
	 * @return 当前配置器实例，支持链式调用
	 *
	 * @example
	 * <pre>{@code
	 * @Override
	 * public void configure(String appName, Properties properties,
	 *                      SpringApplicationBuilder builder, String profile) {
	 *     // 链式调用设置多个配置
	 *     setProperty(properties, "spring.cloud.nacos.server-addr", "localhost:8848")
	 *         .setProperty(properties, "spring.cloud.nacos.username", "nacos")
	 *         .setProperty(properties, "spring.cloud.nacos.password", "${NACOS_PASSWORD:nacos}");
	 *
	 *     // 等价于分别调用
	 *     setProperty(properties, "server.port", "8080");
	 *     setProperty(properties, "logging.level.root", "INFO");
	 *
	 *     builder.properties(properties);
	 * }
	 * }</pre>
	 *
	 * @see #setProperty(Properties, String, String)
	 */
	default StartConfiguration setProperty(Properties properties, String key, String value) {
		if (!StringUtils.hasText(properties.getProperty(key))) {
			properties.setProperty(key, value);
		}
		return this;
	}
}
