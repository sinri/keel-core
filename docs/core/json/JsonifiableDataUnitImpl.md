# JsonifiableDataUnitImpl 类文档

## 概述

`JsonifiableDataUnitImpl` 是 [JsonifiableDataUnit](JsonifiableDataUnit.md) 接口的默认实现类，提供了一个简单而完整的数据单元实现。它避免了泛型类型的复杂性，为那些不需要泛型支持的数据单元提供了标准化的 JSON 处理能力。

## 设计思想

### 简单性原则

[JsonifiableDataUnitImpl](src/main/java/io/github/sinri/keel/core/json/JsonifiableDataUnitImpl.java#L12-L32) 的设计遵循简单性原则，专注于提供核心功能而不引入复杂性：

1. **无泛型约束**：避免了泛型类型参数的复杂性
2. **直接实现**：直接实现所有接口方法，无需抽象层
3. **最小化依赖**：只依赖 Vert.x 的 JsonObject

### 组合优于继承

该类采用了组合模式，将 JsonObject 作为内部组件：

```java
@Nonnull
private JsonObject jsonObject;
```

这种设计提供了：
- **灵活性**：可以轻松替换内部实现
- **封装性**：内部状态对外部不可见
- **可测试性**：便于单元测试和模拟

### 接口实现完整性

类完整实现了 [JsonifiableDataUnit](JsonifiableDataUnit.md) 接口的所有要求：

- **JsonObjectConvertible**：提供 `toJsonObject()` 方法
- **JsonObjectReloadable**：提供 `reloadData()` 方法
- **JsonObjectWritable**：提供写入能力（通过默认实现）
- **UnmodifiableJsonifiableEntity**：提供读取能力（通过默认实现）
- **ClusterSerializable**：提供集群序列化能力（通过默认实现）

## 核心方法

### 构造函数

```java
public JsonifiableDataUnitImpl(@Nonnull JsonObject jsonObject)
public JsonifiableDataUnitImpl()
```

- **功能**：初始化数据单元
- **参数**：可选的初始 JsonObject
- **设计考虑**：提供灵活性，支持从现有数据创建或创建新实例

### toJsonObject()

```java
@Nonnull
@Override
public JsonObject toJsonObject()
```

- **功能**：返回内部 JsonObject 的引用
- **返回值**：非空的 JsonObject 实例
- **设计考虑**：直接返回引用以提高性能

### reloadData()

```java
@Override
public void reloadData(@Nonnull JsonObject jsonObject)
```

- **功能**：使用新的 JsonObject 重新加载数据
- **参数**：包含新数据的 JsonObject
- **设计考虑**：支持数据更新和外部数据源集成

## 使用方法指导

### 基本使用

```java
// 创建空的数据单元
JsonifiableDataUnit dataUnit = new JsonifiableDataUnitImpl();

// 添加数据
dataUnit.ensureEntry("name", "张三");
dataUnit.ensureEntry("age", 25);
dataUnit.ensureEntry("email", "zhangsan@example.com");

// 读取数据
String name = dataUnit.readString("name");
Integer age = dataUnit.readInteger("age");
String email = dataUnit.readString("email");

// 转换为 JsonObject
JsonObject json = dataUnit.toJsonObject();
```

### 从现有数据创建

```java
// 从现有 JsonObject 创建
JsonObject existingData = new JsonObject()
    .put("id", "12345")
    .put("status", "active")
    .put("createdAt", Instant.now().toString());

JsonifiableDataUnit dataUnit = new JsonifiableDataUnitImpl(existingData);

// 读取现有数据
String id = dataUnit.readString("id");
String status = dataUnit.readString("status");
```

### 复杂数据结构操作

```java
JsonifiableDataUnit user = new JsonifiableDataUnitImpl();

// 基本属性
user.ensureEntry("name", "张三");
user.ensureEntry("age", 25);

// 嵌套对象
JsonObject address = user.ensureJsonObject("address");
address.put("city", "北京");
address.put("district", "朝阳区");
address.put("street", "建国路");

// 数组
JsonArray hobbies = user.ensureJsonArray("hobbies");
hobbies.add("读书");
hobbies.add("游泳");
hobbies.add("编程");

// 读取嵌套数据
String city = user.readString("address", "city");
List<String> userHobbies = user.readStringArray("hobbies");
```

### 数据更新和重载

```java
JsonifiableDataUnit config = new JsonifiableDataUnitImpl();

// 初始配置
config.ensureEntry("appName", "MyApp");
config.ensureEntry("version", "1.0.0");
config.ensureEntry("debug", false);

// 从外部数据源更新配置
JsonObject newConfig = new JsonObject()
    .put("appName", "MyApp")
    .put("version", "1.1.0")
    .put("debug", true)
    .put("port", 8080);

config.reloadData(newConfig);

// 读取更新后的配置
String version = config.readString("version");
Boolean debug = config.readBoolean("debug");
Integer port = config.readInteger("port");
```

### 集群序列化支持

```java
JsonifiableDataUnit message = new JsonifiableDataUnitImpl();
message.ensureEntry("type", "notification");
message.ensureEntry("content", "Hello World");
message.ensureEntry("timestamp", System.currentTimeMillis());

// 序列化到 Buffer（用于 EventBus 传输）
Buffer buffer = Buffer.buffer();
message.writeToBuffer(buffer);

// 从 Buffer 反序列化
JsonifiableDataUnit receivedMessage = new JsonifiableDataUnitImpl();
int pos = receivedMessage.readFromBuffer(0, buffer);

// 读取消息内容
String type = receivedMessage.readString("type");
String content = receivedMessage.readString("content");
```

## 最佳实践

### 1. 工厂方法模式

```java
public class DataUnitFactory {
    
    public static JsonifiableDataUnit createUser(String name, int age, String email) {
        JsonifiableDataUnit user = new JsonifiableDataUnitImpl();
        user.ensureEntry("name", name);
        user.ensureEntry("age", age);
        user.ensureEntry("email", email);
        user.ensureEntry("createdAt", Instant.now().toString());
        return user;
    }
    
    public static JsonifiableDataUnit createOrder(String orderId, BigDecimal amount) {
        JsonifiableDataUnit order = new JsonifiableDataUnitImpl();
        order.ensureEntry("orderId", orderId);
        order.ensureEntry("amount", amount.toString());
        order.ensureEntry("status", "pending");
        order.ensureEntry("createdAt", Instant.now().toString());
        return order;
    }
}
```

### 2. 数据验证

```java
public class ValidatedDataUnit extends JsonifiableDataUnitImpl {
    
    public ValidatedDataUnit() {
        super();
    }
    
    public ValidatedDataUnit(JsonObject jsonObject) {
        super(jsonObject);
    }
    
    public void setEmail(String email) {
        if (email != null && !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("邮箱格式不正确");
        }
        ensureEntry("email", email);
    }
    
    public void setAge(Integer age) {
        if (age != null && (age < 0 || age > 150)) {
            throw new IllegalArgumentException("年龄必须在 0-150 之间");
        }
        ensureEntry("age", age);
    }
}
```

### 3. 类型安全的扩展

```java
public class TypedDataUnit extends JsonifiableDataUnitImpl {
    
    public TypedDataUnit() {
        super();
    }
    
    public TypedDataUnit(JsonObject jsonObject) {
        super(jsonObject);
    }
    
    public String getName() {
        return readString("name");
    }
    
    public void setName(String name) {
        ensureEntry("name", name);
    }
    
    public LocalDateTime getCreatedAt() {
        String timestamp = readString("createdAt");
        return timestamp != null ? LocalDateTime.parse(timestamp) : null;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        ensureEntry("createdAt", createdAt != null ? createdAt.toString() : null);
    }
    
    public List<String> getTags() {
        return readStringArray("tags");
    }
    
    public void setTags(List<String> tags) {
        ensureEntry("tags", tags);
    }
}
```

### 4. 缓存和性能优化

```java
public class CachedDataUnit extends JsonifiableDataUnitImpl {
    private transient String cachedJsonString;
    private transient JsonObject cachedJsonObject;
    
    public CachedDataUnit() {
        super();
    }
    
    public CachedDataUnit(JsonObject jsonObject) {
        super(jsonObject);
    }
    
    @Override
    public JsonObject toJsonObject() {
        if (cachedJsonObject == null) {
            cachedJsonObject = super.toJsonObject();
        }
        return cachedJsonObject;
    }
    
    @Override
    public String toJsonExpression() {
        if (cachedJsonString == null) {
            cachedJsonString = super.toJsonExpression();
        }
        return cachedJsonString;
    }
    
    @Override
    public void reloadData(@Nonnull JsonObject jsonObject) {
        super.reloadData(jsonObject);
        // 清除缓存
        this.cachedJsonString = null;
        this.cachedJsonObject = null;
    }
}
```

### 5. 事件驱动的数据单元

```java
public class ObservableDataUnit extends JsonifiableDataUnitImpl {
    private final List<Consumer<JsonObject>> listeners = new ArrayList<>();
    
    public ObservableDataUnit() {
        super();
    }
    
    public ObservableDataUnit(JsonObject jsonObject) {
        super(jsonObject);
    }
    
    public void addChangeListener(Consumer<JsonObject> listener) {
        listeners.add(listener);
    }
    
    @Override
    public void ensureEntry(String key, Object value) {
        super.ensureEntry(key, value);
        notifyListeners();
    }
    
    @Override
    public void reloadData(@Nonnull JsonObject jsonObject) {
        super.reloadData(jsonObject);
        notifyListeners();
    }
    
    private void notifyListeners() {
        JsonObject currentData = toJsonObject();
        listeners.forEach(listener -> listener.accept(currentData));
    }
}
```

## 使用场景

### 1. 配置管理

```java
public class ConfigurationManager {
    private JsonifiableDataUnit config;
    
    public ConfigurationManager() {
        this.config = new JsonifiableDataUnitImpl();
    }
    
    public void loadConfig(JsonObject configData) {
        config.reloadData(configData);
    }
    
    public String getAppName() {
        return config.readString("appName");
    }
    
    public Integer getPort() {
        return config.readInteger("port", 8080);
    }
    
    public Boolean isDebug() {
        return config.readBoolean("debug", false);
    }
}
```

### 2. 消息传递

```java
public class MessageProcessor {
    
    public JsonifiableDataUnit createMessage(String type, String content) {
        JsonifiableDataUnit message = new JsonifiableDataUnitImpl();
        message.ensureEntry("type", type);
        message.ensureEntry("content", content);
        message.ensureEntry("timestamp", System.currentTimeMillis());
        message.ensureEntry("id", UUID.randomUUID().toString());
        return message;
    }
    
    public void processMessage(JsonifiableDataUnit message) {
        String type = message.readString("type");
        String content = message.readString("content");
        Long timestamp = message.readLong("timestamp");
        
        // 处理消息逻辑
        System.out.println("处理消息: " + type + " - " + content);
    }
}
```

### 3. 数据转换

```java
public class DataConverter {
    
    public static JsonifiableDataUnit fromMap(Map<String, Object> map) {
        JsonObject jsonObject = new JsonObject(map);
        return new JsonifiableDataUnitImpl(jsonObject);
    }
    
    public static Map<String, Object> toMap(JsonifiableDataUnit dataUnit) {
        Map<String, Object> map = new HashMap<>();
        for (Map.Entry<String, Object> entry : dataUnit) {
            map.put(entry.getKey(), entry.getValue());
        }
        return map;
    }
}
```

## 注意事项

1. **线程安全**：默认实现不是线程安全的，多线程环境下需要适当的同步
2. **内存管理**：直接返回内部 JsonObject 引用，调用者应避免修改
3. **性能考虑**：频繁的 JSON 操作可能影响性能，考虑缓存机制
4. **数据一致性**：确保业务逻辑与 JSON 数据的一致性

## 相关接口

- [JsonifiableDataUnit](JsonifiableDataUnit.md)：主要接口定义
- [JsonObjectConvertible](JsonObjectConvertible.md)：JSON 转换能力
- [JsonObjectReloadable](JsonObjectReloadable.md)：数据重载能力
- [JsonObjectWritable](JsonObjectWritable.md)：数据写入能力
- [UnmodifiableJsonifiableEntity](UnmodifiableJsonifiableEntity.md)：只读访问能力 