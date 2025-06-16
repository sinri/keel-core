# Keel CSV 集成包

## 概述

Keel CSV 集成包 (`io.github.sinri.keel.integration.poi.csv`) 提供了用于读取和写入 CSV 文件的功能。该包是技术预览版本（自 3.1.1 版本开始），提供了异步和同步两种操作方式。

## 核心组件

### CsvCell

`CsvCell` 类表示 CSV 文件中的单个单元格，支持字符串和数字类型的数据。

#### 特性
- 自动类型检测：尝试将字符串解析为 `BigDecimal` 数字类型
- 空值处理：支持 null 值和空字符串的区分
- 类型判断：提供方法判断单元格是否为数字、空值或 null

#### 主要方法
- `CsvCell(String s)` - 构造函数，接受字符串值
- `boolean isNumber()` - 判断是否为数字类型
- `boolean isEmpty()` - 判断是否为空字符串
- `boolean isNull()` - 判断是否为 null 值
- `BigDecimal getNumber()` - 获取数字值（可能为 null）
- `String getString()` - 获取字符串值（可能为 null）

### CsvRow

`CsvRow` 类表示 CSV 文件中的一行数据，包含多个 `CsvCell` 对象。

#### 主要方法
- `CsvRow addCell(CsvCell cell)` - 添加单元格到行中
- `CsvCell getCell(int i)` - 获取指定索引的单元格
- `int size()` - 获取行中单元格的数量

### KeelCsvReader

`KeelCsvReader` 类提供 CSV 文件的读取功能，支持自定义分隔符和字符编码。

#### 特性
- 支持多种输入源：`InputStream`、`File`、文件路径字符串
- 自定义分隔符：默认为逗号，可自定义
- 字符编码支持：可指定字符集
- 引号处理：正确处理包含引号、换行符和分隔符的字段
- 异步和同步操作：提供 Future 和阻塞两种操作方式

#### 创建实例
```java
// 从文件创建
Future<KeelCsvReader> reader = KeelCsvReader.create("path/to/file.csv", StandardCharsets.UTF_8);

// 从 InputStream 创建
Future<KeelCsvReader> reader = KeelCsvReader.create(inputStream, StandardCharsets.UTF_8);

// 从 BufferedReader 创建
KeelCsvReader reader = new KeelCsvReader(bufferedReader);
```

#### 主要方法
- `KeelCsvReader setSeparator(String separator)` - 设置分隔符
- `Future<CsvRow> readRow()` - 异步读取一行
- `CsvRow blockReadRow()` - 同步读取一行
- `Future<Void> close()` - 异步关闭资源
- `void blockClose()` - 同步关闭资源

### KeelCsvWriter

`KeelCsvWriter` 类提供 CSV 文件的写入功能，支持自定义分隔符和字符编码。

#### 特性
- 支持多种输出目标：`OutputStream`、`File`、文件路径字符串
- 自定义分隔符：默认为逗号，可自定义
- 字符编码支持：默认 UTF-8，可自定义
- 自动引号处理：自动为包含特殊字符的字段添加引号
- 异步和同步操作：提供 Future 和阻塞两种操作方式

#### 创建实例
```java
// 写入到文件
Future<KeelCsvWriter> writer = KeelCsvWriter.create("path/to/output.csv");

// 写入到 OutputStream
Future<KeelCsvWriter> writer = KeelCsvWriter.create(outputStream);
```

#### 主要方法
- `KeelCsvWriter setSeparator(String separator)` - 设置分隔符
- `KeelCsvWriter setCharset(Charset charset)` - 设置字符编码
- `Future<Void> writeRow(List<String> row)` - 异步写入一行
- `void blockWriteRow(List<String> list)` - 同步写入一行
- `Future<Void> close()` - 异步关闭资源
- `void blockClose()` - 同步关闭资源

## 使用示例

### 读取 CSV 文件

```java
import io.github.sinri.keel.integration.poi.csv.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.ArrayList;

// 异步读取示例
KeelCsvReader.create("data.csv", StandardCharsets.UTF_8)
    .compose(reader -> {
        reader.setSeparator(",");
        return reader.readRow();
    })
    .onSuccess(row -> {
        if (row != null) {
            for (int i = 0; i < row.size(); i++) {
                CsvCell cell = row.getCell(i);
                if (cell.isNumber()) {
                    System.out.println("数字: " + cell.getNumber());
                } else {
                    System.out.println("字符串: " + cell.getString());
                }
            }
        }
    })
    .onFailure(Throwable::printStackTrace);

// 同步读取示例
try {
    KeelCsvReader reader = new KeelCsvReader(
        new FileInputStream("data.csv"), 
        StandardCharsets.UTF_8
    );
    reader.setSeparator(",");
    
    CsvRow row;
    while ((row = reader.blockReadRow()) != null) {
        // 处理每一行数据
        for (int i = 0; i < row.size(); i++) {
            CsvCell cell = row.getCell(i);
            System.out.println("单元格 " + i + ": " + cell.getString());
        }
    }
    reader.blockClose();
} catch (IOException e) {
    e.printStackTrace();
}
```

### 写入 CSV 文件

```java
import io.github.sinri.keel.integration.poi.csv.*;
import java.util.Arrays;
import java.nio.charset.StandardCharsets;

// 异步写入示例
KeelCsvWriter.create("output.csv")
    .compose(writer -> {
        writer.setSeparator(",")
              .setCharset(StandardCharsets.UTF_8);
        
        // 写入表头
        return writer.writeRow(Arrays.asList("姓名", "年龄", "城市"));
    })
    .compose(v -> writer.writeRow(Arrays.asList("张三", "25", "北京")))
    .compose(v -> writer.writeRow(Arrays.asList("李四", "30", "上海")))
    .compose(v -> writer.close())
    .onSuccess(v -> System.out.println("CSV 文件写入完成"))
    .onFailure(Throwable::printStackTrace);

// 同步写入示例
try {
    KeelCsvWriter writer = new KeelCsvWriter(new FileOutputStream("output.csv"));
    writer.setSeparator(",")
          .setCharset(StandardCharsets.UTF_8);
    
    // 写入数据
    writer.blockWriteRow(Arrays.asList("产品", "价格", "库存"));
    writer.blockWriteRow(Arrays.asList("苹果", "5.50", "100"));
    writer.blockWriteRow(Arrays.asList("香蕉", "3.20", "150"));
    
    writer.blockClose();
    System.out.println("CSV 文件写入完成");
} catch (IOException e) {
    e.printStackTrace();
}
```

## 特殊字符处理

### 引号处理
- 字段中包含引号时，会被转义为双引号 (`""`)
- 包含引号、换行符或分隔符的字段会被自动用引号包围

### 换行符处理
- 支持字段内的换行符
- 读取时会正确处理跨行的字段

### 分隔符自定义
```java
// 使用分号作为分隔符
reader.setSeparator(";");
writer.setSeparator(";");

// 使用制表符作为分隔符
reader.setSeparator("\t");
writer.setSeparator("\t");
```

## 注意事项

1. **技术预览版本**：该包标记为技术预览版本，API 可能在未来版本中发生变化
2. **资源管理**：使用完毕后务必调用 `close()` 方法释放资源
3. **异常处理**：同步方法可能抛出 `IOException`，需要适当处理
4. **字符编码**：建议明确指定字符编码，避免乱码问题
5. **大文件处理**：对于大文件，建议使用异步方式逐行处理，避免内存溢出

## API 版本

- **引入版本**：3.1.1
- **状态**：技术预览版本
- **包路径**：`io.github.sinri.keel.integration.poi.csv`
