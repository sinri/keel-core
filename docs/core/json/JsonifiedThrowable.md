# JsonifiedThrowable 类文档

## 概述

`JsonifiedThrowable` 是 Keel 框架中用于将 Java 异常（Throwable）转换为 JSON 格式的工具类。它继承自 [JsonifiableEntityImpl](JsonifiableEntityImpl.md)，提供了完整的异常信息序列化能力，包括异常类型、消息、堆栈跟踪和异常链。

## 设计思想

### 异常信息结构化

[JsonifiedThrowable](src/main/java/io/github/sinri/keel/core/json/JsonifiedThrowable.java#L19-L190) 的设计目标是提供结构化的异常信息表示：

1. **完整性**：捕获异常的所有关键信息
2. **可读性**：提供人类可读的 JSON 格式
3. **可分析性**：支持程序化分析和处理
4. **可序列化**：支持网络传输和持久化存储

### 堆栈跟踪优化

该类提供了智能的堆栈跟踪处理机制：

```java
private static List<JsonifiedCallStackItem> filterStackTraceAndReduce(
    @Nullable StackTraceElement[] stackTrace,
    @Nonnull Set<String> ignorableStackPackageSet,
    boolean omitIgnoredStack
)
```

这种设计提供了：
- **性能优化**：过滤无关的堆栈帧
- **可读性提升**：减少噪音信息
- **可配置性**：支持自定义过滤规则
- **灵活性**：可选择是否保留被忽略的信息

### 异常链处理

支持完整的异常链序列化，确保异常的根本原因不被丢失：

```java
JsonifiedThrowable upper = x;
Throwable cause = throwable.getCause();
while (cause != null) {
    // 递归处理异常链
}
```

## 核心方法

### 静态工厂方法

#### wrap() 方法

```java
public static JsonifiedThrowable wrap(@Nonnull Throwable throwable)
public static JsonifiedThrowable wrap(
    @Nonnull Throwable throwable,
    @Nonnull Set<String> ignorableStackPackageSet,
    boolean omitIgnoredStack
)
```

- **功能**：将异常包装为 JSON 格式
- **参数**：
  - `throwable`：要包装的异常
  - `ignorableStackPackageSet`：要忽略的包名集合
  - `omitIgnoredStack`：是否省略被忽略的堆栈信息
- **返回值**：包装后的 JsonifiedThrowable 实例

### 异常信息读取方法

#### getThrowableClass()

```java
public String getThrowableClass()
```

- **功能**：获取异常类名
- **返回值**：异常的完整类名

#### getThrowableMessage()

```java
public String getThrowableMessage()
```

- **功能**：获取异常消息
- **返回值**：异常的消息文本

#### getThrowableStack()

```java
@Nonnull
public List<JsonifiedCallStackItem> getThrowableStack()
```

- **功能**：获取堆栈跟踪信息
- **返回值**：堆栈项列表

#### getThrowableCause()

```java
public JsonifiedThrowable getThrowableCause()
```

- **功能**：获取异常原因
- **返回值**：包装后的原因异常，如果没有则返回 null

## 内部类

### JsonifiedCallStackItem

`JsonifiedCallStackItem` 是表示堆栈跟踪中单个项目的内部类：

#### 构造方法

```java
private JsonifiedCallStackItem(JsonObject jsonObject)
private JsonifiedCallStackItem(String ignoringClassPackage, Integer ignoringCount)
private JsonifiedCallStackItem(StackTraceElement stackTranceItem)
```

#### 读取方法

```java
public String getType()                    // "call" 或 "ignored"
public String getPackage()                 // 包名（忽略项）
public String getIgnoredStackCount()       // 忽略数量（忽略项）
public String getCallStackClass()          // 类名（调用项）
public String getCallStackMethod()         // 方法名（调用项）
public String getCallStackFile()           // 文件名（调用项）
public String getCallStackLine()           // 行号（调用项）
```

## 使用方法指导

### 基本使用

```java
try {
    // 可能抛出异常的代码
    throw new RuntimeException("测试异常");
} catch (Throwable throwable) {
    // 包装异常
    JsonifiedThrowable jsonifiedThrowable = JsonifiedThrowable.wrap(throwable);
    
    // 获取异常信息
    String className = jsonifiedThrowable.getThrowableClass();
    String message = jsonifiedThrowable.getThrowableMessage();
    
    // 转换为 JSON
    JsonObject json = jsonifiedThrowable.toJsonObject();
    String jsonString = jsonifiedThrowable.toJsonExpression();
    
    System.out.println("异常类: " + className);
    System.out.println("异常消息: " + message);
    System.out.println("JSON: " + jsonString);
}
```

### 自定义堆栈过滤

```java
// 定义要忽略的包名
Set<String> ignorablePackages = Set.of(
    "io.github.sinri.keel.core.json",
    "java.util.concurrent",
    "sun.reflect"
);

try {
    throw new RuntimeException("测试异常");
} catch (Throwable throwable) {
    // 使用自定义过滤配置
    JsonifiedThrowable jsonifiedThrowable = JsonifiedThrowable.wrap(
        throwable, 
        ignorablePackages, 
        true  // 省略被忽略的堆栈信息
    );
    
    // 获取过滤后的堆栈信息
    List<JsonifiedCallStackItem> stack = jsonifiedThrowable.getThrowableStack();
    
    for (JsonifiedCallStackItem item : stack) {
        if ("call".equals(item.getType())) {
            System.out.println(item.getCallStackClass() + "." + 
                             item.getCallStackMethod() + ":" + 
                             item.getCallStackLine());
        } else {
            System.out.println("忽略 " + item.getPackage() + " 包中的 " + 
                             item.getIgnoredStackCount() + " 个堆栈帧");
        }
    }
}
```

### 异常链处理

```java
try {
    try {
        throw new IllegalArgumentException("参数错误");
    } catch (Exception e) {
        throw new RuntimeException("处理失败", e);
    }
} catch (Throwable throwable) {
    JsonifiedThrowable jsonifiedThrowable = JsonifiedThrowable.wrap(throwable);
    
    // 处理异常链
    JsonifiedThrowable current = jsonifiedThrowable;
    while (current != null) {
        System.out.println("异常: " + current.getThrowableClass());
        System.out.println("消息: " + current.getThrowableMessage());
        System.out.println("---");
        
        current = current.getThrowableCause();
    }
}
```

### 在日志系统中使用

```java
public class ExceptionLogger {
    
    public void logException(Throwable throwable, String context) {
        JsonifiedThrowable jsonifiedThrowable = JsonifiedThrowable.wrap(throwable);
        
        JsonObject logEntry = new JsonObject()
            .put("timestamp", Instant.now().toString())
            .put("context", context)
            .put("exception", jsonifiedThrowable.toJsonObject());
        
        // 写入日志
        System.err.println(logEntry.encodePrettily());
    }
    
    public void logExceptionWithFilter(Throwable throwable, String context) {
        Set<String> ignorablePackages = Set.of(
            "io.github.sinri.keel.core.json",
            "java.util.concurrent"
        );
        
        JsonifiedThrowable jsonifiedThrowable = JsonifiedThrowable.wrap(
            throwable, 
            ignorablePackages, 
            true
        );
        
        JsonObject logEntry = new JsonObject()
            .put("timestamp", Instant.now().toString())
            .put("context", context)
            .put("exception", jsonifiedThrowable.toJsonObject());
        
        System.err.println(logEntry.encodePrettily());
    }
}
```

### 在 Web API 中使用

```java
public class ErrorHandler {
    
    public void handleError(RoutingContext context, Throwable throwable) {
        JsonifiedThrowable jsonifiedThrowable = JsonifiedThrowable.wrap(throwable);
        
        JsonObject errorResponse = new JsonObject()
            .put("error", true)
            .put("message", "服务器内部错误")
            .put("exception", jsonifiedThrowable.toJsonObject());
        
        context.response()
            .setStatusCode(500)
            .putHeader("Content-Type", "application/json")
            .end(errorResponse.encode());
    }
    
    public void handleErrorWithFilter(RoutingContext context, Throwable throwable) {
        // 在生产环境中过滤敏感信息
        Set<String> ignorablePackages = Set.of(
            "io.github.sinri.keel.core.json",
            "java.util.concurrent",
            "sun.reflect"
        );
        
        JsonifiedThrowable jsonifiedThrowable = JsonifiedThrowable.wrap(
            throwable, 
            ignorablePackages, 
            true
        );
        
        JsonObject errorResponse = new JsonObject()
            .put("error", true)
            .put("message", "服务器内部错误")
            .put("exception", jsonifiedThrowable.toJsonObject());
        
        context.response()
            .setStatusCode(500)
            .putHeader("Content-Type", "application/json")
            .end(errorResponse.encode());
    }
}
```

### 在 EventBus 消息中使用

```java
public class EventBusErrorHandler {
    
    public void sendErrorToEventBus(Throwable throwable, String address) {
        JsonifiedThrowable jsonifiedThrowable = JsonifiedThrowable.wrap(throwable);
        
        JsonObject errorMessage = new JsonObject()
            .put("type", "error")
            .put("timestamp", System.currentTimeMillis())
            .put("exception", jsonifiedThrowable.toJsonObject());
        
        vertx.eventBus().send(address, errorMessage);
    }
    
    public void handleErrorFromEventBus(Message<JsonObject> message) {
        JsonObject errorData = message.body();
        JsonObject exceptionData = errorData.getJsonObject("exception");
        
        if (exceptionData != null) {
            JsonifiedThrowable jsonifiedThrowable = new JsonifiedThrowable(exceptionData);
            
            System.err.println("收到错误: " + jsonifiedThrowable.getThrowableClass());
            System.err.println("错误消息: " + jsonifiedThrowable.getThrowableMessage());
        }
    }
}
```

## 最佳实践

### 1. 性能优化

```java
public class OptimizedExceptionHandler {
    private static final Set<String> DEFAULT_IGNORABLE_PACKAGES = Set.of(
        "io.github.sinri.keel.core.json",
        "java.util.concurrent",
        "sun.reflect"
    );
    
    public JsonifiedThrowable wrapException(Throwable throwable) {
        // 使用预定义的忽略包集合
        return JsonifiedThrowable.wrap(throwable, DEFAULT_IGNORABLE_PACKAGES, true);
    }
    
    public JsonifiedThrowable wrapExceptionForDebug(Throwable throwable) {
        // 调试时保留更多信息
        return JsonifiedThrowable.wrap(throwable, Set.of(), false);
    }
}
```

### 2. 安全考虑

```java
public class SecureExceptionHandler {
    
    public JsonifiedThrowable wrapExceptionForProduction(Throwable throwable) {
        // 在生产环境中过滤敏感信息
        Set<String> ignorablePackages = Set.of(
            "io.github.sinri.keel.core.json",
            "java.util.concurrent",
            "sun.reflect",
            "com.company.internal"  // 内部包
        );
        
        return JsonifiedThrowable.wrap(throwable, ignorablePackages, true);
    }
    
    public JsonifiedThrowable wrapExceptionForDevelopment(Throwable throwable) {
        // 开发环境保留更多信息
        return JsonifiedThrowable.wrap(throwable);
    }
}
```

### 3. 自定义堆栈分析

```java
public class StackAnalyzer {
    
    public void analyzeStack(JsonifiedThrowable jsonifiedThrowable) {
        List<JsonifiedCallStackItem> stack = jsonifiedThrowable.getThrowableStack();
        
        int callCount = 0;
        int ignoredCount = 0;
        
        for (JsonifiedCallStackItem item : stack) {
            if ("call".equals(item.getType())) {
                callCount++;
                // 分析调用堆栈
                analyzeCallStackItem(item);
            } else {
                ignoredCount++;
            }
        }
        
        System.out.println("调用堆栈项: " + callCount);
        System.out.println("忽略堆栈项: " + ignoredCount);
    }
    
    private void analyzeCallStackItem(JsonifiedCallStackItem item) {
        String className = item.getCallStackClass();
        String methodName = item.getCallStackMethod();
        String fileName = item.getCallStackFile();
        String lineNumber = item.getCallStackLine();
        
        // 进行具体的分析逻辑
        if (className.contains("Controller")) {
            System.out.println("控制器方法: " + methodName);
        }
    }
}
```

## 注意事项

### 1. 性能考虑

- **堆栈过滤**：使用堆栈过滤可以提高性能，特别是在生产环境中
- **内存使用**：大型异常链可能占用较多内存
- **序列化开销**：JSON 序列化可能带来性能开销

### 2. 安全考虑

- **敏感信息**：确保不泄露敏感的内部信息
- **包过滤**：在生产环境中过滤内部包名
- **信息脱敏**：考虑对某些字段进行脱敏处理

### 3. 调试支持

- **完整信息**：在开发环境中保留完整的堆栈信息
- **可读性**：确保 JSON 格式便于人工阅读
- **工具支持**：支持常见的 JSON 分析工具

## 相关接口

- [JsonifiableEntityImpl](JsonifiableEntityImpl.md)：抽象实现类
- [JsonifiableEntity](JsonifiableEntity.md)：主要接口定义
- [JsonObjectConvertible](JsonObjectConvertible.md)：JSON 转换能力
- [JsonSerializable](JsonSerializable.md)：序列化能力 