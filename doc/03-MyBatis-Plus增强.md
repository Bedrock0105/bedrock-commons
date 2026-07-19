# MyBatis-Plus 增强

## 概述

`bedrock-common-mybatisplus` 在 MyBatis-Plus 基础上提供：

- 实体基类 `BaseEntity`（雪花 ID、审计字段、逻辑删除）
- 服务基类 `BaseServiceImpl` / `IBaseService`
- 分页参数 `Query` + `PageUtil`
- 自动配置分页插件、Druid 连接池、SQL 日志
- 自动填充创建人/更新人（依赖 `AuthUtil`）

多租户过滤在 `bedrock-common-tenant`，数据权限在 `bedrock-common-datascope`，各自独立模块。

---

## 快速开始

### 1. 实体类

```java
@TableName("bedrock_dict")
public class Dict extends BaseEntity {
    private String dictKey;
    private String dictValue;
}
```

`BaseEntity` 字段：

| 字段 | 列名 | 说明 |
|------|------|------|
| `id` | `id` | 雪花 ID，`IdType.ASSIGN_ID` |
| `createTime` | `create_time` | 创建时间，INSERT 自动填充 |
| `updateTime` | `update_time` | 更新时间，INSERT/UPDATE 自动填充 |
| `createUserId` | `create_user_id` | 创建人 ID |
| `updateUserId` | `update_user_id` | 更新人 ID |
| `isDeleted` | `is_deleted` | 逻辑删除，0=未删除，1=已删除 |

### 2. Mapper

```java
public interface DictMapper extends BaseMapper<Dict> {
    List<DictListVO> selectDictList(@Param("param") DictPageParam param);
}
```

自动扫描路径：`org.bedrock.**.mapper.**`

### 3. Service

```java
@Service
public class DictServiceImpl extends BaseServiceImpl<DictMapper, Dict> implements IDictService {
    public boolean removeById(Long id) {
        return logicRemoveById(id);  // 软删除，自动更新 updateUserId/updateTime
    }
}
```

### 4. Controller 分页

```java
@GetMapping("/page")
public R<IPage<DictListVO>> page(Query query, DictPageParam param) {
    return R.success(dictService.pageDict(PageUtil.getPage(query), param));
}
```

`Query` 分页参数：`current`（默认 1）、`size`（默认 10），也可调用 `query.toPage()`。

---

## 配置

### bedrock.mybatis-plus.*

```yaml
bedrock:
  mybatis-plus:
    sql-log: true          # SQL 日志，默认 true
    sql-log-color: true    # 彩色 SQL 日志
    ignore-table: []       # SQL 日志忽略的表
    overflow: false        # 分页溢出处理
    max-limit: 1000        # 单页最大条数
    optimize-join: false     # 是否优化 JOIN 分页
```

### 内置 mybatis-plus.yml

```yaml
mybatis-plus:
  mapper-locations: classpath:/mapper/*Mapper.xml
  type-aliases-package: org.bedrock.**.entity
  global-config:
    db-config:
      id-type: assign_id
      logic-delete-field: is_deleted
      logic-delete-value: 1
      logic-not-delete-value: 0
      insert-strategy: not_null
      update-strategy: not_null
```

### 内置 druid.yml

Druid 连接池、监控页面（`/druid/*`）等完整默认配置，引入模块即生效。

---

## 自动配置

`MybatisPlusConfiguration` 自动完成：

- `@MapperScan("org.bedrock.**.mapper.**")`
- 注册分页插件（读取 `bedrock.mybatis-plus.max-limit` 等）
- 注册 `DefaultMetaObjectHandler`（自动填充审计字段）
- 可选接入 `TenantLineInnerInterceptor`（tenant 模块）
- 可选接入 `BedrockInnerInterceptor`（扩展点）

---

## BaseServiceImpl 常用方法

继承 `ServiceImpl<M, T>` 并扩展：

```java
logicRemoveById(id)       // 软删除单条
logicRemoveByIds(ids)     // 软删除多条
```

---

## bedrock-cloud 示例

```java
// DictServiceImpl - Lambda 条件 + 逻辑删除字段
.eq(BaseEntity::getIsDeleted, BedrockDBConstant.DB_NOT_DELETED)

// AdminServiceImpl - 软删除
public boolean removeById(Long id) {
    return logicRemoveById(id);
}

// AdminController - 分页
public R<IPage<AdminListVO>> page(Query query, AdminPageParam param) {
    return R.success(adminService.pageAdmin(PageUtil.getPage(query), param));
}
```

---

## 注意事项

1. 逻辑删除字段为 `is_deleted`（非 `deleted`）
2. 子类实体无需重复声明 `@TableId`，`BaseEntity` 已包含
3. 自动填充操作人依赖 `AuthUtil.getAuthUser()`，未登录时为 null
4. 分页参数名为 `current`/`size`（非 pageNum/pageSize）
5. 统一响应使用 `R.success()`（非 `R.ok()`，两者均可用但 cloud 项目主要用 success）
