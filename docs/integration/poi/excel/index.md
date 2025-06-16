# Keel Excel 集成

Keel Excel 集成模块基于 Apache POI 提供了强大的 Excel 文件读写功能，支持 XLS 和 XLSX 格式，并提供了异步和同步两种操作模式。

## 核心组件

### KeelSheets - 工作簿管理器

`KeelSheets` 是 Excel 工作簿的主要管理类，实现了 `io.vertx.core.Closeable` 接口，支持资源自动管理。

#### 主要特性

- **多格式支持**: 支持 XLS (.xls) 和 XLSX (.xlsx) 格式
- **流式处理**: 支持大文件的流式读写，避免内存溢出
- **公式计算**: 可选的公式求值器支持
- **资源管理**: 自动资源清理和异常处理

#### 创建工作簿

```java
// 使用配置选项创建新工作簿
SheetsCreateOptions createOptions = new SheetsCreateOptions()
    .setUseXlsx(true)
    .setUseStreamWriting(false)
    .setWithFormulaEvaluator(true);

KeelSheets.useSheets(createOptions, sheets -> {
    // 在这里操作工作簿
    return Future.succeededFuture();
});
```

#### 打开现有工作簿

```java
// 从文件打开
SheetsOpenOptions openOptions = new SheetsOpenOptions()
    .setFile("path/to/excel.xlsx")
    .setWithFormulaEvaluator(true);

KeelSheets.useSheets(openOptions, sheets -> {
    // 处理工作簿
    return Future.succeededFuture();
});

// 从输入流打开
SheetsOpenOptions streamOptions = new SheetsOpenOptions()
    .setInputStream(inputStream)
    .setUseXlsx(true);
```

#### 大文件处理

对于超大 XLSX 文件，可以使用流式读取模式：

```java
// 配置大文件读取
SheetsOpenOptions.declareReadingVeryLargeExcelFiles();

SheetsOpenOptions hugeFileOptions = new SheetsOpenOptions()
    .setFile("huge-file.xlsx")
    .setHugeXlsxStreamingReaderBuilder(builder -> {
        builder.rowCacheSize(100)  // 内存中保持的行数
               .bufferSize(20480); // 缓冲区大小
    });
```

### KeelSheet - 工作表操作器

`KeelSheet` 提供了对单个工作表的读写操作，支持行级别和矩阵级别的数据处理。

#### 读取操作

```java
// 获取工作表
KeelSheet sheet = sheets.generateReaderForSheet("Sheet1");

// 读取单行
Row row = sheet.readRow(0);

// 读取所有行到矩阵
KeelSheetMatrix matrix = sheet.blockReadAllRowsToMatrix();

// 异步读取
Future<KeelSheetMatrix> futureMatrix = sheet.readAllRowsToMatrix();
```

#### 写入操作

```java
// 获取写入器
KeelSheet writer = sheets.generateWriterForSheet("NewSheet");

// 写入数据
List<List<String>> data = Arrays.asList(
    Arrays.asList("Name", "Age", "City"),
    Arrays.asList("Alice", "25", "Beijing"),
    Arrays.asList("Bob", "30", "Shanghai")
);

writer.blockWriteAllRows(data);
```

### 配置选项

#### SheetsCreateOptions - 创建选项

```java
SheetsCreateOptions options = new SheetsCreateOptions()
    .setUseXlsx(true)           // 使用 XLSX 格式
    .setUseStreamWriting(true)   // 启用流式写入
    .setWithFormulaEvaluator(false); // 禁用公式计算
```

#### SheetsOpenOptions - 打开选项

```java
SheetsOpenOptions options = new SheetsOpenOptions()
    .setFile(new File("data.xlsx"))
    .setWithFormulaEvaluator(true)
    .setUseXlsx(true);
```

### 数据结构

#### KeelSheetMatrix - 数据矩阵

表示 Excel 数据的二维矩阵结构：

```java
KeelSheetMatrix matrix = sheet.blockReadAllRowsToMatrix();

// 获取表头
List<String> headers = matrix.getHeaderRow();

// 获取数据行
List<List<String>> rows = matrix.getRawRowList();

// 迭代行数据
Iterator<KeelSheetMatrixRow> iterator = matrix.getRowIterator();
while (iterator.hasNext()) {
    KeelSheetMatrixRow row = iterator.next();
    String value = row.readValue(0);
    Integer intValue = row.readValueToInteger(1);
    Double doubleValue = row.readValueToDouble(2);
}
```

#### KeelSheetMatrixRow - 行数据

提供类型安全的单元格数据访问：

```java
KeelSheetMatrixRow row = matrix.getRowIterator().next();

// 读取字符串值
String name = row.readValue(0);

// 读取数值
BigDecimal decimal = row.readValueToBigDecimal(1);
Integer age = row.readValueToInteger(1);
Long id = row.readValueToLong(2);
Double score = row.readValueToDouble(3);
```

#### KeelSheetTemplatedMatrix - 模板化矩阵

基于列名模板的数据访问：

```java
// 创建模板
KeelSheetMatrixRowTemplate template = KeelSheetMatrixRowTemplate.create(
    Arrays.asList("name", "age", "city")
);

// 读取模板化矩阵
KeelSheetTemplatedMatrix templatedMatrix = sheet.blockReadAllRowsToTemplatedMatrix();

// 按列名访问数据
for (KeelSheetMatrixTemplatedRow row : templatedMatrix.getRows()) {
    String name = row.readValue("name");
    Integer age = row.readValueToInteger("age");
    String city = row.readValue("city");
}
```

### 行过滤器

`SheetRowFilter` 接口允许在读取时过滤行数据：

```java
// 过滤空行
SheetRowFilter emptyRowFilter = SheetRowFilter.toThrowEmptyRows();

// 自定义过滤器
SheetRowFilter customFilter = rawRow -> {
    // 过滤第一列为空的行
    return rawRow.get(0) == null || rawRow.get(0).trim().isEmpty();
};

// 应用过滤器
KeelSheetMatrix matrix = sheet.blockReadAllRowsToMatrix(0, 10, customFilter);
```

## 使用示例

### 读取 Excel 文件

```java
SheetsOpenOptions options = new SheetsOpenOptions()
    .setFile("data.xlsx")
    .setWithFormulaEvaluator(true);

KeelSheets.useSheets(options, sheets -> {
    KeelSheet sheet = sheets.generateReaderForSheet(0);
    
    return sheet.readAllRowsToMatrix(0, 10, SheetRowFilter.toThrowEmptyRows())
        .compose(matrix -> {
            // 处理数据
            for (KeelSheetMatrixRow row : matrix.getRowIterator()) {
                System.out.println("Row: " + row.readValue(0));
            }
            return Future.succeededFuture();
        });
});
```

### 创建 Excel 文件

```java
SheetsCreateOptions createOptions = new SheetsCreateOptions()
    .setUseXlsx(true)
    .setUseStreamWriting(false);

KeelSheets.useSheets(createOptions, sheets -> {
    KeelSheet sheet = sheets.generateWriterForSheet("Data");
    
    // 准备数据
    List<List<String>> data = Arrays.asList(
        Arrays.asList("ID", "Name", "Score"),
        Arrays.asList("1", "Alice", "95.5"),
        Arrays.asList("2", "Bob", "87.2")
    );
    
    // 写入数据
    sheet.blockWriteAllRows(data);
    
    // 保存文件
    sheets.save("output.xlsx");
    
    return Future.succeededFuture();
});
```

### 处理大文件

```java
// 配置大文件处理
SheetsOpenOptions.declareReadingVeryLargeExcelFiles();

SheetsOpenOptions options = new SheetsOpenOptions()
    .setFile("huge-data.xlsx")
    .setHugeXlsxStreamingReaderBuilder(builder -> {
        builder.rowCacheSize(50)
               .bufferSize(16384);
    });

KeelSheets.useSheets(options, sheets -> {
    KeelSheet sheet = sheets.generateReaderForSheet(0);
    
    // 流式处理行数据
    return sheet.readAllRows(row -> {
        // 处理单行数据
        System.out.println("Processing row: " + row.getRowNum());
        return Future.succeededFuture();
    });
});
```

## 最佳实践

### 1. 资源管理

始终使用 `useSheets` 方法确保资源正确释放：

```java
// ✅ 推荐方式
KeelSheets.useSheets(options, sheets -> {
    // 操作代码
    return Future.succeededFuture();
});

// ❌ 不推荐 - 需要手动管理资源
KeelSheets sheets = KeelSheets.autoGenerateXLSX(); // 已废弃
```

### 2. 大文件处理

- 对于大于 100MB 的 XLSX 文件，使用流式读取
- 调整行缓存大小和缓冲区大小以优化性能
- 考虑使用临时文件而非内存存储

### 3. 公式处理

- 只在需要时启用公式计算器以提高性能
- 对于只读操作，可以使用缓存的公式结果

### 4. 错误处理

```java
KeelSheets.useSheets(options, sheets -> {
    return sheet.readAllRowsToMatrix()
        .recover(throwable -> {
            // 处理读取错误
            logger.error("Failed to read Excel", throwable);
            return Future.succeededFuture(new KeelSheetMatrix());
        });
});
```

## 版本历史

- **3.0.13**: 初始版本
- **3.0.18**: 完成技术预览
- **3.0.20**: 添加行过滤器支持
- **3.1.3**: 添加公式计算器支持
- **3.2.11**: 添加大文件流式处理支持
- **4.0.2**: 实现 Closeable 接口，移除 AutoCloseable

## 依赖项

该模块依赖以下库：

- Apache POI (HSSF/XSSF)
- excel-streaming-reader (用于大文件处理)
- Vert.x Core (异步支持)

## 注意事项

1. **内存使用**: 大文件处理时注意内存配置
2. **线程安全**: POI 工作簿不是线程安全的，避免并发访问
3. **格式兼容**: XLS 格式有行数和列数限制
4. **公式支持**: 复杂公式可能需要特殊处理
