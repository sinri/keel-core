# MySQL Integration

Keel MySQL 集成模块提供了完整的 MySQL 数据库操作功能，包括连接管理、SQL 语句构建、结果处理和事务支持。

## 核心组件

### 1. 配置管理

#### KeelMySQLConfiguration
MySQL 数据源配置类，负责管理数据库连接参数和连接池配置。

**主要配置项：**
- `host`: 数据库主机地址
- `port`: 数据库端口（默认 3306）
- `username/user`: 数据库用户名
- `password`: 数据库密码
- `database/schema`: 数据库名称
- `charset`: 字符集（推荐 utf8）
- `poolMaxSize`: 连接池最大连接数（默认 128）
- `poolShared`: 是否共享连接池（默认 true）
- `connectionTimeout`: 连接超时时间（毫秒）
- `poolConnectionTimeout`: 连接池获取连接超时时间（秒）

**配置示例：**
```json
{
  "mysql": {
    "default": {
      "host": "localhost",
      "port": 3306,
      "username": "root",
      "password": "password",
      "database": "test_db",
      "charset": "utf8",
      "poolMaxSize": 128,
      "poolShared": true
    }
  }
}
```

#### KeelMySQLDataSourceProvider
数据源提供者，用于初始化和管理 MySQL 数据源。

**主要方法：**
- `defaultMySQLDataSourceName()`: 获取默认数据源名称
- `initializeNamedMySQLDataSource()`: 初始化命名数据源
- `initializeDynamicNamedMySQLDataSource()`: 初始化动态命名数据源

### 2. 连接管理

#### NamedMySQLConnection
抽象的命名 MySQL 连接类，提供数据源标识和版本检测功能。

**主要功能：**
- 数据源名称标识
- MySQL 版本检测（支持 5.6、5.7、8.0、8.2）
- 事务状态检测

#### DynamicNamedMySQLConnection
动态命名 MySQL 连接的具体实现，支持运行时指定数据源名称。

#### NamedMySQLDataSource
命名 MySQL 数据源管理器，提供连接池管理和事务支持。

**主要功能：**
- 连接池管理
- 连接获取和释放
- 事务管理
- MySQL 版本自动检测
- 连接状态监控

**使用示例：**
```java
// 初始化数据源
NamedMySQLDataSource<DynamicNamedMySQLConnection> dataSource = 
    KeelMySQLDataSourceProvider.initializeDynamicNamedMySQLDataSource("default");

// 使用连接执行操作
dataSource.withConnection(connection -> {
    // 执行数据库操作
    return Future.succeededFuture();
});

// 使用事务
dataSource.withTransaction(connection -> {
    // 在事务中执行操作
    return Future.succeededFuture();
});
```

### 3. SQL 语句构建

#### AbstractStatement
抽象 SQL 语句基类，提供 SQL 执行和审计日志功能。

**主要功能：**
- SQL 语句执行
- 执行结果审计日志
- 预处理语句支持
- 错误处理和异常记录

#### AnyStatement
SQL 语句接口，定义了所有 SQL 语句的基本行为。

#### 具体语句实现

**SelectStatement**: SELECT 查询语句构建器
- 支持复杂的查询条件
- JOIN 操作支持
- 分组和排序
- 分页查询

**UpdateStatement**: UPDATE 更新语句构建器
- 条件更新
- 批量更新
- 字段值设置

**WriteIntoStatement**: INSERT 插入语句构建器
- 单行插入
- 批量插入
- ON DUPLICATE KEY UPDATE 支持

**DeleteStatement**: DELETE 删除语句构建器
- 条件删除
- 批量删除

**CallStatement**: 存储过程调用语句构建器

**UnionStatement**: UNION 联合查询语句构建器

### 4. 查询条件

#### MySQLCondition
查询条件接口，定义条件表达式的生成规范。

#### CompareCondition
比较条件实现，支持各种比较操作符：
- 等于、不等于
- 大于、小于、大于等于、小于等于
- LIKE、NOT LIKE
- IS NULL、IS NOT NULL

#### AmongstCondition
范围条件实现，支持：
- IN、NOT IN
- BETWEEN、NOT BETWEEN

#### GroupCondition
条件组合，支持：
- AND 逻辑组合
- OR 逻辑组合
- 嵌套条件组合

#### RawCondition
原始 SQL 条件，允许直接使用 SQL 表达式。

### 5. 结果处理

#### ResultMatrix
查询结果矩阵接口，提供丰富的结果处理功能。

**主要功能：**
- 结果行访问
- 类型转换
- 数据映射
- 结果统计

**使用示例：**
```java
// 获取第一行数据
JsonObject firstRow = resultMatrix.getFirstRow();

// 获取指定列的值
String name = resultMatrix.getOneColumnOfFirstRowAsString("name");
Integer age = resultMatrix.getOneColumnOfFirstRowAsInteger("age");

// 转换为对象列表
List<UserRow> users = resultMatrix.buildTableRowList(UserRow.class);

// 构建分类映射
Map<String, List<JsonObject>> categoryMap = 
    resultMatrix.buildCategorizedRowsMap(row -> row.getString("category"));
```

#### ResultRow
结果行基类，用于将查询结果映射为 Java 对象。

### 6. 数据库操作

#### NamedActionInterface
命名操作接口，定义数据库操作的基本规范。

#### AbstractNamedAction
抽象命名操作基类，提供操作的基础实现。

#### NamedActionMixinInterface
命名操作混入接口，支持多重继承的操作组合。

### 7. 工具类

#### Quoter
SQL 引用工具类，提供安全的 SQL 值引用功能。

**主要功能：**
- 字符串转义和引用
- 数字值处理
- 布尔值转换
- 列表值处理
- 通配符转义

**使用示例：**
```java
// 字符串引用
String quoted = new Quoter("user's name").toString(); // 'user\'s name'

// 数字引用
String numberQuoted = new Quoter(123).toString(); // 123

// 列表引用
String listQuoted = new Quoter(Arrays.asList(1, 2, 3)).toString(); // (1,2,3)

// 带通配符的字符串
String wildcardQuoted = new Quoter("user%", true).toString(); // 'user\\%'
```

### 8. 异常处理

#### KeelMySQLException
MySQL 操作基础异常类。

#### KeelMySQLConnectionException
MySQL 连接异常类。

#### KeelSQLGenerateError
SQL 生成错误异常类。

#### KeelSQLResultRowIndexError
结果行索引错误异常类。

## 使用指南

### 1. 基本配置

在配置文件中添加 MySQL 数据源配置：

```json
{
  "mysql": {
    "default_data_source_name": "main",
    "main": {
      "host": "localhost",
      "port": 3306,
      "username": "root",
      "password": "password",
      "database": "myapp",
      "charset": "utf8",
      "poolMaxSize": 50,
      "poolShared": true,
      "connectionTimeout": 30000,
      "poolConnectionTimeout": 10
    }
  }
}
```

### 2. 初始化数据源

```java
// 使用默认数据源
NamedMySQLDataSource<DynamicNamedMySQLConnection> dataSource = 
    KeelMySQLDataSourceProvider.initializeDynamicNamedMySQLDataSource("main");
```

### 3. 执行查询

```java
// 构建查询语句
SelectStatement selectStatement = new SelectStatement()
    .from("users")
    .where(new CompareCondition("status", "=", "active"))
    .orderBy("created_at", "DESC")
    .limit(10);

// 执行查询
dataSource.withConnection(connection -> {
    return selectStatement.execute(connection)
        .compose(resultMatrix -> {
            List<JsonObject> users = resultMatrix.getRowList();
            // 处理查询结果
            return Future.succeededFuture(users);
        });
});
```

### 4. 执行更新

```java
// 构建更新语句
UpdateStatement updateStatement = new UpdateStatement("users")
    .set("last_login", "NOW()")
    .where(new CompareCondition("id", "=", userId));

// 在事务中执行更新
dataSource.withTransaction(connection -> {
    return updateStatement.execute(connection)
        .compose(resultMatrix -> {
            int affectedRows = resultMatrix.getTotalAffectedRows();
            return Future.succeededFuture(affectedRows);
        });
});
```

### 5. 批量操作

```java
// 批量插入
WriteIntoStatement insertStatement = new WriteIntoStatement("users")
    .fields("name", "email", "status")
    .values("John Doe", "john@example.com", "active")
    .values("Jane Smith", "jane@example.com", "active");

dataSource.withConnection(connection -> {
    return insertStatement.execute(connection);
});
```

## 最佳实践

### 1. 连接池配置
- 根据应用负载合理设置 `poolMaxSize`
- 启用 `poolShared` 以在多个 Verticle 间共享连接池
- 设置合适的超时时间避免连接泄露

### 2. 事务管理
- 对于需要原子性的操作使用 `withTransaction`
- 避免长时间持有事务连接
- 合理处理事务回滚异常

### 3. SQL 安全
- 使用 `Quoter` 类进行值引用，避免 SQL 注入
- 优先使用预处理语句
- 验证用户输入数据

### 4. 性能优化
- 使用适当的索引
- 避免 N+1 查询问题
- 合理使用分页查询
- 监控慢查询日志

### 5. 错误处理
- 捕获并处理特定的 MySQL 异常
- 记录详细的错误日志
- 实现合适的重试机制

## 版本兼容性

- 支持 MySQL 5.6+
- 支持 MySQL 8.0+
- 自动检测 MySQL 版本并适配相应特性
- 向后兼容旧版本 API

## 审计日志

MySQL 集成模块提供完整的 SQL 审计日志功能：

- 记录所有 SQL 语句执行
- 包含执行时间和影响行数
- 支持自定义日志记录器
- 异常情况自动记录

通过 `AbstractStatement.reloadSqlAuditIssueRecording()` 方法可以自定义审计日志的记录方式。