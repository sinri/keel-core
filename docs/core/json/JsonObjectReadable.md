# JsonObjectReadable 接口文档

## 概述

`JsonObjectReadable` 接口为可以从 JSON 对象中读取数据的实体提供了丰富的 API。它继承自 `Iterable<Map.Entry<String, Object>>`，允许对 JSON 对象的键值对进行迭代，并提供了大量便捷方法来读取不同类型的值。

## 设计原理

[JsonObjectReadable](file:///Users/sinri/code/keel/src/main/java/io/github/sinri/keel/core/json/JsonObjectReadable.java#L17-L519) 接口的设计目标是提供一个强大而灵活的方式来从 JSON 对象中读取数据。它支持通过 JSON Pointer 路径读取嵌套数据，并为各种常见的数据类型提供了专门的方法。

接口采用默认方法模式，提供了一个核心的 [read()](file:///Users/sinri/code/keel/src/main/java/io/github/sinri/keel/core/json/JsonObjectReadable.java#L22-L24) 方法，其他所有特定类型的读取方法都是基于这个核心方法构建的。

## 核心方法

### read()

```java
@Nullable
<T> T read(@Nonnull Function<JsonPointer, Class<T>> func);
```

这是所有读取操作的基础方法。它接受一个函数，该函数用于构建 JSON Pointer 路径并指定返回类型。

- **参数**: `func` - 一个函数，用于构建 JSON Pointer 并指定返回类型
- **返回值**: 读取到的值，如果未找到则返回 null

### 基本类型读取方法

接口为各种基本类型提供了专门的读取方法：

- `readString(String... args)` - 读取字符串值
- `readNumber(String... args)` - 读取数值
- `readBoolean(String... args)` - 读取布尔值
- `readJsonObject(String... args)` - 读取嵌套的 JsonObject
- `readJsonArray(String... args)` - 读取 JsonArray

### 数组类型读取方法

- `readStringArray(String... args)` - 读取字符串数组
- `readIntegerArray(String... args)` - 读取整数数组
- `readLongArray(String... args)` - 读取长整数数组
- `readFloatArray(String... args)` - 读取浮点数数组
- `readDoubleArray(String... args)` - 读取双精度浮点数数组
- `readJsonObjectArray(String... args)` - 读取 JsonObject 数组

### Required 后缀方法

对于每种读取方法，还有一个带 `Required` 后缀的版本，如 `readStringRequired()`。这些方法在读取值为 null 时会抛出 `NullPointerException`。

### 复杂对象读取方法

#### readJsonifiableEntity()

```java
@Nullable
<B extends JsonifiableEntity<?>> B readJsonifiableEntity(@Nonnull Class<B> bClass, String... args);
```

读取并转换为实现了 [JsonifiableEntity](file:///Users/sinri/code/keel/src/main/java/io/github/sinri/keel/core/json/JsonifiableEntity.java#L40-L187) 接口的实体。

#### readEntity()

```java
@Nullable
<C> C readEntity(@Nonnull Class<C> cClass, String... args);
```

使用 Jackson 的 `mapTo` 方法将 JSON 对象映射到指定类的实例。

## 使用示例

```java
// 假设我们有一个实现了 JsonObjectReadable 的类
JsonObjectReadable entity = ...;

// 读取基本类型
String name = entity.readString("user", "name");
Integer age = entity.readInteger("user", "age");
Boolean active = entity.readBoolean("user", "active");

// 读取嵌套对象
JsonObject address = entity.readJsonObject("user", "address");

// 读取数组
List<String> hobbies = entity.readStringArray("user", "hobbies");

// 使用 read 方法自定义读取逻辑
String value = entity.read(pointer -> {
    pointer.append("user").append("profile").append("displayName");
    return String.class;
});

// 使用 Required 方法确保值存在
try {
    String requiredName = entity.readStringRequired("user", "name");
} catch (NullPointerException e) {
    // 处理缺失值的情况
}
```

## 实现注意事项

1. 实现类需要提供 [read()](file:///Users/sinri/code/keel/src/main/java/io/github/sinri/keel/core/json/JsonObjectReadable.java#L22-L24) 方法的具体实现，其他方法将基于此实现
2. 所有 `Required` 方法在值为 null 时应抛出 `NullPointerException`
3. 数组读取方法应正确处理数组中包含 null 值的情况
4. 实现类应处理类型转换错误，通常返回 null 或抛出运行时异常

## 相关接口

- [JsonObjectWritable](file:///Users/sinri/code/keel/src/main/java/io/github/sinri/keel/core/json/JsonObjectWritable.java#L12-L58): 提供写入 JSON 数据的能力
- [JsonifiableEntity](file:///Users/sinri/code/keel/src/main/java/io/github/sinri/keel/core/json/JsonifiableEntity.java#L40-L187): 结合了读写能力的完整实体接口
- [UnmodifiableJsonifiableEntity](file:///Users/sinri/code/keel/src/main/java/io/github/sinri/keel/core/json/UnmodifiableJsonifiableEntity.java#L19-L54): 只读的 JSON 实体接口