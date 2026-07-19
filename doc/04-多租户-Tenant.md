# 多租户 (Tenant)

## 概述

`bedrock-common-tenant` 基于 MyBatis-Plus `TenantLineInnerInterceptor` 实现行级租户隔离。含 `tenant_id` 列的表自动追加租户条件，无该列的表自动跳过。

---

## 快速开始

### 1. 实体继承 TenantEntity

```java
@TableName("bedrock_admin")
public class Admin extends TenantEntity {
    private String username;
    // tenantId 字段由 TenantEntity 提供
}
```

`TenantEntity` 在 `BaseEntity` 基础上增加 `tenantId`（String 类型）。

### 2. 忽略租户过滤

租户管理等需跨租户操作的 Service 类标注：

```java
@TenantIgnore
@Service
public class TenantServiceImpl extends BaseServiceImpl<TenantMapper, Tenant> { ... }
```

或在代码块中临时忽略：

```java
TenantHandler.ignore(() -> {
    // 此处 SQL 不追加 tenant_id 条件
});

// 带返回值
String result = TenantHandler.ignore(() -> tenantMapper.selectAll());
```

切换租户执行：

```java
TenantHandler.tenant("000001", () -> {
    // 以指定租户 ID 执行
});
```

---

## 配置

```yaml
bedrock:
  tenant:
    enabled: true              # 是否启用，默认 true
    tenant-column: tenant_id   # 租户列名，默认 tenant_id
    excludes: []               # 显式排除的表名集合
```

---

## 租户 ID 来源链

```
请求头 X-Tenant-Id
    → RequestListener 写入 MDC
    → AuthUtil.getTenantId()（优先 MDC，其次 JWT claims）
    → TenantHandler.getTenantId()（优先 ThreadLocal，回落 AuthUtil）
    → DefaultTenantHandler 追加 SQL 条件
```

默认租户 ID 常量：`TenantConstant.DEFAULT_TENANT_ID = "000000"`

请求头名称可通过 `bedrock.content.tenant-id` 配置（默认 `X-Tenant-Id`）。

---

## 表过滤规则

1. 启动时扫描数据库，**仅含 `tenant_id` 列的表**才加入租户过滤白名单
2. `excludes` 配置中的表显式排除
3. `tenant_id` 为空时**跳过过滤**（查全表），不会报错

---

## 与 Feign 协作

Feign 请求头转发由 `bedrock-common-cloud` 的 `DefaultFeignRequestInterceptor` 处理。需在 `bedrock.content.headers` 中配置需要转发的头：

```yaml
bedrock:
  content:
    headers:
      - Authorization
      - X-Tenant-Id
```

---

## bedrock-cloud 示例

| 实体 | 说明 |
|------|------|
| `Admin`、`Dept`、`Role` | 继承 `TenantEntity` |
| `TenantServiceImpl` | 类级 `@TenantIgnore`，管理所有租户 |

---

## 注意事项

1. `TenantHandler.getTenantId()` 返回 **String**（非 Long）
2. 无 `clearTenantId()` 方法，请求结束由 `RequestListener` 自动清理
3. 异步任务需配合 `ContextTaskDecorator`（boot 模块已配置）传递租户上下文
4. 租户解析不在 tenant 模块内，依赖 boot 模块的 `RequestListener`
