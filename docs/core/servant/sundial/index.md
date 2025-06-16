# Keel Sundial 定时任务调度系统

Keel Sundial 是一个基于 Vert.x 的分布式定时任务调度系统，支持 Cron 表达式的任务调度，提供完整的任务生命周期管理和监控功能。

## 版本信息

- **引入版本**: 3.0.0
- **架构升级**: 3.2.4 使用 Verticle 处理任务执行
- **日志升级**: 4.0.0 改用 Issue Recorder 日志系统
- **设计模式**: Verticle 模式 + 策略模式
- **线程安全**: 是（使用 ConcurrentHashMap）
- **异步支持**: 基于 Vert.x Future 的异步操作

## 系统架构

### 核心组件层次结构

```
KeelSundial (抽象类)
├── 继承: KeelVerticleImpl
├── 管理: Map<String, KeelSundialPlan> planMap
├── 定时器: Long timerID (每分钟触发)
└── 日志: KeelIssueRecorder<SundialIssueRecord>

KeelSundialPlan (接口)
├── key(): String - 任务唯一标识
├── cronExpression(): KeelCronExpression - Cron 表达式
├── execute(): Future<Void> - 异步执行方法
└── isWorkerThreadRequired(): boolean - 是否需要工作线程

KeelSundialVerticle (内部类)
├── 继承: KeelVerticleImpl
├── 封装: KeelSundialPlan + Calendar + IssueRecorder
└── 职责: 单次任务执行的 Verticle 容器

SundialIssueRecord (日志记录)
├── 继承: KeelIssueRecord<SundialIssueRecord>
├── 主题: "Sundial"
└── 分类: "Scheduler" | "Plan"
```

### 工作流程

#### 1. 系统启动流程
```
1. KeelSundial.startVerticle()
   ├── 初始化 sundialIssueRecorder
   ├── 计算首次延迟时间 (61 - 当前秒数)
   └── 设置定时器 (每60秒触发一次)

2. 定时器触发
   ├── handleEveryMinute(Calendar.getInstance())
   └── refreshPlans()
```

#### 2. 任务匹配与执行流程
```
handleEveryMinute(Calendar now)
├── 解析当前时间: ParsedCalenderElements
├── 遍历所有计划: planMap.forEach()
├── 匹配 Cron 表达式: plan.cronExpression().match()
├── 匹配成功:
│   ├── 记录调试日志: "Sundial Plan Matched"
│   ├── 配置部署选项: DeploymentOptions
│   ├── 设置线程模型: WORKER (如果需要)
│   └── 部署执行器: new KeelSundialVerticle().deployMe()
└── 匹配失败:
    └── 记录调试日志: "Sundial Plan Not Match"
```

#### 3. 计划刷新流程
```
refreshPlans()
├── 异步独占调用: Keel.asyncCallExclusively()
├── 获取最新计划: fetchPlans()
├── 更新计划映射:
│   ├── 添加新计划: planMap.put()
│   └── 删除过期计划: planMap.remove()
└── 异常处理: sundialIssueRecorder.exception()
```

## 核心接口与抽象方法

### KeelSundial 抽象方法

#### 1. getIssueRecordCenter()
```java
abstract protected KeelIssueRecordCenter getIssueRecordCenter();
```
- **用途**: 提供日志记录中心
- **实现要求**: 返回用于创建 Issue Recorder 的中心实例

#### 2. fetchPlans()
```java
abstract protected Future<Collection<KeelSundialPlan>> fetchPlans();
```
- **用途**: 获取当前有效的任务计划集合
- **返回值**: 
  - `null`: 表示计划未修改，不更新
  - `Collection<KeelSundialPlan>`: 最新的计划集合
- **实现策略**: 可从数据库、配置文件、远程服务等获取

### KeelSundialPlan 接口方法

#### 1. 基础属性
```java
String key();                           // 任务唯一标识
KeelCronExpression cronExpression();    // Cron 表达式
```

#### 2. 执行方法
```java
Future<Void> execute(Calendar now, KeelIssueRecorder<SundialIssueRecord> sundialIssueRecorder);
```
- **参数**:
  - `now`: 触发时间
  - `sundialIssueRecorder`: 专用日志记录器
- **返回**: 异步执行结果

#### 3. 线程配置
```java
default boolean isWorkerThreadRequired() { return true; }
```
- **默认**: `true` (使用工作线程)
- **用途**: 控制任务是否在工作线程池中执行

## 实现示例

### 1. 基础 Sundial 实现

```java
public class MySundialService extends KeelSundial {
    
    @Override
    protected KeelIssueRecordCenter getIssueRecordCenter() {
        return KeelIssueRecordCenter.outputCenter();
    }
    
    @Override
    protected Future<Collection<KeelSundialPlan>> fetchPlans() {
        // 从数据库或配置文件获取计划
        List<KeelSundialPlan> plans = new ArrayList<>();
        
        // 示例：每天凌晨2点执行数据备份
        plans.add(new DataBackupPlan());
        
        // 示例：每5分钟执行健康检查
        plans.add(new HealthCheckPlan());
        
        return Future.succeededFuture(plans);
    }
}
```

### 2. 数据备份任务计划

```java
public class DataBackupPlan implements KeelSundialPlan {
    
    @Override
    public String key() {
        return "data-backup-daily";
    }
    
    @Override
    public KeelCronExpression cronExpression() {
        // 每天凌晨2点执行
        return new KeelCronExpression("0 2 * * *");
    }
    
    @Override
    public Future<Void> execute(Calendar now, KeelIssueRecorder<SundialIssueRecord> recorder) {
        recorder.info(r -> r
            .message("开始执行数据备份")
            .context("backup_time", now.getTime().toString())
        );
        
        return performDataBackup()
            .onSuccess(v -> recorder.info(r -> r.message("数据备份完成")))
            .onFailure(throwable -> recorder.exception(throwable, "数据备份失败"));
    }
    
    @Override
    public boolean isWorkerThreadRequired() {
        return true; // 数据备份需要工作线程
    }
    
    private Future<Void> performDataBackup() {
        // 实际的数据备份逻辑
        return Future.succeededFuture();
    }
}
```

### 3. 健康检查任务计划

```java
public class HealthCheckPlan implements KeelSundialPlan {
    
    @Override
    public String key() {
        return "health-check-5min";
    }
    
    @Override
    public KeelCronExpression cronExpression() {
        // 每5分钟执行一次
        return new KeelCronExpression("*/5 * * * *");
    }
    
    @Override
    public Future<Void> execute(Calendar now, KeelIssueRecorder<SundialIssueRecord> recorder) {
        return checkSystemHealth()
            .onSuccess(status -> {
                recorder.info(r -> r
                    .message("系统健康检查完成")
                    .context("status", status)
                    .context("check_time", now.getTime().toString())
                );
            })
            .onFailure(throwable -> {
                recorder.exception(throwable, "系统健康检查失败");
            });
    }
    
    @Override
    public boolean isWorkerThreadRequired() {
        return false; // 轻量级检查，使用事件循环线程
    }
    
    private Future<String> checkSystemHealth() {
        // 实际的健康检查逻辑
        return Future.succeededFuture("HEALTHY");
    }
}
```

### 4. 动态计划管理

```java
public class DynamicSundialService extends KeelSundial {
    private final DatabaseService databaseService;
    
    public DynamicSundialService(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }
    
    @Override
    protected KeelIssueRecordCenter getIssueRecordCenter() {
        return KeelIssueRecordCenter.outputCenter();
    }
    
    @Override
    protected Future<Collection<KeelSundialPlan>> fetchPlans() {
        return databaseService.getActivePlans()
            .map(dbPlans -> dbPlans.stream()
                .map(this::convertToPlan)
                .collect(Collectors.toList())
            );
    }
    
    private KeelSundialPlan convertToPlan(DatabasePlan dbPlan) {
        return new KeelSundialPlan() {
            @Override
            public String key() {
                return dbPlan.getId();
            }
            
            @Override
            public KeelCronExpression cronExpression() {
                return new KeelCronExpression(dbPlan.getCronExpression());
            }
            
            @Override
            public Future<Void> execute(Calendar now, KeelIssueRecorder<SundialIssueRecord> recorder) {
                return executeCustomTask(dbPlan, now, recorder);
            }
            
            @Override
            public boolean isWorkerThreadRequired() {
                return dbPlan.isWorkerThreadRequired();
            }
        };
    }
    
    private Future<Void> executeCustomTask(DatabasePlan plan, Calendar now, 
                                         KeelIssueRecorder<SundialIssueRecord> recorder) {
        // 根据计划类型执行不同的任务
        switch (plan.getTaskType()) {
            case "EMAIL_REPORT":
                return sendEmailReport(plan, now, recorder);
            case "DATA_CLEANUP":
                return performDataCleanup(plan, now, recorder);
            case "SYSTEM_MAINTENANCE":
                return performSystemMaintenance(plan, now, recorder);
            default:
                return Future.failedFuture("未知任务类型: " + plan.getTaskType());
        }
    }
}
```

## Cron 表达式支持

### 表达式格式
```
分钟 小时 日 月 星期
*    *   *  *  *
```

### 字段说明
- **分钟**: 0-59
- **小时**: 0-23
- **日**: 1-31
- **月**: 1-12
- **星期**: 0-6 (0=星期日)

### 特殊字符
- `*`: 匹配所有值
- `*/n`: 每n个单位
- `n-m`: 范围
- `n,m,k`: 列表

### 常用表达式示例
```java
// 每分钟执行
new KeelCronExpression("* * * * *");

// 每小时的第30分钟执行
new KeelCronExpression("30 * * * *");

// 每天凌晨2点执行
new KeelCronExpression("0 2 * * *");

// 每周一上午9点执行
new KeelCronExpression("0 9 * * 1");

// 每月1号凌晨执行
new KeelCronExpression("0 0 1 * *");

// 工作日每小时执行
new KeelCronExpression("0 * * * 1-5");

// 每5分钟执行
new KeelCronExpression("*/5 * * * *");
```

## 日志与监控

### SundialIssueRecord 使用

#### 1. 基础日志记录
```java
@Override
public Future<Void> execute(Calendar now, KeelIssueRecorder<SundialIssueRecord> recorder) {
    // 信息日志
    recorder.info(r -> r
        .message("任务开始执行")
        .context("task_id", key())
        .context("start_time", now.getTime().toString())
    );
    
    // 调试日志
    recorder.debug(r -> r
        .message("任务执行详情")
        .context("parameters", getTaskParameters())
    );
    
    // 警告日志
    recorder.warning(r -> r
        .message("任务执行缓慢")
        .context("duration_ms", executionTime)
    );
    
    // 错误日志
    recorder.exception(throwable, "任务执行异常");
    
    return Future.succeededFuture();
}
```

#### 2. 自定义日志分类
```java
// 在 KeelSundial 实现中自定义日志记录器
@Override
protected KeelIssueRecorder<SundialIssueRecord> buildIssueRecorder() {
    return getIssueRecordCenter().generateIssueRecorder(
        "CustomSundial", 
        () -> new SundialIssueRecord().classification("CustomScheduler")
    );
}
```

## 最佳实践

### 1. 任务设计原则

#### 幂等性
```java
public class IdempotentTaskPlan implements KeelSundialPlan {
    @Override
    public Future<Void> execute(Calendar now, KeelIssueRecorder<SundialIssueRecord> recorder) {
        String taskId = generateTaskId(now);
        
        return checkTaskAlreadyExecuted(taskId)
            .compose(executed -> {
                if (executed) {
                    recorder.info(r -> r.message("任务已执行，跳过").context("task_id", taskId));
                    return Future.succeededFuture();
                }
                return executeTask(taskId, recorder);
            });
    }
}
```

#### 错误处理
```java
@Override
public Future<Void> execute(Calendar now, KeelIssueRecorder<SundialIssueRecord> recorder) {
    return performTask()
        .recover(throwable -> {
            recorder.exception(throwable, "任务执行失败，尝试恢复");
            return performFallbackTask();
        })
        .onFailure(throwable -> {
            recorder.exception(throwable, "任务完全失败");
            sendAlertNotification(throwable);
        });
}
```

### 2. 性能优化

#### 线程模型选择
```java
@Override
public boolean isWorkerThreadRequired() {
    // CPU 密集型任务使用工作线程
    if (isComputeIntensive()) {
        return true;
    }
    
    // I/O 密集型任务可以使用事件循环线程
    if (isIOIntensive() && !isBlocking()) {
        return false;
    }
    
    // 默认使用工作线程保证安全
    return true;
}
```

#### 批量处理
```java
public class BatchProcessingPlan implements KeelSundialPlan {
    @Override
    public Future<Void> execute(Calendar now, KeelIssueRecorder<SundialIssueRecord> recorder) {
        return fetchPendingItems()
            .compose(items -> {
                if (items.isEmpty()) {
                    recorder.debug(r -> r.message("没有待处理项目"));
                    return Future.succeededFuture();
                }
                
                // 分批处理
                List<Future<Void>> futures = new ArrayList<>();
                for (List<Item> batch : partition(items, BATCH_SIZE)) {
                    futures.add(processBatch(batch, recorder));
                }
                
                return Future.all(futures).mapEmpty();
            });
    }
}
```

### 3. 部署与配置

#### 服务启动
```java
public class Application {
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        
        MySundialService sundialService = new MySundialService();
        
        vertx.deployVerticle(sundialService)
            .onSuccess(id -> System.out.println("Sundial 服务启动成功: " + id))
            .onFailure(throwable -> {
                System.err.println("Sundial 服务启动失败: " + throwable.getMessage());
                vertx.close();
            });
    }
}
```

#### 配置管理
```java
public class ConfigurableSundialService extends KeelSundial {
    @Override
    protected Future<Collection<KeelSundialPlan>> fetchPlans() {
        // 从配置中心获取计划
        return configService.getScheduledTasks()
            .map(configs -> configs.stream()
                .filter(config -> config.isEnabled())
                .map(this::createPlanFromConfig)
                .collect(Collectors.toList())
            );
    }
}
```

## 与其他组件的关系

### 1. 与 Watchman 的区别
- **Sundial**: 单节点定时任务，适合简单场景
- **Watchman**: 集群定时任务，支持分布式协调

### 2. 与 Queue 的配合
```java
public class QueueIntegratedPlan implements KeelSundialPlan {
    private final KeelQueue taskQueue;
    
    @Override
    public Future<Void> execute(Calendar now, KeelIssueRecorder<SundialIssueRecord> recorder) {
        // 定时向队列添加任务
        return generateTasks(now)
            .compose(tasks -> {
                tasks.forEach(taskQueue::addTask);
                recorder.info(r -> r
                    .message("已添加任务到队列")
                    .context("task_count", tasks.size())
                );
                return Future.succeededFuture();
            });
    }
}
```

### 3. 与 Funnel 的配合
```java
public class FunnelIntegratedPlan implements KeelSundialPlan {
    private final KeelFunnel processingFunnel;
    
    @Override
    public Future<Void> execute(Calendar now, KeelIssueRecorder<SundialIssueRecord> recorder) {
        // 定时向漏斗添加处理任务
        processingFunnel.add(() -> {
            return performScheduledProcessing(now, recorder);
        });
        return Future.succeededFuture();
    }
}
```

## 故障排查

### 1. 常见问题

#### 任务不执行
- 检查 Cron 表达式是否正确
- 确认 `fetchPlans()` 返回了正确的计划
- 查看日志中的匹配信息

#### 任务重复执行
- 确保任务具有幂等性
- 检查系统时间是否准确
- 避免长时间运行的任务

#### 内存泄漏
- 确保任务执行完成后正确清理资源
- 避免在任务中创建过多的对象
- 定期监控内存使用情况

### 2. 调试技巧

#### 启用详细日志
```java
@Override
protected KeelIssueRecorder<SundialIssueRecord> buildIssueRecorder() {
    var recorder = super.buildIssueRecorder();
    recorder.setVisibleLevel(KeelLogLevel.DEBUG); // 启用调试日志
    return recorder;
}
```

#### 任务执行统计
```java
public class StatisticalPlan implements KeelSundialPlan {
    private final AtomicLong executionCount = new AtomicLong(0);
    private final AtomicLong totalDuration = new AtomicLong(0);
    
    @Override
    public Future<Void> execute(Calendar now, KeelIssueRecorder<SundialIssueRecord> recorder) {
        long startTime = System.currentTimeMillis();
        long count = executionCount.incrementAndGet();
        
        return performTask()
            .onComplete(ar -> {
                long duration = System.currentTimeMillis() - startTime;
                totalDuration.addAndGet(duration);
                
                recorder.info(r -> r
                    .message("任务执行统计")
                    .context("execution_count", count)
                    .context("duration_ms", duration)
                    .context("average_duration_ms", totalDuration.get() / count)
                );
            });
    }
}
```

Keel Sundial 提供了一个强大而灵活的定时任务调度框架，通过合理的架构设计和丰富的扩展点，能够满足各种复杂的定时任务需求。

