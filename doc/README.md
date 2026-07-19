# Bedrock Commons 功能文档

## 文档说明

本文档是 Bedrock Commons 框架的功能说明和使用指南，内容基于源码和 `bedrock-cloud` 微服务项目的实际用法编写。

---

## P0 - 核心必备模块

| # | 模块 | 文档 | 说明 |
|---|------|------|------|
| 1 | bedrock-common-boot + code | [启动模块 (Boot)](./01-启动模块-Boot.md) | 聚合 Starter、BedrockApplication、BaseController |
| 2 | bedrock-common-auth + authentication | [认证授权 (Auth)](./02-认证授权.md) | JWT、OAuth 登录、AuthUtil |
| 3 | bedrock-common-mybatisplus | [MyBatis-Plus 增强](./03-MyBatis-Plus增强.md) | BaseEntity、分页、自动填充 |
| 4 | bedrock-common-tenant | [多租户 (Tenant)](./04-多租户-Tenant.md) | 行级租户隔离 |

---

## P1 - 重要功能模块

| # | 模块 | 文档 | 说明 |
|---|------|------|------|
| 5 | bedrock-common-dynamic | [动态数据源](./05-动态数据源.md) | @DS 切换数据源 |
| 6 | bedrock-common-datascope | [数据权限 (DataScope)](./06-数据权限-DataScope.md) | 基于角色的数据范围 |
| 7 | bedrock-common-security | [安全模块 (Security)](./07-安全模块.md) | @PrePermissionCheck 权限校验 |
| 8 | bedrock-common-log | [日志模块 (Log)](./08-日志模块.md) | @OperationLog 操作日志 |
| 9 | bedrock-common-redis | [Redis 缓存](./09-Redis缓存.md) | RedisUtil、Spring Cache、Stream |

---

## P2 - 辅助功能模块

| # | 模块 | 文档 | 说明 |
|---|------|------|------|
| 10 | bedrock-common-excel | [Excel 处理](./10-Excel处理.md) | @RequestExcel / @ResponseExcel |
| 11 | bedrock-common-http | [HTTP 客户端](./11-HTTP客户端.md) | @BedrockClient 声明式 HTTP |
| 12 | bedrock-common-code | [事件总线 (EventBus)](./12-事件总线-EventBus.md) | 发布-订阅消息 |
| 13 | bedrock-common-code | [树形结构 (TreeUtil)](./13-树形结构工具-TreeUtil.md) | 列表转树 |
| 14 | bedrock-common-code | [通用工具类](./14-通用工具类.md) | R、工具类清单 |
| 15 | bedrock-common-swagger | [Swagger 文档](./15-Swagger文档.md) | Knife4j + OpenAPI 3 |
| 16 | bedrock-common-websocket | [WebSocket](./16-WebSocket.md) | WebSocket 自动配置 |
| 17 | bedrock-common-resource | [资源管理 (Resource)](./17-资源管理.md) | OSS、邮件、短信 |

---

## P3 - 微服务与扩展

| # | 模块 | 文档 | 说明 |
|---|------|------|------|
| 18 | bedrock-common-spi | [SPI 扩展](./18-SPI扩展.md) | @AutoService、自动配置注册 |
| 19 | bedrock-common-cloud | [微服务云模块 (Cloud)](./19-微服务云模块.md) | @BedrockCloudApplication、Feign |
| 20 | bedrock-common-test | [测试模块 (Test)](./20-测试模块.md) | @BedrockTest |
| 21 | — | [编码规范](./21-编码规范.md) | 代码规范 |
| 22 | — | [常见问题 FAQ](./22-常见问题FAQ.md) | 问题排查 |
| 23 | bedrock-common-ai | [AI 公共模块](./23-AI公共模块.md) | Spring AI 工厂、Advisor、工具与 MCP |

---

## 阅读建议

### 新手入门

1. [启动模块](./01-启动模块-Boot.md) — 了解项目结构和启动方式
2. [认证授权](./02-认证授权.md) + [MyBatis-Plus](./03-MyBatis-Plus增强.md) — 基础开发
3. [日志模块](./08-日志模块.md) + [安全模块](./07-安全模块.md) — 操作日志和权限

### 微服务项目（bedrock-cloud）

1. [微服务云模块](./19-微服务云模块.md) — @BedrockCloudApplication + Feign
2. [多租户](./04-多租户-Tenant.md) — 租户隔离
3. [Swagger 文档](./15-Swagger文档.md) — 接口文档
4. [AI 公共模块](./23-AI公共模块.md) — 模型/向量库工厂、Advisor、工具与 MCP（`bedrock-ai` 依赖）

---

## 项目结构

```
bedrock-commons/     ← 后端公共模块（本文档）
bedrock-cloud/       ← 微服务项目，引用 bedrock-commons
bedrock-ui/          ← 前端项目
```

---

## 更新记录

| 日期 | 版本 | 说明 |
|------|------|------|
| 2026-07-19 | 2.1.0 | 新增 bedrock-common-ai 模块说明 |
| 2026-07-06 | 2.0.0 | 基于源码和 bedrock-cloud 实际用法全面重写 |
| 2026-06-14 | 1.0.0 | 初始版本 |
