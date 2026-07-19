# 事件总线 (EventBus)

## 概述

`bedrock-common-code` 中的 EventBus 提供发布-订阅模式的消息通信，支持本地（进程内）和 Redis（跨服务）两种实现。

> **bedrock-cloud 当前未使用 EventBus**。需显式开启配置后才会装配。

---

## 快速开始

### 1. 启用

```yaml
bedrock:
  eventbus:
    enabled: true              # 默认 false，必须显式开启
    type: LOCAL                # LOCAL | REDIS
    global-topic: bedrock:eventbus:channel  # 仅 REDIS 模式生效
```

### 2. 订阅

```java
@Autowired
private EventBus eventBus;

@PostConstruct
public void init() {
    eventBus.subscribe(Subscribes.<OrderEvent>builder()
        .topic("order/create", "order/cancel")
        .subscribe(eventMessage -> {
            OrderEvent msg = eventMessage.message();
            String topic = eventMessage.topic();
            // 处理事件
        })
        .build());
}
```

### 3. 发布

```java
eventBus.publish("order/create", new OrderEvent(orderId, amount));
```

---

## API

| 方法 | 说明 |
|------|------|
| `subscribe(Subscribes<?>)` | 订阅主题 |
| `unsubscribe(Subscribes<?>)` | 取消订阅 |
| `publish(topic, message)` | 发布消息 |
| `getEventBusType()` | 获取当前实现类型 |

---

## 实现对比

| 特性 | LOCAL | REDIS |
|------|-------|-------|
| 范围 | 进程内 | 跨服务 |
| Topic 分隔 | `/` 层级（如 `order/create`） | 统一频道 + 内部路由 |
| 通配符 | `*`（单层）、`**`（多层） | 同 LOCAL |
| 默认频道 | — | `bedrock:eventbus:channel` |

Topic 推荐使用 `/` 分隔的层级命名，如 `order/create`、`user/register`。

---

## Subscribes 构建

```java
Subscribes.<T>builder()
    .topic("topic/a", "topic/b")     // 支持多主题
    .subscribe(eventMessage -> {      // Consumer<EventMessage<T>>
        T msg = eventMessage.message();
        String topic = eventMessage.topic();
    })
    .build();
```

回调类型为 `Consumer<EventMessage<T>>`（非直接接收 message 对象）。

---

## 注意事项

1. 默认 `enabled=false`，不配置则 EventBus Bean 不存在
2. 可自定义 `eventBusExecutor` Bean 覆盖默认线程池
3. REDIS 模式依赖 `bedrock-common-redis`，所有消息走单一 Redis 频道
4. 不要手动创建 `LocalEventBusImpl`，自动配置已提供
