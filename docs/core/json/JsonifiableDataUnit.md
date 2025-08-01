# JsonifiableDataUnit 接口文档

## 概述

`JsonifiableDataUnit` 是 Keel 框架中用于数据单元的核心接口。它整合了 JSON 对象的完整读写能力，同时避免了泛型类型带来的复杂性，是 [JsonifiableEntity](file:///Users/sinri/code/keel/src/main/java/io/github/sinri/keel/core/json/JsonifiableEntity.java#L40-L187) 的非泛型替代方案。

## 设计原理

[JsonifiableDataUnit](file:///Users/sinri/code/keel/src/main/java/io/github/sinri/keel/core/json/JsonifiableDataUnit.java#L19-L74) 接口的设计目的是为那些不需要泛型类型支持的数据单元提供一个完整的 JSON 处理解决方案。它继承了多个核心接口，提供了：

1. 将对象转换为 JSON 对象的能力 ([JsonObjectConvertible](file:///Users/sinri/code/keel/src/main/java/io/github/sinri/keel/core/json/JsonObjectConvertible.java#L17-L21))
2. 从 JSON 对象重新加载数据的能力 ([JsonObjectReloadable](file:///Users/sinri/code/keel/src/main/java/io/github/sinri/keel/core/json/JsonObjectReloadable.java#L15-L19))
3. 读取和写入 JSON 数据的能力 ([JsonObjectWritable](file:///Users/sinri/code/keel/src/main/java/io/github/sinri/keel/core/json/JsonObjectWritable.java#L12-L58))
4. 只读访问 JSON 数据的能力 ([UnmodifiableJsonifiableEntity](file:///Users/sinri/code/keel/src/main/java/io/github/sinri/keel/core/json/UnmodifiableJsonifiableEntity.java#L19-L54))
5. 集群序列化能力 (ClusterSerializable)

通过继承这些接口，[JsonifiableDataUnit](file:///Users/sinri/code/keel/src/main/java/io/github/sinri/keel/core/json/JsonifiableDataUnit.java#L19-L74) 为数据单元提供了一站式的 JSON 处理能力。

## 核心功能

### JSON 转换能力

作为 [JsonObjectConvertible](file:///Users/sinri/code/keel/src/main/java/io/github/sinri/keel/core/json/JsonObjectConvertible.java#L17-L21) 的子接口，[JsonifiableDataUnit](file:///Users/sinri/code/keel/src/main/java/io/github/sinri/keel/core/json/JsonifiableDataUnit.java#L19-L74) 可以将自身转换为 Vert.x [JsonObject](https://vertx.io/docs/apidocs/io/vertx/core/json/JsonObject.html)：

```java
@Nonnull
JsonObject toJsonObject();
```

### 数据重载能力

通过 [JsonObjectReloadable](file:///Users/sinri/code/keel/src/main/java/io/github/sinri/keel/core/json/JsonObjectReloadable.java#L15-L19) 接口，支持从 JSON 对象重新加载数据：

```java
void reloadData(@Nonnull JsonObject jsonObject);
```

### 读写能力

继承自 [JsonObjectWritable](file:///Users/sinri/code/keel/src/main/java/io/github/sinri/keel/core/json/JsonObjectWritable.java#L12-L58) 和 [JsonObjectReadable](file:///Users/sinri/code/keel/src/main/java/io/github/sinri/keel/core/json/JsonObjectReadable.java#L17-L519)，提供了完整的 JSON 数据读写能力。

### 集群序列化

通过继承 ClusterSerializable 接口，支持在集群环境中的序列化和反序列化。

## 默认实现

[JsonifiableDataUnit](file:///Users/sinri/code/keel/src/main/java/io/github/sinri/keel/core/json/JsonifiableDataUnit.java#L19-L74) 提供了一些默认方法的实现：

### read() 方法

```java
@Nullable
default <T> T read(@Nonnull Function<JsonPointer, Class<T>> func)
```

基于 JSON Pointer 读取指定类型的数据。

### ensureEntry() 方法

```java
default void ensureEntry(String key, Object value)
```

确保指定键值对存在于 JSON 对象中。

### isEmpty() 和 iterator() 方法

提供了检查是否为空和迭代键值对的功能。

### writeToBuffer() 和 readFromBuffer() 方法

实现了 ClusterSerializable 接口的序列化方法。

## 使用示例

```java
// 使用默认实现类
JsonifiableDataUnit dataUnit = new JsonifiableDataUnitImpl();

// 写入数据
dataUnit.ensureEntry("name", "张三");
dataUnit.ensureEntry("age", 25);

JsonObject address = dataUnit.ensureJsonObject("address");
address.put("city", "北京");
address.put("district", "朝阳区");

// 读取数据
String name = dataUnit.readString("name");
Integer age = dataUnit.readInteger("age");
String city = dataUnit.readString("address", "city");

// 转换为 JsonObject
JsonObject json = dataUnit.toJsonObject();

// 从 JsonObject 重新加载数据
JsonObject newData = new JsonObject()
    .put("name", "李四")
    .put("age", 30);
dataUnit.reloadData(newData);
```

## 与 JsonifiableEntity 的关系

[JsonifiableDataUnit](file:///Users/sinri/code/keel/src/main/java/io/github/sinri/keel/core/json/JsonifiableDataUnit.java#L19-L74) 是 [JsonifiableEntity](file:///Users/sinri/code/keel/src/main/java/io/github/sinri/keel/core/json/JsonifiableEntity.java#L40-L187) 的非泛型版本：

1. [JsonifiableEntity<E>](file:///Users/sinri/code/keel/src/main/java/io/github/sinri/keel/core/json/JsonifiableEntity.java#L40-L187) 通过泛型参数支持链式调用
2. [JsonifiableDataUnit](file:///Users/sinri/code/keel/src/main/java/io/github/sinri/keel/core/json/JsonifiableDataUnit.java#L19-L74) 避免了泛型复杂性，更适合简单的数据单元

选择使用哪个接口取决于具体需求：
- 如果需要链式调用和泛型支持，使用 [JsonifiableEntity](file:///Users/sinri/code/keel/src/main/java/io/github/sinri/keel/core/json/JsonifiableEntity.java#L40-L187)
- 如果只需要基本的 JSON 处理能力且希望避免泛型，使用 [JsonifiableDataUnit](file:///Users/sinri/code/keel/src/main/java/io/github/sinri/keel/core/json/JsonifiableDataUnit.java#L19-L74)

## 实现注意事项

1. **线程安全**: 根据使用场景考虑实现的线程安全性
2. **数据一致性**: 确保内部 JsonObject 与对外接口的一致性
3. **性能优化**: 对于频繁访问的数据，考虑缓存机制
4. **错误处理**: 合理处理类型转换错误和数据验证

## 最佳实践

1. **选择合适的实现**: 对于简单场景，可以直接使用 [JsonifiableDataUnitImpl](file:///Users/sinri/code/keel/src/main/java/io/github/sinri/keel/core/json/JsonifiableDataUnitImpl.java#L12-L32)
2. **自定义实现**: 对于复杂业务逻辑，可以创建自定义实现类
3. **数据验证**: 在 [reloadData()](file:///Users/sinri/code/keel/src/main/java/io/github/sinri/keel/core/json/JsonObjectReloadable.java#L15-L19) 方法中添加数据验证逻辑
4. **防御性复制**: 在可能的情况下，对传入的 JsonObject 进行复制以避免外部修改

## 在 Keel 框架中的应用

[JsonifiableDataUnit](file:///Users/sinri/code/keel/src/main/java/io/github/sinri/keel/core/json/JsonifiableDataUnit.java#L19-L74) 在 Keel 框架中广泛应用于：
1. 配置数据的处理
2. 网络消息的序列化和反序列化
3. 数据库存储对象的表示
4. 日志和监控数据的格式化