# 常见问题 FAQ

## 启动相关

### Q: 应用启动后 Nacos 配置未加载？

检查以下几点：

1. Nacos 配置文件扩展名为 `yml`（非 yaml）
2. 共享配置 data-id 为 `application-{profile}.yml`
3. 服务配置 data-id 为 `{applicationName}.yml`
4. `BedrockApplication.run()` 第一个参数即为 applicationName

### Q: Bean 冲突或找不到？

Bedrock 各模块无 `bedrock.xxx.enabled` 开关，引入依赖即自动装配。检查：

1. pom.xml 是否引入正确依赖
2. 是否重复定义了同名 Bean
3. 启动类是否在 `org.bedrock` 包路径下（组件扫描范围）

---

## 认证授权

### Q: Token 校验失败？

1. 确认 `bedrock.authentication.secret-key` 长度 ≥ 32 位
2. 所有服务 secret-key 必须一致
3. Token 通过 `Authorization: Bearer {token}` 传递
4. Token 过期时间由 `bedrock_client` 表配置，非 YAML

### Q: 401 未登录？

1. 检查是否开启了 `bedrock.security.enabled=true`
2. 确认接口不在 `ignore-urls` 白名单中
3. 网关 `AuthorizeFilter` 和服务内 `SecurityInterceptor` 是两层校验

### Q: 403 权限不足？

1. 检查 `@PrePermissionCheck` 的 SpEL 表达式
2. 确认 `bedrock_role_permission` 和 `bedrock_permission_api` 表中有对应权限配置
3. 角色别名需与 `RoleAliasConstant` 一致

---

## 数据库

### Q: 逻辑删除不生效？

逻辑删除字段为 `is_deleted`（非 `deleted`），全局配置在 `mybatis-plus.yml` 中。

### Q: 自动填充 createUserId 为 null？

`DefaultMetaObjectHandler` 依赖 `AuthUtil.getAuthUser()`，未登录或无 Token 时为 null。

### Q: 多租户 SQL 未追加 tenant_id？

1. 确认表有 `tenant_id` 列
2. 确认 `bedrock.tenant.enabled=true`
3. 确认请求头携带 `X-Tenant-Id` 或 JWT 中有 tenantId
4. tenant_id 为空时跳过过滤（查全表）

---

## Redis

### Q: 缓存序列化异常？

默认序列化为 PROTOSTUFF，修改 `bedrock.redis.serializer` 后需清空 Redis 旧数据。

### Q: Redis 连接失败？

Boot 3 配置前缀为 `spring.data.redis`（非 `spring.redis`）。

---

## 日志

### Q: 操作日志未入库？

1. `@OperationLog` 必须标注在 Controller 方法上（非 Service）
2. 确认 `bedrock-log` 微服务正常运行
3. 确认 Feign 能调用 `bedrock-log` 服务
4. `condition` 表达式返回 false 时不记录

### Q: 请求日志过多？

```yaml
bedrock:
  log:
    request:
      exclude:
        - /actuator/**
        - /health
```

或使用 `@ExcludeRequestLog` 注解。

---

## Feign

### Q: Feign 调用时 Token/租户丢失？

配置需要转发的请求头：

```yaml
bedrock:
  content:
    headers:
      - Authorization
      - X-Tenant-Id
```

默认 `headers` 列表为空，不会自动转发任何头。

### Q: Feign 熔断后返回 R.error()？

开启 `feign.sentinel.enabled=true` 后，无 fallback 的 Feign 接口返回 `R` 类型时会自动返回 `R.error()`。

---

## Swagger

### Q: doc.html 访问 404？

1. 确认 `bedrock.swagger.enabled=true`（默认 true）
2. 单服务访问 `http://localhost:{port}/doc.html`
3. 网关聚合访问 `http://localhost:{gateway-port}/doc.html`

---

## Excel

### Q: 导入 Excel 数据为空？

1. 检查 `headRowNumber` 是否与实际表头行对应
2. 确认 multipart 字段名与 `@RequestExcel(value="file")` 一致
3. 实体类 `@ExcelProperty` 列名与 Excel 表头一致

---

## 性能

### Q: 分页查询慢？

1. 检查 `bedrock.mybatis-plus.max-limit`（默认 1000）
2. 开启 SQL 日志排查：`bedrock.mybatis-plus.sql-log=true`
3. 访问 Druid 监控：`/druid/index.html`

---

## AI / 向量库

### Q: Redis 报 `Not allowed filter identifier name: knowledgeId`？

创建 `VectorStore` 时需在 `VectorStoreCreateParam.metadataFieldTypes` 中声明可过滤字段（如 `knowledgeId` → `String.class`）。详见 [AI 公共模块](./23-AI公共模块.md)。

### Q: Redis 报 `SEARCH_SYNTAX Unknown field ... near knowledgeId`？

已有索引 schema 中无该字段。Java 侧声明**不会**升级旧索引。需 `FT.DROPINDEX` 或更换 `indexName` 后重建，并重新入库向量。

### Q: Milvus / ES 也要传 `metadataFieldTypes` 吗？

不必。该 Map 主要给 Redis 建索引用；Milvus / Elasticsearch / Simple 创建时忽略即可。
