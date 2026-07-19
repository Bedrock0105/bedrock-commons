# SPI 扩展

## 概述

`bedrock-common-spi` 提供编译期 SPI 自动生成能力，简化 Java `ServiceLoader` 的使用。框架内部用于：

1. **自动配置注册**：编译期生成 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`
2. **启动扩展**：`StartConfiguration` SPI 扩展点
3. **通用服务注册**：`@AutoService` 注解自动生成 `META-INF/services/` 文件

---

## @AutoService

标注在实现类上，编译期自动生成 SPI 配置文件：

```java
@AutoService(StartConfiguration.class)
public class MyStartConfiguration implements StartConfiguration {
    @Override
    public void configure(String appName, Properties props,
                          SpringApplicationBuilder builder, String profile) {
        // 启动时自定义配置
    }
    @Override
    public int getOrder() {
        return 0;
    }
}
```

编译后自动生成 `META-INF/services/org.bedrock.common.code.start.StartConfiguration`，内容为实现类全限定名。

---

## AutoConfigurationProcessor

编译期注解处理器，扫描所有 `@AutoConfiguration` 类，生成 Spring Boot 3 的自动配置 imports 文件。

各模块只需标注 `@AutoConfiguration`，无需手动维护 `spring.factories` 或 `imports` 文件。

### @AutoIgnore

标注在 `@AutoConfiguration` 类上，编译时排除该配置类不写入 imports 文件。

---

## StartConfiguration 扩展点

```java
public interface StartConfiguration {
    void configure(String applicationName, Properties properties,
                   SpringApplicationBuilder builder, String profile);
    int getOrder();  // 执行顺序，越小越先执行
}
```

`BedrockApplication.run()` 启动时通过 `ServiceLoader.load(StartConfiguration.class)` 加载所有实现，按 `getOrder()` 排序执行。

---

## 使用要求

1. 实现类必须是**非内部类、非匿名、具体类**
2. 必须有**公开的无参构造函数**
3. 必须实现 `@AutoService` 的 `value()` 指定的接口
4. 需要在编译时引入 `bedrock-common-spi` 的注解处理器

---

## 注意事项

1. SPI 文件在**编译期**生成，非运行时
2. 各 bedrock-common-* 模块的 `@AutoConfiguration` 均通过此机制注册
3. 业务项目扩展启动逻辑时，实现 `StartConfiguration` + `@AutoService` 即可
