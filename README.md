git remote add # Bedrock Commons

## 📖 项目介绍

Bedrock Commons 是一个基于 Spring Boot 3.x 和 Spring Cloud 2025.x 的企业级通用基础框架组件库。该框架封装了常用的开发功能模块，提供开箱即用的解决方案，帮助开发者快速构建 Spring Boot 或 Spring Cloud 微服务项目。

### ✨ 核心特性

- 🚀 **快速启动**：提供一站式依赖管理，简化项目初始化配置
- 🔐 **安全认证**：集成 JWT、OAuth2 认证授权机制
- 💾 **数据访问**：封装 MyBatis-Plus，支持多数据源动态切换
- 📊 **缓存支持**：基于 Redis 的高性能缓存解决方案
- 📝 **接口文档**：集成 Knife4j（Swagger）自动生成 API 文档
- 🔍 **日志追踪**：完善的日志记录与链路追踪功能
- 🏢 **多租户**：支持多租户架构，实现数据隔离
- 📈 **服务治理**：集成 Nacos、Sentinel 等微服务治理组件
- 📦 **Excel 处理**：高性能 Excel 导入导出功能
- 🌐 **HTTP 客户端**：封装便捷的 HTTP 请求工具
- 🔌 **SPI 扩展**：支持 SPI 机制，灵活扩展功能

---

## 🛠️ 技术栈

### 基础环境

- **Java 版本**：JDK 17+
- **构建工具**：Maven 3.6+
- **编码格式**：UTF-8

### 核心框架

| 框架 | 版本 |
|------|------|
| Spring Boot | 3.5.11 |
| Spring Cloud | 2025.0.0 |
| Spring Cloud Alibaba | 2025.0.0.0 |

### 主要依赖

| 组件 | 版本 | 说明 |
|------|------|------|
| MyBatis-Plus | 3.5.15 | ORM 框架 |
| Dynamic Datasource | 4.3.1 | 动态多数据源 |
| Knife4j | 4.5.0 | API 文档增强 |
| Druid | 1.2.23 | 数据库连接池 |
| JJWT | 0.13.0 | JWT 令牌生成与验证 |
| FastExcel | 1.3.0 | Excel 处理 |
| Spring Boot Admin | 3.5.7 | 应用监控 |

---

## 📦 模块介绍

### 核心模块

#### 1. bedrock-common-boot
**核心启动模块**，集成了 Web 开发常用组件，是使用本框架的基础模块。

**包含功能**：
- Web 容器（Undertow）
- 统一响应码
- 日志配置
- 认证授权
- MyBatis-Plus 集成
- Redis 缓存
- Swagger 文档
- 数据权限
- 多租户支持

**适用场景**：所有 Spring Boot 项目必选

---

#### 2. bedrock-common-auth
**认证模块**，提供 JWT Token 生成、验证和用户认证功能。

**核心功能**：
- JWT Token 生成与解析
- 用户认证信息封装
- 角色权限管理
- Token 自动刷新

**主要类**：
- `JwtUtil`：JWT 工具类
- `AuthUtil`：认证工具类
- `AuthUser`：认证用户实体
- `JwtConfiguration`：JWT 配置类

---

#### 3. bedrock-common-authentication
**OAuth2 认证服务器模块**，实现完整的 OAuth2 授权流程。

**支持的授权模式**：
- 密码模式（Password）
- 刷新令牌（Refresh Token）
- 可扩展其他授权模式

**核心功能**：
- OAuth2 Token 端点
- 客户端详情服务
- 用户详情服务
- 异常处理机制

---

#### 4. bedrock-common-security
**安全模块**，提供方法级别的安全控制和数据加密功能。

**核心功能**：
- 方法权限控制
- 数据加密/解密
- SM2 国密算法支持
- 安全上下文管理

---

#### 5. bedrock-common-cloud
**微服务模块**，集成 Spring Cloud 微服务相关组件。

**包含功能**：
- Nacos 服务注册与发现
- Nacos 配置中心
- OpenFeign 服务调用
- Sentinel 流量控制
- LoadBalancer 负载均衡
- Bootstrap 配置支持

**适用场景**：Spring Cloud 微服务项目

---

### 数据相关模块

#### 6. bedrock-common-mybatisplus
**MyBatis-Plus 增强模块**，提供增强的 ORM 功能。

**核心功能**：
- 通用 CRUD 操作
- 分页插件
- 自动填充
- 逻辑删除
- 乐观锁
- 自定义 SQL 注入器

---

#### 7. bedrock-common-redis
**Redis 缓存模块**，封装 Redis 常用操作。

**核心功能**：
- RedisTemplate 配置
- 自定义序列化器（JSON、Protostuff、Kryo、FST）
- Stream 消息队列支持
- 分布式锁
- 缓存工具类

---

#### 8. bedrock-common-dynamic
**动态数据源模块**，支持运行时动态切换数据源。

**核心功能**：
- 多数据源配置
- 动态数据源切换
- 数据源路由
- AOP 切面支持

---

#### 9. bedrock-common-datascope
**数据权限模块**，实现基于角色的数据范围控制。

**核心功能**：
- 数据权限注解
- 数据范围过滤
- SQL 自动拼接
- 多种数据权限策略

---

#### 10. bedrock-common-tenant
**多租户模块**，支持多租户架构的数据隔离。

**核心功能**：
- 租户上下文管理
- 租户字段自动填充
- 租户数据隔离
- 忽略租户标记

---

### 工具模块

#### 11. bedrock-common-code
**通用编码模块**，提供统一的响应码、常量、工具类等。

**核心功能**：
- 统一响应结果封装
- 业务状态码定义
- 常用工具类
- 常量定义

---

#### 12. bedrock-common-log
**日志模块**，提供操作日志记录和链路追踪功能。

**核心功能**：
- 操作日志注解
- 日志异步记录
- 链路追踪 ID
- 日志格式化输出

---

#### 13. bedrock-common-excel
**Excel 处理模块**，基于 FastExcel 实现高性能 Excel 操作。

**核心功能**：
- Excel 导入
- Excel 导出
- 自定义样式
- 大数据量处理
- 注解驱动

---

#### 14. bedrock-common-http
**HTTP 客户端模块**，封装便捷的 HTTP 请求功能。

**核心功能**：
- RESTful API 调用
- 请求/响应拦截器
- SSL/TLS 支持
- 自动重试机制
- 多种转换器支持

---

#### 15. bedrock-common-swagger
**API 文档模块**，集成 Knife4j 增强 Swagger 文档。

**核心功能**：
- Swagger 配置
- Knife4j 增强 UI
- 分组文档
- 全局参数配置

---

#### 16. bedrock-common-websocket
**WebSocket 模块**，提供 WebSocket 通信支持。

**核心功能**：
- WebSocket 配置
- 消息推送
- 连接管理

---

#### 17. bedrock-common-resource
**资源管理模块**，统一管理各类资源服务。

**核心功能**：
- 文件上传下载
- 对象存储支持
- 资源访问控制

---

#### 18. bedrock-common-spi
**SPI 扩展模块**，提供 SPI 机制支持。

**核心功能**：
- SPI 加载器
- 服务提供者接口
- 扩展点管理

---

#### 19. bedrock-common-test
**测试模块**，提供测试相关的工具类和扩展。

**核心功能**：
- 测试基类
- Spring 测试扩展
- Mock 工具

---

#### 20. bedrock-common-dependencies
**依赖管理模块**，统一管理所有依赖版本。

**作用**：通过 BOM（Bill of Materials）方式管理依赖版本，避免版本冲突。

---

## 📋 使用方式

### 方式一：单独引入模块（推荐用于 Spring Boot 项目）

在项目的 `pom.xml` 中引入所需模块：

```xml
<dependencies>
    <!-- 核心启动模块（必选） -->
    <dependency>
        <groupId>org.bedrock</groupId>
        <artifactId>bedrock-common-boot</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </dependency>
    
    <!-- 根据需要引入其他模块 -->
    <!-- Redis 缓存 -->
    <dependency>
        <groupId>org.bedrock</groupId>
        <artifactId>bedrock-common-redis</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </dependency>
    
    <!-- MyBatis-Plus -->
    <dependency>
        <groupId>org.bedrock</groupId>
        <artifactId>bedrock-common-mybatisplus</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </dependency>
</dependencies>
```

### 方式二：使用依赖管理（推荐用于多模块项目）

在父 POM 中引入依赖管理：

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

然后在子模块中引入时不需要指定版本号：

```xml
<dependencies>
    <dependency>
        <groupId>org.bedrock</groupId>
        <artifactId>bedrock-common-boot</artifactId>
    </dependency>
</dependencies>
```

### 方式三：微服务项目（Spring Cloud）

对于 Spring Cloud 微服务项目，引入 cloud 模块：

```xml
<dependencies>
    <!-- 微服务模块（包含了 boot 模块的所有功能） -->
    <dependency>
        <groupId>org.bedrock</groupId>
        <artifactId>bedrock-common-cloud</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </dependency>
</dependencies>
```

---

## ⚙️ 配置示例

### application.yml 基础配置

```yaml
server:
  port: 8080

spring:
  application:
    name: your-application-name
  
  # 数据源配置
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/your_database?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: root
    password: your_password
    type: com.alibaba.druid.pool.DruidDataSource
  
  # Redis 配置
  data:
    redis:
      host: localhost
      port: 6379
      password: your_redis_password
      database: 0

# JWT 配置
jwt:
  secret: your-secret-key-at-least-32-characters
  expire: 7200  # Token 过期时间（秒）

# MyBatis-Plus 配置
mybatis-plus:
  mapper-locations: classpath*:/mapper/**/*.xml
  type-aliases-package: com.yourpackage.entity
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      id-type: ASSIGN_ID
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0

# Knife4j 配置
knife4j:
  enable: true
  setting:
    language: zh_cn
```

### 微服务配置（bootstrap.yml）

```yaml
spring:
  application:
    name: your-service-name
  cloud:
    nacos:
      # 服务注册中心
      discovery:
        server-addr: 127.0.0.1:8848
        namespace: your-namespace
      # 配置中心
      config:
        server-addr: 127.0.0.1:8848
        namespace: your-namespace
        file-extension: yaml
        shared-configs:
          - application-${spring.profiles.active}.${spring.cloud.nacos.config.file-extension}
```

---

## 🚀 快速开始

### 1. 克隆并安装到本地仓库

```bash
git clone https://gitee.com/your-repo/bedrock-commons.git
cd bedrock-commons
mvn clean source:jar install
```

### 2. 创建 Spring Boot 项目

使用 Spring Initializr 创建项目，或在现有项目中引入依赖。

### 3. 引入依赖

```xml
<dependency>
    <groupId>org.bedrock</groupId>
    <artifactId>bedrock-common-boot</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### 4. 启动类添加注解

```java
@SpringBootApplication
public class YourApplication {
    public static void main(String[] args) {
        SpringApplication.run(YourApplication.class, args);
    }
}
```

### 5. 编写 Controller

```java
@RestController
@RequestMapping("/api")
public class TestController {
    
    @GetMapping("/hello")
    public String hello() {
        return "Hello, Bedrock Commons!";
    }
}
```

### 6. 访问 API 文档

启动项目后访问：`http://localhost:8080/doc.html`

---

## 📚 功能文档

详细的模块使用指南、API 说明和实战案例已整理至 [功能文档](./doc/README.md)。

**文档优先级分类**：
- 🔥 **P0 - 核心必备**：启动模块、认证授权、MyBatis-Plus、多租户
- 💎 **P1 - 重要功能**：动态数据源、数据权限、安全模块、日志、Redis
- 🔧 **P2 - 辅助功能**：Excel、HTTP客户端、事件总线、工具类、Swagger等
- 🚀 **P3 - 微服务扩展**：SPI扩展、微服务模块、测试模块

👉 [查看完整功能文档](./doc/README.md)

---

## 📝 开发建议

### 模块选择指南

| 项目类型 | 推荐模块 |
|---------|---------|
| 简单 Spring Boot 项目 | bedrock-common-boot |
| 需要缓存的项目 | + bedrock-common-redis |
| 需要数据库操作 | + bedrock-common-mybatisplus |
| 需要多数据源 | + bedrock-common-dynamic |
| 需要多租户 | + bedrock-common-tenant |
| 需要数据权限 | + bedrock-common-datascope |
| Spring Cloud 微服务 | bedrock-common-cloud |

### 注意事项

1. **版本兼容性**：确保 JDK 版本为 17 或以上
2. **依赖冲突**：如遇到依赖冲突，可使用 `bedrock-common-dependencies` 统一管理版本
3. **配置优先级**：自定义配置会覆盖框架默认配置
4. **SPI 扩展**：部分模块支持 SPI 扩展，可通过实现接口自定义行为


## 📄 License

本项目采用 MIT 开源协议，详见 [LICENSE](LICENSE) 文件。

---


**⭐ 如果这个项目对你有帮助，请给一个 Star！**
