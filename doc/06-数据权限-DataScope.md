# 数据权限 (DataScope)

## 概述

`bedrock-common-datascope` 提供基于角色的数据范围控制，通过 MyBatis 拦截器直接改写 SQL（JSqlParser），**非** XML 占位符注入。

提供两条通道：

| 通道 | 注解位置 | 拦截方式 |
|------|----------|----------|
| MyBatis | `@DataScope` 标注 **Mapper 方法** | `DataScopeInnerInterceptor` |
| Druid | `@DataScopeFilter` 标注 **Service 方法** | `DataAuthFilterAspect` + Druid 拦截器 |

---

## 配置

```yaml
bedrock:
  datascope:
    enabled: true    # 默认 true，仅此一项配置
```

---

## 数据范围类型（DataScopeTypeEnum）

| 枚举值 | 说明 |
|--------|------|
| `DATA_SCOPE_ALL` | 全部数据 |
| `DATA_SCOPE_CUSTOM` | 自定义（指定部门/用户） |
| `DATA_SCOPE_DEPT` | 本部门 |
| `DATA_SCOPE_DEPT_AND_CHILD` | 本部门及子部门 |
| `DATA_SCOPE_SELF` | 仅本人 |
| `DATA_SCOPE_CUSTOM_AUTH` | 自定义权限处理器 |

---

## @DataScope（Mapper 方法）

```java
public interface AdminMapper extends BaseMapper<Admin> {
    @DataScope(type = DataScopeTypeEnum.DATA_SCOPE_DEPT, column = "dept_id")
    List<AdminListVO> selectAdminDetailList(IPage<?> page, @Param("param") AdminPageParam param, @Param("noRoleAlias") List<String> noRoleAlias);
}
```

| 属性 | 默认值 | 说明 |
|------|--------|------|
| `mapperId` | 方法全限定名 | 关联权限规则 |
| `type` | — | 数据范围类型 |
| `column` | `dept_id` | 过滤列名 |
| `field` | `*` | 查询字段 |
| `value` | — | 自定义值 |

---

## @DataScopeFilter（Service 方法）

```java
@DataScopeFilter(mapperId = "org.bedrock.system.mapper.AdminMapper.selectAdminDetailList")
public List<AdminListVO> listAdmin(AdminPageParam param) { ... }
```

---

## 数据库驱动

权限规则存储在：

- `bedrock_permission_datascope`：数据权限定义（mapperId、scopeType、scopeColumn）
- `bedrock_role_permission`：角色与权限关联（`perm_type=2` 表示数据权限）

`DefaultScopeModelSupport` 按 `mapperId` + `roleId` 查询规则，缓存 key 前缀：`SCOPE_DATA_SCOPE_MAPPER_ROLE_PREFIX`。

管理员（`AuthUtil.isAdmin()` / `isAdministrator()`）自动跳过数据权限过滤。

---

## 扩展点

| 接口 | 作用 |
|------|------|
| `AuthHandler` | 自定义权限范围，`getAuthScope()` 返回权限集合 |
| `DataPermissionSqlProcessor` | 自定义 SQL 改写逻辑 |

---

## bedrock-cloud 现状

- **元数据管理已就绪**：`PermissionDatascopeController` / `PermissionDatascopeServiceImpl` 提供 CRUD
- **注解尚未启用**：全仓库无 `@DataScope` 或 `@DataScopeFilter` 使用
- 角色绑定权限时会刷新 `SCOPE_CACHE`（`RoleServiceImpl`、`DeptServiceImpl`）

### 启用步骤

1. 在 `bedrock_permission_datascope` 表配置规则
2. 在 Mapper 方法上添加 `@DataScope`
3. 角色绑定对应数据权限
4. 验证非管理员用户的数据隔离效果

---

## 注意事项

1. `@DataScope` 标注在 **Mapper 接口方法**上，非 Service
2. 无 `${dataScope}` XML 占位符，拦截器直接改写 SQL
3. 与多租户模块独立运作，各自追加 SQL 条件
4. 缓存由 `CacheUtil` + `SCOPE_CACHE` 实现，无独立 cache 配置项
