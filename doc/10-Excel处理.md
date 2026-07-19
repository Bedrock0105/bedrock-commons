# Excel 处理

## 概述

`bedrock-common-excel` 基于 **FastExcel**（`cn.idev.excel`）提供注解驱动的 Excel 导入导出，无需手动操作 `HttpServletResponse`。

核心注解：

| 注解 | 作用 |
|------|------|
| `@RequestExcel` | 解析上传 Excel → `List<T>` |
| `@ResponseExcel` | 方法返回 `List`/`R<List>` 自动下载 Excel |

---

## 快速开始

### 导入

```java
@PostMapping("/import-excel")
public R<Void> importExcel(
    @RequestExcel(value = "file", head = AdminImportExcel.class, headRowNumber = 2)
    List<AdminImportExcel> list) {
    return R.status(adminService.importExcel(list));
}
```

| 属性 | 说明 |
|------|------|
| `value` | multipart 字段名（必填） |
| `head` | 表头映射类 |
| `headRowNumber` | 表头行号（从 1 开始） |
| `sheetNo` | Sheet 序号，默认 0 |
| `skipRowNum` | 跳过的行数 |
| `listener` | 自定义读取监听器，默认 `ExcelListener` |

### 导出

```java
@GetMapping("/export-excel")
@ResponseExcel(fileName = "管理员数据", sheets = @ResponseExcel.Sheet(name = "管理员列表", head = AdminExportExcel.class))
public R<List<AdminExportExcel>> exportExcel(AdminPageParam param) {
    return R.success(adminService.exportExcel(param));
}
```

| 属性 | 说明 |
|------|------|
| `fileName` | 下载文件名 |
| `sheets` | Sheet 配置（`name`、`head`、`index`） |
| `excelType` | 文件类型，默认 XLSX |
| `inMemory` | 内存模式，富文本/下拉需设为 true |
| `password` | 文件密码 |

返回值支持：`List<T>`、`Map<sheetName, List>`、`R<List<T>>`（自动取 data 字段）。

### 模板下载

```java
@GetMapping("/download-excel")
@ResponseExcel(fileName = "用户导入模版", sheets = @ResponseExcel.Sheet(name = "管理员列表", head = AdminImportExcel.class), inMemory = true)
public R<List<AdminImportExcel>> downloadExcel() {
    return R.success(List.of());  // 空列表即空模板
}
```

---

## 实体类注解

### FastExcel 原生

```java
public class AdminImportExcel {
    @ExcelProperty("用户名")
    private String username;

    @ExcelProperty("手机号")
    private String phone;
}
```

### Bedrock 扩展注解

| 注解 | 作用 |
|------|------|
| `@ExcelHeaderDesc` | 表头描述/行高 |
| `@ExcelConverter` | 自定义值转换（SpEL） |
| `@ExcelHeaderComment` | 表头批注 |
| `@ExcelContentDown` | 下拉选项联动 |

---

## ExcelUtil 编程式 API

不通过 Controller 注解，直接操作流：

```java
// 读取
List<AdminImportExcel> list = ExcelUtil.readExcel(inputStream, AdminImportExcel.class);
List<AdminImportExcel> list = ExcelUtil.readExcel(inputStream, AdminImportExcel.class, skipRowNum);

// 导出
ExcelUtil.exportExcel(response, "文件名", "Sheet1", dataList, AdminExportExcel.class);
ExcelUtil.exportExcel(outputStream, "Sheet1", dataList, AdminExportExcel.class);

// 下拉校验
ExcelUtil.addValidationToSheet(sheet, options, firstRow, lastRow, firstCol, lastCol);
```

---

## 配置

模块无独立 YAML 配置项，`ExcelConfiguration` 自动注册 `RequestExcelArgumentResolver` 和 `ResponseReturnValueHandler`。

---

## bedrock-cloud 参考

完整示例见 `bedrock-system/.../AdminController.java` 和 `AdminImportExcel.java`。

---

## 注意事项

1. 导入时 `headRowNumber` 需与实际 Excel 表头行对应（bedrock-cloud 管理员导入为 2，第一行是说明）
2. 含下拉/富文本的模板导出需 `inMemory = true`
3. Swagger 文档中文件参数需 `@Parameter(hidden = true)` 配合 `@RequestExcel`
4. 无 `bedrock.excel.*` 配置项，无行数限制等内置校验
