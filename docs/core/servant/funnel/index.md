# KeelFunnel

`KeelFunnel` 是 Keel 框架中的任务队列处理器，用于按顺序处理自定义任务。它继承自 `KeelVerticleImpl`，作为 Vert.x Verticle 运行，提供异步、有序的任务执行机制。

## 版本信息

- **引入版本**: 3.0.0
- **日志记录器版本**: 4.0.2
- **设计模式**: Verticle 模式
- **线程安全**: 是（使用并发安全的数据结构）
- **异步支持**: 基于 Vert.x Future 的异步操作

## 设计理念

KeelFunnel 采用"漏斗"设计模式，将多个并发的任务请求汇聚到一个串行执行的通道中，确保任务按照添加顺序依次执行。这种设计特别适用于：

- 需要保证执行顺序的任务序列
- 避免并发冲突的资源操作
- 限制并发度的任务处理
- 网络连接的数据包处理

## 核心组件

### 1. 任务队列
```java
private final Queue<Supplier<Future<Void>>> queue;
```
- 使用 `ConcurrentLinkedQueue` 保证线程安全
- 存储返回 `Future<Void>` 的任务供应商
- 支持动态添加任务

### 2. 中断机制
```java
private final AtomicReference<Promise<Void>> interruptRef;
```
- 用于在空闲时中断睡眠状态
- 当新任务到达时立即唤醒处理循环
- 提高响应性能

### 3. 睡眠时间控制
```java
private final AtomicLong sleepTimeRef;
```
- 控制空闲时的睡眠间隔
- 默认值为 1000 毫秒
- 可通过 `setSleepTime()` 方法调整

### 4. 日志记录器
```java
private KeelIssueRecorder<KeelEventLog> funnelLogger;
```
- 专用的日志记录器
- 在构造函数中初始化
- 记录任务执行异常
- 支持自定义日志实现

## 主要方法

### 构造方法
```java
public KeelFunnel() {
    this.sleepTimeRef = new AtomicLong(1_000L);
    this.queue = new ConcurrentLinkedQueue<>();
    this.interruptRef = new AtomicReference<>();
    this.funnelLogger = buildFunnelLogger(); // 在构造函数中初始化
}
```

### 添加任务
```java
public void add(Supplier<Future<Void>> supplier)
```
- 将任务添加到队列末尾
- 如果处理器正在睡眠，立即唤醒
- 线程安全操作

### 设置睡眠时间
```java
public void setSleepTime(long sleepTime)
```
- 设置空闲时的睡眠间隔（毫秒）
- 必须大于 1，否则抛出 `IllegalArgumentException`
- 影响处理器的响应性和资源消耗

### 日志记录器管理
```java
protected KeelIssueRecorder<KeelEventLog> buildFunnelLogger()
protected KeelIssueRecorder<KeelEventLog> getFunnelLogger()
```
- `buildFunnelLogger()`: 创建默认日志记录器（4.0.2 版本引入）
- `getFunnelLogger()`: 获取当前日志记录器
- 支持子类重写以自定义日志行为

## 工作原理

### 1. 启动流程
```java
@Override
protected Future<Void> startVerticle() {
    // 启动无限循环的任务处理
    Keel.asyncCallEndlessly(this::executeCircle);
    return Future.succeededFuture();
}
```

### 2. 任务处理循环
`executeCircle()` 方法实现了复杂的任务处理逻辑：

1. **重置中断引用**: `this.interruptRef.set(null)`
2. **记录调试日志**: `funnelLogger.debug("funnel one circle start")`
3. **嵌套异步处理**: 使用 `Keel.asyncCallRepeatedly` 处理队列中的任务
4. **任务提取和执行**: 从队列中轮询任务并执行
5. **异常处理**: 捕获并记录任务执行异常
6. **睡眠设置**: 在 `eventually` 块中设置中断引用并睡眠

### 3. 任务执行逻辑
```java
return Keel.asyncCallRepeatedly(routineResult -> {
    return Future.succeededFuture()
        .compose(ready -> {
            Supplier<Future<Void>> supplier = queue.poll();
            if (supplier == null) {
                // 没有任务时停止重复执行
                routineResult.stop();
                return Future.succeededFuture(Future::succeededFuture);
            } else {
                return Future.succeededFuture(supplier);
            }
        })
        .compose(Supplier::get);
})
.recover(throwable -> {
    funnelLogger.exception(throwable);
    return Future.succeededFuture();
})
.eventually(() -> {
    this.interruptRef.set(Promise.promise());
    return Keel.asyncSleep(this.sleepTimeRef.get(), getCurrentInterrupt());
});
```

### 4. 中断唤醒机制
- 当队列为空时，处理器进入睡眠状态
- 新任务添加时，检查并完成中断 Promise
- 睡眠被中断后，重新开始任务处理循环

## 使用示例

### 基本用法
```java
import io.github.sinri.keel.core.servant.funnel.KeelFunnel;
import io.vertx.core.Future;
import io.vertx.core.DeploymentOptions;

// 创建并部署 KeelFunnel
KeelFunnel funnel = new KeelFunnel();
funnel.deployMe(new DeploymentOptions())
    .onSuccess(deploymentId -> {
        System.out.println("Funnel 部署成功: " + deploymentId);
        
        // 添加任务
        funnel.add(() -> {
            System.out.println("执行任务 1");
            return Future.succeededFuture();
        });
        
        funnel.add(() -> {
            System.out.println("执行任务 2");
            return Keel.asyncSleep(1000L); // 异步任务
        });
        
        funnel.add(() -> {
            System.out.println("执行任务 3");
            return Future.succeededFuture();
        });
    })
    .onFailure(throwable -> {
        System.err.println("Funnel 部署失败: " + throwable.getMessage());
    });
```

### 网络数据处理示例
```java
// 在 TCP 连接处理中使用 KeelFunnel
public class SocketHandler {
    private final KeelFunnel funnel;
    
    public SocketHandler() {
        this.funnel = new KeelFunnel();
        this.funnel.deployMe(new DeploymentOptions());
    }
    
    public void handleIncomingData(Buffer buffer) {
        // 将数据处理任务添加到漏斗中，确保按顺序处理
        funnel.add(() -> {
            return processBuffer(buffer);
        });
    }
    
    private Future<Void> processBuffer(Buffer buffer) {
        // 处理缓冲区数据
        System.out.println("处理 " + buffer.length() + " 字节数据");
        return Future.succeededFuture();
    }
}
```

### 自定义睡眠时间
```java
KeelFunnel funnel = new KeelFunnel();
// 设置更短的睡眠时间以提高响应性
funnel.setSleepTime(500L); // 500 毫秒

// 或设置更长的睡眠时间以降低资源消耗
funnel.setSleepTime(5000L); // 5 秒
```

### 自定义日志记录器
```java
public class CustomKeelFunnel extends KeelFunnel {
    @Override
    protected KeelIssueRecorder<KeelEventLog> buildFunnelLogger() {
        // 返回自定义的日志记录器
        return KeelIssueRecordCenter.outputCenter()
            .generateIssueRecorder("CustomFunnel", KeelEventLog::new);
    }
}
```

## 性能特性

### 优势
1. **有序执行**: 严格按照任务添加顺序执行
2. **异步非阻塞**: 基于 Vert.x 异步模型
3. **线程安全**: 使用并发安全的数据结构
4. **响应及时**: 中断机制确保新任务快速响应
5. **异常隔离**: 单个任务异常不影响后续任务
6. **无限循环**: 使用 `asyncCallEndlessly` 确保处理器持续运行
7. **嵌套异步**: 复杂的异步处理逻辑确保任务完整执行

### 注意事项
1. **内存使用**: 大量待处理任务会占用内存
2. **执行延迟**: 长时间运行的任务会阻塞后续任务
3. **资源消耗**: 睡眠时间设置需要平衡响应性和资源消耗
4. **部署要求**: 需要作为 Verticle 部署才能正常工作
5. **复杂逻辑**: 嵌套的异步调用增加了调试难度
6. **异常处理**: 任务异常会被捕获并记录，但不会停止处理器

## 适用场景

### 推荐使用
- **网络数据处理**: TCP/UDP 连接的数据包按序处理
- **文件操作序列**: 需要保证顺序的文件读写操作
- **状态机处理**: 状态变更需要严格顺序的场景
- **消息处理**: 消息队列的有序消费

### 不推荐使用
- **高并发计算**: 需要并行处理提高性能的场景
- **独立任务**: 任务间无依赖关系且可并行执行
- **实时性要求极高**: 无法容忍任务排队延迟的场景

## 扩展和定制

### 继承扩展
```java
public class EnhancedKeelFunnel extends KeelFunnel {
    private final AtomicLong taskCounter = new AtomicLong();
    
    @Override
    public void add(Supplier<Future<Void>> supplier) {
        long taskId = taskCounter.incrementAndGet();
        super.add(() -> {
            getFunnelLogger().info(r -> r.message("开始执行任务 " + taskId));
            return supplier.get()
                .compose(v -> {
                    getFunnelLogger().info(r -> r.message("任务 " + taskId + " 执行完成"));
                    return Future.succeededFuture();
                });
        });
    }
    
    @Override
    protected KeelIssueRecorder<KeelEventLog> buildFunnelLogger() {
        return KeelIssueRecordCenter.outputCenter()
            .generateIssueRecorder("EnhancedFunnel", KeelEventLog::new);
    }
}
```

### 监控和统计
```java
public class MonitoredKeelFunnel extends KeelFunnel {
    private final AtomicLong totalTasks = new AtomicLong();
    private final AtomicLong completedTasks = new AtomicLong();
    private final AtomicLong failedTasks = new AtomicLong();
    
    @Override
    public void add(Supplier<Future<Void>> supplier) {
        totalTasks.incrementAndGet();
        super.add(() -> {
            return supplier.get()
                .compose(v -> {
                    completedTasks.incrementAndGet();
                    return Future.succeededFuture();
                }, throwable -> {
                    failedTasks.incrementAndGet();
                    return Future.failedFuture(throwable);
                });
        });
    }
    
    public long getTotalTasks() { return totalTasks.get(); }
    public long getCompletedTasks() { return completedTasks.get(); }
    public long getFailedTasks() { return failedTasks.get(); }
    public long getPendingTasks() { return totalTasks.get() - completedTasks.get() - failedTasks.get(); }
}
```

## 相关类和接口

- **KeelVerticleImpl**: 父类，提供 Verticle 基础功能
- **KeelIssueRecorder**: 日志记录接口
- **KeelEventLog**: 事件日志实体类
- **ConcurrentLinkedQueue**: 线程安全队列实现
- **AtomicReference/AtomicLong**: 原子操作类
- **KeelAsyncMixin**: 提供 `asyncCallEndlessly` 和 `asyncCallRepeatedly` 方法
- **RepeatedlyCallTask**: 重复执行任务的控制类

## 总结

KeelFunnel 是一个设计精良的任务队列处理器，通过"漏斗"模式实现了高效的有序任务执行。它结合了 Vert.x 的异步特性和并发安全的数据结构，使用复杂的嵌套异步调用逻辑确保任务的完整执行。通过 `asyncCallEndlessly` 和 `asyncCallRepeatedly` 的组合使用，KeelFunnel 为需要保证执行顺序的场景提供了可靠的解决方案。合理使用 KeelFunnel 可以有效避免并发冲突，简化复杂的异步任务协调逻辑。

