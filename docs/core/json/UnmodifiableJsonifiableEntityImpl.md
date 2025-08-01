# UnmodifiableJsonifiableEntityImpl 类文档

## 概述

`UnmodifiableJsonifiableEntityImpl` 是 [UnmodifiableJsonifiableEntity](UnmodifiableJsonifiableEntity.md) 接口的实现类，提供了一个围绕 JsonObject 的只读包装器。该类确保底层 JSON 对象不能被修改，提供了安全且不可变的 JSON 数据表示。

## 设计思想

### 不可变性设计

[UnmodifiableJsonifiableEntityImpl](src/main/java/io/github/sinri/keel/core/json/UnmodifiableJsonifiableEntityImpl.java#L25-L110) 的核心设计理念是**不可变性**，通过以下机制实现：

1. **内部状态不可变**：使用 `final` 修饰内部 JsonObject
2. **无修改方法**：不提供任何修改内部数据的方法
3. **防御性复制**：在构造函数中进行数据净化

```java
private final @Nonnull JsonObject jsonObject;
```

这种设计确保了：
- **数据安全**：外部无法修改内部数据
- **线程安全**：不可变对象天然线程安全
- **缓存友好**：不可变对象可以安全缓存

### 数据净化机制

类提供了 `purify()` 方法用于数据净化：

```java
protected JsonObject purify(JsonObject raw) {
    return raw;
}
```

这个方法允许子类：
- **数据清理**：移除敏感字段
- **数据转换**：转换数据格式
- **数据验证**：验证数据完整性
- **防御性复制**：创建数据副本

### 装饰器模式应用

该类采用了装饰器模式，在不修改原始 JsonObject 的情况下，为其添加了只读访问能力：

- **原始功能保留**：保持 JsonObject 的所有读取功能
- **安全增强**：移除所有修改功能
- **接口扩展**：提供额外的只读操作接口

## 核心方法

### 构造函数

```java
public UnmodifiableJsonifiableEntityImpl(@Nonnull JsonObject jsonObject)
```

- **功能**：创建只读包装器
- **参数**：要包装的 JsonObject
- **设计考虑**：通过 `purify()` 方法进行数据净化

### purify() 方法

```java
protected JsonObject purify(JsonObject raw)
```

- **功能**：净化原始 JsonObject
- **参数**：原始 JsonObject
- **返回值**：净化后的 JsonObject
- **设计考虑**：子类可以重写此方法实现自定义净化逻辑

### toJsonExpression() 和 toString()

```java
@Override
public final String toJsonExpression()
@Override
public final String toString()
```

- **功能**：将对象转换为 JSON 字符串
- **设计考虑**：确保两个方法返回相同内容，标记为 `final` 防止子类修改

### read() 方法

```java
@Override
public @Nullable <T> T read(@Nonnull Function<JsonPointer, Class<T>> func)
```

- **功能**：使用 JSON Pointer 读取指定类型的数据
- **参数**：构建 JSON Pointer 并指定返回类型的函数
- **返回值**：读取到的值，如果未找到或类型转换失败则返回 null
- **异常处理**：捕获 ClassCastException 并返回 null

### toBuffer() 方法

```java
@Override
public Buffer toBuffer()
```

- **功能**：将对象序列化为 Buffer
- **返回值**：包含序列化数据的 Buffer
- **设计考虑**：支持集群序列化

### iterator() 方法

```java
@Override
public Iterator<Map.Entry<String, Object>> iterator()
```

- **功能**：提供键值对的迭代器
- **返回值**：JsonObject 的迭代器
- **设计考虑**：支持 foreach 循环

### isEmpty() 方法

```java
@Override
public boolean isEmpty()
```

- **功能**：检查对象是否为空
- **返回值**：如果内部 JsonObject 为空则返回 true

### copy() 方法

```java
@Override
public UnmodifiableJsonifiableEntityImpl copy()
```

- **功能**：创建当前对象的深拷贝
- **返回值**：新的 UnmodifiableJsonifiableEntityImpl 实例
- **设计考虑**：通过 `cloneAsJsonObject()` 创建深拷贝

## 使用方法指导

### 基本使用

```java
// 创建只读包装器
JsonObject originalData = new JsonObject()
    .put("name", "张三")
    .put("age", 25);

UnmodifiableJsonifiableEntityImpl wrapper = 
    new UnmodifiableJsonifiableEntityImpl(originalData);

// 读取数据
String name = wrapper.readString("name");
Integer age = wrapper.readInteger("age");

// 转换为 JSON 字符串
String jsonString = wrapper.toJsonExpression();

// 迭代键值对
for (Map.Entry<String, Object> entry : wrapper) {
    System.out.println(entry.getKey() + ": " + entry.getValue());
}
```

### 自定义净化逻辑

```java
public class SanitizedUserWrapper extends UnmodifiableJsonifiableEntityImpl {
    
    public SanitizedUserWrapper(JsonObject jsonObject) {
        super(jsonObject);
    }
    
    @Override
    protected JsonObject purify(JsonObject raw) {
        // 移除敏感字段
        JsonObject sanitized = raw.copy();
        sanitized.remove("password");
        sanitized.remove("creditCard");
        
        // 添加审计信息
        sanitized.put("sanitizedAt", Instant.now().toString());
        
        return sanitized;
    }
}

// 使用示例
JsonObject userData = new JsonObject()
    .put("name", "张三")
    .put("email", "zhangsan@example.com")
    .put("password", "secret123")
    .put("creditCard", "1234-5678-9012-3456");

SanitizedUserWrapper safeUser = new SanitizedUserWrapper(userData);
// 现在 safeUser 不包含密码和信用卡信息
```

### 数据验证包装器

```java
public class ValidatedConfigWrapper extends UnmodifiableJsonifiableEntityImpl {
    
    public ValidatedConfigWrapper(JsonObject jsonObject) {
        super(jsonObject);
    }
    
    @Override
    protected JsonObject purify(JsonObject raw) {
        // 验证必需字段
        if (!raw.containsKey("appName")) {
            throw new IllegalArgumentException("缺少必需字段: appName");
        }
        
        if (!raw.containsKey("version")) {
            throw new IllegalArgumentException("缺少必需字段: version");
        }
        
        // 设置默认值
        JsonObject validated = raw.copy();
        if (!validated.containsKey("debug")) {
            validated.put("debug", false);
        }
        
        if (!validated.containsKey("port")) {
            validated.put("port", 8080);
        }
        
        return validated;
    }
}
```

### 缓存友好的使用

```java
public class ConfigurationCache {
    private final Map<String, UnmodifiableJsonifiableEntityImpl> cache = 
        new ConcurrentHashMap<>();
    
    public UnmodifiableJsonifiableEntityImpl getConfig(String configKey) {
        return cache.computeIfAbsent(configKey, this::loadConfig);
    }
    
    private UnmodifiableJsonifiableEntityImpl loadConfig(String key) {
        // 从数据库或文件加载配置
        JsonObject configData = loadConfigFromSource(key);
        return new UnmodifiableJsonifiableEntityImpl(configData);
    }
}
```

### 线程安全的数据共享

```java
public class SharedDataManager {
    private volatile UnmodifiableJsonifiableEntityImpl sharedData;
    
    public void updateSharedData(JsonObject newData) {
        // 创建新的不可变包装器
        this.sharedData = new UnmodifiableJsonifiableEntityImpl(newData);
    }
    
    public UnmodifiableJsonifiableEntityImpl getSharedData() {
        return sharedData;
    }
    
    // 多个线程可以安全地读取数据
    public void processDataInThread() {
        UnmodifiableJsonifiableEntityImpl data = getSharedData();
        String value = data.readString("someKey");
        // 安全地处理数据，无需同步
    }
}
```

## 最佳实践

### 1. 数据净化策略

```java
public class DataSanitizer {
    
    public static UnmodifiableJsonifiableEntityImpl sanitizeForLogging(JsonObject data) {
        return new UnmodifiableJsonifiableEntityImpl(data) {
            @Override
            protected JsonObject purify(JsonObject raw) {
                JsonObject sanitized = raw.copy();
                
                // 移除敏感字段
                Arrays.asList("password", "token", "secret", "key")
                    .forEach(sanitized::remove);
                
                // 脱敏处理
                if (sanitized.containsKey("phone")) {
                    String phone = sanitized.getString("phone");
                    if (phone != null && phone.length() >= 4) {
                        sanitized.put("phone", phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4));
                    }
                }
                
                return sanitized;
            }
        };
    }
}
```

### 2. 性能优化

```java
public class OptimizedWrapper extends UnmodifiableJsonifiableEntityImpl {
    private transient String cachedString; // 缓存 JSON 字符串
    
    public OptimizedWrapper(JsonObject jsonObject) {
        super(jsonObject);
    }
    
    @Override
    public String toJsonExpression() {
        if (cachedString == null) {
            cachedString = super.toJsonExpression();
        }
        return cachedString;
    }
}
```

### 3. 类型安全的扩展

```java
public class TypedWrapper extends UnmodifiableJsonifiableEntityImpl {
    
    public TypedWrapper(JsonObject jsonObject) {
        super(jsonObject);
    }
    
    public String getName() {
        return readString("name");
    }
    
    public Integer getAge() {
        return readInteger("age");
    }
    
    public LocalDateTime getCreatedAt() {
        String timestamp = readString("createdAt");
        return timestamp != null ? LocalDateTime.parse(timestamp) : null;
    }
    
    public List<String> getTags() {
        return readStringArray("tags");
    }
}
```

## 注意事项

1. **内存使用**：不可变对象可能增加内存使用，但提供了更好的安全性
2. **性能考虑**：频繁创建包装器可能影响性能，考虑对象池或缓存
3. **数据一致性**：确保净化逻辑不会破坏数据完整性
4. **异常处理**：在 `purify()` 方法中合理处理异常情况

## 相关接口

- [UnmodifiableJsonifiableEntity](UnmodifiableJsonifiableEntity.md)：主要接口定义
- [JsonObjectReadable](JsonObjectReadable.md)：JSON 读取能力
- [JsonSerializable](JsonSerializable.md)：JSON 序列化能力
- [Shareable](https://vertx.io/docs/apidocs/io/vertx/core/shareddata/Shareable.html)：共享数据能力 