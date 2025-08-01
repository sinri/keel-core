# JsonObjectConvertible 接口文档

## 概述

`JsonObjectConvertible` 是一个标记接口，用于表示可以序列化为 JSON 对象的实体。它继承自 [JsonSerializable](file:///Users/sinri/code/keel/src/main/java/io/github/sinri/keel/core/json/JsonSerializable.java#L10-L22) 接口，添加了将实体转换为 Vert.x [JsonObject](https://vertx.io/docs/apidocs/io/vertx/core/json/JsonObject.html) 的能力。

## 设计原理

在 Keel 框架中，许多组件需要能够序列化为 JSON 格式，以便在网络中传输或持久化存储。[JsonObjectConvertible](file:///Users/sinri/code/keel/src/main/java/io/github/sinri/keel/core/json/JsonObjectConvertible.java#L17-L21) 接口提供了一个标准的方式来实现这种转换，确保所有实现类都可以转换为标准的 JSON 对象。

该接口的设计遵循了单一职责原则，只关注对象到 JSON 的转换，而不涉及从 JSON 反序列化对象的功能。

## 核心方法

### toJsonObject()

```java
@Nonnull
JsonObject toJsonObject();
```

将当前实体的状态转换为 Vert.x [JsonObject](https://vertx.io/docs/apidocs/io/vertx/core/json/JsonObject.html)。

- **返回值**: 表示实体当前状态的非空 [JsonObject](https://vertx.io/docs/apidocs/io/vertx/core/json/JsonObject.html)
- **异常**: 无特定异常，但实现类应确保返回非空对象

## 使用示例

```java
public class User implements JsonObjectConvertible {
    private String name;
    private int age;
    
    // 构造函数和其他方法...
    
    @Override
    @Nonnull
    public JsonObject toJsonObject() {
        return new JsonObject()
            .put("name", name)
            .put("age", age);
    }
    
    @Override
    public String toJsonExpression() {
        return toJsonObject().encode();
    }
    
    @Override
    public String toFormattedJsonExpression() {
        return toJsonObject().encodePrettily();
    }
}

// 使用示例
User user = new User("张三", 25);
JsonObject json = user.toJsonObject();  // 转换为 JsonObject
String jsonString = user.toJsonExpression();  // 转换为 JSON 字符串
```

## 相关接口

- [JsonSerializable](file:///Users/sinri/code/keel/src/main/java/io/github/sinri/keel/core/json/JsonSerializable.java#L10-L22): 提供基本的 JSON 序列化能力
- [JsonObjectReadable](file:///Users/sinri/code/keel/src/main/java/io/github/sinri/keel/core/json/JsonObjectReadable.java#L17-L519): 提供从 JSON 对象读取数据的能力
- [JsonObjectWritable](file:///Users/sinri/code/keel/src/main/java/io/github/sinri/keel/core/json/JsonObjectWritable.java#L12-L58): 提供向 JSON 对象写入数据的能力

## 最佳实践

1. 实现类应确保 [toJsonObject()](file:///Users/sinri/code/keel/src/main/java/io/github/sinri/keel/core/json/JsonObjectConvertible.java#L17-L21) 方法始终返回非空对象
2. 实现类通常也应该实现 [JsonSerializable](file:///Users/sinri/code/keel/src/main/java/io/github/sinri/keel/core/json/JsonSerializable.java#L10-L22) 接口中的方法，提供标准的 JSON 字符串表示
3. 返回的 [JsonObject](https://vertx.io/docs/apidocs/io/vertx/core/json/JsonObject.html) 应该是实体当前状态的完整表示