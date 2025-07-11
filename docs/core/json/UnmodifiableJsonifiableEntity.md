# UnmodifiableJsonifiableEntity

## 概述

`UnmodifiableJsonifiableEntity` 是一个不可修改的 JSON 实体接口，提供了对 JSON 数据的只读访问能力。该接口继承了 `Iterable<Map.Entry<String, Object>>` 和 `Shareable`，为处理 JSON 数据提供了类型安全和便捷的读取方法。

## 主要特性

### 1. 不可修改性
- 提供只读访问，确保数据的不可变性
- 通过接口设计保证数据安全性

### 2. 类型安全的数据读取
- 提供多种类型化的读取方法
- 支持基本数据类型：`String`、`Number`、`Boolean`
- 支持数值类型：`Long`、`Integer`、`Float`、`Double`
- 支持复合类型：`JsonObject`、`JsonArray`
- 支持数组类型：各种基本类型的数组形式

### 3. 灵活的路径访问
- 使用 `JsonPointer` 进行路径导航
- 支持嵌套属性的链式访问
- 通过可变参数简化路径指定

### 4. 异常安全
- 类型转换异常时返回 `null` 而非抛出异常
- 提供优雅的错误处理机制

### 5. 必需值读取
- 提供 `*Required` 方法，确保返回值不为 `null`
- 当值为 `null` 时抛出 `NullPointerException`

## 核心方法

### 基础方法

#### `wrap(JsonObject jsonObject)`
```java
static UnmodifiableJsonifiableEntity wrap(@Nonnull JsonObject jsonObject)
```
静态工厂方法，将 `JsonObject` 包装为不可修改的实体。

#### `cloneAsJsonObject()`
```java
@Nonnull JsonObject cloneAsJsonObject()
```
克隆当前实体为新的 `JsonObject`。

#### `toJsonExpression()`
```java
default String toJsonExpression()
```
返回 JSON 对象的字符串表达式。

#### `read(Function<JsonPointer, Class<T>> func)`
```java
@Nullable <T> T read(@Nonnull Function<JsonPointer, Class<T>> func)
```
核心读取方法，通过函数式接口指定路径和目标类型。

### 类型化读取方法

#### 基本类型读取
```java
@Nullable String readString(String... args)
@Nullable Number readNumber(String... args)
@Nullable Boolean readBoolean(String... args)
```

#### 必需值读取（4.1.0新增）
```java
@Nonnull String readStringRequired(String... args)
@Nonnull Number readNumberRequired(String... args)
@Nonnull Boolean readBooleanRequired(String... args)
```

#### 数值类型读取
```java
@Nullable Long readLong(String... args)
@Nullable Integer readInteger(String... args)
@Nullable Float readFloat(String... args)
@Nullable Double readDouble(String... args)
```

#### 数值类型必需值读取（4.1.0新增）
```java
@Nonnull Long readLongRequired(String... args)
@Nonnull Integer readIntegerRequired(String... args)
@Nonnull Float readFloatRequired(String... args)
@Nonnull Double readDoubleRequired(String... args)
```

#### 复合类型读取
```java
@Nullable JsonObject readJsonObject(String... args)
@Nullable JsonArray readJsonArray(String... args)
```

#### 复合类型必需值读取（4.1.0新增）
```java
@Nonnull JsonObject readJsonObjectRequired(String... args)
@Nonnull JsonArray readJsonArrayRequired(String... args)
```

#### 数组类型读取
```java
@Nullable List<JsonObject> readJsonObjectArray(String... args)
@Nullable List<String> readStringArray(String... args)
@Nullable List<Integer> readIntegerArray(String... args)
@Nullable List<Long> readLongArray(String... args)
@Nullable List<Float> readFloatArray(String... args)
@Nullable List<Double> readDoubleArray(String... args)
```

#### 数组类型必需值读取（4.1.0新增）
```java
@Nonnull List<JsonObject> readJsonObjectArrayRequired(String... args)
@Nonnull List<String> readStringArrayRequired(String... args)
@Nonnull List<Integer> readIntegerArrayRequired(String... args)
@Nonnull List<Long> readLongArrayRequired(String... args)
@Nonnull List<Float> readFloatArrayRequired(String... args)
@Nonnull List<Double> readDoubleArrayRequired(String... args)
```

#### 通用读取
```java
@Nullable Object readValue(String... args)
@Nonnull Object readValueRequired(String... args)
```

### 工具方法

#### `toBuffer()`
```java
Buffer toBuffer()
```
将实体转换为 `Buffer` 对象。

#### `iterator()`
```java
@Nonnull Iterator<Map.Entry<String, Object>> iterator()
```
提供迭代器支持，可以遍历所有键值对。

#### `isEmpty()`
```java
boolean isEmpty()
```
检查实体是否为空。

## 典型用法

### 1. 基本使用
```java
// 创建 JsonObject
JsonObject jsonObject = new JsonObject()
    .put("name", "John Doe")
    .put("age", 30)
    .put("active", true);

// 包装为不可修改实体
UnmodifiableJsonifiableEntity entity = UnmodifiableJsonifiableEntity.wrap(jsonObject);

// 读取数据
String name = entity.readString("name");           // "John Doe"
Integer age = entity.readInteger("age");           // 30
Boolean active = entity.readBoolean("active");    // true
```

### 2. 嵌套对象访问
```java
JsonObject jsonObject = new JsonObject()
    .put("user", new JsonObject()
        .put("profile", new JsonObject()
            .put("email", "john@example.com")
            .put("phone", "123-456-7890")));

UnmodifiableJsonifiableEntity entity = UnmodifiableJsonifiableEntity.wrap(jsonObject);

// 访问嵌套属性
String email = entity.readString("user", "profile", "email");  // "john@example.com"
String phone = entity.readString("user", "profile", "phone");  // "123-456-7890"
```

### 3. 数组数据处理
```java
JsonObject jsonObject = new JsonObject()
    .put("tags", new JsonArray().add("java").add("json").add("api"))
    .put("scores", new JsonArray().add(85).add(92).add(78));

UnmodifiableJsonifiableEntity entity = UnmodifiableJsonifiableEntity.wrap(jsonObject);

// 读取数组
List<String> tags = entity.readStringArray("tags");      // ["java", "json", "api"]
List<Integer> scores = entity.readIntegerArray("scores"); // [85, 92, 78]
```

### 4. 复杂数据结构
```java
JsonObject jsonObject = new JsonObject()
    .put("users", new JsonArray()
        .add(new JsonObject().put("id", 1).put("name", "Alice"))
        .add(new JsonObject().put("id", 2).put("name", "Bob")));

UnmodifiableJsonifiableEntity entity = UnmodifiableJsonifiableEntity.wrap(jsonObject);

// 读取对象数组
List<JsonObject> users = entity.readJsonObjectArray("users");
if (users != null) {
    for (JsonObject user : users) {
        System.out.println("ID: " + user.getInteger("id") + 
                          ", Name: " + user.getString("name"));
    }
}
```

### 5. 安全的类型转换
```java
UnmodifiableJsonifiableEntity entity = UnmodifiableJsonifiableEntity.wrap(jsonObject);

// 安全读取，不存在的键返回 null
String nonExistent = entity.readString("nonExistentKey");  // null

// 类型不匹配时返回 null（而非抛出异常）
Integer invalidType = entity.readInteger("stringField");   // null
```

### 6. 必需值读取（4.1.0新增）
```java
UnmodifiableJsonifiableEntity entity = UnmodifiableJsonifiableEntity.wrap(jsonObject);

// 必需值读取，确保返回值不为 null
try {
    String name = entity.readStringRequired("name");  // 如果为 null 会抛出 NullPointerException
    Integer age = entity.readIntegerRequired("age");  // 如果为 null 会抛出 NullPointerException
} catch (NullPointerException e) {
    // 处理必需值缺失的情况
    System.err.println("Required field is missing");
}
```

### 7. 迭代访问
```java
UnmodifiableJsonifiableEntity entity = UnmodifiableJsonifiableEntity.wrap(jsonObject);

// 遍历所有键值对
for (Map.Entry<String, Object> entry : entity) {
    System.out.println(entry.getKey() + ": " + entry.getValue());
}

// 检查是否为空
if (!entity.isEmpty()) {
    // 处理非空实体
}
```

### 8. 数据转换
```java
UnmodifiableJsonifiableEntity entity = UnmodifiableJsonifiableEntity.wrap(jsonObject);

// 转换为 Buffer
Buffer buffer = entity.toBuffer();

// 克隆为新的 JsonObject
JsonObject cloned = entity.cloneAsJsonObject();

// 转换为字符串
String jsonString = entity.toString();
String jsonExpression = entity.toJsonExpression();
```

## 实现类

### UnmodifiableJsonifiableEntityImpl

`UnmodifiableJsonifiableEntityImpl` 是 `UnmodifiableJsonifiableEntity` 接口的标准实现类：

```java
public class UnmodifiableJsonifiableEntityImpl implements UnmodifiableJsonifiableEntity {
    private final @Nonnull JsonObject jsonObject;
    
    public UnmodifiableJsonifiableEntityImpl(@Nonnull JsonObject jsonObject) {
        this.jsonObject = jsonObject;
    }
    
    // 实现所有接口方法...
}
```

#### 主要特性
- 提供不可修改的 JSON 对象包装
- 支持深度复制
- 线程安全的只读访问

#### 复制功能
```java
@Override
public UnmodifiableJsonifiableEntityImpl copy() {
    return new UnmodifiableJsonifiableEntityImpl(cloneAsJsonObject());
}
```

## 设计优势

### 1. 类型安全
- 编译时类型检查
- 避免运行时类型转换错误

### 2. 空值安全
- 优雅处理不存在的键
- 类型不匹配时返回 `null`
- 提供必需值读取方法确保非空

### 3. 链式访问
- 支持深层嵌套属性访问
- 简洁的 API 设计

### 4. 性能优化
- 延迟计算
- 避免不必要的对象创建

### 5. 线程安全
- 不可修改特性保证线程安全
- 实现 `Shareable` 接口支持共享

### 6. 扩展性
- 支持 Jackson 序列化
- 提供丰富的工具方法

## 版本历史

- **2.7**: 引入基础读取方法
- **2.8**: 添加数组读取支持和异常安全处理
- **3.0.0**: 接口重构，添加 `Iterable` 和 `Shareable` 支持
- **3.1.10**: 抽象化核心方法
- **3.2.17**: 添加 `cloneAsJsonObject()` 方法
- **4.0.0**: 添加 `toBuffer()` 默认实现
- **4.1.0**: 
  - 添加 `toJsonExpression()` 方法
  - 添加所有 `*Required` 方法
  - 添加 `readValueRequired()` 方法
  - 所有 `toString()` 方法变为 `final`

## 注意事项

1. **空值处理**: 所有读取方法在找不到键或类型不匹配时返回 `null`
2. **必需值读取**: `*Required` 方法在值为 `null` 时会抛出 `NullPointerException`
3. **类型转换**: 数值类型之间会自动转换，但非数值类型转换会返回 `null`
4. **数组处理**: 数组中的 `null` 元素会被保留或转换为默认值（如数值类型的 0）
5. **性能考虑**: 频繁的深层访问可能影响性能，建议缓存常用值
6. **线程安全**: 虽然接口本身是线程安全的，但底层 `JsonObject` 的线程安全性取决于具体实现
7. **Jackson序列化**: 如需要Jackson序列化支持，请参考 [JsonifiableSerializer](JsonifiableSerializer.md) 文档
8. **实现类**: 推荐使用 `UnmodifiableJsonifiableEntityImpl` 作为标准实现

## 相关文档

- [JsonifiableEntity](JsonifiableEntity.md) - 可修改的JSON实体接口
- [JsonifiableSerializer](JsonifiableSerializer.md) - Jackson序列化器支持
- [JsonifiedThrowable](JsonifiedThrowable.md) - 异常JSON化工具

