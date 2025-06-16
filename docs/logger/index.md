# Logger

Keel Logger 是一个功能完整的日志记录系统，提供了结构化日志记录、事件记录、指标收集等功能。该系统基于问题记录（Issue Record）的概念设计，支持多种输出适配器和渲染格式。

## 核心组件

### 1. 日志级别 (KeelLogLevel)

定义了七个日志级别，按严重程度递增：

- `DEBUG` - 调试信息
- `INFO` - 一般信息
- `NOTICE` - 通知信息
- `WARNING` - 警告信息
- `ERROR` - 错误信息
- `FATAL` - 致命错误
- `SILENT` - 静默模式

提供级别比较方法：
- `isEnoughSeriousAs(KeelLogLevel)` - 判断当前级别是否足够严重
- `isNegligibleThan(KeelLogLevel)` - 判断当前级别是否可忽略
- `isSilent()` - 判断是否为静默模式

### 2. 问题记录系统 (Issue Record System)

#### KeelIssueRecord
抽象基类，定义了日志记录的基本结构：
- **时间戳** - 记录创建时间
- **日志级别** - 记录的严重程度
- **分类** - 记录的分类标签
- **属性** - 自定义属性集合
- **异常** - 关联的异常对象
- **消息** - 日志消息内容
- **上下文** - 上下文信息

#### KeelIssueRecordCenter
问题记录中心，负责管理记录器的创建和配置：
- `outputCenter()` - 获取标准输出中心
- `silentCenter()` - 获取静默中心
- `build(KeelIssueRecorderAdapter)` - 使用指定适配器构建中心
- `generateIssueRecorder(String, Supplier<T>)` - 生成问题记录器

#### KeelIssueRecorder
问题记录器接口，提供完整的日志记录功能：
- 支持各种日志级别的记录方法
- 支持结构化数据记录
- 支持异常记录
- 支持旁路记录器（bypass recorder）
- 支持记录格式化器

### 3. 事件日志系统 (Event Logger System)

#### KeelEventLog
事件日志记录类，继承自 `KeelIssueRecord`，专门用于事件记录。

#### KeelEventLogger
事件日志记录器，基于 `KeelIssueRecorder` 实现：
- **已弃用** - 从 4.0.0 版本开始弃用，建议直接使用 `KeelIssueRecorder<KeelEventLog>`
- 提供向后兼容性
- 支持记录格式化器

### 4. 指标记录系统 (Metric System)

#### KeelMetricRecord
指标记录类，用于存储性能指标和统计数据。

#### KeelMetricRecorder
抽象指标记录器，提供批量处理指标数据的功能：
- 异步处理指标队列
- 支持缓冲区大小配置
- 支持优雅关闭
- 需要子类实现具体的处理逻辑

### 5. 输出适配器 (Output Adapters)

#### KeelIssueRecorderAdapter
适配器接口，定义了日志输出的标准接口：
- `record(String, KeelIssueRecord)` - 记录日志
- `issueRecordRender()` - 获取渲染器
- `close(Promise<Void>)` - 关闭适配器
- `gracefullyClose()` - 优雅关闭

#### 内置适配器实现

**SyncStdoutAdapter**
- 同步标准输出适配器
- 直接输出到控制台
- 单例模式实现

**AsyncStdoutAdapter**
- 异步标准输出适配器
- 非阻塞输出到控制台

**SilentAdapter**
- 静默适配器
- 不产生任何输出
- 用于测试或禁用日志场景

**AliyunSLSIssueAdapter**
- 阿里云日志服务适配器
- 支持将日志发送到阿里云SLS

### 6. 渲染器 (Renderers)

#### KeelIssueRecordRender
渲染器接口，负责将日志记录转换为特定格式：

**KeelIssueRecordStringRender**
- 字符串渲染器
- 将日志记录格式化为可读字符串

**KeelIssueRecordJsonObjectRender**
- JSON对象渲染器
- 将日志记录转换为JSON格式

## 使用示例

### 基本日志记录

```java
// 创建问题记录中心
KeelIssueRecordCenter center = KeelIssueRecordCenter.outputCenter();

// 创建记录器
KeelIssueRecorder<KeelIssueRecord> recorder = center.generateIssueRecorder(
    "MyApp", 
    KeelIssueRecord::new
);

// 记录不同级别的日志
recorder.info("应用启动成功");
recorder.warning("配置文件缺少某些参数");
recorder.error("数据库连接失败", exception);
```

### 事件日志记录

```java
// 创建事件记录器
KeelIssueRecorder<KeelEventLog> eventRecorder = center.generateIssueRecorder(
    "Events", 
    KeelEventLog::new
);

// 记录事件
eventRecorder.info("用户登录")
    .context(new JsonObject().put("userId", "12345"))
    .classification(Arrays.asList("auth", "login"));
```

### 指标记录

```java
// 自定义指标记录器
public class MyMetricRecorder extends KeelMetricRecorder {
    @Override
    protected Future<Void> handleForTopic(String topic, List<KeelMetricRecord> buffer) {
        // 处理指标数据
        buffer.forEach(record -> {
            // 发送到监控系统
        });
        return Future.succeededFuture();
    }
}

// 使用指标记录器
MyMetricRecorder metricRecorder = new MyMetricRecorder();
metricRecorder.start();
metricRecorder.recordMetric(new KeelMetricRecord().name("response_time").value(150));
```

### 自定义适配器

```java
// 创建自定义适配器
KeelIssueRecorderAdapter customAdapter = new KeelIssueRecorderAdapter() {
    @Override
    public KeelIssueRecordRender<?> issueRecordRender() {
        return KeelIssueRecordRender.renderForJsonObject();
    }
    
    @Override
    public void record(String topic, KeelIssueRecord<?> issueRecord) {
        // 自定义输出逻辑
        JsonObject json = (JsonObject) issueRecordRender()
            .renderIssueRecord(topic, issueRecord);
        // 发送到外部系统
    }
    
    // ... 其他方法实现
};

// 使用自定义适配器
KeelIssueRecordCenter customCenter = KeelIssueRecordCenter.build(customAdapter);
```

## 版本变更说明

### 4.0.0 版本变更
- `KeelEventLogger` 标记为弃用，建议使用 `KeelIssueRecorder<KeelEventLog>`
- 简化了指标记录器的主题支持
- 增强了类型安全性

### 3.2.0 版本
- 引入了基于 `KeelIssueRecorder` 的全新 `KeelEventLogger`

### 3.1.10 版本
- 引入了问题记录系统的技术预览版

## 最佳实践

1. **选择合适的日志级别** - 根据信息的重要性选择合适的日志级别
2. **使用结构化日志** - 利用上下文和属性功能记录结构化数据
3. **异常处理** - 记录异常时同时提供上下文信息
4. **性能考虑** - 对于高频日志，考虑使用异步适配器
5. **测试环境** - 在测试中使用静默适配器避免日志干扰
6. **监控集成** - 使用指标记录器集成监控系统

## 注意事项

- `KeelEventLogger` 已弃用，新项目应使用 `KeelIssueRecorder<KeelEventLog>`
- 指标记录器需要手动启动和关闭
- 自定义适配器需要正确实现资源清理逻辑
- 日志级别比较基于枚举的序号顺序