# Action based on MySQL

MySQL操作包，提供了一套基于命名连接的数据库操作抽象框架。该包定义了数据库操作的基本规范，支持传统继承和混入（Mixin）两种设计模式，为构建类型安全、可扩展的数据库操作层提供了基础设施。

## 包结构

### 核心接口

#### NamedActionInterface<C>
**位置**: `io.github.sinri.keel.integration.mysql.action.NamedActionInterface`  
**版本**: 3.2.11+

命名操作接口，定义了基于命名MySQL连接的操作基本契约。

**主要功能**：
- 提供获取命名MySQL连接的标准方法
- 支持泛型化的连接类型约束
- 为数据库操作提供统一的连接访问接口

**接口定义**：
```java
public interface NamedActionInterface<C extends NamedMySQLConnection> {
    @Nonnull
    C getNamedSqlConnection();
}
```

**设计原则**：
- **事务管理外置**：所有MySQL连接操作应该被更高层级的事务管理包装，操作内部不应管理事务
- **混入风格支持**：接口设计支持混入模式，可以通过扩展添加自定义方法
- **类型安全**：通过泛型参数确保连接类型的编译时安全

#### NamedActionMixinInterface<C, W>
**位置**: `io.github.sinri.keel.integration.mysql.action.NamedActionMixinInterface`  
**版本**: 3.2.11+

命名操作混入接口，扩展了`SelfInterface`以支持混入模式的数据库操作。

**主要功能**：
- 继承`SelfInterface<W>`提供自引用能力
- 支持方法链式调用
- 适用于多重继承和组合式功能扩展

**接口定义**：
```java
public interface NamedActionMixinInterface<C extends NamedMySQLConnection, W>
        extends SelfInterface<W> {
    @Nonnull
    C getNamedSqlConnection();
}
```

**泛型参数**：
- `C`: 具体的命名MySQL连接类型，必须继承`NamedMySQLConnection`
- `W`: 混入或附加上下文的泛型类型，通常是实现类本身

### 抽象实现类

#### AbstractNamedAction<C>
**位置**: `io.github.sinri.keel.integration.mysql.action.AbstractNamedAction`  
**版本**: 3.2.11+ (从`io.github.sinri.keel.mysql.AbstractNamedAction`迁移并优化)

抽象命名操作基类，提供了`NamedActionInterface`的标准实现。

**主要功能**：
- 封装命名MySQL连接的管理
- 提供连接获取的标准实现
- 作为具体操作类的基础父类

**类定义**：
```java
public abstract class AbstractNamedAction<C extends NamedMySQLConnection> 
    implements NamedActionInterface<C> {
    
    private final @Nonnull C namedSqlConnection;

    public AbstractNamedAction(@Nonnull C namedSqlConnection) {
        this.namedSqlConnection = namedSqlConnection;
    }

    @Nonnull
    @Override
    public C getNamedSqlConnection() {
        return namedSqlConnection;
    }
}
```

**使用场景**：
- 需要基于命名连接的数据库操作类
- 传统的单继承架构
- 简单的操作封装

#### AbstractNamedMixinAction<C, W>
**位置**: `io.github.sinri.keel.integration.mysql.action.AbstractNamedMixinAction`  
**版本**: 3.2.11+ (为混入风格优化，提取了NamedActionInterface)

抽象命名混入操作基类，提供了`NamedActionMixinInterface`的标准实现。

**主要功能**：
- 支持混入模式的数据库操作
- 提供自引用能力支持方法链
- 适用于复杂的多重继承场景

**类定义**：
```java
public abstract class AbstractNamedMixinAction<C extends NamedMySQLConnection, W> 
    implements NamedActionMixinInterface<C, W> {
    
    private final @Nonnull C namedSqlConnection;

    public AbstractNamedMixinAction(@Nonnull C namedSqlConnection) {
        this.namedSqlConnection = namedSqlConnection;
    }

    @Nonnull
    @Override
    public final C getNamedSqlConnection() {
        return namedSqlConnection;
    }
}
```

**关键特性**：
- `getNamedSqlConnection()`方法被标记为`final`，确保连接获取逻辑的一致性
- 需要子类实现`getImplementation()`方法以支持自引用

**使用场景**：
- 需要方法链式调用的操作类
- 复杂的功能组合和扩展
- 混入模式的架构设计

## 设计模式

### 1. 传统继承模式

适用于简单的数据库操作封装，使用`AbstractNamedAction`作为基类：

```java
public class UserAction extends AbstractNamedAction<DynamicNamedMySQLConnection> {
    
    public UserAction(DynamicNamedMySQLConnection connection) {
        super(connection);
    }
    
    public Future<User> findById(Long userId) {
        return AnyStatement.select(select -> select
            .from("users")
            .columnAsExpression("*")
            .where(where -> where.expressionEqualsNumericValue("id", userId))
        ).queryForOneRow(getNamedSqlConnection(), User.class);
    }
    
    public Future<Void> updateName(Long userId, String newName) {
        return AnyStatement.update(update -> update
            .table("users")
            .setWithValue("name", newName)
            .setWithExpression("updated_at", "NOW()")
            .where(where -> where.expressionEqualsNumericValue("id", userId))
        ).execute(getNamedSqlConnection()).mapEmpty();
    }
}
```

### 2. 混入模式

适用于需要方法链式调用和复杂功能组合的场景：

```java
public class FluentUserAction extends AbstractNamedMixinAction<DynamicNamedMySQLConnection, FluentUserAction> {
    
    private Long currentUserId;
    
    public FluentUserAction(DynamicNamedMySQLConnection connection) {
        super(connection);
    }
    
    @Override
    public FluentUserAction getImplementation() {
        return this;
    }
    
    public FluentUserAction withUserId(Long userId) {
        this.currentUserId = userId;
        return getImplementation();
    }
    
    public Future<FluentUserAction> loadUser() {
        if (currentUserId == null) {
            return Future.failedFuture("User ID not set");
        }
        
        return AnyStatement.select(select -> select
            .from("users")
            .columnAsExpression("*")
            .where(where -> where.expressionEqualsNumericValue("id", currentUserId))
        ).queryForOneRow(getNamedSqlConnection(), User.class)
         .compose(user -> {
             // 处理用户数据
             return Future.succeededFuture(getImplementation());
         });
    }
    
    public Future<FluentUserAction> updateStatus(String status) {
        return AnyStatement.update(update -> update
            .table("users")
            .setWithValue("status", status)
            .where(where -> where.expressionEqualsNumericValue("id", currentUserId))
        ).execute(getNamedSqlConnection())
         .compose(result -> Future.succeededFuture(getImplementation()));
    }
}

// 使用示例
FluentUserAction action = new FluentUserAction(connection);
Future<FluentUserAction> result = action
    .withUserId(123L)
    .loadUser()
    .compose(FluentUserAction::updateStatus("active"));
```

### 3. 接口组合模式

通过实现多个接口来组合功能：

```java
public interface UserQueryMixin<T> extends NamedActionMixinInterface<DynamicNamedMySQLConnection, T> {
    
    default Future<List<User>> findActiveUsers() {
        return AnyStatement.select(select -> select
            .from("users")
            .columnAsExpression("*")
            .where(where -> where.expressionEqualsLiteralValue("status", "active"))
            .orderByDesc("created_at")
        ).queryForRowList(getNamedSqlConnection(), User.class);
    }
    
    default Future<Long> countUsers() {
        return AnyStatement.select(select -> select
            .from("users")
            .columnAsExpression("COUNT(*) as total")
        ).queryForOneRow(getNamedSqlConnection(), JsonObject.class)
         .compose(row -> Future.succeededFuture(row.getLong("total")));
    }
}

public interface UserModifyMixin<T> extends NamedActionMixinInterface<DynamicNamedMySQLConnection, T> {
    
    default Future<T> createUser(String name, String email) {
        return AnyStatement.insert(insert -> insert
            .intoTable("users")
            .macroWriteOneRow(row -> row
                .put("name", name)
                .put("email", email)
                .putNow("created_at")
            )
        ).execute(getNamedSqlConnection())
         .compose(result -> Future.succeededFuture(getImplementation()));
    }
    
    default Future<T> deleteUser(Long userId) {
        return AnyStatement.delete(delete -> delete
            .from("users")
            .where(where -> where.expressionEqualsNumericValue("id", userId))
        ).execute(getNamedSqlConnection())
         .compose(result -> Future.succeededFuture(getImplementation()));
    }
}

// 组合实现
public class ComprehensiveUserAction extends AbstractNamedMixinAction<DynamicNamedMySQLConnection, ComprehensiveUserAction>
    implements UserQueryMixin<ComprehensiveUserAction>, UserModifyMixin<ComprehensiveUserAction> {
    
    public ComprehensiveUserAction(DynamicNamedMySQLConnection connection) {
        super(connection);
    }
    
    @Override
    public ComprehensiveUserAction getImplementation() {
        return this;
    }
}
```

## 与数据源集成

### 1. 基本集成

```java
// 初始化数据源
NamedMySQLDataSource<DynamicNamedMySQLConnection> dataSource = 
    KeelMySQLDataSourceProvider.initializeDynamicNamedMySQLDataSource("default");

// 在连接中使用操作
Future<User> result = dataSource.withConnection(connection -> {
    UserAction userAction = new UserAction(connection);
    return userAction.findById(123L);
});
```

### 2. 事务集成

```java
// 在事务中执行多个操作
Future<Void> result = dataSource.withTransaction(connection -> {
    ComprehensiveUserAction userAction = new ComprehensiveUserAction(connection);
    
    return userAction
        .createUser("John Doe", "john@example.com")
        .compose(action -> action.findActiveUsers())
        .compose(users -> {
            // 处理用户列表
            return Future.succeededFuture();
        });
});
```

### 3. 操作工厂模式

```java
public class ActionFactory {
    private final NamedMySQLDataSource<DynamicNamedMySQLConnection> dataSource;
    
    public ActionFactory(NamedMySQLDataSource<DynamicNamedMySQLConnection> dataSource) {
        this.dataSource = dataSource;
    }
    
    public <T> Future<T> withUserAction(Function<UserAction, Future<T>> actionFunction) {
        return dataSource.withConnection(connection -> {
            UserAction userAction = new UserAction(connection);
            return actionFunction.apply(userAction);
        });
    }
    
    public <T> Future<T> withFluentUserAction(Function<FluentUserAction, Future<T>> actionFunction) {
        return dataSource.withConnection(connection -> {
            FluentUserAction userAction = new FluentUserAction(connection);
            return actionFunction.apply(userAction);
        });
    }
}

// 使用工厂
ActionFactory factory = new ActionFactory(dataSource);

Future<User> user = factory.withUserAction(action -> 
    action.findById(123L)
);

Future<List<User>> users = factory.withFluentUserAction(action ->
    action.withUserId(123L)
          .loadUser()
          .compose(a -> a.findActiveUsers())
);
```

## 最佳实践

### 1. 连接管理
- **不要在操作内部管理事务**：事务应该由更高层级管理
- **使用依赖注入**：通过构造函数注入命名连接
- **避免连接泄露**：确保连接在数据源层面正确管理

### 2. 错误处理
```java
public class SafeUserAction extends AbstractNamedAction<DynamicNamedMySQLConnection> {
    
    public SafeUserAction(DynamicNamedMySQLConnection connection) {
        super(connection);
    }
    
    public Future<Optional<User>> findByIdSafely(Long userId) {
        return AnyStatement.select(select -> select
            .from("users")
            .columnAsExpression("*")
            .where(where -> where.expressionEqualsNumericValue("id", userId))
        ).queryForOneRow(getNamedSqlConnection(), User.class)
         .compose(user -> Future.succeededFuture(Optional.of(user)))
         .recover(throwable -> {
             if (throwable instanceof KeelSQLResultRowIndexError) {
                 // 未找到记录
                 return Future.succeededFuture(Optional.empty());
             }
             return Future.failedFuture(throwable);
         });
    }
}
```

### 3. 性能优化
- **批量操作**：对于大量数据使用批量API
- **连接复用**：在同一个事务中复用操作实例
- **异步组合**：合理使用Future组合避免阻塞

### 4. 代码组织
- **按功能分组**：将相关的数据库操作组织在同一个Action类中
- **接口分离**：使用混入接口分离不同类型的操作（查询、修改、删除等）
- **命名规范**：使用清晰的命名约定区分不同类型的操作

## 架构优势

### 1. 类型安全
- 通过泛型确保连接类型的编译时检查
- 避免运行时的类型转换错误

### 2. 可扩展性
- 支持传统继承和混入两种扩展模式
- 接口组合支持灵活的功能组合

### 3. 事务一致性
- 明确的事务边界定义
- 避免操作内部的事务管理混乱

### 4. 代码复用
- 抽象基类提供通用功能
- 混入接口支持功能的横向复用

## 版本历史

- **3.2.11** - 引入Action包，从`io.github.sinri.keel.mysql`迁移并优化
- **3.2.11** - 添加混入模式支持，提取NamedActionInterface
- **3.2.11** - 完善泛型设计和类型安全

## 相关组件

- **NamedMySQLConnection** - 命名MySQL连接抽象
- **NamedMySQLDataSource** - 命名MySQL数据源
- **AnyStatement** - SQL语句构建器
- **SelfInterface** - 自引用接口支持