# HTTP 客户端

## 概述

`bedrock-common-http` 提供声明式 HTTP 客户端，通过 `@BedrockClient` 标注接口，框架自动代理为 HTTP 请求客户端（类似 Feign 但面向第三方 API）。

底层基于 OkHttp，支持拦截器、重试、代理、SSL 自定义、日志等。

---

## 快速开始

### 1. 引入依赖

```xml
<dependency>
    <groupId>org.bedrock</groupId>
    <artifactId>bedrock-common-http</artifactId>
</dependency>
```

### 2. 定义客户端接口

```java
@BedrockClient(url = "https://api.example.com", basePath = "/api/v1")
public interface ExampleClient {

    @GetRequest("/users/{userId}")
    UserDTO getUser(@PathParam("userId") Long userId);

    @PostRequest("/users")
    UserDTO createUser(@Body UserCreateRequest request);
}
```

### 3. 注入使用

```java
@Service
@RequiredArgsConstructor
public class UserService {
    private final ExampleClient exampleClient;

    public UserDTO getUser(Long id) {
        return exampleClient.getUser(id);
    }
}
```

---

## @BedrockClient

| 属性 | 说明 |
|------|------|
| `url` | 完整请求根地址（协议+域名+端口） |
| `basePath` | 统一路径前缀，以 `/` 开头 |
| `contentType` | 全局 Content-Type |
| `headers` | 全局请求头 |
| `connectTimeout` / `readTimeout` / `writeTimeout` | 超时（毫秒） |
| `interceptors` | 全局拦截器 |
| `converter` | 响应转换器 |

最终请求路径 = `url` + `basePath` + 方法上的 `url`

支持 `${property}` 占位符解析。

---

## 请求方法注解

| 注解 | HTTP 方法 |
|------|-----------|
| `@GetRequest` | GET |
| `@PostRequest` | POST |
| `@PutRequest` | PUT |
| `@DeleteRequest` | DELETE |
| `@PatchRequest` | PATCH |
| `@HeadRequest` | HEAD |
| `@OptionsRequest` | OPTIONS |
| `@TraceRequest` | TRACE |

### 参数注解

| 注解 | 说明 |
|------|------|
| `@PathParam` | 路径参数，支持 `{name}` 占位 |
| `@QueryParam` | Query 参数 |
| `@Body` | 请求体 |
| `@FormParam` | 表单参数 |
| `@HeaderParam` | 请求头参数 |

### 其他注解

| 注解 | 说明 |
|------|------|
| `@Retry` | 重试配置 |
| `@HttpProxy` | HTTP 代理 |
| `@LogConf` | 请求日志配置 |

---

## 拦截器

实现 `Interceptor` 接口，在 `@BedrockClient` 或方法注解中注册：

```java
public class AuthInterceptor implements Interceptor {
    @Override
    public HttpResponse intercept(Chain chain) {
        // 添加 Token、签名等
        return chain.proceed(request);
    }
}
```

内置拦截器：`LogInterceptor`、`RetryInterceptor`、`HttpProxyInterceptor` 等。

---

## 全局配置

通过 `HttpConfiguration.getDefault()` 进行全局配置：

```java
HttpConfiguration config = HttpConfiguration.getDefault();
config.getHttpClientProperties().setConnectTimeout(5000);
config.getHttpClientProperties().setReadTimeout(10000);
```

---

## 注意事项

1. `@BedrockClient` 标注在**接口**上，非类
2. 属性名为 `url`（非 `baseUrl`）
3. bedrock-cloud 项目主要使用 OpenFeign 做微服务间通信，HTTP 客户端模块用于调用第三方 API
4. SSL 自签名证书可配置 `disableSslValidation` 或使用 `TrustAllSslContextProvider`
