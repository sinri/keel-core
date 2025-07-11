# JsonifiedThrowable

## 概述

`JsonifiedThrowable` 是一个用于将 Java 异常（Throwable）转换为 JSON 格式的工具类。它继承自 `JsonifiableEntityImpl<JsonifiedThrowable>`，提供了将异常信息序列化为 JSON 对象的功能，包括异常类型、消息、堆栈跟踪和原因链。

## 主要特性

### 1. 异常信息完整捕获
- 异常类名和消息
- 完整的堆栈跟踪信息
- 异常原因链（cause chain）
- 支持堆栈过滤和简化

### 2. JSON 序列化支持
- 实现 `JsonifiableEntity` 接口
- 支持 Jackson 序列化
- 提供丰富的读取方法

### 3. 堆栈跟踪处理
- 支持忽略特定包名的堆栈帧
- 可配置是否省略被忽略的堆栈信息
- 提供堆栈项的类型化访问

### 4. 线程安全
- 不可修改的 JSON 实体
- 支持多线程环境

## 核心方法

### 静态工厂方法

#### `wrap(Throwable throwable)`
```java
static JsonifiedThrowable wrap(@Nonnull Throwable throwable)
```
使用默认配置包装异常对象。

#### `wrap(Throwable throwable, Set<String> ignorableStackPackageSet, boolean omitIgnoredStack)`
```java
static JsonifiedThrowable wrap(
    @Nonnull Throwable throwable,
    @Nonnull Set<String> ignorableStackPackageSet,
    boolean omitIgnoredStack
)
```
使用自定义配置包装异常对象。

### 实例方法

#### 异常信息读取
```java
String getThrowableClass()
String getThrowableMessage()
List<JsonifiedCallStackItem> getThrowableStack()
JsonifiedThrowable getThrowableCause()
```

## 内部类

### JsonifiedCallStackItem

`JsonifiedCallStackItem` 是 `JsonifiedThrowable` 的内部类，用于表示堆栈跟踪中的单个项目：

```java
public static class JsonifiedCallStackItem extends JsonifiableEntityImpl<JsonifiedCallStackItem>
```

#### 构造方法
- `JsonifiedCallStackItem(JsonObject jsonObject)` - 从 JSON 对象创建
- `JsonifiedCallStackItem(String ignoringClassPackage, Integer ignoringCount)` - 创建忽略项
- `JsonifiedCallStackItem(StackTraceElement stackTranceItem)` - 从堆栈元素创建

#### 读取方法
```java
String getType()                    // 类型："call" 或 "ignored"
String getPackage()                 // 包名（仅忽略项）
String getIgnoredStackCount()       // 忽略的堆栈数量（仅忽略项）
String getCallStackClass()          // 类名（仅调用项）
String getCallStackMethod()         // 方法名（仅调用项）
String getCallStackFile()           // 文件名（仅调用项）
String getCallStackLine()           // 行号（仅调用项）
```

## 典型用法

### 1. 基本异常包装
```java
try {
    // 可能抛出异常的代码
    throw new RuntimeException("测试异常");
} catch (Throwable throwable) {
    // 包装异常为 JSON 格式
    JsonifiedThrowable jsonifiedThrowable = JsonifiedThrowable.wrap(throwable);
    
    // 获取异常信息
    String className = jsonifiedThrowable.getThrowableClass();
    String message = jsonifiedThrowable.getThrowableMessage();
    
    // 转换为 JSON 对象
    JsonObject jsonObject = jsonifiedThrowable.toJsonObject();
    System.out.println(jsonObject.encodePrettily());
}
```

### 2. 自定义堆栈过滤
```java
Set<String> ignorablePackages = Set.of(
    "java.lang",
    "sun.misc",
    "io.github.sinri.keel.core.helper"
);

try {
    // 可能抛出异常的代码
    throw new NullPointerException("空指针异常");
} catch (Throwable throwable) {
    // 使用自定义配置包装异常
    JsonifiedThrowable jsonifiedThrowable = JsonifiedThrowable.wrap(
        throwable,
        ignorablePackages,
        true  // 省略被忽略的堆栈信息
    );
    
    // 获取过滤后的堆栈信息
    List<JsonifiedCallStackItem> stackItems = jsonifiedThrowable.getThrowableStack();
    for (JsonifiedCallStackItem item : stackItems) {
        if ("call".equals(item.getType())) {
            System.out.println(item.getCallStackClass() + "." + 
                             item.getCallStackMethod() + ":" + 
                             item.getCallStackLine());
        }
    }
}
```

### 3. 异常链处理
```java
try {
    try {
        throw new IllegalArgumentException("参数错误");
    } catch (Exception e) {
        throw new RuntimeException("运行时错误", e);
    }
} catch (Throwable throwable) {
    JsonifiedThrowable jsonifiedThrowable = JsonifiedThrowable.wrap(throwable);
    
    // 遍历异常链
    JsonifiedThrowable current = jsonifiedThrowable;
    while (current != null) {
        System.out.println("异常类型: " + current.getThrowableClass());
        System.out.println("异常消息: " + current.getThrowableMessage());
        System.out.println("堆栈项数量: " + current.getThrowableStack().size());
        System.out.println("---");
        
        current = current.getThrowableCause();
    }
}
```

### 4. 堆栈项详细分析
```java
JsonifiedThrowable jsonifiedThrowable = JsonifiedThrowable.wrap(throwable);

List<JsonifiedCallStackItem> stackItems = jsonifiedThrowable.getThrowableStack();
for (JsonifiedCallStackItem item : stackItems) {
    String type = item.getType();
    
    if ("call".equals(type)) {
        // 处理调用堆栈项
        System.out.println("调用: " + item.getCallStackClass() + 
                          "." + item.getCallStackMethod() + 
                          " (" + item.getCallStackFile() + ":" + 
                          item.getCallStackLine() + ")");
    } else if ("ignored".equals(type)) {
        // 处理忽略项
        System.out.println("忽略包: " + item.getPackage() + 
                          " (数量: " + item.getIgnoredStackCount() + ")");
    }
}
```

### 5. JSON 序列化
```java
// 注册序列化器（程序启动时调用一次）
JsonifiableSerializer.register();

try {
    throw new RuntimeException("测试异常");
} catch (Throwable throwable) {
    JsonifiedThrowable jsonifiedThrowable = JsonifiedThrowable.wrap(throwable);
    
    // 直接序列化为 JSON 字符串
    String jsonString = jsonifiedThrowable.toString();
    System.out.println(jsonString);
    
    // 使用 Jackson 序列化
    ObjectMapper mapper = new ObjectMapper();
    String jacksonJson = mapper.writeValueAsString(jsonifiedThrowable);
    System.out.println(jacksonJson);
}
```

## JSON 结构

`JsonifiedThrowable` 生成的 JSON 结构如下：

```json
{
  "class": "java.lang.RuntimeException",
  "message": "测试异常",
  "stack": [
    {
      "type": "call",
      "class": "com.example.MyClass",
      "method": "myMethod",
      "file": "MyClass.java",
      "line": 42
    },
    {
      "type": "ignored",
      "package": "java.lang",
      "count": 5
    },
    {
      "type": "call",
      "class": "com.example.MainClass",
      "method": "main",
      "file": "MainClass.java",
      "line": 10
    }
  ],
  "cause": {
    "class": "java.lang.NullPointerException",
    "message": "空指针异常",
    "stack": [...],
    "cause": null
  }
}
```

## 配置选项

### 堆栈过滤配置

#### ignorableStackPackageSet
- 类型：`Set<String>`
- 描述：指定要忽略的包名集合
- 默认值：`KeelRuntimeHelper.ignorableCallStackPackage`

#### omitIgnoredStack
- 类型：`boolean`
- 描述：是否在 JSON 中省略被忽略的堆栈信息
- 默认值：`true`

## 设计优势

### 1. 完整性
- 捕获异常的所有重要信息
- 支持异常链的完整序列化

### 2. 可配置性
- 灵活的堆栈过滤配置
- 支持自定义忽略规则

### 3. 类型安全
- 提供类型化的读取方法
- 编译时类型检查

### 4. 性能优化
- 支持堆栈信息过滤
- 减少不必要的序列化开销

### 5. 扩展性
- 继承自 `JsonifiableEntity`
- 支持 Jackson 序列化

## 版本历史

- **4.1.0**: 初始版本，提供异常 JSON 化功能

## 注意事项

1. **序列化器注册**: 使用前需要调用 `JsonifiableSerializer.register()` 注册序列化器
2. **堆栈过滤**: 合理配置忽略包名可以提高性能和可读性
3. **内存使用**: 大型异常链可能占用较多内存，建议适当过滤
4. **线程安全**: 该类是线程安全的，可以在多线程环境中使用
5. **性能考虑**: 堆栈跟踪处理可能影响性能，建议在生产环境中适当配置过滤规则

## 相关文档

- [UnmodifiableJsonifiableEntity](UnmodifiableJsonifiableEntity.md) - 不可修改的JSON实体接口
- [JsonifiableEntity](JsonifiableEntity.md) - 可修改的JSON实体接口
- [JsonifiableSerializer](JsonifiableSerializer.md) - Jackson序列化器支持 