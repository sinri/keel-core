# JsonifiableEntityImpl 抽象类文档

## 概述

`JsonifiableEntityImpl` 是 [JsonifiableEntity](JsonifiableEntity.md) 接口的抽象实现类，提供了 JSON 实体操作的基础功能。它封装了内部 JsonObject 的管理，为具体的实体类提供了标准化的 JSON 处理能力。

## 设计思想

### 模板方法模式的应用

[JsonifiableEntityImpl](src/main/java/io/github/sinri/keel/core/json/JsonifiableEntityImpl.java#L12-L50) 采用了模板方法模式，将通用的 JSON 操作逻辑固定在抽象类中，而将特定的业务逻辑留给子类实现。这种设计确保了：

1. **代码复用**：所有继承类都自动获得标准的 JSON 操作能力
2. **一致性**：统一的 JSON 处理行为，减少重复代码
3. **扩展性**：子类可以专注于业务逻辑，而不必关心底层的 JSON 操作

### 内部状态管理

类内部维护一个 `JsonObject` 实例作为数据存储，所有 JSON 操作都基于这个内部对象：

```java
private JsonObject jsonObject;
```

这种设计提供了：
- **封装性**：内部状态对外部不可见
- **一致性**：所有操作都基于同一个数据源
- **线程安全**：可以在子类中实现适当的同步机制

### 不可变性保证

关键方法被标记为 `final`，确保核心行为不被子类修改：

```java
@Nonnull
@Override
public final JsonObject toJsonObject() {
    return jsonObject;
}
```

这保证了：
- **行为一致性**：所有子类都具有相同的核心行为
- **安全性**：防止子类意外破坏核心功能
- **性能优化**：编译器可以进行更好的优化

## 核心方法

### 构造函数

```java
public JsonifiableEntityImpl(JsonObject jsonObject)
public JsonifiableEntityImpl()
```

- **功能**：初始化实体，可以传入现有的 JsonObject 或创建新的空对象
- **设计考虑**：提供灵活性，支持从现有数据创建或创建新实例

### toJsonObject()

```java
@Nonnull
@Override
public final JsonObject toJsonObject()
```

- **功能**：返回内部 JsonObject 的引用
- **返回值**：非空的 JsonObject 实例
- **设计考虑**：直接返回引用以提高性能，调用者应避免修改返回的对象

### reloadDataFromJsonObject()

```java
@Nonnull
@Override
public final E reloadDataFromJsonObject(@Nonnull JsonObject jsonObject)
```

- **功能**：使用新的 JsonObject 重新加载实体数据
- **参数**：包含新数据的 JsonObject
- **返回值**：当前实体实例（支持链式调用）
- **设计考虑**：支持数据更新和链式调用模式

### reloadData()

```java
@Override
public final void reloadData(@Nonnull JsonObject jsonObject)
```

- **功能**：重新加载数据的无返回值版本
- **设计考虑**：提供更简洁的接口，符合 [JsonObjectReloadable](JsonObjectReloadable.md) 接口要求

### toJsonExpression() 和 toString()

```java
@Override
public final String toJsonExpression()
@Override
public final String toString()
```

- **功能**：将实体转换为 JSON 字符串表示
- **设计考虑**：确保 toString() 与 toJsonExpression() 返回相同内容

## 使用方法指导

### 基本继承模式

```java
public class User extends JsonifiableEntityImpl<User> {
    
    public User() {
        super();
    }
    
    public User(JsonObject jsonObject) {
        super(jsonObject);
    }
    
    @Override
    public User getImplementation() {
        return this;
    }
    
    // 业务方法
    public String getName() {
        return readString("name");
    }
    
    public void setName(String name) {
        ensureEntry("name", name);
    }
}
```

### 数据操作示例

```java
User user = new User();

// 设置数据
user.setName("张三");
user.ensureEntry("age", 25);

// 读取数据
String name = user.getName();
Integer age = user.readInteger("age");

// 从 JSON 重新加载
JsonObject newData = new JsonObject()
    .put("name", "李四")
    .put("age", 30);
user.reloadDataFromJsonObject(newData);

// 转换为 JSON 字符串
String jsonString = user.toJsonExpression();
```

### 链式调用支持

```java
User user = new User()
    .write("name", "张三")
    .write("age", 25)
    .write("email", "zhangsan@example.com");
```

### 子类扩展建议

1. **业务方法封装**：为常用字段提供类型安全的 getter/setter 方法
2. **数据验证**：在 setter 方法中添加数据验证逻辑
3. **默认值处理**：为可选字段提供合理的默认值
4. **计算属性**：基于现有数据提供计算属性

```java
public class Product extends JsonifiableEntityImpl<Product> {
    
    public String getName() {
        return readString("name");
    }
    
    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("产品名称不能为空");
        }
        ensureEntry("name", name.trim());
    }
    
    public BigDecimal getPrice() {
        return new BigDecimal(readString("price", "0"));
    }
    
    public void setPrice(BigDecimal price) {
        ensureEntry("price", price.toString());
    }
    
    // 计算属性
    public BigDecimal getPriceWithTax() {
        BigDecimal price = getPrice();
        BigDecimal taxRate = new BigDecimal("0.13"); // 13% 税率
        return price.multiply(BigDecimal.ONE.add(taxRate));
    }
}
```

## 最佳实践

### 1. 构造函数设计

```java
// 推荐：提供多个构造函数
public class Order extends JsonifiableEntityImpl<Order> {
    
    public Order() {
        super();
        // 设置默认值
        ensureEntry("status", "pending");
        ensureEntry("createdAt", Instant.now().toString());
    }
    
    public Order(JsonObject jsonObject) {
        super(jsonObject);
    }
    
    public Order(String orderId) {
        super();
        ensureEntry("orderId", orderId);
        ensureEntry("status", "pending");
        ensureEntry("createdAt", Instant.now().toString());
    }
}
```

### 2. 数据验证

```java
public void setEmail(String email) {
    if (email != null && !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
        throw new IllegalArgumentException("邮箱格式不正确");
    }
    ensureEntry("email", email);
}
```

### 3. 类型安全

```java
public LocalDateTime getCreatedAt() {
    String timestamp = readString("createdAt");
    return timestamp != null ? LocalDateTime.parse(timestamp) : null;
}

public void setCreatedAt(LocalDateTime createdAt) {
    ensureEntry("createdAt", createdAt != null ? createdAt.toString() : null);
}
```

### 4. 性能优化

```java
public class CachedUser extends JsonifiableEntityImpl<CachedUser> {
    private transient String cachedName; // 缓存常用字段
    
    public String getName() {
        if (cachedName == null) {
            cachedName = readString("name");
        }
        return cachedName;
    }
    
    public void setName(String name) {
        this.cachedName = name; // 更新缓存
        ensureEntry("name", name);
    }
}
```

## 注意事项

1. **线程安全**：默认实现不是线程安全的，多线程环境下需要适当的同步
2. **内存管理**：直接返回内部 JsonObject 引用，调用者应避免修改
3. **性能考虑**：频繁的 JSON 操作可能影响性能，考虑缓存机制
4. **数据一致性**：确保业务逻辑与 JSON 数据的一致性

## 相关接口

- [JsonifiableEntity](JsonifiableEntity.md)：主要接口定义
- [JsonObjectConvertible](JsonObjectConvertible.md)：JSON 转换能力
- [JsonObjectReloadable](JsonObjectReloadable.md)：数据重载能力
- [JsonObjectWritable](JsonObjectWritable.md)：数据写入能力 