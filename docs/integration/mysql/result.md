# MySQL Result Package Documentation

## 概述

`io.github.sinri.keel.integration.mysql.result` 包提供了处理 MySQL 查询结果的完整解决方案。该包包含三个主要子包，分别处理行级数据、矩阵级数据和流式数据处理。

## 包结构

```
result/
├── row/           # 行级数据处理
├── matrix/        # 矩阵级数据处理  
└── stream/        # 流式数据处理
```

## 核心组件

### 1. Row 包 - 行级数据处理

#### ResultRow 接口

`ResultRow` 是处理单行查询结果的核心接口，继承自 `JsonifiableEntity<ResultRow>`。

**主要功能：**
- 将 Vert.x 的 `Row` 对象转换为 JSON 格式
- 提供类型安全的数据访问方法
- 支持日期时间格式化

**核心方法：**

```java
// 静态工厂方法
static <R extends ResultRow> R of(@Nonnull JsonObject tableRow, Class<R> clazz)
static <R extends ResultRow> R of(@Nonnull Row tableRow, Class<R> clazz)

// 批量转换方法
static JsonArray batchToJsonArray(@Nonnull Collection<? extends ResultRow> rows)
static JsonArray batchToJsonArray(@Nonnull Collection<? extends ResultRow> rows, 
                                  @Nonnull Function<ResultRow, JsonObject> transformer)

// 数据访问方法
@Nullable String readDateTime(@Nonnull String field)
@Nullable String readDate(@Nonnull String field)  
@Nullable String readTime(@Nonnull String field)
@Nullable String readTimestamp(@Nonnull String field)
```

**特性：**
- 自动处理 null 字段
- 日期时间格式化为 "yyyy-MM-dd HH:mm:ss" 格式
- 时间字段自动清理格式标记符

#### SimpleResultRow 类

`SimpleResultRow` 是 `ResultRow` 接口的基础实现类。

**特点：**
- 简单的 JSON 对象包装器
- 提供基本的数据存取功能
- 支持数据重新加载

#### AbstractTableRow 类

`AbstractTableRow` 是 `SimpleResultRow` 的抽象扩展，专门用于表行数据。

**抽象方法：**
- `sourceTableName()`: 返回源表名称
- `sourceSchemaName()`: 返回源模式名称（可选）

### 2. Matrix 包 - 矩阵级数据处理

#### ResultMatrix 接口

`ResultMatrix` 提供了处理多行查询结果的强大功能，支持各种数据操作和转换。

**基本信息获取：**
```java
List<JsonObject> getRowList()           // 获取所有行数据
int getTotalFetchedRows()               // 获取查询到的行数
int getTotalAffectedRows()              // 获取受影响的行数  
long getLastInsertedID()                // 获取最后插入的ID
JsonArray toJsonArray()                 // 转换为JSON数组
```

**单行数据访问：**
```java
JsonObject getFirstRow()                                    // 获取第一行
JsonObject getRowByIndex(int index)                         // 按索引获取行
<T extends ResultRow> T buildTableRowByIndex(int index, Class<T> classOfTableRow)  // 构建类型化行对象
```

**列数据提取：**
```java
// 第一行的列值获取
String getOneColumnOfFirstRowAsDateTime(String columnName)
String getOneColumnOfFirstRowAsString(String columnName)
Numeric getOneColumnOfFirstRowAsNumeric(String columnName)
Integer getOneColumnOfFirstRowAsInteger(String columnName)
Long getOneColumnOfFirstRowAsLong(String columnName)

// 整列数据获取
List<String> getOneColumnAsDateTime(String columnName)
List<String> getOneColumnAsString(String columnName)
List<Numeric> getOneColumnAsNumeric(String columnName)
List<Long> getOneColumnAsLong(String columnName)
List<Integer> getOneColumnAsInteger(String columnName)
```

**高级数据处理：**

1. **分类映射**
```java
// 基于JSON对象的分类
<K> Future<Map<K, List<JsonObject>>> buildCategorizedRowsMap(Function<JsonObject, K> categoryGenerator)

// 基于ResultRow的分类
<K, T extends ResultRow> Future<Map<K, List<T>>> buildCategorizedRowsMap(Class<T> classOfTableRow, 
                                                                         Function<T, K> categoryGenerator)
```

2. **唯一键映射**
```java
// 基于JSON对象的唯一键映射
<K> Future<Map<K, JsonObject>> buildUniqueKeyBoundRowMap(Function<JsonObject, K> uniqueKeyGenerator)

// 基于ResultRow的唯一键映射  
<K, T extends ResultRow> Future<Map<K, T>> buildUniqueKeyBoundRowMap(Class<T> classOfTableRow,
                                                                     Function<T, K> uniqueKeyGenerator)
```

3. **自定义映射**
```java
<K, V> Future<Map<K, V>> buildCustomizedMap(BiConsumer<Map<K, V>, JsonObject> rowToMapHandler)
```

4. **数据收缩**
```java
Future<List<JsonObject>> buildShrinkList(Collection<String> shrinkByKeys, String shrinkBodyListKey)
```

#### ResultMatrixImpl 类

`ResultMatrixImpl` 是 `ResultMatrix` 接口的具体实现。

**实现特点：**
- 基于 Vert.x 的 `RowSet<Row>` 构建
- 内部维护行数据列表，提高访问效率
- 自动提取查询元数据（行数、受影响行数、最后插入ID）
- 支持 MySQL 特定的功能

### 3. Stream 包 - 流式数据处理

#### ResultStreamReader 接口

`ResultStreamReader` 提供流式处理查询结果的能力，适用于大数据集的处理。

**核心方法：**
```java
Future<Void> read(Row row)  // 处理单行数据
```

**静态工具方法：**
```java
// 将行映射为普通实体
static <T> T mapRowToEntity(Row row, Class<T> clazz)

// 将行映射为ResultRow
static <R extends ResultRow> R mapRowToResultRow(Row row, Class<R> clazz)
```

## 使用示例

### 基本行处理
```java
// 创建ResultRow实例
JsonObject rowData = // ... 从查询获取
MyResultRow row = ResultRow.of(rowData, MyResultRow.class);

// 读取日期时间字段
String dateTime = row.readDateTime("created_at");
String date = row.readDate("birth_date");
```

### 矩阵数据处理
```java
// 创建ResultMatrix
RowSet<Row> rowSet = // ... 从查询获取
ResultMatrix matrix = ResultMatrix.create(rowSet);

// 获取基本信息
int totalRows = matrix.getTotalFetchedRows();
long lastId = matrix.getLastInsertedID();

// 获取第一行数据
JsonObject firstRow = matrix.getFirstRow();

// 构建类型化行列表
List<MyResultRow> rows = matrix.buildTableRowList(MyResultRow.class);

// 分类处理
Future<Map<String, List<MyResultRow>>> categorizedMap = 
    matrix.buildCategorizedRowsMap(MyResultRow.class, row -> row.getCategory());
```

### 流式处理
```java
// 实现流式读取器
ResultStreamReader reader = new ResultStreamReader() {
    @Override
    public Future<Void> read(Row row) {
        // 处理单行数据
        MyEntity entity = ResultStreamReader.mapRowToEntity(row, MyEntity.class);
        // 执行业务逻辑
        return Future.succeededFuture();
    }
};
```

## 版本历史

- **1.1**: 初始版本，引入基础矩阵功能
- **1.8**: ResultMatrix 改为接口设计
- **1.10**: 引入 AbstractTableRow 和类型化行构建
- **2.0**: AbstractTableRow 重命名
- **2.7**: 引入 ResultRow 接口，增强日期时间处理
- **2.8**: 重构和优化
- **2.9.4**: 增加分类和映射功能，修复空字段处理
- **3.2.2**: 优化内存使用，增加数据收缩功能
- **4.0.0**: 引入流式处理支持

## 异常处理

- `KeelSQLResultRowIndexError`: 当访问不存在的行索引时抛出
- `RuntimeException`: 在类型转换或反射操作失败时抛出

## 最佳实践

1. **选择合适的处理方式**：
   - 小数据集使用 ResultMatrix
   - 大数据集使用 ResultStreamReader
   - 单行处理使用 ResultRow

2. **类型安全**：
   - 优先使用类型化的 ResultRow 子类
   - 利用静态工厂方法进行安全转换

3. **性能优化**：
   - 对于大数据集，考虑使用流式处理
   - 合理使用分类和映射功能避免重复遍历

4. **错误处理**：
   - 始终处理可能的索引越界异常
   - 注意空值字段的处理
