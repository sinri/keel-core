# JsonifiableEntity

`JsonifiableEntity` 是 Keel 框架中的核心接口，用于定义可以与 JSON 对象相互转换的实体。它提供了一套完整的 JSON 数据处理能力，支持序列化、反序列化、集群消息传递和本地映射共享等功能。

## 特性概述

### 核心特性

1. **JSON 双向转换**：实体与 JSON 对象之间的无缝转换
2. **集群序列化**：支持 EventBus 消息传递（实现 `ClusterSerializable`）
3. **本地映射共享**：支持 LocalMap 共享（实现 `Shareable`）
4. **迭代器支持**：支持 foreach 遍历（实现 `Iterable`）
5. **缓冲区操作**：支持 Buffer 序列化和反序列化
6. **类型安全读取**：提供多种类型安全的数据读取方法
7. **自引用接口**：实现 `SelfInterface` 支持链式调用

### 继承体系

```
UnmodifiableJsonifiableEntity (只读操作)
    ↑
JsonifiableEntity (可修改操作)
    ↑
JsonifiableEntityImpl (抽象实现)
    ↑
SimpleJsonifiableEntity (一个具体实现)
```

## 接口定义

```java
public interface JsonifiableEntity<E>
        extends UnmodifiableJsonifiableEntity, ClusterSerializable, SelfInterface<E>
```

### 核心方法

#### 基础转换方法

```java
// 将实体转换为 JsonObject
@Nonnull JsonObject toJsonObject();

// 从 JsonObject 重新加载数据
@Nonnull E reloadDataFromJsonObject(@Nonnull JsonObject jsonObject);

// 返回 JSON 字符串表示
String toString();
```

#### 数据读取方法

```java
// 通用读取方法，支持 JsonPointer 路径
@Nullable <T> T read(@Nonnull Function<JsonPointer, Class<T>> func);

// 读取各种基本类型
@Nullable String readString(String... args);
@Nullable Number readNumber(String... args);
@Nullable Long readLong(String... args);
@Nullable Integer readInteger(String... args);
@Nullable Boolean readBoolean(String... args);

// 读取复合类型
@Nullable JsonObject readJsonObject(String... args);
@Nullable JsonArray readJsonArray(String... args);
@Nullable List<JsonObject> readJsonObjectArray(String... args);
```

#### 实体读取方法

```java
// 读取 JsonifiableEntity 实例
default @Nullable <B extends JsonifiableEntity<?>> B readJsonifiableEntity(
    @Nonnull Class<B> bClass, String... args);

// 使用 Jackson 读取普通实体
default @Nullable <C> C readEntity(@Nonnull Class<C> cClass, String... args);
```

#### 缓冲区操作

```java
// 从 Buffer 加载数据
default void fromBuffer(@Nonnull Buffer buffer);

// 写入到 Buffer
default void writeToBuffer(Buffer buffer);

// 从指定位置读取 Buffer
default int readFromBuffer(int pos, Buffer buffer);

// 转换为 Buffer
default Buffer toBuffer();
```

#### 便利方法

```java
// 确保指定键存在 JsonObject，不存在则创建
default JsonObject ensureJsonObject(String key);

// 确保指定键存在 JsonArray，不存在则创建
default JsonArray ensureJsonArray(String key);

// 写入键值对
default E write(String key, Object value);
```

## SimpleJsonifiableEntity 实现

`SimpleJsonifiableEntity` 是 `JsonifiableEntity` 的简单具体实现，继承自 `JsonifiableEntityImpl`。

### 类定义

```java
public class SimpleJsonifiableEntity extends JsonifiableEntityImpl<SimpleJsonifiableEntity>
```

### 构造方法

```java
// 创建空的实体
public SimpleJsonifiableEntity();

// 使用现有 JsonObject 创建实体
public SimpleJsonifiableEntity(@Nonnull JsonObject jsonObject);
```

### 核心方法

```java
// 创建副本
@Override
public SimpleJsonifiableEntity copy();

// 获取实现实例（用于链式调用）
@Nonnull
@Override
public SimpleJsonifiableEntity getImplementation();
```

## 典型用法

### 1. 基本使用

```java
// 创建空实体
SimpleJsonifiableEntity entity = new SimpleJsonifiableEntity();

// 写入数据
entity.write("name", "John Doe")
      .write("age", 30)
      .write("active", true);

// 读取数据
String name = entity.readString("name");
Integer age = entity.readInteger("age");
Boolean active = entity.readBoolean("active");

// 转换为 JSON 字符串
String jsonString = entity.toString();
```

### 2. 从 JsonObject 创建

```java
JsonObject jsonObject = new JsonObject()
    .put("id", 123)
    .put("email", "john@example.com")
    .put("profile", new JsonObject()
        .put("firstName", "John")
        .put("lastName", "Doe"));

SimpleJsonifiableEntity entity = new SimpleJsonifiableEntity(jsonObject);

// 读取嵌套数据
String firstName = entity.readString("profile", "firstName");
String lastName = entity.readString("profile", "lastName");
```

### 3. 包装现有 JsonObject

```java
JsonObject existingJson = new JsonObject().put("data", "value");
SimpleJsonifiableEntity wrapped = JsonifiableEntity.wrap(existingJson);
```

### 4. 处理复合数据

```java
SimpleJsonifiableEntity entity = new SimpleJsonifiableEntity();

// 确保嵌套对象存在
JsonObject profile = entity.ensureJsonObject("profile");
profile.put("name", "John");

// 确保数组存在
JsonArray tags = entity.ensureJsonArray("tags");
tags.add("developer").add("java");

// 读取数组数据
List<String> tagList = entity.readStringArray("tags");
```

### 5. 自定义实体类

```java
public class UserEntity extends JsonifiableEntityImpl<UserEntity> {
    public UserEntity() {
        super();
    }
    
    public UserEntity(JsonObject jsonObject) {
        super(jsonObject);
    }
    
    // 便利方法
    public String getName() {
        return readString("name");
    }
    
    public UserEntity setName(String name) {
        return write("name", name);
    }
    
    public Integer getAge() {
        return readInteger("age");
    }
    
    public UserEntity setAge(Integer age) {
        return write("age", age);
    }
    
    @Override
    public UserEntity getImplementation() {
        return this;
    }
}
```

### 6. 集群消息传递

```java
// 在 EventBus 中使用
EventBus eventBus = vertx.eventBus();
SimpleJsonifiableEntity message = new SimpleJsonifiableEntity()
    .write("type", "notification")
    .write("content", "Hello World");

eventBus.send("address", message);
```

### 7. 缓冲区操作

```java
SimpleJsonifiableEntity entity = new SimpleJsonifiableEntity()
    .write("data", "example");

// 转换为 Buffer
Buffer buffer = entity.toBuffer();

// 从 Buffer 恢复
SimpleJsonifiableEntity restored = new SimpleJsonifiableEntity();
restored.fromBuffer(buffer);
```

### 8. 读取复杂实体

```java
// 假设有一个 Address 类实现了 JsonifiableEntity
SimpleJsonifiableEntity person = new SimpleJsonifiableEntity()
    .write("name", "John")
    .write("address", new JsonObject()
        .put("street", "123 Main St")
        .put("city", "New York"));

// 读取为 JsonifiableEntity
Address address = person.readJsonifiableEntity(Address.class, "address");

// 使用 Jackson 读取普通 POJO
AddressPojo addressPojo = person.readEntity(AddressPojo.class, "address");
```

## 版本历史

- **1.14**: 初始版本
- **2.7**: 添加读取方法和 JsonPointer 支持
- **2.8**: 
  - 扩展 ClusterSerializable 支持 EventBus 消息传递
  - 扩展 Shareable 支持 LocalMap
  - 扩展 Iterable 支持 FOREACH
  - 添加 Buffer 操作方法
  - 改进异常处理（ClassCastException 返回 null）
- **3.0.0**: 引入 UnmodifiableJsonifiableEntity
- **3.1.10**: 方法重构和移动
- **3.2.11**: 添加静态包装方法
- **4.0.12**: 
  - 扩展 SelfInterface
  - 添加 ensure 方法
  - 添加 write 方法
- **4.0.13**: 添加 readEntity 方法支持 Jackson

## 最佳实践

1. **类型安全**: 优先使用类型安全的读取方法而不是通用的 `readValue`
2. **空值处理**: 所有读取方法都可能返回 null，需要进行空值检查
3. **链式调用**: 利用 `write` 方法的链式调用特性提高代码可读性
4. **异常处理**: 读取方法在类型转换失败时返回 null 而不是抛出异常
5. **性能考虑**: 对于频繁访问的数据，考虑缓存读取结果
6. **继承实现**: 对于复杂实体，建议继承 `JsonifiableEntityImpl` 而不是直接实现接口

## 注意事项

1. 所有读取操作都是基于当前的 `toJsonObject()` 结果
2. `reloadDataFromJsonObject` 会完全替换内部的 JsonObject
3. 实现类需要正确实现 `getImplementation()` 方法以支持链式调用
4. 在集群环境中使用时，确保所有节点都有相同的类定义

