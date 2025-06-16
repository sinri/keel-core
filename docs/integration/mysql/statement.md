# Statement of MySQL

MySQL语句构建器包，提供了一套完整的SQL语句构建和执行框架。该包支持各种类型的SQL语句构建，包括查询、修改、插入、删除等操作，并提供了模板化SQL、条件构建、审计日志等高级功能。

## 包结构

### 核心类

#### AbstractStatement
抽象基类，实现了`AnyStatement`接口，提供了SQL语句执行的基础功能：

- **SQL审计日志**：自动记录SQL执行过程和结果
- **异步执行**：基于Vert.x的异步SQL执行
- **预处理支持**：支持预处理语句和直接执行两种模式
- **注释支持**：可为SQL语句添加注释标记

```java
// 核心执行方法
Future<ResultMatrix> execute(NamedMySQLConnection namedSqlConnection)

// 设置是否跳过预处理
AbstractStatement setWithoutPrepare(boolean withoutPrepare)

// 添加注释
AbstractStatement setRemarkAsComment(String remarkAsComment)
```

#### AnyStatement
核心接口，定义了所有SQL语句的基本契约，并提供了丰富的静态工厂方法：

**基础语句构建**：
- `select()` - 构建SELECT语句
- `update()` - 构建UPDATE语句  
- `insert()` - 构建INSERT语句
- `delete()` - 构建DELETE语句
- `replace()` - 构建REPLACE语句
- `union()` - 构建UNION语句
- `call()` - 构建存储过程调用

**DDL语句构建**：
- `createTable()` - 创建表
- `alterTable()` - 修改表结构
- `truncateTable()` - 清空表
- `createView()` - 创建视图
- `alterView()` - 修改视图
- `dropView()` - 删除视图

**模板化语句**：
- `templatedRead()` - 模板化查询语句
- `templatedModify()` - 模板化修改语句

**原始SQL**：
- `raw()` - 执行原始SQL语句

### 实现类 (impl包)

#### SelectStatement
SELECT语句构建器，支持复杂的查询构建：

**基本查询构建**：
```java
SelectStatement select = new SelectStatement()
    .from("users", "u")
    .column(col -> col.field("u", "id").alias("user_id"))
    .column(col -> col.field("u", "name"))
    .where(where -> where.expressionEqualsLiteralValue("u.status", "active"))
    .orderByDesc("u.created_at")
    .limit(10);
```

**连接查询**：
```java
select.leftJoin(join -> join
    .table("profiles", "p")
    .onForCompare(on -> on
        .compareExpression("u.id")
        .operator("=")
        .againstExpression("p.user_id")
    )
);
```

**子查询支持**：
```java
select.from(subQuery, "sub");
```

**分页查询**：
```java
Future<PaginationResult> queryForPagination(
    NamedMySQLConnection sqlConnection,
    long pageNo,
    long pageSize
)
```

#### UpdateStatement
UPDATE语句构建器：

```java
UpdateStatement update = new UpdateStatement()
    .table("users")
    .setWithValue("name", "新名称")
    .setWithExpression("updated_at", "NOW()")
    .where(where -> where.expressionEqualsNumericValue("id", 123))
    .limit(1);
```

#### WriteIntoStatement
INSERT/REPLACE语句构建器，支持批量插入和重复键处理：

**单行插入**：
```java
WriteIntoStatement insert = new WriteIntoStatement(WriteIntoStatement.INSERT)
    .intoTable("users")
    .macroWriteOneRow(row -> row
        .put("name", "张三")
        .put("email", "zhangsan@example.com")
        .putNow("created_at")
    );
```

**批量插入**：
```java
insert.macroWriteRows(rowList);
```

**重复键处理**：
```java
insert.onDuplicateKeyUpdate("name", "VALUES(name)")
      .onDuplicateKeyUpdateField("updated_at");
```

#### DeleteStatement
DELETE语句构建器：

```java
DeleteStatement delete = new DeleteStatement()
    .from("users")
    .where(where -> where.expressionEqualsNumericValue("id", 123));
```

#### UnionStatement
UNION语句构建器，用于合并多个查询结果。

#### CallStatement
存储过程调用语句构建器。

### 混入接口 (mixin包)

#### ReadStatementMixin
为查询语句提供便捷的结果处理方法：

```java
// 查询单行结果
Future<UserRow> queryForOneRow(connection, UserRow.class)

// 查询多行结果
Future<List<UserRow>> queryForRowList(connection, UserRow.class)

// 查询分类映射
Future<Map<String, List<UserRow>>> queryForCategorizedMap(
    connection, UserRow.class, user -> user.getCategory()
)

// 查询唯一键映射
Future<Map<Long, UserRow>> queryForUniqueKeyBoundMap(
    connection, UserRow.class, user -> user.getId()
)

// 流式处理大结果集
Future<Void> stream(connection, resultStreamReader)
```

#### ModifyStatementMixin
为修改语句提供的混入接口。

#### WriteIntoStatementMixin
为插入语句提供的混入接口，包含批量分割功能：

```java
// 将大批量插入分割为小批次
List<WriteIntoStatementMixin> divide(int chunkSize)
```

#### SelectStatementMixin
为SELECT语句提供的混入接口。

### 组件类 (component包)

#### ConditionsComponent
条件构建组件，提供丰富的条件构建方法：

**比较条件**：
```java
conditions.expressionEqualsLiteralValue("status", "active")
          .expressionNotNumericValue("age", 0)
          .expressionIsNull("deleted_at")
          .expressionIsNotNull("email");
```

**范围条件**：
```java
conditions.expressionAmongLiteralValues("status", Arrays.asList("active", "pending"))
          .expressionNotInNumericValues("id", excludeIds);
```

**复合条件**：
```java
conditions.intersection(and -> and
    .expressionEqualsLiteralValue("type", "user")
    .union(or -> or
        .expressionEqualsLiteralValue("status", "active")
        .expressionEqualsLiteralValue("status", "pending")
    )
);
```

**原始条件**：
```java
conditions.raw("FIND_IN_SET(?, tags)");
```

#### UpdateSetAssignmentComponent
UPDATE语句的SET子句组件：

```java
UpdateSetAssignmentComponent assignment = new UpdateSetAssignmentComponent("column_name")
    .assignmentToValue("new_value")
    // 或
    .assignmentToExpression("column_name + 1");
```

#### 条件操作符组件
- **IfOperator** - IF条件操作符
- **IfNullOperator** - IFNULL操作符  
- **NullIfOperator** - NULLIF操作符
- **CaseOperator** - CASE WHEN操作符
- **CaseOperatorPair** - CASE操作符的条件对

### 模板化语句 (templated包)

#### TemplatedStatement
模板化SQL语句接口，支持参数化SQL模板：

```java
// 加载模板文件
TemplatedReadStatement statement = TemplatedStatement.loadTemplateToRead("sql/user_query.sql");

// 设置参数
statement.getArguments()
    .put("status", "active")
    .put("limit", "10");

// 执行查询
Future<List<UserRow>> result = statement.queryForRowList(connection, UserRow.class);
```

**模板语法**：
```sql
-- sql/user_query.sql
SELECT * FROM users 
WHERE status = '{status}'
ORDER BY created_at DESC
LIMIT {limit}
```

#### TemplateArgument
模板参数类，支持多种数据类型的参数。

#### TemplateArgumentMapping
模板参数映射管理器。

#### TemplatedReadStatement / TemplatedModifyStatement
分别用于查询和修改操作的模板化语句实现。

### DDL语句实现 (impl/ddl包)

#### 表操作
- **CreateTableStatement** - 创建表语句
- **CreateTableLikeTableStatement** - 基于现有表创建表
- **AlterTableStatement** - 修改表结构
- **TruncateTableStatement** - 清空表

#### 视图操作  
- **CreateViewStatement** - 创建视图
- **AlterViewStatement** - 修改视图
- **DropViewStatement** - 删除视图

## 使用示例

### 基本查询
```java
// 简单查询
AnyStatement.select(select -> select
    .from("users")
    .columnAsExpression("*")
    .where(where -> where.expressionEqualsLiteralValue("status", "active"))
    .limit(10)
).queryForRowList(connection, UserRow.class);

// 复杂连接查询
AnyStatement.select(select -> select
    .from("users", "u")
    .leftJoin(join -> join
        .table("profiles", "p")
        .onForCompare(on -> on.compareExpression("u.id").operator("=").againstExpression("p.user_id"))
    )
    .column(col -> col.field("u", "name"))
    .column(col -> col.field("p", "avatar"))
    .where(where -> where
        .expressionEqualsLiteralValue("u.status", "active")
        .expressionIsNotNull("p.avatar")
    )
    .orderByDesc("u.created_at")
);
```

### 数据修改
```java
// 更新数据
AnyStatement.update(update -> update
    .table("users")
    .setWithValue("last_login", new Date())
    .setWithExpression("login_count", "login_count + 1")
    .where(where -> where.expressionEqualsNumericValue("id", userId))
).execute(connection);

// 插入数据
AnyStatement.insert(insert -> insert
    .intoTable("users")
    .macroWriteOneRow(row -> row
        .put("name", "新用户")
        .put("email", "user@example.com")
        .putNow("created_at")
    )
    .onDuplicateKeyUpdate("name", "VALUES(name)")
).execute(connection);
```

### 模板化查询
```java
// 使用模板文件
AnyStatement.templatedRead("sql/complex_report.sql", args -> args
    .put("start_date", "2023-01-01")
    .put("end_date", "2023-12-31")
    .put("department_id", "10")
).queryForRowList(connection, ReportRow.class);
```

### 批量操作
```java
// 批量插入
List<RowToWrite> rows = users.stream()
    .map(user -> RowToWrite.fromJsonObject(user.toJson()))
    .collect(Collectors.toList());

AnyStatement.insert(insert -> insert
    .intoTable("users")
    .macroWriteRows(rows)
).divide(1000) // 分割为每批1000条
 .forEach(batch -> batch.execute(connection));
```

## 特性

### SQL审计日志
所有SQL执行都会自动记录审计日志，包括：
- 语句UUID
- SQL内容
- 执行时间
- 影响行数
- 异常信息

### 异步执行
基于Vert.x的完全异步执行模型，支持高并发场景。

### 类型安全
通过泛型和强类型接口确保编译时类型安全。

### 灵活的条件构建
提供丰富的条件构建API，支持复杂的WHERE子句构建。

### 模板化支持
支持外部SQL模板文件，便于SQL管理和维护。

### 批量处理
内置批量操作支持，自动处理大数据量场景。

## 最佳实践

1. **使用工厂方法**：优先使用`AnyStatement`的静态工厂方法构建语句
2. **条件构建**：使用类型安全的条件构建方法而非原始SQL字符串
3. **资源管理**：确保正确关闭数据库连接
4. **批量操作**：对于大量数据使用批量API和分割功能
5. **模板化**：复杂SQL使用模板文件管理
6. **异常处理**：正确处理异步操作的异常情况

## 版本历史

- **1.7** - 引入AbstractStatement基类
- **2.8** - 添加子查询支持和SQL审计
- **3.0** - 重构异步执行模型
- **3.1** - 添加最大执行时间限制
- **3.2** - 引入混入接口和分页支持
- **4.0** - 完善DDL支持和模板化功能