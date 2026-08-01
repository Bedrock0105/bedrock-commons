# API 加解密 (Crypto)

## 概述

`bedrock-common-crypto` 提供基于注解的 API 请求解密 / 响应加密能力：

1. **请求体解密**：`ApiDecryptRequestBodyAdvice`（`@RequestBody`）
2. **Query/Form 参数解密**：`ApiDecryptParamResolver`（统一参数名，默认 `params`）
3. **响应体加密**：`ApiEncryptResponseBodyAdvice`

内置算法（SPI + `@AutoService`）：AES、DES、RSA、SM4、SM2。算法可通过注解 `handler`、配置 `algorithm-bean` / `algorithm` 选择。

> 本模块按需引入，**不**默认打进 `bedrock-common-boot`。

---

## 快速开始

### 1. 引入依赖

```xml
<dependency>
    <groupId>org.bedrock</groupId>
    <artifactId>bedrock-common-crypto</artifactId>
</dependency>
```

### 2. 配置密钥

```yaml
bedrock:
  crypto:
    enabled: true
    algorithm: AES
    key: "你的对称密钥Hex"          # AES / DES / SM4
    # public-key: ""               # RSA / SM2 加密用
    # private-key: ""              # RSA / SM2 解密用
    param-name: params             # Query/Form 统一密文参数名
```

### 3. 标注注解

```java
// 类上：该类所有方法入参解密 + 返回加密
@ApiCrypto
@RestController
@RequestMapping("/demo")
public class DemoController {

    @PostMapping("/save")
    public R<UserVO> save(@RequestBody UserDTO dto) { ... }

    // 排除加解密
    @ApiExcludeCrypto
    @GetMapping("/ping")
    public R<String> ping() { ... }

    // 仅加密返回
    @ApiEncrypt(algorithm = CryptoAlgorithmEnum.AES)
    @GetMapping("/export")
    public R<String> export() { ... }
}
```

---

## 注解说明

| 注解 | 作用范围 | 说明 |
|------|----------|------|
| `@ApiEncrypt` | 类、方法 | 响应加密 |
| `@ApiDecrypt` | 类、方法、参数 | 请求解密 |
| `@ApiCrypto` | 类、方法、参数 | 组合注解 = `@ApiEncrypt` + `@ApiDecrypt` |
| `@ApiExcludeCrypto` | 类、方法、参数 | 排除加解密（优先级最高） |

### `@ApiEncrypt` / `@ApiDecrypt` 属性

| 属性 | 说明 |
|------|------|
| `handler` | 自定义 `CryptoAlgorithm` 实现 Class，**优先级最高**；默认接口本身表示未指定 |
| `algorithm` | 内置枚举：`AES` / `DES` / `RSA` / `SM4` / `SM2` / `NULL` |
| `key` | 对称密钥 Hex，空则用配置 |
| `publicKey` | 非对称公钥 Hex |
| `privateKey` | 非对称私钥 Hex |

### 生效判定

1. 存在 `@ApiExcludeCrypto` → 不加解密
2. 方法/参数有显式注解 → 按显式注解
3. 否则继承类级注解

---

## 算法选择优先级

高 → 低：

1. 注解 `handler`（具体实现 Class）
2. 配置 `bedrock.crypto.algorithm-bean`（实现 Class）
3. 注解 `algorithm` 枚举
4. 配置 `bedrock.crypto.algorithm` 枚举（默认 AES）

密钥：注解非空字段 > 配置文件。

---

## 报文约定

| 方向 | 约定 |
|------|------|
| 请求体 | Body 为 Base64 密文；服务端解密后再反序列化为业务对象 |
| Query/Form | 密文放在统一参数名（默认 `params`）中，解密后为 JSON，再按参数名绑定 |
| 响应 | 整段业务 JSON 加密后以密文字符串写出（`text/plain`） |

客户端需按「整段正文 = 密文」处理响应，不能当作普通 JSON 解析。

---

## 自定义算法

```java
@AutoService(CryptoAlgorithm.class)
public class MyCryptoAlgorithm implements CryptoAlgorithm {

    @Override
    public void init(CryptoProperties cryptoProperties) { ... }

    @Override
    public String encrypt(byte[] plainText, String secretKey, String publicKey, String privateKey) { ... }

    @Override
    public byte[] decrypt(String cipherText, String secretKey, String publicKey, String privateKey) { ... }
}
```

使用方式：

1. 注解：`@ApiEncrypt(handler = MyCryptoAlgorithm.class)`
2. 或配置：`bedrock.crypto.algorithm-bean: org.xxx.MyCryptoAlgorithm`
3. 若要进枚举，在 `CryptoAlgorithmEnum` 增加一项挂 Class

---

## 配置项

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `bedrock.crypto.enabled` | `true` | 是否启用 |
| `bedrock.crypto.algorithm` | `AES` | 默认算法枚举 |
| `bedrock.crypto.algorithm-bean` | 接口本身 | 自定义算法 Class，优先于枚举 |
| `bedrock.crypto.param-name` | `params` | Query/Form 密文参数名 |
| `bedrock.crypto.key` | `""` | 对称密钥 Hex |
| `bedrock.crypto.public-key` | `""` | 非对称公钥 Hex |
| `bedrock.crypto.private-key` | `""` | 非对称私钥 Hex |
| `bedrock.crypto.order` | `0` | Advice 执行顺序，越小越先 |

---

## 相关组件

| 类 | 职责 |
|----|------|
| `CryptoAlgorithm` | 策略接口 |
| `CryptoAlgorithmRegistry` | SPI `ServiceLoader` 加载并缓存实现 |
| `CryptoConfiguration` | 自动配置 |
| `ApiDecryptRequestBodyAdvice` | 请求体解密 |
| `ApiEncryptResponseBodyAdvice` | 响应体加密 |
| `ApiDecryptParamResolver` | 参数解密 |

密文作为 `String` 返回时，若仍走 Jackson，可能被包一层引号；框架在 `WebMvcMessageConverterConfiguration#extendMessageConverters` 中增强了 Jackson，对 `String` 明文写出，避免该问题。

---

## 注意事项

1. 对称算法密钥、非对称公私钥请使用 Hex；生产环境勿把密钥写死在注解里
2. DES 仅作兼容，生产建议使用 AES / SM4
3. Query 多参数场景共用一个 `params` 密文字段，解密后的 JSON 需包含各参数字段名
4. 模块按需引入，未引入依赖时注解无效
