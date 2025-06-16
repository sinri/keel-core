# Tesuto (Test) 测试框架

Tesuto 是 Keel 框架提供的测试工具包，包含两种主要的测试方式：单元测试（Unit Test）和即时运行测试（Instant Runner）。该包位于 `io.github.sinri.keel.facade.tesuto`。

## 包结构

```
tesuto/
├── unit/                    # 单元测试相关类
│   ├── KeelUnitTest.java   # 单元测试基类
│   └── KeelUnitTestCore.java # 单元测试核心接口
└── instant/                 # 即时运行测试相关类
    ├── KeelInstantRunner.java      # 即时运行器基类
    ├── InstantRunUnit.java         # 测试单元注解
    ├── InstantRunUnitSkipped.java  # 跳过测试注解
    ├── InstantRunUnitWrapper.java  # 测试单元包装器
    └── InstantRunnerResult.java    # 测试结果类
```

## 单元测试框架 (Unit Test)

### KeelUnitTestCore 接口

核心测试接口，定义了测试的基本生命周期方法：

```java
public interface KeelUnitTestCore {
    default void setUp() {}                    // 每个测试方法执行前调用
    default void tearDown() {}                // 每个测试方法执行后调用
    KeelIssueRecorder<KeelEventLog> getUnitTestLogger(); // 获取测试日志记录器
    default void async(Handler<Promise<Void>> testHandler) {} // 异步测试支持
    default void async(Supplier<Future<Void>> testSupplier) {} // 异步测试支持
}
```

### KeelUnitTest 基类

用于 `mvn test` 的基础测试类，自动初始化测试环境：

**主要特性：**
- 自动初始化 Vertx 环境（可配置）
- 自动加载配置文件 `config.properties`
- 提供标准输出日志记录器
- 支持异步测试

**使用方法：**

```java
public class MyUnitTest extends KeelUnitTest {
    
    @Override
    protected void prepareEnvironment() {
        // 准备测试环境
        getUnitTestLogger().info("准备测试环境");
    }
    
    @Override
    protected VertxOptions buildVertxOptions() {
        // 自定义 Vertx 配置，返回 null 禁用 Vertx
        return new VertxOptions().setEventLoopPoolSize(2);
    }
    
    @Override
    protected KeelIssueRecorder<KeelEventLog> buildUnitTestLogger() {
        // 自定义日志记录器，返回 null 使用默认
        return null;
    }
    
    @Test
    public void testSyncOperation() {
        // 同步测试
        String result = someOperation();
        getUnitTestLogger().info("测试结果: " + result);
        Assertions.assertEquals("expected", result);
    }
    
    @Test
    public void testAsyncOperation() {
        // 异步测试方式1：使用 Handler
        async(promise -> {
            someAsyncOperation()
                .onSuccess(result -> {
                    getUnitTestLogger().info("异步操作成功: " + result);
                    promise.complete();
                })
                .onFailure(promise::fail);
        });
    }
    
    @Test
    public void testAsyncOperationWithSupplier() {
        // 异步测试方式2：使用 Supplier
        async(() -> {
            return someAsyncOperation()
                .compose(result -> {
                    getUnitTestLogger().info("异步操作结果: " + result);
                    return Future.succeededFuture();
                });
        });
    }
}
```

### 缓存测试示例

```java
public class KeelCacheUnitTest extends KeelUnitTest {
    @Test
    public void testForSyncCache() {
        async(() -> {
            KeelCacheInterface<String, String> cache = KeelCacheInterface.createDefaultInstance();
            cache.save("a", "apple", 2); // 保存2秒
            
            return Keel.asyncSleep(1_000L)
                .compose(v -> {
                    try {
                        String value = cache.read("a");
                        getUnitTestLogger().info("读取到缓存值: " + value);
                        return Keel.asyncSleep(1_000L);
                    } catch (NotCached e) {
                        throw new RuntimeException(e);
                    }
                })
                .compose(v -> {
                    try {
                        cache.read("a");
                        Assertions.fail("缓存应该已过期");
                    } catch (NotCached e) {
                        getUnitTestLogger().info("缓存已过期，符合预期");
                    }
                    return Future.succeededFuture();
                });
        });
    }
}
```

## 即时运行测试框架 (Instant Runner)

### KeelInstantRunner 基类

用于在开发环境（如 IDE）中直接运行的测试框架：

**主要特性：**
- 支持直接在 IDE 中运行（通过 main 方法）
- 自动发现和执行带有 `@InstantRunUnit` 注解的方法
- 提供详细的测试报告和统计信息
- 支持测试跳过功能
- 异步测试支持

**基本使用：**

```java
public class MyInstantTest extends KeelInstantRunner {
    
    @Override
    protected VertxOptions buildVertxOptions() {
        // 配置 Vertx 选项
        return new VertxOptions().setEventLoopPoolSize(4);
    }
    
    @Override
    protected Future<Void> starting() {
        // 测试开始前的初始化
        getInstantLogger().info("开始测试初始化");
        Keel.getConfiguration().loadPropertiesFile("config.properties");
        return Future.succeededFuture();
    }
    
    @Override
    protected Future<Void> ending(List<InstantRunnerResult> testUnitResults) {
        // 测试结束后的清理工作
        getInstantLogger().info("测试完成，共执行 " + testUnitResults.size() + " 个测试");
        return Future.succeededFuture();
    }
    
    @InstantRunUnit
    public Future<Void> testBasicOperation() {
        getInstantLogger().info("执行基础操作测试");
        // 执行测试逻辑
        return Future.succeededFuture();
    }
    
    @InstantRunUnit
    @InstantRunUnitSkipped  // 跳过此测试
    public Future<Void> testSkippedOperation() {
        getInstantLogger().info("此测试将被跳过");
        return Future.succeededFuture();
    }
    
    @InstantRunUnit
    public Future<Void> testAsyncOperation() {
        return Keel.asyncSleep(1000L)
            .compose(v -> {
                getInstantLogger().info("异步操作完成");
                return Future.succeededFuture();
            });
    }
}
```

### 配置测试示例

```java
public class KeelConfigTest extends KeelInstantRunner {
    
    @Override
    protected Future<Void> starting() {
        Keel.getConfiguration().loadPropertiesFile("config.properties");
        return Future.succeededFuture();
    }
    
    @InstantRunUnit
    public Future<Void> readTest() {
        // 测试配置读取
        getInstantLogger().info(x -> x
            .message("所有配置")
            .context(Keel.getConfiguration().toJsonObject())
        );
        
        String smtpName = Keel.config("email.smtp.default_smtp_name");
        getInstantLogger().info("SMTP配置: " + smtpName);
        
        return Future.succeededFuture();
    }
}
```

### 数据库测试示例

```java
public class DatabaseTest extends KeelInstantRunner {
    private NamedMySQLDataSource<DynamicNamedMySQLConnection> dataSource;
    
    @Override
    protected Future<Void> starting() {
        return super.starting()
            .compose(v -> {
                Keel.getConfiguration().loadPropertiesFile("config.properties");
                dataSource = KeelMySQLDataSourceProvider
                    .initializeDynamicNamedMySQLDataSource("test_db");
                return Future.succeededFuture();
            });
    }
    
    @InstantRunUnit
    public Future<Void> testDatabaseQuery() {
        return dataSource.useConnection(connection -> {
            return connection.executeQuery("SELECT 1 as test_value")
                .compose(resultMatrix -> {
                    getInstantLogger().info(x -> x
                        .message("查询结果")
                        .context("result", resultMatrix.toJsonArray())
                    );
                    return Future.succeededFuture();
                });
        });
    }
}
```

## 注解说明

### @InstantRunUnit

标记方法为即时运行测试单元：

```java
@Retention(RetentionPolicy.RUNTIME)
public @interface InstantRunUnit {
    @Deprecated(since = "4.0.0")
    boolean skip() default false;  // 已废弃，使用 @InstantRunUnitSkipped 代替
}
```

**要求：**
- 方法必须是 public
- 方法必须返回 `Future<Void>`
- 方法所在类必须继承 `KeelInstantRunner`

### @InstantRunUnitSkipped

标记测试单元为跳过状态：

```java
@Retention(RetentionPolicy.RUNTIME)
public @interface InstantRunUnitSkipped {
}
```

**使用示例：**

```java
@InstantRunUnit
@InstantRunUnitSkipped
public Future<Void> temporarilyDisabledTest() {
    // 此测试将被跳过
    return Future.succeededFuture();
}
```

## 测试结果类

### InstantRunnerResult

封装测试执行结果：

```java
public class InstantRunnerResult {
    private final String testName;      // 测试名称
    private Long spentTime;             // 执行耗时（毫秒）
    private Boolean done;               // 是否完成
    private Throwable cause;            // 失败原因
    private Boolean skipped;            // 是否跳过
    
    // 状态查询方法
    public boolean isDone()     // 是否成功完成
    public boolean isFailed()   // 是否失败
    public boolean isSkipped()  // 是否跳过
}
```

## 最佳实践

### 1. 选择合适的测试框架

- **KeelUnitTest**: 适用于传统的单元测试，与 JUnit 集成，支持 `mvn test`
- **KeelInstantRunner**: 适用于快速开发测试，可在 IDE 中直接运行

### 2. 异步测试处理

```java
// 推荐：使用 async() 方法处理异步操作
@Test
public void testAsync() {
    async(() -> {
        return someAsyncOperation()
            .compose(result -> {
                // 验证结果
                return Future.succeededFuture();
            });
    });
}
```

### 3. 日志记录

```java
// 使用结构化日志
getUnitTestLogger().info(x -> x
    .message("测试执行")
    .context("input", inputData)
    .context("output", outputData)
);
```

### 4. 环境配置

```java
@Override
protected void prepareEnvironment() {
    // 设置测试专用配置
    System.setProperty("test.mode", "true");
    
    // 初始化测试数据
    initTestData();
}
```

### 5. 资源清理

```java
@Override
protected Future<Void> ending(List<InstantRunnerResult> results) {
    return cleanupTestResources()
        .compose(v -> super.ending(results));
}
```

## 运行测试

### 单元测试运行

```bash
# 运行所有单元测试
mvn test

# 运行特定测试类
mvn test -Dtest=MyUnitTest

# 运行特定测试方法
mvn test -Dtest=MyUnitTest#testMethod
```

### 即时运行测试

在 IDE 中直接运行继承了 `KeelInstantRunner` 的类的 `main` 方法，或者：

```bash
# 命令行运行
java -cp classpath com.example.MyInstantTest
```

## 测试输出示例

即时运行测试的典型输出：

```
[INFO] STARTING...
[INFO] RUNNING INSTANT UNITS...
[INFO] ☑︎	UNIT [testBasicOperation] PASSED. Spent 45 ms;
[INFO] ☐	UNIT [testSkippedOperation] SKIPPED. Spent 2 ms;
[INFO] ☑︎	UNIT [testAsyncOperation] PASSED. Spent 1023 ms;
[INFO] PASSED RATE: 2 / 2 i.e. 100.0%
```

通过 Tesuto 框架，开发者可以高效地编写和执行各种类型的测试，确保代码质量和系统稳定性。

