# MySQL Integration Base

Keel框架的MySQL集成模块提供了一套完整的数据库连接和操作解决方案，支持连接池管理、事务处理和多数据源配置。

## 核心类概览

### NamedMySQLDataSource<C>
**位置**: `io.github.sinri.keel.integration.mysql.NamedMySQLDataSource`  
**版本**: 3.0.11+ (3.0.18完成技术预览)

命名MySQL数据源类，负责管理数据库连接池和提供连接服务。

**主要功能**:
- 管理MySQL连接池
- 提供连接获取和释放机制
- 支持事务处理
- 监控连接状态
- 自动检测MySQL版本

**核心方法**:
```java
// 使用连接执行操作
<T> Future<T> withConnection(Function<C, Future<T>> function)

// 在事务中执行操作
<T> Future<T> withTransaction(Function<C, Future<T>> function)

// 获取可用连接数
int getAvailableConnectionCount()

// 关闭数据源
Future<Void> close()
```

**构造参数**:
- `KeelMySQLConfiguration configuration`: 数据库配置
- `Function<SqlConnection, C> sqlConnectionWrapper`: 连接包装器
- `Function<SqlConnection, Future<Void>> connectionSetUpFunction`: 连接初始化函数(可选)

### KeelMySQLDataSourceProvider
**位置**: `io.github.sinri.keel.integration.mysql.KeelMySQLDataSourceProvider`

数据源提供者工具类，用于创建和初始化命名数据源。

**主要功能**:
- 提供默认数据源名称获取
- 创建命名数据源实例
- 支持动态命名连接创建

**核心方法**:
```java
// 获取默认数据源名称
static String defaultMySQLDataSourceName()

// 初始化命名数据源
static <C extends NamedMySQLConnection> NamedMySQLDataSource<C> 
    initializeNamedMySQLDataSource(String dataSourceName, Function<SqlConnection, C> sqlConnectionWrapper)

// 初始化动态命名数据源
static NamedMySQLDataSource<DynamicNamedMySQLConnection> 
    initializeDynamicNamedMySQLDataSource(String dataSourceName)
```

### NamedMySQLConnection
**位置**: `io.github.sinri.keel.integration.mysql.NamedMySQLConnection`  
**版本**: 3.0.11+ (3.0.18完成技术预览)

抽象的命名MySQL连接类，封装了SqlConnection并提供数据源标识。

**主要功能**:
- 封装Vert.x SqlConnection
- 提供数据源名称标识
- MySQL版本检测和判断
- 事务状态检测

**核心方法**:
```java
// 获取底层SQL连接
SqlConnection getSqlConnection()

// 获取数据源名称(抽象方法)
abstract String getDataSourceName()

// MySQL版本相关方法
String getMysqlVersion()
boolean isMySQLVersion5dot6()
boolean isMySQLVersion5dot7()
boolean isMySQLVersion8dot0()
boolean isMySQLVersion8dot2()

// 事务状态检测
boolean isForTransaction()
```

### KeelMySQLConfiguration
**位置**: `io.github.sinri.keel.integration.mysql.KeelMySQLConfiguration`

MySQL配置类，继承自KeelConfigElement，提供数据库连接和池配置。

**主要功能**:
- 数据库连接参数配置
- 连接池参数配置
- 即时查询支持
- 流式查询支持

**配置参数**:
```java
// 连接配置
String getHost()           // 主机地址
Integer getPort()          // 端口(默认3306)
String getUsername()       // 用户名
String getPassword()       // 密码
String getDatabase()       // 数据库名
String getCharset()        // 字符集

// 池配置
Integer getPoolMaxSize()           // 最大连接数
Integer getPoolConnectionTimeout() // 连接超时时间
boolean getPoolShared()           // 是否共享池(默认true)
```

**核心方法**:
```java
// 获取连接选项
MySQLConnectOptions getConnectOptions()

// 获取池选项
PoolOptions getPoolOptions()

// 即时查询
Future<ResultMatrix> instantQuery(String sql)

// 流式查询
Future<Void> instantQueryForStream(String sql, int readWindowSize, 
    Function<RowSet<Row>, Future<Void>> readWindowFunction)
```

### DynamicNamedMySQLConnection
**位置**: `io.github.sinri.keel.integration.mysql.DynamicNamedMySQLConnection`  
**版本**: 3.0.11+ (3.0.18完成技术预览)

动态命名MySQL连接的具体实现，允许在运行时指定数据源名称。

**主要功能**:
- 实现NamedMySQLConnection抽象类
- 支持动态数据源名称设置
- 适用于多数据源场景

## 使用模式

### 1. 基本数据源创建
```java
// 创建动态命名数据源
NamedMySQLDataSource<DynamicNamedMySQLConnection> dataSource = 
    KeelMySQLDataSourceProvider.initializeDynamicNamedMySQLDataSource("myDataSource");
```

### 2. 执行数据库操作
```java
// 使用连接执行查询
Future<ResultMatrix> result = dataSource.withConnection(connection -> {
    return connection.getSqlConnection()
        .preparedQuery("SELECT * FROM users WHERE id = ?")
        .execute(Tuple.of(userId))
        .compose(rows -> Future.succeededFuture(ResultMatrix.create(rows)));
});
```

### 3. 事务处理
```java
// 在事务中执行多个操作
Future<Void> result = dataSource.withTransaction(connection -> {
    return connection.getSqlConnection()
        .preparedQuery("INSERT INTO users (name) VALUES (?)")
        .execute(Tuple.of("John"))
        .compose(v -> connection.getSqlConnection()
            .preparedQuery("UPDATE user_stats SET count = count + 1")
            .execute())
        .compose(v -> Future.succeededFuture());
});
```

### 4. 即时查询
```java
// 使用配置直接执行查询
KeelMySQLConfiguration config = new KeelMySQLConfiguration(configElement);
Future<ResultMatrix> result = config.instantQuery("SELECT COUNT(*) FROM users");
```

## 配置示例

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
      "charset": "utf8mb4",
      "poolMaxSize": 128,
      "poolShared": true,
      "connectionTimeout": 60000,
      "poolConnectionTimeout": 30
    }
  }
}
```

## 版本兼容性

- MySQL 5.6+
- MySQL 5.7+
- MySQL 8.0+
- MySQL 8.2+

框架会自动检测MySQL版本并提供相应的版本判断方法。

## 注意事项

1. **连接管理**: 使用`withConnection`和`withTransaction`方法会自动管理连接的获取和释放
2. **事务处理**: 事务失败时会自动回滚，成功时自动提交
3. **连接池**: 建议在生产环境中启用连接池共享以提高性能
4. **资源清理**: 应用关闭时记得调用`dataSource.close()`释放资源
5. **多数据源**: 使用不同的数据源名称来区分不同的数据库连接

