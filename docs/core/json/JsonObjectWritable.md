# JsonObjectWritable 接口文档

## 概述

`JsonObjectWritable` 接口为可以向 JSON 对象写入数据的实体提供了标准方法。它继承自 [JsonObjectReadable](file:///Users/sinri/code/keel/src/main/java/io/github/sinri/keel/core/json/JsonObjectReadable.java#L17-L519) 接口，不仅提供了读取功能，还添加了写入和修改 JSON 数据的能力。

## 设计原理

[JsonObjectWritable](file:///Users/sinri/code/keel/src/main/java/io/github/sinri/keel/core/json/JsonObjectWritable.java#L12-L58) 接口的设计目标是提供一个统一的方法来修改 JSON 对象的内容。它允许开发者以编程方式向 JSON 对象添加或更新键值对，并提供了便捷方法来确保嵌套对象和数组的存在。

该接口采用了默认方法模式，定义了一个核心的写入方法 [ensureEntry()](file:///Users/sinri/code/keel/src/main/java/io/github/sinri/keel/core/json/JsonObjectWritable.java#L18-L20)，并提供了辅助方法来处理常见的写入场景。

## 核心方法

### ensureEntry()

```java
void ensureEntry(String key, Object value);
```

在包装的 JSON 对象中创建或替换键值对。

- **参数**:
  - `key` - 键名
  - `value` - 要存储的值
- **返回值**: 无

### ensureJsonObject()

```java
default JsonObject ensureJsonObject(String key)
```

获取指定键的 JSON 对象，如果不存在则创建一个新的空 JSON 对象。

- **参数**: `key` - 要获取或创建的 JSON 对象的键
- **返回值**: 现有的或新创建的 JSON 对象

### ensureJsonArray()

```java
default JsonArray ensureJsonArray(String key)
```

获取指定键的 JSON 数组，如果不存在则创建一个新的空 JSON 数组。

- **参数**: `key` - 要获取或创建的 JSON 数组的键
- **返回值**: 现有的或新创建的 JSON 数组

## 使用示例

```java
public class User implements JsonObjectWritable, JsonObjectReadable {
    private JsonObject data = new JsonObject();
    
    @Override
    public void ensureEntry(String key, Object value) {
        data.put(key, value);
    }
    
    @Override
    public <T> T read(Function<JsonPointer, Class<T>> func) {
        // 实现读取逻辑
        // ...
        return null;
    }
    
    @Override
    public boolean isEmpty() {
        return data.isEmpty();
    }
    
    @Override
    public Iterator<Map.Entry<String, Object>> iterator() {
        return data.iterator();
    }
}

// 使用示例
User user = new User();

// 添加基本属性
user.ensureEntry("name", "张三");
user.ensureEntry("age", 25);

// 添加嵌套对象
JsonObject address = user.ensureJsonObject("address");
address.put("city", "北京");
address.put("district", "朝阳区");

// 添加数组
JsonArray hobbies = user.ensureJsonArray("hobbies");
hobbies.add("读书");
hobbies.add("游泳");

// 现在 user 对象包含了完整的数据结构
```

## 高级用法

### 构建复杂嵌套结构

```java
User user = new User();

// 构建复杂的嵌套结构
JsonObject profile = user.ensureJsonObject("profile");
JsonObject contact = profile.ensureJsonObject("contact");
JsonArray emails = contact.ensureJsonArray("emails");
emails.add("user@example.com");
emails.add("user.work@example.com");

JsonObject settings = user.ensureJsonObject("settings");
settings.put("theme", "dark");
settings.put("notifications", true);
```

### 与读取功能结合使用

```java
User user = new User();

// 先添加一些数据
user.ensureEntry("name", "张三");
user.ensureEntry("age", 25);

// 然后读取数据
String name = user.readString("name");
Integer age = user.readInteger("age");

// 修改现有数据
user.ensureEntry("age", 26);

// 添加更多嵌套数据
JsonObject address = user.ensureJsonObject("address");
address.put("city", "上海");
address.put("district", "浦东新区");

// 读取嵌套数据
String city = user.readString("address", "city");
```

## 实现注意事项

1. **键冲突处理**: [ensureEntry()](file:///Users/sinri/code/keel/src/main/java/io/github/sinri/keel/core/json/JsonObjectWritable.java#L18-L20) 方法应替换已存在的键值对，而不是抛出异常
2. **空值处理**: 实现应明确如何处理 null 值
3. **类型安全**: 确保写入的值是 JSON 兼容的类型
4. **嵌套结构**: [ensureJsonObject()](file:///Users/sinri/code/keel/src/main/java/io/github/sinri/keel/core/json/JsonObjectWritable.java#L32-L41) 和 [ensureJsonArray()](file:///Users/sinri/code/keel/src/main/java/io/github/sinri/keel/core/json/JsonObjectWritable.java#L49-L57) 方法应确保返回有效的 JSON 结构

## 最佳实践

1. **链式调用**: 可以结合使用读取和写入方法来实现链式操作
2. **防御性编程**: 在写入复杂对象时进行必要的验证
3. **性能优化**: 对于频繁操作，考虑缓存常用的嵌套对象引用
4. **一致性**: 确保写入方法与读取方法在数据表示上保持一致

## 相关接口

- [JsonObjectReadable](file:///Users/sinri/code/keel/src/main/java/io/github/sinri/keel/core/json/JsonObjectReadable.java#L17-L519): 提供读取 JSON 数据的能力
- [JsonifiableEntity](file:///Users/sinri/code/keel/src/main/java/io/github/sinri/keel/core/json/JsonifiableEntity.java#L40-L187): 结合了读写能力的完整实体接口
- [JsonObjectConvertible](file:///Users/sinri/code/keel/src/main/java/io/github/sinri/keel/core/json/JsonObjectConvertible.java#L17-L21): 提供将对象转换为 JSON 的能力