# Keel Queue 队列系统

Keel Queue 是一个基于 Vert.x 的异步队列处理系统，专为单节点模式设计，提供了完整的任务调度、执行和管理功能。

## 系统架构

### 核心组件

#### 1. KeelQueue - 队列管理器
- **作用**: 队列系统的核心控制器，负责任务调度和生命周期管理
- **继承**: `KeelVerticleImpl`
- **实现接口**: `KeelQueueNextTaskSeeker`, `KeelQueueSignalReader`
- **特点**: 
  - 单节点模式运行
  - 基于 Vert.x Verticle 架构
  - 支持信号控制（启动/停止）
  - 内置工作线程池管理

#### 2. KeelQueueTask - 队列任务
- **作用**: 队列中执行的具体任务单元
- **继承**: `KeelVerticleImpl`
- **特点**:
  - 每个任务作为独立的 Verticle 运行
  - 支持任务分类和引用标识
  - 内置日志记录功能
  - 自动生命周期管理

#### 3. QueueWorkerPoolManager - 工作线程池管理器
- **作用**: 管理队列的并发执行能力
- **功能**:
  - 控制最大并发任务数量
  - 跟踪当前运行任务数
  - 支持无限制或限制模式

### 状态管理

#### KeelQueueStatus - 队列状态
```java
public enum KeelQueueStatus {
    INIT,     // 初始化状态
    RUNNING,  // 运行状态
    STOPPED   // 停止状态
}
```

#### KeelQueueSignal - 控制信号
```java
public enum KeelQueueSignal {
    RUN,   // 运行信号
    STOP   // 停止信号
}
```

## 核心接口

### KeelQueueNextTaskSeeker - 任务查找器
负责从队列中查找下一个待执行的任务：
```java
Future<KeelQueueTask> seekNextTask();
long getWaitingPeriodInMsWhenTaskFree(); // 默认 10 秒
```

### KeelQueueSignalReader - 信号读取器
负责读取队列控制信号：
```java
Future<KeelQueueSignal> readSignal();
```

## 工作流程

### 1. 队列启动流程
1. 队列状态设置为 `RUNNING`
2. 初始化工作线程池管理器
3. 启动主循环 `routine()`
4. 读取控制信号
5. 根据信号执行相应操作

### 2. 任务执行流程
1. 检查工作线程池是否繁忙
2. 调用 `seekNextTask()` 查找任务
3. 如果找到任务，部署为 Worker Verticle
4. 任务执行完成后自动清理
5. 继续下一轮循环

### 3. 任务生命周期
1. **部署阶段**: 任务被部署为 Verticle
2. **初始化**: 调用 `notifyAfterDeployed()`
3. **执行**: 调用抽象方法 `run()`
4. **清理**: 调用 `notifyBeforeUndeploy()`
5. **卸载**: 自动卸载 Verticle

## 使用方法

### 1. 实现队列管理器

```java
public class MyQueue extends KeelQueue {
    
    @Override
    public Future<KeelQueueTask> seekNextTask() {
        // 实现任务查找逻辑
        // 返回 null 表示没有更多任务
        return Future.succeededFuture(new MyTask("task-id"));
    }
    
    @Override
    public Future<KeelQueueSignal> readSignal() {
        // 实现信号读取逻辑
        // 可以从配置、数据库或其他源读取
        return Future.succeededFuture(KeelQueueSignal.RUN);
    }
    
    @Override
    protected KeelIssueRecordCenter getIssueRecordCenter() {
        return KeelIssueRecordCenter.outputCenter();
    }
    
    @Override
    protected QueueWorkerPoolManager getQueueWorkerPoolManager() {
        // 可选：自定义工作线程池大小
        return new QueueWorkerPoolManager(5); // 最大 5 个并发任务
    }
    
    @Override
    protected long getWaitingPeriodInMsWhenTaskFree() {
        // 可选：自定义空闲等待时间
        return 5000L; // 5 秒
    }
}
```

### 2. 实现队列任务

```java
public class MyTask extends KeelQueueTask {
    private final String taskId;
    private final String data;
    
    public MyTask(String taskId, String data) {
        this.taskId = taskId;
        this.data = data;
    }
    
    @Override
    public String getTaskReference() {
        return taskId;
    }
    
    @Override
    public String getTaskCategory() {
        return "DATA_PROCESSING";
    }
    
    @Override
    protected Future<Void> run() {
        // 实现具体的任务逻辑
        getQueueTaskIssueRecorder().info(r -> r.message("开始处理任务: " + taskId));
        
        return processData(data)
            .onSuccess(result -> {
                getQueueTaskIssueRecorder().info(r -> r.message("任务完成: " + taskId));
            })
            .onFailure(throwable -> {
                getQueueTaskIssueRecorder().exception(throwable, 
                    r -> r.message("任务失败: " + taskId));
            });
    }
    
    @Override
    protected KeelIssueRecordCenter getIssueRecordCenter() {
        return KeelIssueRecordCenter.outputCenter();
    }
    
    @Override
    protected void notifyAfterDeployed() {
        // 可选：任务部署后的回调
    }
    
    @Override
    protected void notifyBeforeUndeploy() {
        // 可选：任务卸载前的回调
    }
    
    private Future<Void> processData(String data) {
        // 实际的数据处理逻辑
        return Future.succeededFuture();
    }
}
```

### 3. 启动队列

```java
public class QueueApplication {
    public static void main(String[] args) {
        // 初始化 Vert.x
        Keel.initializeVertxStandalone(new VertxOptions());
        
        // 创建并部署队列
        MyQueue queue = new MyQueue();
        queue.deployMe(new DeploymentOptions()
            .setThreadingModel(ThreadingModel.WORKER));
    }
}
```

## 配置选项

### 工作线程池配置
```java
// 无限制并发（默认）
new QueueWorkerPoolManager(0);

// 限制最大并发数
new QueueWorkerPoolManager(10);

// 运行时调整
queueWorkerPoolManager.changeMaxWorkerCount(5);
```

### 等待时间配置
```java
@Override
protected long getWaitingPeriodInMsWhenTaskFree() {
    return 30_000L; // 30 秒
}
```

## 日志记录

### 队列管理日志
- 使用 `QueueManageIssueRecord` 记录队列管理相关事件
- 主题: `"Queue"`
- 分类: `"manage"`

### 任务执行日志
- 使用 `QueueTaskIssueRecord` 记录任务执行相关事件
- 主题: `"Queue"`
- 分类: `"task"`，包含任务引用和分类信息

## 最佳实践

### 1. 任务设计原则
- **幂等性**: 任务应该支持重复执行
- **原子性**: 每个任务应该是一个完整的工作单元
- **异常处理**: 妥善处理异常，避免影响队列运行

### 2. 性能优化
- 合理设置工作线程池大小
- 根据任务特性调整等待时间
- 使用适当的日志级别

### 3. 监控建议
- 监控队列状态变化
- 跟踪任务执行时间和成功率
- 关注工作线程池使用情况

## 注意事项

1. **单节点限制**: 当前实现仅支持单节点模式
2. **任务锁定**: 任务查找器需要确保返回的任务已经被锁定
3. **资源管理**: 任务执行完成后会自动清理资源
4. **异常恢复**: 队列具有异常恢复能力，单个任务失败不会影响整体运行

## 版本历史

- **2.1**: 初始版本
- **3.0.9**: 引入 `QueueWorkerPoolManager`
- **4.0.0**: 重构接口设计，改进日志系统
- **4.0.2**: 完善日志记录器
- **4.0.4**: 重命名等待时间方法

