# 树形结构工具 (TreeUtil)

## 概述

`bedrock-common-code` 中的 `TreeUtil` 提供列表到树形结构的转换，适用于菜单树、部门树、分类树等场景。

---

## 使用方式

### 1. 实体实现 INode 接口

```java
public class MenuVO implements INode<MenuVO, Long> {
    private Long id;
    private Long parentId;
    private String name;
    private List<MenuVO> children = new ArrayList<>();

    @Override
    public Long getId() { return id; }

    @Override
    public Long getParentId() { return parentId; }

    @Override
    public List<MenuVO> getChildren() { return children; }
}
```

### 2. 构建树

```java
List<MenuVO> flatList = menuService.listAll();
List<MenuVO> tree = TreeUtil.buildTree(flatList);
```

指定根节点 ID：

```java
List<MenuVO> tree = TreeUtil.buildTree(flatList, 0L);
```

---

## API

| 方法 | 说明 |
|------|------|
| `buildTree(list)` | 构建树，根节点 parentId 为 null 或 0 |
| `buildTree(list, topId)` | 构建树，指定根节点 parentId |

---

## bedrock-cloud 示例

`bedrock-system` 模块中菜单、部门等树形数据使用 `TreeUtil.buildTree()` 转换：

```java
List<MenuTreeVO> tree = TreeUtil.buildTree(menuList);
```

---

## 注意事项

1. 列表元素必须实现 `INode<T, ID>` 接口
2. `children` 字段需初始化为空列表（非 null）
3. 找不到父节点的孤儿节点会被当作根节点处理
4. 时间复杂度 O(n)，通过 Map 映射实现
