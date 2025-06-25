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

#### 核心Mixin接口

**KeelIssueRecorderCore**
- 定义记录器的核心功能
- 提供各级别日志记录方法（debug、info、notice、warning、error、fatal）
- 支持异常记录和自定义处理器

**KeelIssueRecorderCommonMixin**
- 提供常用的日志记录便捷方法
- 支持简单字符串消息记录
- 支持异常和消息组合记录

**KeelIssueRecorderJsonMixin**
- 提供JSON上下文支持的日志记录方法
- 支持JsonObject上下文参数
- 支持Handler\<JsonObject>形式的上下文构建

#### 记录相关Mixin接口

**IssueRecordMessageMixin**
- 处理日志消息的设置和获取
- 定义消息属性的常量

**IssueRecordContextMixin**
- 处理日志上下文信息
- 支持JsonObject形式的上下文数据

### 3. 事件日志系统 (Event Logger System)

#### KeelEventLog
事件日志记录类，继承自 `KeelIssueRecord`，专门用于事件记录：
- 提供复制构造函数支持从基础记录创建事件日志
- 实现类型安全的泛型支持

#### 事件记录器转换
通过 `KeelIssueRecorder.toEventLogger()` 方法可将任何问题记录器转换为事件日志记录器：
```java
KeelIssueRecorder<KeelEventLog> eventLogger = someRecorder.toEventLogger();
```

### 4. 指标记录系统 (Metric System)

#### KeelMetricRecord
指标记录类，用于存储性能指标和统计数据：
- 支持指标名称和数值
- 支持标签（labels）系统
- 提供JSON序列化功能

#### KeelMetricRecorder
抽象指标记录器，提供批量处理指标数据的功能：
- 异步处理指标队列
- 支持缓冲区大小配置
- 支持优雅关闭
- 需要子类实现具体的处理逻辑
- **4.0.0版本简化**：不再支持多主题，使用单一主题处理

### 5. 输出适配器 (Output Adapters)

#### KeelIssueRecorderAdapter
适配器接口，定义了日志输出的标准接口：
- `record(String, KeelIssueRecord)` - 记录日志
- `issueRecordRender()` - 获取渲染器
- `close(Promise<Void>)` - 关闭适配器
- `gracefullyClose()` - 优雅关闭
- `isStopped()` - 检查是否已停止
- `isClosed()` - 检查是否已关闭

#### 内置适配器实现

**SyncStdoutAdapter**
- 同步标准输出适配器
- 直接输出到控制台
- 单例模式实现

**AsyncStdoutAdapter**
- 异步标准输出适配器
- 使用 `KeelIntravenous` 实现非阻塞输出
- 支持批量处理

**SilentAdapter**
- 静默适配器
- 不产生任何输出
- 用于测试或禁用日志场景

**AliyunSLSIssueAdapter**
- 阿里云日志服务适配器
- 支持将日志发送到阿里云SLS
- 支持批量处理和队列管理

### 6. 渲染器 (Renderers)

#### KeelIssueRecordRender
渲染器接口，负责将日志记录转换为特定格式：

**KeelIssueRecordStringRender**
- 字符串渲染器
- 将日志记录格式化为可读字符串
- 包含时间戳、级别、主题、分类等信息
- 支持异常堆栈跟踪渲染

**KeelIssueRecordJsonObjectRender**
- JSON对象渲染器
- 将日志记录转换为JSON格式
- 适用于结构化日志处理

### 7. 静默记录器 (Silent Recorder)

#### SilentIssueRecorder
- 完全静默的记录器实现
- 不执行任何记录操作
- 支持旁路记录器功能
- 用于性能敏感或测试场景

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
eventRecorder.info("用户登录", new JsonObject().put("userId", "12345"));

// 或使用转换方法
KeelIssueRecorder<KeelEventLog> convertedEventRecorder = recorder.toEventLogger();
```

### 使用JsonMixin功能

```java
// 使用JsonObject上下文
recorder.info("处理请求", new JsonObject()
    .put("requestId", "req-123")
    .put("userId", "user-456"));

// 使用Handler构建上下文
recorder.warning("性能警告", context -> {
    context.put("responseTime", 2500);
    context.put("threshold", 2000);
});
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
metricRecorder.recordMetric(
    new KeelMetricRecord("response_time", 150)
        .label("service", "api")
        .label("endpoint", "/users")
);
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

### 静默记录器使用

```java
// 创建静默记录器
KeelIssueRecorder<KeelIssueRecord> silentRecorder = 
    KeelIssueRecorder.buildSilentIssueRecorder();

// 添加旁路记录器（仍然会输出到旁路）
silentRecorder.addBypassIssueRecorder(normalRecorder);
```

## 版本变更说明

### 4.0.0 版本变更（重大重构）
- 将 `KeelIssueRecord` 改为抽象类，增强类型安全性
- 引入 Mixin 接口系统，提供更好的功能组合
- 简化指标记录器，不再支持多主题处理
- 增加静默记录器实现
- 增强事件日志系统，支持从基础记录创建事件日志
- 添加记录器转换功能（`toEventLogger()`）

### 3.2.0 版本
- 引入了基于 `KeelIssueRecorder` 的事件日志系统

### 3.1.10 版本
- 引入了问题记录系统的技术预览版

## 最佳实践

1. **选择合适的日志级别** - 根据信息的重要性选择合适的日志级别
2. **使用结构化日志** - 利用上下文和属性功能记录结构化数据
3. **异常处理** - 记录异常时同时提供上下文信息
4. **性能考虑** - 对于高频日志，考虑使用异步适配器
5. **测试环境** - 在测试中使用静默适配器或静默记录器避免日志干扰
6. **监控集成** - 使用指标记录器集成监控系统
7. **类型安全** - 使用具体的记录类型（如 `KeelEventLog`）而不是泛型基类

## 注意事项

- 指标记录器需要手动启动和关闭
- 自定义适配器需要正确实现资源清理逻辑
- 日志级别比较基于枚举的序号顺序
- 4.0.0版本进行了重大重构，建议查看最新API文档
- 静默记录器仍支持旁路记录器功能，可实现条件性日志输出