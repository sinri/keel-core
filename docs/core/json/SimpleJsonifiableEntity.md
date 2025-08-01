# SimpleJsonifiableEntity 类文档

## 概述

`SimpleJsonifiableEntity` 是 [JsonifiableEntity](JsonifiableEntity.md) 接口的一个简单实现类，提供了将实体与 JSON 对象相互转换的基本功能。**该类自 4.1.0 版本起已被标记为弃用**，不建议在新代码中使用。

## 弃用状态

### 弃用原因

[SimpleJsonifiableEntity](src/main/java/io/github/sinri/keel/core/json/SimpleJsonifiableEntity.java#L12-62) 被弃用的主要原因包括：

1. **过度通用性**：该类过于通用，缺乏具体的业务语义
2. **类型安全不足**：没有提供类型安全的字段访问方法
3. **维护困难**：通用实现难以满足特定业务需求
4. **性能考虑**：通用实现可能带来不必要的性能开销
5. **设计演进**：框架设计向更具体、更类型安全的实现演进

### 弃用时间表

- **4.1.0**：标记为弃用，添加 `@Deprecated` 注解
- **未来版本**：计划在后续版本中移除该类
- **迁移建议**：建议立即迁移到自定义实现类

## 设计思想

### 通用实现模式

该类采用了通用的实现模式，为 JSON 实体提供了基础功能：

```java
public class SimpleJsonifiableEntity extends JsonifiableEntityImpl<SimpleJsonifiableEntity>
```

这种设计提供了：
- **快速原型**：可以快速创建 JSON 实体原型
- **基础功能**：提供标准的 JSON 操作能力
- **学习示例**：作为学习 JSON 实体实现的示例

### 继承结构

```
JsonifiableEntity<E> (interface)
    ↑
JsonifiableEntityImpl<E> (abstract class)
    ↑
SimpleJsonifiableEntity (concrete class) ⚠️ 已弃用
```

## 核心方法

### 构造函数

```java
public SimpleJsonifiableEntity()
public SimpleJsonifiableEntity(@Nonnull JsonObject jsonObject)
```

- **功能**：创建简单 JSON 实体实例
- **参数**：可选的初始 JsonObject
- **设计考虑**：提供灵活的初始化选项

### copy() 方法

```java
@Override
public SimpleJsonifiableEntity copy()
```

- **功能**：创建当前对象的深拷贝
- **返回值**：新的 SimpleJsonifiableEntity 实例
- **设计考虑**：通过 `cloneAsJsonObject()` 创建深拷贝

### getImplementation() 方法

```java
@Nonnull
@Override
public SimpleJsonifiableEntity getImplementation()
```

- **功能**：返回当前实例（支持链式调用）
- **返回值**：当前实例的引用
- **设计考虑**：实现 SelfInterface 接口要求

## 迁移指南

### 从 SimpleJsonifiableEntity 迁移

#### 1. 创建具体的业务实体类

```java
// 旧代码（不推荐）
SimpleJsonifiableEntity user = new SimpleJsonifiableEntity();
user.ensureEntry("name", "张三");
user.ensureEntry("age", 25);

// 新代码（推荐）
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
    
    // 类型安全的 getter/setter 方法
    public String getName() {
        return readString("name");
    }
    
    public void setName(String name) {
        ensureEntry("name", name);
    }
    
    public Integer getAge() {
        return readInteger("age");
    }
    
    public void setAge(Integer age) {
        ensureEntry("age", age);
    }
}

// 使用新的实体类
User user = new User();
user.setName("张三");
user.setAge(25);
```

#### 2. 使用 JsonifiableDataUnit 作为替代

```java
// 如果不需要泛型支持，可以使用 JsonifiableDataUnit
public class UserData implements JsonifiableDataUnit {
    private final JsonifiableDataUnitImpl delegate = new JsonifiableDataUnitImpl();
    
    @Override
    public JsonObject toJsonObject() {
        return delegate.toJsonObject();
    }
    
    @Override
    public void reloadData(@Nonnull JsonObject jsonObject) {
        delegate.reloadData(jsonObject);
    }
    
    // 类型安全的访问方法
    public String getName() {
        return delegate.readString("name");
    }
    
    public void setName(String name) {
        delegate.ensureEntry("name", name);
    }
}
```

#### 3. 使用 UnmodifiableJsonifiableEntity 包装器

```java
// 对于只读场景，使用不可变包装器
JsonObject userData = new JsonObject()
    .put("name", "张三")
    .put("age", 25);

UnmodifiableJsonifiableEntity user = UnmodifiableJsonifiableEntity.wrap(userData);

// 读取数据
String name = user.readString("name");
Integer age = user.readInteger("age");
```

### 迁移步骤

1. **识别使用场景**：分析当前 SimpleJsonifiableEntity 的使用场景
2. **设计具体实体**：根据业务需求设计具体的实体类
3. **实现类型安全方法**：为常用字段提供类型安全的访问方法
4. **更新调用代码**：将使用 SimpleJsonifiableEntity 的代码更新为使用新的实体类
5. **测试验证**：确保迁移后的代码功能正确
6. **移除依赖**：删除对 SimpleJsonifiableEntity 的依赖

## 使用示例（仅用于理解）

### 基本使用（已弃用）

```java
// ⚠️ 已弃用，仅用于理解
SimpleJsonifiableEntity user = new SimpleJsonifiableEntity();

// 设置数据
user.ensureEntry("name", "张三");
user.ensureEntry("age", 25);
user.ensureEntry("email", "zhangsan@example.com");

// 读取数据
String name = user.readString("name");
Integer age = user.readInteger("age");

// 转换为 JSON
JsonObject json = user.toJsonObject();
String jsonString = user.toJsonExpression();

// 从 JSON 重新加载
JsonObject newData = new JsonObject()
    .put("name", "李四")
    .put("age", 30);
user.reloadDataFromJsonObject(newData);
```

### 复杂数据结构（已弃用）

```java
// ⚠️ 已弃用，仅用于理解
SimpleJsonifiableEntity order = new SimpleJsonifiableEntity();

// 基本属性
order.ensureEntry("orderId", "12345");
order.ensureEntry("status", "pending");

// 嵌套对象
JsonObject customer = order.ensureJsonObject("customer");
customer.put("name", "张三");
customer.put("email", "zhangsan@example.com");

// 数组
JsonArray items = order.ensureJsonArray("items");
items.add(new JsonObject().put("productId", "P001").put("quantity", 2));
items.add(new JsonObject().put("productId", "P002").put("quantity", 1));

// 读取嵌套数据
String customerName = order.readString("customer", "name");
List<JsonObject> orderItems = order.readJsonObjectArray("items");
```

## 替代方案

### 1. 具体业务实体类

```java
public class Order extends JsonifiableEntityImpl<Order> {
    
    public Order() {
        super();
    }
    
    public Order(JsonObject jsonObject) {
        super(jsonObject);
    }
    
    @Override
    public Order getImplementation() {
        return this;
    }
    
    // 订单相关方法
    public String getOrderId() {
        return readString("orderId");
    }
    
    public void setOrderId(String orderId) {
        ensureEntry("orderId", orderId);
    }
    
    public String getStatus() {
        return readString("status");
    }
    
    public void setStatus(String status) {
        ensureEntry("status", status);
    }
    
    public Customer getCustomer() {
        return readJsonifiableEntity(Customer.class, "customer");
    }
    
    public void setCustomer(Customer customer) {
        ensureEntry("customer", customer.toJsonObject());
    }
    
    public List<OrderItem> getItems() {
        List<JsonObject> itemObjects = readJsonObjectArray("items");
        if (itemObjects == null) return new ArrayList<>();
        
        return itemObjects.stream()
            .map(itemJson -> new OrderItem(itemJson))
            .collect(Collectors.toList());
    }
    
    public void setItems(List<OrderItem> items) {
        JsonArray itemArray = new JsonArray();
        items.forEach(item -> itemArray.add(item.toJsonObject()));
        ensureEntry("items", itemArray);
    }
}
```

### 2. 使用 JsonifiableDataUnit

```java
public class OrderData implements JsonifiableDataUnit {
    private final JsonifiableDataUnitImpl delegate = new JsonifiableDataUnitImpl();
    
    @Override
    public JsonObject toJsonObject() {
        return delegate.toJsonObject();
    }
    
    @Override
    public void reloadData(@Nonnull JsonObject jsonObject) {
        delegate.reloadData(jsonObject);
    }
    
    // 委托其他方法到 delegate
    @Override
    public <T> T read(@Nonnull Function<JsonPointer, Class<T>> func) {
        return delegate.read(func);
    }
    
    @Override
    public void ensureEntry(String key, Object value) {
        delegate.ensureEntry(key, value);
    }
    
    // 业务方法
    public String getOrderId() {
        return delegate.readString("orderId");
    }
    
    public void setOrderId(String orderId) {
        delegate.ensureEntry("orderId", orderId);
    }
}
```

### 3. 不可变包装器

```java
public class OrderReader {
    private final UnmodifiableJsonifiableEntity order;
    
    public OrderReader(JsonObject orderData) {
        this.order = UnmodifiableJsonifiableEntity.wrap(orderData);
    }
    
    public String getOrderId() {
        return order.readString("orderId");
    }
    
    public String getStatus() {
        return order.readString("status");
    }
    
    public String getCustomerName() {
        return order.readString("customer", "name");
    }
    
    public List<String> getItemProductIds() {
        List<JsonObject> items = order.readJsonObjectArray("items");
        if (items == null) return new ArrayList<>();
        
        return items.stream()
            .map(item -> item.getString("productId"))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }
}
```

## 注意事项

### 1. 迁移优先级

- **高优先级**：生产环境中的新代码
- **中优先级**：开发环境中的代码
- **低优先级**：测试代码和示例代码

### 2. 兼容性考虑

- **渐进式迁移**：可以逐步迁移，不需要一次性替换所有代码
- **向后兼容**：在迁移期间，旧代码仍然可以正常工作
- **测试覆盖**：确保迁移后的代码有充分的测试覆盖

### 3. 性能影响

- **类型安全**：具体实体类提供更好的类型安全
- **编译时检查**：减少运行时错误
- **代码可读性**：提高代码的可读性和可维护性

## 相关接口

- [JsonifiableEntity](JsonifiableEntity.md)：主要接口定义
- [JsonifiableEntityImpl](JsonifiableEntityImpl.md)：推荐的抽象实现类
- [JsonifiableDataUnit](JsonifiableDataUnit.md)：非泛型替代方案
- [UnmodifiableJsonifiableEntity](UnmodifiableJsonifiableEntity.md)：只读包装器
- [JsonObjectConvertible](JsonObjectConvertible.md)：JSON 转换能力 