# 启动模块 (Boot)

## 概述

`bedrock-common-boot` 是 Bedrock 的**聚合 Starter**，引入后自动装配 auth、mybatisplus、redis、security、swagger、log 等常用模块。

启动入口和统一响应在 `bedrock-common-code` 模块：

| 类 | 包 | 作用 |
|----|-----|------|
| `BedrockApplication` | `org.bedrock.common.code.start` | 统一启动器，自动配置 Nacos |
| `StartConfiguration` | `org.bedrock.common.code.start` | SPI 扩展点 |
| `BaseController` | `org.bedrock.common.boot.base` | Controller 基类 |
| `R<T>` | `org.bedrock.common.code.api` | 统一响应体 |

微服务项目使用 `@BedrockCloudApplication`（`bedrock-common-cloud`）组合注解。

---

## 快速开始

### 1. 引入依赖

微服务项目推荐引入 cloud 模块（已包含 boot）：

```xml
<dependency>
    <groupId>org.bedrock</groupId>
    <artifactId>bedrock-common-cloud</artifactId>
</dependency>
```

或使用 BOM 管理版本：

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.bedrock</groupId>
            <artifactId>bedrock-common-dependencies</artifactId>
            <version>1.0.0-SNAPSHOT</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

### 2. 创建启动类

参考 `bedrock-cloud` 中 `SystemApplication`：

```java
@BedrockCloudApplication
public class SystemApplication {
    public static void main(String[] args) {
        BedrockApplication.run(ApplicationConstant.APPLICATION_SYSTEM_NAME, SystemApplication.class, args);
    }
}
```

`@BedrockCloudApplication` = `@SpringBootApplication` + `@EnableDiscoveryClient` + `@EnableFeignClients("org.bedrock.**.feign.**")`

### 3. Controller 基类

```java
@RestController
@RequestMapping("/admin")
public class AdminController extends BaseController {
    public R<AdminDetailVO> detail(Long id) {
        return R.success(adminService.detail(id));
    }
}
```

`BaseController` 提供 `success()`、`failure()`、`error()`、`status(boolean)` 及文件下载辅助方法。

---

## BedrockApplication

```java
BedrockApplication.run(applicationName, primarySource, args);
```

启动时自动设置：

| 属性 | 值 |
|------|-----|
| `spring.application.name` | 第一个参数 applicationName |
| `spring.profiles.active` | 命令行 > 环境变量 > 系统属性 > 默认 `dev` |
| `spring.main.allow-bean-definition-overriding` | `true` |
| Nacos 共享配置 | `application-{profile}.yml` |
| `spring.config.import` | `optional:nacos:{appName}.yml` + `optional:nacos:application-{profile}.yml` |

### SPI 扩展

实现 `StartConfiguration` 接口，通过 `@AutoService(StartConfiguration.class)` 注册，按 `getOrder()` 排序执行 `configure()` 方法。

---

## Boot 模块自动配置

| 配置类 | 作用 |
|--------|------|
| `ContentConfiguration` | 异步线程池、调度器、请求上下文 |
| `RequestListenerConfiguration` | 请求生命周期监听，MDC 写入 tenantId/accountId/requestId |
| `ExceptionAdviceHandler` | 全局异常处理，统一返回 `R` |

自动配置通过 `@AutoConfiguration` + SPI 处理器注册，无需手动 `spring.factories`。

---

## 配置参考

### bedrock.content.*（请求头上下文）

```yaml
bedrock:
  content:
    tenant-id: X-Tenant-Id       # 租户 ID 请求头，默认 X-Tenant-Id
    account-id: X-User-Account-Id # 账号 ID 请求头
    headers: []                   # Feign 转发时额外携带的请求头列表
```

### boot.yml 内置默认值

```yaml
spring:
  servlet:
    multipart:
      max-file-size: 2048MB
      max-request-size: 2048MB
```

---

## 依赖链

```
bedrock-common-cloud
  └── bedrock-common-boot
        ├── bedrock-common-code      (R, BedrockApplication, 工具类)
        ├── bedrock-common-auth      (JWT, AuthUtil)
        ├── bedrock-common-mybatisplus
        ├── bedrock-common-redis
        ├── bedrock-common-security
        ├── bedrock-common-log
        ├── bedrock-common-swagger
        └── bedrock-common-tenant
```

Web 容器使用 **Undertow**（非 Tomcat）。

---

## bedrock-cloud 服务清单

| 服务 | 启动类 | 说明 |
|------|--------|------|
| bedrock-system | `SystemApplication` | 系统管理 |
| bedrock-auth | `AuthApplication` | 认证服务 |
| bedrock-log | `LogApplication` | 日志服务 |
| bedrock-resource | `ResourceApplication` | 资源/OSS |
| bedrock-websocket | `WebsocketApplication` | WebSocket |
| bedrock-gateway | 独立配置 | 网关（不用 @BedrockCloudApplication） |

---

## 注意事项

1. 各模块**无** `bedrock.xxx.enabled` 开关，引入依赖即自动装配
2. Nacos 配置文件扩展名为 `yml`（非 yaml）
3. 全局异常返回 `R.error()`，字段名为 `msg`（非 message）
4. `BedrockApplication` 在 `bedrock-common-code` 模块，不在 boot 模块内
