# Redis 缓存

## 概述

`bedrock-common-redis` 提供：

- `RedisUtil` 静态工具类（String/Hash/List/Set/ZSet 操作）
- Spring Cache 集成（`RedisCacheManagerConfiguration`）
- Redis Stream 消息（`@RSMessageListener` + `RSMessageTemplate`）
- 多种序列化器可选（默认 Protostuff）

> 本模块**不提供**内置分布式锁、限流器。

---

## 快速开始

微服务项目通过 `bedrock-common-boot` 间接引入。

### RedisUtil 基本用法

```java
// 初始化在 RedisTemplateConfiguration 中自动完成
RedisUtil.set("key", value);
Object val = RedisUtil.get("key");
RedisUtil.set("key", value, 3600);  // 带过期秒数
RedisUtil.delete("key");

// Hash
RedisUtil.hSet("hashKey", "field", value);
Object hVal = RedisUtil.hGet("hashKey", "field");

// Set
RedisUtil.setAdd("setKey", "member1", "member2");
Set<Object> members = RedisUtil.setMembers("setKey");

// ZSet
RedisUtil.zAdd("zsetKey", "member", 1.0);
RedisUtil.zIncrementScore("zsetKey", "member", 1.0);
```

---

## 配置

### 连接配置（Spring Boot 标准）

```yaml
spring:
  data:
    redis:
      host: 127.0.0.1
      port: 6379
      password: your-password
      database: 0
```

> Boot 3 使用 `spring.data.redis`（非 `spring.redis`）。

### 序列化

```yaml
bedrock:
  redis:
    serializer: PROTOSTUFF   # 默认 PROTOSTUFF
                           # 可选：JDK / JSON / KRYO / FST
```

### Spring Cache

```yaml
spring:
  cache:
    type: redis
    redis:
      time-to-live: 3600000    # 毫秒
      key-prefix: "cache:"
```

使用 Spring 标准注解：

```java
@Cacheable(cacheNames = CacheConstant.ADMIN_CACHE, key = "'detail:' + #id")
public AdminDetailVO detail(Long id) { ... }

@CacheEvict(cacheNames = CacheConstant.ADMIN_CACHE, key = "'detail:' + #param.username")
public boolean submit(AdminSubmitParam param) { ... }
```

---

## Redis Stream

### 启用

```yaml
bedrock:
  redis:
    stream:
      enabled: true              # 默认 false，需显式开启
      batch-size: 10
      seconds-poll-timeout: 5
      core-pool-size: 10
      max-pool-size: 100
      queue-capacity: 1000
      thread-name-prefix: redisListenerStreamExecutor-
      keep-alive-seconds: 30
```

### 发送消息

```java
@Autowired
private RSMessageTemplate rsMessageTemplate;

rsMessageTemplate.send("topic/name", messageObject);
```

### 消费消息

```java
@RSMessageListener(topic = "topic/name", group = "my-group", consumer = "consumer-1")
public void onMessage(EventMessage message) {
    // 处理消息
}
```

---

## bedrock-cloud 示例

- **Spring Cache**：`AdminServiceImpl`、`DictServiceImpl` 等广泛使用 `@Cacheable` / `@CacheEvict`
- **Redis Stream**：`bedrock-websocket` 模块用于 WebSocket 消息广播

```yaml
# bedrock-websocket application.yml
bedrock:
  redis:
    stream:
      enabled: true
```

---

## 注意事项

1. 默认序列化为 **PROTOSTUFF**，跨语言需注意兼容性
2. `setIfAbsent` 仅两参数版本，无 timeout 重载
3. Stream 默认全局频道：`bedrock:eventbus:channel`（REDIS 模式 EventBus 共用）
4. `RedisUtil` 在 bedrock-cloud 业务代码中较少直接使用，主要用 Spring Cache
