# JSON 处理包文档

## 概述

`io.github.sinri.keel.core.json` 包提供了基于 JSON 对象的数据实体在 Vert.x 体系下基于 Jackson 的 JSON 类的上层封装。主要理念是，不定义 fields，将具体的字段存储交给底层的 JSON 对象，通过定义相关 getter 和 setter 来实现读写具体内容，同时通过通用读写兼容未事先定义的 field。

## 设计理念

### 核心思想

1. **数据驱动**：不预定义字段，通过 JSON 对象动态存储数据
2. **类型安全**：提供类型安全的读取和写入方法
3. **灵活性**：支持动态字段和嵌套结构
4. **性能优化**：基于 Vert.x 的 JsonObject 提供高性能操作
5. **集群支持**：支持 EventBus 消息传递和集群序列化

### 架构层次

```
JsonSerializable (基础序列化接口)
    ↑
JsonObjectConvertible (JSON 对象转换)
    ↑
JsonObjectReadable (只读访问) ← JsonObjectWritable (读写访问)
    ↑
UnmodifiableJsonifiableEntity (不可变实体) ← JsonifiableEntity (可变实体)
    ↑
具体实现类
```

## 接口体系

### 基础接口

#### JsonSerializable
- **功能**：提供基本的 JSON 序列化能力
- **核心方法**：`toJsonExpression()`, `toFormattedJsonExpression()`, `toString()`
- **设计目标**：统一的 JSON 字符串表示标准

#### JsonObjectConvertible
- **功能**：将对象转换为 Vert.x JsonObject
- **核心方法**：`toJsonObject()`
- **设计目标**：提供 JSON 对象转换能力

#### JsonObjectReadable
- **功能**：从 JSON 对象中读取数据
- **核心方法**：`read()`, `readString()`, `readInteger()` 等
- **设计目标**：提供丰富的类型安全读取方法

#### JsonObjectWritable
- **功能**：向 JSON 对象写入数据
- **核心方法**：`ensureEntry()`, `ensureJsonObject()`, `ensureJsonArray()`
- **设计目标**：提供便捷的数据写入能力

#### JsonObjectReloadable
- **功能**：从 JSON 对象重新加载数据
- **核心方法**：`reloadData()`
- **设计目标**：支持数据更新和外部数据源集成

### 高级接口

#### UnmodifiableJsonifiableEntity
- **功能**：不可变的 JSON 实体接口
- **继承**：JsonObjectReadable, JsonSerializable, Shareable
- **设计目标**：提供只读、线程安全的 JSON 数据访问

#### JsonifiableEntity<E>
- **功能**：可变的 JSON 实体接口
- **继承**：JsonObjectConvertible, JsonObjectReloadable, JsonObjectWritable, UnmodifiableJsonifiableEntity, ClusterSerializable, SelfInterface<E>
- **设计目标**：提供完整的 JSON 实体功能，支持链式调用

#### JsonifiableDataUnit
- **功能**：非泛型的 JSON 数据单元接口
- **继承**：JsonObjectConvertible, JsonObjectReloadable, JsonObjectWritable, UnmodifiableJsonifiableEntity, ClusterSerializable
- **设计目标**：避免泛型复杂性，提供简单的数据单元实现

## 实现类

### 抽象实现类

#### JsonifiableEntityImpl<E>
- **功能**：JsonifiableEntity 的抽象实现
- **设计模式**：模板方法模式
- **核心特性**：提供标准的 JSON 操作实现，支持子类扩展

#### UnmodifiableJsonifiableEntityImpl
- **功能**：UnmodifiableJsonifiableEntity 的实现类
- **设计模式**：装饰器模式
- **核心特性**：提供只读包装器，支持数据净化

### 具体实现类

#### JsonifiableDataUnitImpl
- **功能**：JsonifiableDataUnit 的默认实现
- **设计模式**：组合模式
- **核心特性**：简单、完整的数据单元实现

#### SimpleJsonifiableEntity ⚠️ 已弃用
- **功能**：JsonifiableEntity 的简单实现
- **状态**：自 4.1.0 版本起已弃用
- **建议**：迁移到具体的业务实体类

### 工具类

#### JsonifiableSerializer
- **功能**：Jackson Databind 序列化器
- **设计目标**：为 JsonSerializable 接口提供 Jackson 序列化支持
- **核心特性**：统一序列化标准，避免循环引用问题

#### JsonifiedThrowable
- **功能**：异常 JSON 化工具
- **设计目标**：将 Java 异常转换为 JSON 格式
- **核心特性**：完整的异常信息捕获，支持异常链序列化

## 使用指南

### 选择合适的基础类

#### 1. 需要泛型支持和链式调用
```java
public class User extends JsonifiableEntityImpl<User> {
    @Override
    public User getImplementation() {
        return this;
    }
    
    public String getName() {
        return readString("name");
    }
    
    public User setName(String name) {
        ensureEntry("name", name);
        return this;
    }
}
```

#### 2. 不需要泛型，只需要基本功能
```java
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
    
    public String getName() {
        return delegate.readString("name");
    }
}
```

#### 3. 只读场景
```java
JsonObject userData = new JsonObject()
    .put("name", "张三")
    .put("age", 25);

UnmodifiableJsonifiableEntity user = UnmodifiableJsonifiableEntity.wrap(userData);
String name = user.readString("name");
```

### 序列化配置

```java
// 在应用启动时注册序列化器
JsonifiableSerializer.register();

// 使用 Jackson 序列化
ObjectMapper mapper = new ObjectMapper();
String json = mapper.writeValueAsString(user);
```

### 集群序列化

```java
// 序列化到 Buffer
Buffer buffer = Buffer.buffer();
user.writeToBuffer(buffer);

// 从 Buffer 反序列化
User newUser = new User();
newUser.readFromBuffer(0, buffer);
```

## 最佳实践

### 1. 类型安全
- 为常用字段提供类型安全的 getter/setter 方法
- 使用 `readString()`, `readInteger()` 等类型安全方法
- 避免直接使用 `readValue()` 方法

### 2. 数据验证
- 在 setter 方法中添加数据验证逻辑
- 使用 `readStringRequired()` 等方法确保必需字段存在
- 提供合理的默认值处理

### 3. 性能优化
- 对于不可变对象，考虑缓存序列化结果
- 使用 `UnmodifiableJsonifiableEntity` 进行只读操作
- 避免频繁创建大型 JSON 对象

### 4. 错误处理
- 合理处理类型转换错误
- 提供降级策略
- 记录序列化错误以便调试

### 5. 线程安全
- 默认实现不是线程安全的
- 多线程环境下需要适当的同步
- 考虑使用不可变对象

## 迁移指南

### 从 SimpleJsonifiableEntity 迁移

1. **创建具体实体类**：继承 `JsonifiableEntityImpl<E>`
2. **实现类型安全方法**：提供 getter/setter 方法
3. **更新调用代码**：使用新的实体类
4. **测试验证**：确保功能正确

### 从旧版本升级

1. **注册序列化器**：调用 `JsonifiableSerializer.register()`
2. **更新弃用类**：替换 `SimpleJsonifiableEntity`
3. **检查兼容性**：确保所有功能正常工作

## 相关文档

### 接口文档
- [JsonSerializable](JsonSerializable.md) - 基础序列化接口
- [JsonObjectConvertible](JsonObjectConvertible.md) - JSON 对象转换
- [JsonObjectReadable](JsonObjectReadable.md) - 只读访问接口
- [JsonObjectWritable](JsonObjectWritable.md) - 读写访问接口
- [JsonObjectReloadable](JsonObjectReloadable.md) - 数据重载接口
- [UnmodifiableJsonifiableEntity](UnmodifiableJsonifiableEntity.md) - 不可变实体接口
- [JsonifiableEntity](JsonifiableEntity.md) - 可变实体接口
- [JsonifiableDataUnit](JsonifiableDataUnit.md) - 数据单元接口

### 实现类文档
- [JsonifiableEntityImpl](JsonifiableEntityImpl.md) - 抽象实现类
- [UnmodifiableJsonifiableEntityImpl](UnmodifiableJsonifiableEntityImpl.md) - 只读实现类
- [JsonifiableDataUnitImpl](JsonifiableDataUnitImpl.md) - 数据单元实现
- [SimpleJsonifiableEntity](SimpleJsonifiableEntity.md) - 已弃用的简单实现

### 工具类文档
- [JsonifiableSerializer](JsonifiableSerializer.md) - Jackson 序列化器
- [JsonifiedThrowable](JsonifiedThrowable.md) - 异常 JSON 化工具

### 规范文档
- [IETF-RFC-6901](IETF-RFC-6901.md) - JSON Pointer 规范

## 版本信息

- **当前版本**：4.1.1
- **最低 Java 版本**：11
- **Vert.x 版本**：4.x
- **Jackson 版本**：2.x
- **向后兼容性**：完全向后兼容
- **弃用警告**：SimpleJsonifiableEntity 已弃用

