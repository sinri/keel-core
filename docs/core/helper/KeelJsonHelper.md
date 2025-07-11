# KeelJsonHelper

`KeelJsonHelper` 是 Keel 框架中的 JSON 数据处理工具类，提供深度读写、键链访问、排序功能、异常处理、格式化输出等全面的 JSON 操作功能。

## 版本信息

- **引入版本**: 2.6
- **设计模式**: 单例模式
- **线程安全**: 是
- **当前状态**: 大部分核心方法已在 4.1.0 版本中废弃

## ⚠️ 重要变更通知

**自 4.1.0 版本起，KeelJsonHelper 的大部分核心方法已被废弃**。这些方法包括：

- 所有 `writeIntoJsonObject` 方法
- 所有 `writeIntoJsonArray` 方法  
- 所有 `readFromJsonObject` 方法
- 所有 `readFromJsonArray` 方法
- `renderThrowableChain` 方法

**推荐使用替代方案**：
- 对于 JSON 读写操作，直接使用 Vert.x 的 `JsonObject` 和 `JsonArray` 原生方法
- 对于异常处理，使用 `JsonifiedThrowable` 类

## 获取实例

```java
import static io.github.sinri.keel.facade.KeelInstance.Keel;

// 通过 Keel 实例获取（推荐方式）
KeelJsonHelper jsonHelper = Keel.jsonHelper();

// 直接获取单例实例
KeelJsonHelper jsonHelper = KeelJsonHelper.getInstance();
```

## 当前可用功能

### 1. JSON 排序功能

#### 对象键排序

```java
// 创建无序的 JSON 对象
JsonObject unorderedObject = new JsonObject();
unorderedObject.put("zebra", "斑马");
unorderedObject.put("apple", "苹果");
unorderedObject.put("banana", "香蕉");
unorderedObject.put("cat", "猫");

System.out.println("原始对象: " + unorderedObject.encode());

// 获取键排序后的 JSON 字符串
String sortedJson = Keel.jsonHelper().getJsonForObjectWhoseItemKeysSorted(unorderedObject);
System.out.println("键排序后: " + sortedJson);
```

#### 数组元素排序

```java
// 创建无序的 JSON 数组
JsonArray unorderedArray = new JsonArray();
unorderedArray.add("zebra");
unorderedArray.add("apple");
unorderedArray.add("banana");
unorderedArray.add("cat");

System.out.println("原始数组: " + unorderedArray.encode());

// 获取元素排序后的 JSON 字符串
String sortedArrayJson = Keel.jsonHelper().getJsonForArrayWhoseItemsSorted(unorderedArray);
System.out.println("元素排序后: " + sortedArrayJson);
```

### 2. 堆栈跟踪过滤

```java
// 自定义堆栈跟踪过滤处理
StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
Set<String> ignorablePackages = Set.of("java.lang", "sun.reflect");

Keel.jsonHelper().filterStackTrace(
    stackTrace,
    ignorablePackages,
    (ignoringClassPackage, ignoringCount) -> {
        // 处理被忽略的堆栈项
        System.out.println("忽略包 " + ignoringClassPackage + " 的 " + ignoringCount + " 个堆栈项");
    },
    stackTraceItem -> {
        // 处理保留的堆栈项
        System.out.println("保留: " + stackTraceItem.getClassName() + "." + stackTraceItem.getMethodName());
    }
);
```

### 3. 格式化输出

```java
// 创建复杂的 JSON 结构
JsonObject complexData = new JsonObject();
complexData.put("application", new JsonObject()
    .put("name", "MyApp")
    .put("version", "1.0.0"));

// 使用块状格式显示
String blockFormat = Keel.jsonHelper().renderJsonToStringBlock("应用配置", complexData);
System.out.println("块状格式:");
System.out.println(blockFormat);
```

输出格式说明：
- 对象属性使用 `+` 标记
- 数组元素使用 `-` 标记
- 支持多级缩进显示
- 每个条目以换行符结束

## 废弃方法（4.1.0+）

### 基本读写操作（已废弃）

```java
// ❌ 已废弃的方法
JsonObject jsonObject = new JsonObject();
Keel.jsonHelper().writeIntoJsonObject(jsonObject, "name", "张三"); // 废弃
Object value = Keel.jsonHelper().readFromJsonObject(jsonObject, "name"); // 废弃

// ✅ 推荐使用 Vert.x 原生方法
JsonObject jsonObject = new JsonObject();
jsonObject.put("name", "张三"); // 推荐
Object value = jsonObject.getValue("name"); // 推荐
```

### 键链（Keychain）深度访问（已废弃）

```java
// ❌ 已废弃的方法
List<Object> keychain = Arrays.asList("user", "profile", "name");
Keel.jsonHelper().writeIntoJsonObject(jsonObject, keychain, "李四"); // 废弃
Object value = Keel.jsonHelper().readFromJsonObject(jsonObject, keychain); // 废弃

// ✅ 推荐使用原生方法
JsonObject user = jsonObject.getJsonObject("user", new JsonObject());
JsonObject profile = user.getJsonObject("profile", new JsonObject());
profile.put("name", "李四"); // 推荐

// 读取
String name = jsonObject.getJsonObject("user", new JsonObject())
    .getJsonObject("profile", new JsonObject())
    .getString("name"); // 推荐
```

### 异常处理（已废弃）

```java
// ❌ 已废弃的方法
try {
    throw new RuntimeException("测试异常");
} catch (Exception e) {
    JsonObject exceptionJson = Keel.jsonHelper().renderThrowableChain(e); // 废弃
    System.out.println(exceptionJson.encodePrettily());
}

// ✅ 推荐使用 JsonifiedThrowable
import io.github.sinri.keel.core.json.JsonifiedThrowable;

try {
    throw new RuntimeException("测试异常");
} catch (Exception e) {
    JsonifiedThrowable jsonifiedThrowable = JsonifiedThrowable.wrap(e);
    System.out.println(jsonifiedThrowable.toJsonObject().encodePrettily());
    
    // 忽略特定包的堆栈信息
    Set<String> ignorablePackages = Set.of("java.lang", "sun.reflect");
    JsonifiedThrowable filteredThrowable = JsonifiedThrowable.wrap(e, ignorablePackages, true);
    System.out.println(filteredThrowable.toJsonObject().encodePrettily());
}
```

## JsonifiedThrowable 详细用法

### 基本用法

```java
import io.github.sinri.keel.core.json.JsonifiedThrowable;

// 确保注册序列化器（应用启动时执行一次）
JsonifiableSerializer.register();

try {
    throw new RuntimeException("这是一个测试异常", 
        new IllegalArgumentException("参数错误"));
} catch (Exception e) {
    // 基本包装
    JsonifiedThrowable jsonifiedThrowable = JsonifiedThrowable.wrap(e);
    System.out.println("异常 JSON: " + jsonifiedThrowable.toJsonObject().encodePrettily());
}
```

### 高级用法

```java
// 忽略特定包的堆栈信息，并省略被忽略的堆栈项
Set<String> ignorablePackages = Set.of("java.lang", "sun.reflect");
JsonifiedThrowable filteredThrowable = JsonifiedThrowable.wrap(e, ignorablePackages, true);

// 保留被忽略的堆栈项信息
JsonifiedThrowable withIgnoredInfo = JsonifiedThrowable.wrap(e, ignorablePackages, false);

// 访问异常信息
String className = jsonifiedThrowable.getThrowableClass();
String message = jsonifiedThrowable.getThrowableMessage();
List<JsonifiedThrowable.JsonifiedCallStackItem> stack = jsonifiedThrowable.getThrowableStack();
JsonifiedThrowable cause = jsonifiedThrowable.getThrowableCause();
```

### 堆栈项访问

```java
List<JsonifiedThrowable.JsonifiedCallStackItem> stackItems = jsonifiedThrowable.getThrowableStack();
for (JsonifiedThrowable.JsonifiedCallStackItem item : stackItems) {
    String type = item.getType();
    if ("call".equals(type)) {
        System.out.println("调用: " + item.getCallStackClass() + "." + item.getCallStackMethod());
    } else if ("ignored".equals(type)) {
        System.out.println("忽略: " + item.getPackage() + " (" + item.getIgnoredStackCount() + " 项)");
    }
}
```

## 迁移指南

### 从废弃方法迁移

1. **基本读写操作**：
   - 将 `writeIntoJsonObject` 替换为 `JsonObject.put`
   - 将 `readFromJsonObject` 替换为 `JsonObject.getValue` 或类型特定的 getter

2. **数组操作**：
   - 将 `writeIntoJsonArray` 替换为 `JsonArray.add` 或 `JsonArray.set`
   - 将 `readFromJsonArray` 替换为 `JsonArray.getValue` 或类型特定的 getter

3. **键链访问**：
   - 使用链式调用替代键链参数
   - 考虑创建辅助方法来简化深层访问

4. **异常处理**：
   - 将 `renderThrowableChain` 替换为 `JsonifiedThrowable.wrap`
   - 确保在应用启动时调用 `JsonifiableSerializer.register()`

### 性能优化建议

1. **避免频繁的深层访问**：缓存中间结果
2. **使用类型安全的 getter**：避免类型转换错误
3. **合理使用默认值**：减少空值检查
4. **批量操作**：减少方法调用次数

## 注意事项

1. **废弃警告**：使用废弃方法会产生编译警告，建议尽快迁移
2. **序列化器注册**：使用 `JsonifiedThrowable` 前必须调用 `JsonifiableSerializer.register()`
3. **类型安全**：推荐使用类型特定的 getter 方法
4. **性能考虑**：排序和格式化操作对大型 JSON 对象有性能影响
5. **向后兼容**：废弃方法仍可使用，但将在未来版本中移除

## 版本历史

- **2.6**: 引入 KeelJsonHelper 基础功能
- **2.4**: 添加 JSON 排序功能
- **2.9**: 添加异常处理和堆栈跟踪功能
- **3.0.0**: 添加格式化输出和块状显示功能
- **3.1.0**: 通过 KeelHelpersInterface 提供统一访问方式
- **4.1.0**: 废弃大部分核心方法，引入 JsonifiedThrowable
- **当前版本**: 4.1.0-SNAPSHOT，保留排序、过滤和格式化功能 