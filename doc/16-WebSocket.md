# WebSocket

## 概述

`bedrock-common-websocket` 提供 WebSocket 自动配置，注册 `ServerEndpointExporter` Bean，支持 JSR-356 `@ServerEndpoint` 注解。

实际的 WebSocket 业务逻辑在 `bedrock-cloud/bedrock-websocket` 微服务中实现，通过 Redis Stream 实现跨实例消息广播。

---

## 快速开始

### 1. 引入依赖

微服务项目引入 `bedrock-common-cloud` 后，websocket 模块需单独添加（bedrock-websocket 服务已引入）。

```xml
<dependency>
    <groupId>org.bedrock</groupId>
    <artifactId>bedrock-common-websocket</artifactId>
</dependency>
```

### 2. 定义端点

```java
@ServerEndpoint("/ws/{userId}")
@Component
public class ChatEndpoint {

    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId) { ... }

    @OnMessage
    public void onMessage(String message, Session session) { ... }

    @OnClose
    public void onClose(Session session) { ... }
}
```

`WebSocketConfiguration` 自动注册 `ServerEndpointExporter`。

---

## bedrock-cloud 架构

```
客户端 WebSocket 连接
    → bedrock-websocket 服务（@ServerEndpoint）
    → Redis Stream 广播（跨实例）
    → 所有实例的 WebSocket 连接收到消息
```

### 配置

```yaml
# bedrock-websocket application.yml
bedrock:
  redis:
    stream:
      enabled: true    # 启用 Redis Stream 消息广播
```

### 关键类

| 类 | 说明 |
|----|------|
| `WebSocketClient`（Feign） | 其他服务通过 Feign 推送 WebSocket 消息 |
| `RedisStreamListener` | Redis Stream 消费者，广播到本地 WebSocket 连接 |

---

## 注意事项

1. `@ServerEndpoint` 类需标注 `@Component` 才能被 Spring 管理
2. 多实例部署必须启用 Redis Stream，否则消息无法跨实例传递
3. 网关需配置 WebSocket 路由转发
