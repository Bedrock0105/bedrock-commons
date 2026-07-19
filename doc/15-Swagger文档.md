# Swagger 文档

## 概述

`bedrock-common-swagger` 集成 **SpringDoc OpenAPI 3** + **Knife4j**，引入依赖即自动启用，无需 `@EnableSwagger`。

---

## 快速开始

微服务项目通过 `bedrock-common-boot` 间接引入。

### 访问地址

| 环境 | 地址 |
|------|------|
| 单服务 | `http://localhost:{port}/doc.html` |
| API JSON | `http://localhost:{port}/v3/api-docs` |
| 网关聚合 | `http://localhost:{gateway-port}/doc.html` |

### Controller 注解

```java
@Tag(name = "管理员 控制器")
@RestController
@RequestMapping("/admin")
public class AdminController extends BaseController {

    @GetMapping("/page")
    @Operation(summary = "管理员 分页查询")
    @ApiOperationSupport(order = 4)
    public R<IPage<AdminListVO>> page(Query query, AdminPageParam param) { ... }
}
```

| 注解 | 来源 | 作用 |
|------|------|------|
| `@Tag` | OpenAPI 3 | Controller 分组 |
| `@Operation` | OpenAPI 3 | 接口描述 |
| `@Parameter` | OpenAPI 3 | 参数描述 |
| `@ApiOperationSupport(order=N)` | Knife4j | 接口排序 |
| `@Hidden` | OpenAPI 3 | 隐藏接口/参数 |

文件上传参数文档写法：

```java
@Parameters({
    @Parameter(description = "excel", required = true, name = "file",
               in = ParameterIn.QUERY, schema = @Schema(type = "string", format = "binary")),
})
public R<Void> importExcel(@Parameter(hidden = true) @RequestExcel(...) List<AdminImportExcel> list) { ... }
```

---

## 配置

```yaml
springdoc:
  swagger-ui:
    path: /swagger-ui.html
  api-docs:
    path: /v3/api-docs
  default-flat-param-object: true

knife4j:
  enable: true
  production: false
  setting:
    language: zh_cn

bedrock:
  swagger:
    enabled: true                    # 默认 true
    packages-to-scan: org.bedrock
    paths-to-match: ["/**"]
    paths-to-exclude: []
    packages-to-exclude: []
    info:
      title: Bedrock API
      description: Bedrock 接口文档
      version: 1.0.0
      contact:
        name: Bedrock
        url: ""
        email: ""
      license:
        name: Apache 2.0
        url: ""
```

### 全局注入的请求头

框架自动为所有接口注入以下 Header 参数：

| Header | 说明 |
|--------|------|
| `Authorization` | Bearer Token |
| `X-Tenant-ID` | 租户 ID |
| `X-User-Account-ID` | 账号 ID |

---

## 网关聚合

`bedrock-gateway` 的 `bootstrap.yml`：

```yaml
knife4j:
  gateway:
    enabled: true
    tags-sorter: order
    operations-sorter: order
    strategy: discover
    discover:
      enabled: true
      version: OpenAPI3
      excluded-services:
        - bedrock-gateway
```

---

## 生产环境关闭

```yaml
bedrock:
  swagger:
    enabled: false

knife4j:
  production: true
```

---

## 注意事项

1. 统一响应模型为 `R<T>`，字段为 `code`、`msg`、`data`、`timestamp`
2. 无 `@EnableSwagger` 注解，依赖引入即生效
3. 隐藏接口使用 `@Hidden`（非 `@ApiIgnore`）
4. 默认仅 `default` 一个 API 分组，可自定义 `GroupedOpenApi` Bean 覆盖
