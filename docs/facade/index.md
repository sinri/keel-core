# Facade

许多基础的代码逻辑机制已经封装在 `KeelInstance` 里，详见本文档后续正文。

其他较为重要相关内容参见相关链接：

* 关于异步环境下的逻辑支持封装 [async](./async/index.md)
* 关于配置项的封装 [configuration](./configuration/index.md)
* 关于测试体系的封装 [tesuto](./tesuto/index.md)

## KeelInstance 类文档

### 概述

`KeelInstance` 是 Keel 框架的核心门面类，提供了一个统一的入口点来访问框架的各种功能。它采用单例模式设计，作为整个应用程序的中央协调器。

### 类信息

- **包名**: `io.github.sinri.keel.facade`
- **版本历史**: 
  - `@since 3.1.0` - 初始版本
  - `@since 4.0.0` - 改为 final 类并实现 KeelAsyncMixin
- **设计模式**: 单例模式
- **访问修饰符**: `public final`

### 实现的接口

`KeelInstance` 实现了多个混入接口，提供了丰富的功能：

1. **KeelHelpersInterface** - 核心辅助功能接口
2. **KeelClusterKit** - 集群管理功能
3. **KeelAsyncMixin** - 异步操作支持
4. **KeelWebRequestMixin** - Web 请求处理功能

### 核心字段

#### 静态实例
```java
public final static KeelInstance Keel = new KeelInstance();
```
- 全局唯一的 KeelInstance 实例，可通过 `KeelInstance.Keel` 访问

#### 私有字段
- **configuration** (`KeelConfigElement`): 配置管理器，自 3.2.3 版本引入
- **logger** (`KeelIssueRecorder<KeelEventLog>`): 日志记录器，自 4.0.0 版本引入
- **vertx** (`Vertx`): Vert.x 实例，可为空
- **clusterManager** (`ClusterManager`): 集群管理器，可为空

### 构造函数

```java
private KeelInstance()
```
私有构造函数确保单例模式：
- 初始化空配置元素
- 创建默认日志记录器，设置可见级别为 WARNING
- 从 KeelIssueRecordCenter 获取输出中心

### 主要方法

#### 配置管理

##### getConfiguration()
```java
@Nonnull
public KeelConfigElement getConfiguration()
```
- **返回**: 配置元素对象
- **用途**: 获取应用程序配置

##### config(String dotJoinedKeyChain)
```java
public @Nullable String config(@Nonnull String dotJoinedKeyChain)
```
- **参数**: `dotJoinedKeyChain` - 点分隔的配置键链（如 "database.host"）
- **返回**: 配置值字符串，如果不存在则返回 null
- **用途**: 通过键链获取配置值

#### Vert.x 管理

##### getVertx()
```java
public @Nonnull Vertx getVertx()
```
- **返回**: Vert.x 实例
- **异常**: 如果 Vertx 未初始化则抛出 NullPointerException
- **用途**: 获取当前的 Vert.x 实例

##### setVertx(Vertx outsideVertx)
```java
public void setVertx(@Nonnull Vertx outsideVertx)
```
- **参数**: `outsideVertx` - 外部 Vert.x 实例
- **限制**: 仅在 KeelLauncher 中使用，不建议其他地方调用
- **异常**: 如果 Vertx 已初始化则抛出 IllegalStateException

##### initializeVertx(VertxOptions vertxOptions)
```java
public Future<Void> initializeVertx(@Nonnull VertxOptions vertxOptions)
```
- **参数**: `vertxOptions` - Vert.x 配置选项
- **返回**: Future<Void> - 异步初始化结果
- **用途**: 使用指定选项初始化 Vert.x（无集群管理器）

##### initializeVertx(VertxOptions vertxOptions, ClusterManager clusterManager)
```java
public Future<Void> initializeVertx(
    @Nonnull VertxOptions vertxOptions,
    @Nullable ClusterManager clusterManager
)
```
- **参数**: 
  - `vertxOptions` - Vert.x 配置选项
  - `clusterManager` - 集群管理器（可选）
- **返回**: Future<Void> - 异步初始化结果
- **逻辑**: 
  - 如果提供集群管理器，创建集群化的 Vert.x 实例
  - 否则创建独立的 Vert.x 实例

##### initializeVertxStandalone(VertxOptions vertxOptions)
```java
public void initializeVertxStandalone(@Nonnull VertxOptions vertxOptions)
```
- **参数**: `vertxOptions` - Vert.x 配置选项
- **用途**: 同步初始化独立的 Vert.x 实例（非集群模式）

#### 状态检查

##### isVertxInitialized()
```java
public boolean isVertxInitialized()
```
- **返回**: Vert.x 是否已初始化
- **用途**: 检查 Vert.x 实例状态

##### isRunningInVertxCluster()
```java
public boolean isRunningInVertxCluster()
```
- **返回**: 是否运行在 Vert.x 集群模式下
- **用途**: 检查集群运行状态

#### 集群管理

##### getClusterManager()
```java
@Nullable
public ClusterManager getClusterManager()
```
- **返回**: 当前的集群管理器，可能为 null
- **用途**: 获取集群管理器实例

#### 日志管理

##### getLogger()
```java
public KeelIssueRecorder<KeelEventLog> getLogger()
```
- **返回**: 日志记录器实例
- **版本**: 自 4.0.2 引入
- **特性**: 
  - 默认输出到 stdout
  - 默认只记录 WARNING 及以上级别
  - 可重新设置可见级别用于本地调试

#### 生命周期管理

##### gracefullyClose(Handler<Promise<Void>> promiseHandler)
```java
public Future<Void> gracefullyClose(@Nonnull io.vertx.core.Handler<Promise<Void>> promiseHandler)
```
- **参数**: `promiseHandler` - Promise 处理器
- **返回**: Future<Void> - 关闭操作的异步结果
- **用途**: 优雅地关闭应用程序，允许自定义关闭逻辑

##### close()
```java
public Future<Void> close()
```
- **返回**: Future<Void> - 关闭操作的异步结果
- **用途**: 简单的关闭操作，直接完成 Promise

### 使用示例

#### 基本使用
```java
// 获取 KeelInstance 实例
KeelInstance keel = KeelInstance.Keel;

// 获取配置
String dbHost = keel.config("database.host");

// 初始化 Vert.x
VertxOptions options = new VertxOptions();
keel.initializeVertx(options)
    .onSuccess(v -> {
        // Vert.x 初始化成功
        Vertx vertx = keel.getVertx();
        // 使用 vertx 实例
    })
    .onFailure(throwable -> {
        // 处理初始化失败
        keel.getLogger().error("Failed to initialize Vertx", throwable);
    });
```

#### 集群模式使用
```java
// 使用集群管理器初始化
ClusterManager clusterManager = new HazelcastClusterManager();
VertxOptions options = new VertxOptions();

KeelInstance.Keel.initializeVertx(options, clusterManager)
    .onSuccess(v -> {
        if (KeelInstance.Keel.isRunningInVertxCluster()) {
            // 运行在集群模式
        }
    });
```

#### 优雅关闭
```java
// 自定义关闭逻辑
KeelInstance.Keel.gracefullyClose(promise -> {
    // 执行清理工作
    cleanupResources()
        .onComplete(ar -> promise.complete());
});

// 或简单关闭
KeelInstance.Keel.close()
    .onComplete(ar -> {
        System.out.println("Application closed");
    });
```

### 设计特点

1. **单例模式**: 确保全局唯一实例
2. **门面模式**: 提供统一的访问接口
3. **混入设计**: 通过接口组合提供多种功能
4. **异步支持**: 基于 Vert.x 的异步编程模型
5. **配置管理**: 集成的配置系统
6. **日志集成**: 内置日志记录功能
7. **生命周期管理**: 完整的初始化和关闭流程

### 注意事项

1. **线程安全**: 作为单例，需要注意并发访问
2. **初始化顺序**: Vert.x 必须在使用前初始化
3. **资源管理**: 应用程序结束时需要调用 close() 方法
4. **集群模式**: 集群和独立模式的初始化方式不同
5. **日志级别**: 默认日志级别为 WARNING，调试时需要调整
