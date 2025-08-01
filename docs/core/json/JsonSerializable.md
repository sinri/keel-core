# JsonSerializable 接口文档

## 概述

`JsonSerializable` 接口是 Keel 框架中所有可序列化为 JSON 表达式的类的基础接口。它定义了将对象转换为 JSON 字符串的标准方法，为框架中的 JSON 处理提供了统一的入口。

## 设计原理

[JsonSerializable](file:///Users/sinri/code/keel/src/main/java/io/github/sinri/keel/core/json/JsonSerializable.java#L10-L22) 接口的设计目标是提供一个轻量级但功能完整的 JSON 序列化标准。它不涉及反序列化操作，只关注对象到 JSON 字符串的转换，符合单一职责原则。

该接口在 Keel 框架的 JSON 处理体系中处于基础地位，被其他更高级的接口如 [JsonObjectConvertible](file:///Users/sinri/code/keel/src/main/java/io/github/sinri/keel/core/json/JsonObjectConvertible.java#L17-L21) 所继承，形成了一个层次化的 JSON 处理架构。

## 核心方法

### toJsonExpression()

```java
String toJsonExpression();
```

将对象序列化为紧凑格式的 JSON 字符串表达式。

- **返回值**: 表示对象状态的 JSON 字符串，不含格式化空格和换行

### toFormattedJsonExpression()

```java
String toFormattedJsonExpression();
```

将对象序列化为格式化的 JSON 字符串表达式，包含适当的缩进和换行，便于阅读。

- **返回值**: 格式化的 JSON 字符串，便于人类阅读

### toString()

```java
@Override
String toString();
```

重写 Object 类的 toString 方法，按照接口要求应返回与 [toJsonExpression()](file:///Users/sinri/code/keel/src/main/java/io/github/sinri/keel/core/json/JsonSerializable.java#L10-L22) 相同的内容。

- **返回值**: 与 [toJsonExpression()](file:///Users/sinri/code/keel/src/main/java/io/github/sinri/keel/core/json/JsonSerializable.java#L10-L22) 相同的 JSON 字符串

## 使用示例

```java
public class Person implements JsonSerializable {
    private String name;
    private int age;
    
    public Person(String name, int age) {
        this.name = name;
        this.age = age;
    }
    
    @Override
    public String toJsonExpression() {
        return new JsonObject()
            .put("name", name)
            .put("age", age)
            .encode();
    }
    
    @Override
    public String toFormattedJsonExpression() {
        return new JsonObject()
            .put("name", name)
            .put("age", age)
            .encodePrettily();
    }
    
    @Override
    public String toString() {
        return toJsonExpression();
    }
}

// 使用示例
Person person = new Person("张三", 25);

// 获取紧凑格式的 JSON 字符串
String json = person.toJsonExpression();
// 输出: {"name":"张三","age":25}

// 获取格式化的 JSON 字符串
String formattedJson = person.toFormattedJsonExpression();
// 输出:
// {
//   "name": "张三",
//   "age": 25
// }

// 使用 toString 方法
String str = person.toString();
// 输出: {"name":"张三","age":25}
```

## 在 Keel 框架中的作用

[JsonSerializable](file:///Users/sinri/code/keel/src/main/java/io/github/sinri/keel/core/json/JsonSerializable.java#L10-L22) 是 Keel 框架 JSON 处理体系的基础接口，其他接口都直接或间接继承自它：

1. [JsonObjectConvertible](file:///Users/sinri/code/keel/src/main/java/io/github/sinri/keel/core/json/JsonObjectConvertible.java#L17-L21) extends [JsonSerializable](file:///Users/sinri/code/keel/src/main/java/io/github/sinri/keel/core/json/JsonSerializable.java#L10-L22)
2. [UnmodifiableJsonifiableEntity](file:///Users/sinri/code/keel/src/main/java/io/github/sinri/keel/core/json/UnmodifiableJsonifiableEntity.java#L19-L54) extends [JsonObjectReadable](file:///Users/sinri/code/keel/src/main/java/io/github/sinri/keel/core/json/JsonObjectReadable.java#L17-L519), [JsonSerializable](file:///Users/sinri/code/keel/src/main/java/io/github/sinri/keel/core/json/JsonSerializable.java#L10-L22), Shareable
3. [JsonifiableDataUnit](file:///Users/sinri/code/keel/src/main/java/io/github/sinri/keel/core/json/JsonifiableDataUnit.java#L19-L74) extends [JsonObjectConvertible](file:///Users/sinri/code/keel/src/main/java/io/github/sinri/keel/core/json/JsonObjectConvertible.java#L17-L21), [JsonObjectReloadable](file:///Users/sinri/code/keel/src/main/java/io/github/sinri/keel/core/json/JsonObjectReloadable.java#L15-L19), [JsonObjectWritable](file:///Users/sinri/code/keel/src/main/java/io/github/sinri/keel/core/json/JsonObjectWritable.java#L12-L58), [UnmodifiableJsonifiableEntity](file:///Users/sinri/code/keel/src/main/java/io/github/sinri/keel/core/json/UnmodifiableJsonifiableEntity.java#L19-L54), ClusterSerializable

## 实现注意事项

1. **一致性**: [toString()](file:///Users/sinri/code/keel/src/main/java/io/github/sinri/keel/core/json/JsonSerializable.java#L10-L22) 方法必须返回与 [toJsonExpression()](file:///Users/sinri/code/keel/src/main/java/io/github/sinri/keel/core/json/JsonSerializable.java#L10-L22) 相同的内容
2. **性能**: 考虑缓存序列化结果以提高性能（如果对象是不可变的）
3. **字符编码**: 确保正确处理特殊字符的转义
4. **null 值处理**: 明确定义如何处理 null 值（是否包含在输出中）

## 最佳实践

1. **选择合适的格式**: 根据使用场景选择 [toJsonExpression()](file:///Users/sinri/code/keel/src/main/java/io/github/sinri/keel/core/json/JsonSerializable.java#L10-L22)（网络传输）或 [toFormattedJsonExpression()](file:///Users/sinri/code/keel/src/main/java/io/github/sinri/keel/core/json/JsonSerializable.java#L10-L22)（日志记录、调试）
2. **避免敏感信息**: 不要在序列化输出中包含密码、密钥等敏感信息
3. **循环引用处理**: 如果对象图中存在循环引用，需要特殊处理以避免无限递归
4. **版本兼容性**: 考虑序列化格式的向后兼容性

## 与其他序列化框架的关系

虽然 Keel 提供了自己的 JSON 序列化接口，但它底层仍然使用 Vert.x 的 JSON 处理功能和 Jackson 库。[JsonifiableSerializer](file:///Users/sinri/code/keel/src/main/java/io/github/sinri/keel/core/json/JsonifiableSerializer.java#L24-L38) 类提供了与 Jackson Databind 的集成，允许 [JsonSerializable](file:///Users/sinri/code/keel/src/main/java/io/github/sinri/keel/core/json/JsonSerializable.java#L10-L22) 实现在 Jackson 序列化过程中被正确处理。