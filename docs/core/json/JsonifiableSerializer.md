# JsonifiableSerializer

## 概述

`JsonifiableSerializer` 是一个专门针对 `UnmodifiableJsonifiableEntity` 及其相关类的 Jackson Databind 序列化器。该类继承自 `JsonSerializer<UnmodifiableJsonifiableEntity>`，提供了自定义的 JSON 序列化逻辑。

## 版本信息

- **引入版本**: 4.1.0
- **包路径**: `io.github.sinri.keel.core.json`

## 类结构

```java
public class JsonifiableSerializer extends JsonSerializer<UnmodifiableJsonifiableEntity>
```

## 主要功能

### 1. 序列化器注册

提供静态方法用于注册序列化器到 Jackson 的 ObjectMapper 中。

#### 方法签名
```java
public static void register()
```

#### 功能说明
- 将 `JsonifiableSerializer` 注册到 Vert.x 的 `DatabindCodec.mapper()` 中
- 通过 `SimpleModule` 添加对 `UnmodifiableJsonifiableEntity` 类的序列化支持
- **重要**: 必须在程序入口处，在 `UnmodifiableJsonifiableEntity` 相关类被使用之前调用此方法

#### 使用示例
```java
// 在应用程序启动时注册序列化器
JsonifiableSerializer.register();
```

### 2. 序列化实现

实现了 Jackson 的序列化接口，定义了如何将 `UnmodifiableJsonifiableEntity` 对象转换为 JSON。

#### 方法签名
```java
@Override
public void serialize(UnmodifiableJsonifiableEntity value, JsonGenerator gen, SerializerProvider serializers) throws IOException
```

#### 参数说明
- `value`: 要序列化的 `UnmodifiableJsonifiableEntity` 对象
- `gen`: Jackson 的 JSON 生成器
- `serializers`: 序列化器提供者

#### 实现细节
- 使用 `gen.writeRaw(value.toString())` 直接写入对象的字符串表示
- 这种实现方式依赖于 `UnmodifiableJsonifiableEntity.toString()` 方法返回有效的 JSON 字符串

## 设计考虑

### 序列化策略
当前实现采用了简单直接的策略：
- 直接使用对象的 `toString()` 方法获取 JSON 字符串表示
- 通过 `writeRaw()` 方法将字符串直接写入输出流

### 注册与未注册的差异

#### 未注册序列化器的情况
当没有调用 `JsonifiableSerializer.register()` 时：
- Jackson 会使用默认的序列化策略处理 `UnmodifiableJsonifiableEntity` 对象
- 默认策略通常会尝试通过反射访问对象的字段和 getter 方法
- 可能会产生不期望的 JSON 结构，例如包含内部实现细节
- 序列化结果可能包含额外的元数据或包装结构
- 性能可能较差，因为需要进行反射操作

#### 注册序列化器后的情况
调用 `JsonifiableSerializer.register()` 后：
- Jackson 会使用自定义的序列化逻辑
- 直接使用对象的 `toString()` 方法获取 JSON 字符串
- 序列化结果与 `UnmodifiableJsonifiableEntity.toString()` 的输出完全一致
- 避免了不必要的反射操作，提高了性能
- 确保了序列化结果的可预测性和一致性

#### 对比示例

以下示例展示了序列化器注册前后的差异，适用于 `UnmodifiableJsonifiableEntity` 及其所有子类（包括 `JsonifiableEntity`）：

```java
// 创建 JsonifiableEntity 实例
SimpleJsonifiableEntity entity = new SimpleJsonifiableEntity();
entity.write("name", "John Doe")
      .write("age", 28)
      .write("department", "Engineering")
      .write("active", true);

// 创建自定义的 JsonifiableEntity 子类实例
class UserEntity extends JsonifiableEntityImpl<UserEntity> {
    public UserEntity() { super(); }
    
    @Override
    public UserEntity getImplementation() { return this; }
}

UserEntity userEntity = new UserEntity();
userEntity.write("userId", 1001)
          .write("email", "john@company.com")
          .write("role", "developer");

ObjectMapper mapper = DatabindCodec.mapper();

// === 未注册序列化器时 ===
// Jackson 使用默认策略，会序列化对象的内部字段和实现细节
System.out.println("=== 未注册序列化器时的输出 ===");

String simpleEntityJson = mapper.writeValueAsString(entity);
System.out.println("SimpleJsonifiableEntity:");
System.out.println(simpleEntityJson);
// 可能的输出（包含内部实现细节）:
// {"jsonObject":{"name":"John Doe","age":28,"department":"Engineering","active":true}}

try {
    String userEntityJson = mapper.writeValueAsString(userEntity);
    System.out.println("UserEntity:");
    System.out.println(userEntityJson);
    // 可能的输出（包含内部实现细节）:
    // {"jsonObject":{"userId":1001,"email":"john@company.com","role":"developer"}}
} catch (Exception e) {
    System.out.println("UserEntity 序列化失败: " + e.getMessage());
    // 常见错误: getImplementation() 方法可能导致循环引用或其他序列化异常
    // 例如: "Could not write JSON: Infinite recursion (StackOverflowError)"
}

// === 注册序列化器后 ===
JsonifiableSerializer.register();
System.out.println("\n=== 注册序列化器后的输出 ===");

String simpleEntityJsonAfter = mapper.writeValueAsString(entity);
System.out.println("SimpleJsonifiableEntity:");
System.out.println(simpleEntityJsonAfter);
// 清晰简洁的输出（与 toString() 一致）:
// {"name":"John Doe","age":28,"department":"Engineering","active":true}

String userEntityJsonAfter = mapper.writeValueAsString(userEntity);
System.out.println("UserEntity:");
System.out.println(userEntityJsonAfter);
// 清晰简洁的输出（与 toString() 一致）:
// {"userId":1001,"email":"john@company.com","role":"developer"}
```

**影响范围说明**：
- 影响所有 `UnmodifiableJsonifiableEntity` 接口的实现类
- 影响所有 `JsonifiableEntity` 及其子类（如 `SimpleJsonifiableEntity`）
- 影响所有继承自 `JsonifiableEntityImpl` 的自定义实体类
- 确保整个 JSON 实体类型层次结构的序列化行为一致性

**序列化问题说明**：
- **未注册时的潜在问题**：自定义实体类（如上例中的 `UserEntity`）在默认序列化过程中可能因为 `getImplementation()` 方法导致循环引用，从而引发 `StackOverflowError` 或其他序列化异常
- **问题原因**：Jackson 默认会尝试序列化对象的所有公共方法返回值，包括 `getImplementation()` 方法，而该方法返回对象自身，造成无限递归
- **解决方案**：注册 `JsonifiableSerializer` 后，Jackson 会跳过默认的反射序列化逻辑，直接使用对象的 `toString()` 方法，避免了循环引用问题

**实际应用场景**：
```java
// 在 Web API 中返回不同类型的 JsonifiableEntity
@RestController
public class ApiController {
    
    @GetMapping("/user/{id}")
    public JsonifiableEntity<?> getUser(@PathVariable Long id) {
        SimpleJsonifiableEntity user = new SimpleJsonifiableEntity();
        user.write("id", id)
            .write("username", "john_doe")
            .write("email", "john@example.com")
            .write("profile", new JsonObject()
                .put("firstName", "John")
                .put("lastName", "Doe"));
        return user;
    }
    
    @GetMapping("/config")
    public UnmodifiableJsonifiableEntity getConfig() {
        return UnmodifiableJsonifiableEntity.wrap(
            new JsonObject()
                .put("version", "1.0.0")
                .put("features", new JsonArray().add("auth").add("logging"))
        );
    }
}

// 未注册序列化器：返回包含 "jsonObject" 包装的复杂 JSON 结构
// 注册序列化器：返回干净的 JSON，直接是业务数据，客户端使用更方便
```

#### 最佳实践建议
1. **始终注册**: 在应用程序启动时务必调用 `JsonifiableSerializer.register()`
2. **早期注册**: 在任何 JSON 序列化操作之前完成注册
3. **一次注册**: 只需要注册一次，通常在应用程序的入口点进行
4. **避免序列化异常**: 特别是对于自定义的 `JsonifiableEntity` 实现类，不注册序列化器可能导致 `getImplementation()` 方法引发的循环引用异常

## 使用场景

### 适用情况
- 需要将 `UnmodifiableJsonifiableEntity` 对象序列化为 JSON
- 使用 Jackson 作为 JSON 处理库的应用
- 需要自定义序列化逻辑的场景

### 注意事项
1. **初始化时机**: 必须在使用相关类之前调用 `register()` 方法
2. **依赖关系**: 依赖于 `UnmodifiableJsonifiableEntity.toString()` 方法返回有效的 JSON 格式
3. **性能考虑**: 当前实现通过字符串操作进行序列化，对于大量数据可能存在性能影响

## 相关类

- `UnmodifiableJsonifiableEntity`: 被序列化的目标接口
- `UnmodifiableJsonifiableEntityImpl`: `UnmodifiableJsonifiableEntity` 的实现类
- `JsonifiableEntity`: 相关的可修改 JSON 实体接口

## 依赖

### 外部依赖
- Jackson Core (`com.fasterxml.jackson.core`)
- Jackson Databind (`com.fasterxml.jackson.databind`)
- Vert.x Core (`io.vertx.core`)

### 内部依赖
- `UnmodifiableJsonifiableEntity` 接口

## 示例代码

```java
// 1. 注册序列化器（应用启动时）
JsonifiableSerializer.register();

// 2. 使用示例
UnmodifiableJsonifiableEntity entity = UnmodifiableJsonifiableEntity.wrap(new JsonObject().put("name", "example"));

// 3. 通过 Jackson ObjectMapper 序列化
ObjectMapper mapper = DatabindCodec.mapper();
String json = mapper.writeValueAsString(entity);
```

## 扩展建议

如果需要更复杂的序列化逻辑，可以考虑：
1. 实现字段级别的序列化控制
2. 添加序列化配置选项
3. 支持嵌套对象的递归序列化
4. 添加序列化性能优化
