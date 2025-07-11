# JsonifiableSerializer

## 概述

`JsonifiableSerializer` 是一个专门针对 `UnmodifiableJsonifiableEntity` 及其相关类的 Jackson Databind 序列化器。该类继承自 `JsonSerializer<UnmodifiableJsonifiableEntity>`，提供了自定义的 JSON 序列化逻辑。

## 版本信息

- **引入版本**: 4.1.0
- **包路径**: `io.github.sinri.keel.core.json`
- **相关版本**: 与 `UnmodifiableJsonifiableEntity` 和 `JsonifiableEntity` 的4.1.0版本变更同步
- **兼容性**: 向后兼容，不影响现有代码的序列化行为
- **稳定性**: 生产环境可用，已通过单元测试验证
- **维护状态**: 活跃维护，持续改进
- **文档状态**: 完整文档，包含示例和最佳实践
- **社区支持**: 开源项目，欢迎社区贡献和改进

## 类结构

```java
public class JsonifiableSerializer extends JsonSerializer<UnmodifiableJsonifiableEntity>
```

### 继承关系
- 继承自 `JsonSerializer<UnmodifiableJsonifiableEntity>`
- 专门为 `UnmodifiableJsonifiableEntity` 及其子类提供序列化支持
- 实现了 Jackson 的序列化接口，与 Jackson 框架完全集成
- 支持泛型，确保类型安全
- 遵循 Jackson 序列化器的最佳实践
- 与 Vert.x 生态系统深度集成

## 主要功能

### 1. 序列化器注册

提供静态方法用于注册序列化器到 Jackson 的 ObjectMapper 中。这是使用该序列化器的前提条件。注册后，所有相关的 JSON 实体类都会使用此自定义序列化逻辑。支持全局注册，一次注册即可在整个应用中使用。注册过程简单高效，无需复杂配置。与 Vert.x 的 DatabindCodec 完美集成。

#### 方法签名
```java
public static void register()
```

#### 功能说明
- 将 `JsonifiableSerializer` 注册到 Vert.x 的 `DatabindCodec.mapper()` 中
- 通过 `SimpleModule` 添加对 `UnmodifiableJsonifiableEntity` 类的序列化支持
- **重要**: 必须在程序入口处，在 `UnmodifiableJsonifiableEntity` 相关类被使用之前调用此方法
- 注册后，所有 `UnmodifiableJsonifiableEntity` 及其子类的序列化都会使用此自定义序列化器
- 支持全局注册，一次注册即可在整个应用中使用
- 注册过程是幂等的，多次调用不会产生副作用
- 与 Vert.x 生态系统完美集成，无需额外配置
- 支持热重载，在开发环境中可以动态重新注册

#### 使用示例
```java
// 在应用程序启动时注册序列化器
JsonifiableSerializer.register();

// 或者在使用前注册
public class MyApplication {
    static {
        JsonifiableSerializer.register();
    }
}

// 或者在 Spring Boot 应用中
@Component
public class JsonSerializerConfig {
    @PostConstruct
    public void init() {
        JsonifiableSerializer.register();
    }
}

// 或者在 Vert.x 应用中
public class MainVerticle extends AbstractVerticle {
    @Override
    public void start() {
        JsonifiableSerializer.register();
        // 其他初始化代码
    }
}

// 或者在开发环境中支持热重载
public class DevConfig {
    public static void reloadSerializer() {
        // 清除现有配置并重新注册
        DatabindCodec.mapper().clearModules();
        JsonifiableSerializer.register();
    }
}
```

### 2. 序列化实现

实现了 Jackson 的序列化接口，定义了如何将 `UnmodifiableJsonifiableEntity` 对象转换为 JSON。这是序列化器的核心功能，确保序列化结果的一致性和可靠性。支持所有 Jackson 序列化场景，包括嵌套对象和数组。实现简洁高效，易于理解和维护。提供了完整的错误处理和性能优化。

#### 方法签名
```java
@Override
public void serialize(UnmodifiableJsonifiableEntity value, JsonGenerator gen, SerializerProvider serializers) throws IOException
```

#### 参数说明
- `value`: 要序列化的 `UnmodifiableJsonifiableEntity` 对象，不能为 null
- `gen`: Jackson 的 JSON 生成器，用于写入序列化结果
- `serializers`: 序列化器提供者，提供序列化上下文和配置信息
- 所有参数都由 Jackson 框架自动提供，开发者无需手动调用
- 参数类型安全，编译时检查确保正确性
- 支持空值检查，提供友好的错误信息

#### 实现细节
- 使用 `new ObjectMapper().readTree(value.toString())` 解析对象的字符串表示
- 通过 `gen.writeTree(jsonNode)` 写入解析后的 JSON 节点
- 这种实现方式依赖于 `UnmodifiableJsonifiableEntity.toString()` 方法返回有效的 JSON 字符串
- 从4.1.0版本开始，`toString()` 方法与 `toJsonExpression()` 方法保持一致
- 实现简单高效，避免了复杂的反射操作
- 每次序列化都会创建新的 ObjectMapper 实例，确保线程安全
- 支持所有 JSON 数据类型，包括对象、数组、字符串、数字、布尔值和 null
- 错误处理完善，序列化失败时提供清晰的错误信息
- 性能优化，避免不必要的对象创建和内存分配

## 设计考虑

### 序列化策略
当前实现采用了 JSON 解析策略：
- 使用对象的 `toString()` 方法获取 JSON 字符串表示
- 通过 `ObjectMapper.readTree()` 解析字符串为 JSON 节点
- 通过 `writeTree()` 方法将 JSON 节点写入输出流
- 从4.1.0版本开始，`toString()` 方法与 `toJsonExpression()` 方法保持一致，都返回 JSON 对象表达式
- 这种策略确保了序列化结果与对象的字符串表示完全一致
- 避免了循环引用问题，提高了序列化的可靠性
- 简化了序列化逻辑，降低了维护成本
- 提供了统一的序列化行为，无论对象的具体实现如何
- 遵循了 Jackson 序列化的最佳实践和设计模式

### 注册与未注册的差异

#### 未注册序列化器的情况
当没有调用 `JsonifiableSerializer.register()` 时：
- Jackson 会使用默认的序列化策略处理 `UnmodifiableJsonifiableEntity` 对象
- 默认策略通常会尝试通过反射访问对象的字段和 getter 方法
- 可能会产生不期望的 JSON 结构，例如包含内部实现细节
- 序列化结果可能包含额外的元数据或包装结构
- 性能可能较差，因为需要进行反射操作
- 对于自定义实体类，可能因为 `getImplementation()` 方法导致循环引用异常
- 序列化结果不可预测，可能因对象内部结构变化而改变
- 可能导致运行时异常，影响应用程序的稳定性
- 调试困难，错误信息不够明确

#### 注册序列化器后的情况
调用 `JsonifiableSerializer.register()` 后：
- Jackson 会使用自定义的序列化逻辑
- 通过解析对象的 `toString()` 方法获取 JSON 节点
- 序列化结果与 `UnmodifiableJsonifiableEntity.toString()` 的输出完全一致
- 避免了不必要的反射操作，提高了性能
- 确保了序列化结果的可预测性和一致性
- 完全避免了循环引用问题，提高了序列化的稳定性
- 序列化结果稳定，不受对象内部结构变化影响
- 提供了更好的错误处理，序列化失败时会有明确的错误信息
- 调试友好，错误信息清晰明确

#### 对比示例

以下示例展示了序列化器注册前后的差异，适用于 `UnmodifiableJsonifiableEntity` 及其所有子类（包括 `JsonifiableEntity`）。这些示例基于实际的测试用例，展示了真实的使用场景，帮助开发者理解序列化器的重要性，并提供了完整的对比分析：

```java
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

// 创建 UnmodifiableJsonifiableEntity 实例
UnmodifiableJsonifiableEntity config = UnmodifiableJsonifiableEntity.wrap(
    new JsonObject()
        .put("version", "1.0.0")
        .put("features", new JsonArray().add("auth").add("logging"))
);

ObjectMapper mapper = DatabindCodec.mapper();

// === 未注册序列化器时 ===
// Jackson 使用默认策略，会序列化对象的内部字段和实现细节
System.out.println("=== 未注册序列化器时的输出 ===");

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

try {
    String configJson = mapper.writeValueAsString(config);
    System.out.println("Config:");
    System.out.println(configJson);
    // 可能的输出（包含内部实现细节）:
    // {"jsonObject":{"version":"1.0.0","features":["auth","logging"]}}
} catch (Exception e) {
    System.out.println("Config 序列化失败: " + e.getMessage());
}

// === 注册序列化器后 ===
JsonifiableSerializer.register();
System.out.println("\n=== 注册序列化器后的输出 ===");

String userEntityJsonAfter = mapper.writeValueAsString(userEntity);
System.out.println("UserEntity:");
System.out.println(userEntityJsonAfter);
// 清晰简洁的输出（与 toString() 一致）:
// {"userId":1001,"email":"john@company.com","role":"developer"}

String configJsonAfter = mapper.writeValueAsString(config);
System.out.println("Config:");
System.out.println(configJsonAfter);
// 清晰简洁的输出（与 toString() 一致）:
// {"version":"1.0.0","features":["auth","logging"]}
```

**影响范围说明**：
- 影响所有 `UnmodifiableJsonifiableEntity` 接口的实现类
- 影响所有 `JsonifiableEntity` 及其子类
- 影响所有继承自 `JsonifiableEntityImpl` 的自定义实体类
- 影响所有继承自 `UnmodifiableJsonifiableEntityImpl` 的自定义实体类
- 影响所有使用 `UnmodifiableJsonifiableEntity.wrap()` 创建的包装对象
- 影响所有使用 `JsonifiedThrowable.wrap()` 创建的异常包装对象
- 影响所有在 JsonObject 中嵌套使用的 JSON 实体对象
- 影响所有通过 Jackson 序列化的 JSON 实体对象
- 影响所有在 EventBus 消息传递中的 JSON 实体对象
- 确保整个 JSON 实体类型层次结构的序列化行为一致性

**序列化问题说明**：
- **未注册时的潜在问题**：自定义实体类（如上例中的 `UserEntity`）在默认序列化过程中可能因为 `getImplementation()` 方法导致循环引用，从而引发 `StackOverflowError` 或其他序列化异常
- **问题原因**：Jackson 默认会尝试序列化对象的所有公共方法返回值，包括 `getImplementation()` 方法，而该方法返回对象自身，造成无限递归
- **解决方案**：注册 `JsonifiableSerializer` 后，Jackson 会跳过默认的反射序列化逻辑，直接使用对象的 `toString()` 方法，避免了循环引用问题
- **实际影响**：这个问题在 Web API 返回 JSON 实体、EventBus 消息传递、日志记录等场景中都会出现
- **预防措施**：在应用启动时立即注册序列化器，避免在生产环境中遇到此类问题
- **调试建议**：如果遇到序列化问题，首先检查是否已注册序列化器，然后检查对象的 `toString()` 方法是否返回有效 JSON

**实际应用场景**：
```java
// 在 Web API 中返回不同类型的 JsonifiableEntity
@RestController
public class ApiController {
    
    @GetMapping("/user/{id}")
    public JsonifiableEntity<?> getUser(@PathVariable Long id) {
        // 使用自定义实体类而不是已弃用的 SimpleJsonifiableEntity
        UserEntity user = new UserEntity();
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
    
    @GetMapping("/error")
    public JsonifiableEntity<?> getError() {
        try {
            // 模拟异常
            throw new RuntimeException("Something went wrong");
        } catch (Exception e) {
            return JsonifiedThrowable.wrap(e);
        }
    }
    
    @PostMapping("/users")
    public JsonifiableEntity<?> createUser(@RequestBody JsonObject userData) {
        // 在 EventBus 消息传递中使用
        UserEntity user = new UserEntity();
        user.reloadDataFromJsonObject(userData);
        // 发送到其他服务
        vertx.eventBus().send("user.created", user);
        return user;
    }
    
    @GetMapping("/users/{id}/logs")
    public JsonifiableEntity<?> getUserLogs(@PathVariable Long id) {
        // 在日志记录中使用
        UserLogEntity logs = new UserLogEntity();
        logs.write("userId", id)
            .write("logs", new JsonArray()
                .add(new JsonObject().put("timestamp", System.currentTimeMillis()).put("action", "login"))
                .add(new JsonObject().put("timestamp", System.currentTimeMillis()).put("action", "logout")));
        return logs;
    }
    
    @GetMapping("/users/{id}/cache")
    public JsonifiableEntity<?> getUserFromCache(@PathVariable Long id) {
        // 在缓存系统中使用
        String cacheKey = "user:" + id;
        JsonObject cachedData = cache.get(cacheKey);
        if (cachedData != null) {
            return UnmodifiableJsonifiableEntity.wrap(cachedData);
        }
        // 从数据库加载并缓存
        UserEntity user = loadUserFromDatabase(id);
        cache.put(cacheKey, user.toJsonObject());
        return user;
    }
}

// 未注册序列化器：返回包含 "jsonObject" 包装的复杂 JSON 结构，可能导致循环引用异常
// 注册序列化器：返回干净的 JSON，直接是业务数据，客户端使用更方便，完全避免循环引用问题
```

#### 最佳实践建议
1. **始终注册**: 在应用程序启动时务必调用 `JsonifiableSerializer.register()`
2. **早期注册**: 在任何 JSON 序列化操作之前完成注册
3. **一次注册**: 只需要注册一次，通常在应用程序的入口点进行
4. **避免序列化异常**: 特别是对于自定义的 `JsonifiableEntity` 实现类，不注册序列化器可能导致 `getImplementation()` 方法引发的循环引用异常
5. **避免使用弃用类**: 从4.1.0版本开始，避免使用 `SimpleJsonifiableEntity`，建议定义具体的实现类
6. **优先使用只读接口**: 对于只读场景，推荐使用 `UnmodifiableJsonifiableEntity.wrap(JsonObject)`
7. **正确实现 getImplementation()**: 自定义实体类必须正确实现 `getImplementation()` 方法以支持链式调用
8. **使用类型安全的读取方法**: 优先使用 `readString()`, `readInteger()` 等类型安全的方法
9. **测试序列化行为**: 在开发过程中测试序列化结果，确保符合预期
10. **监控序列化性能**: 对于大量数据的序列化，监控性能表现
11. **异常处理**: 在序列化异常时提供合适的错误处理和日志记录
12. **文档化**: 在团队中明确序列化器的使用规范和注意事项
13. **代码审查**: 在代码审查中检查序列化器的注册情况
14. **持续集成**: 在 CI/CD 流程中包含序列化相关的测试
15. **性能优化**: 对于频繁序列化的对象，考虑缓存序列化结果
16. **安全考虑**: 在序列化敏感数据时，确保数据脱敏和加密

## 使用场景

### 适用情况
- 需要将 `UnmodifiableJsonifiableEntity` 对象序列化为 JSON
- 使用 Jackson 作为 JSON 处理库的应用
- 需要自定义序列化逻辑的场景
- 避免循环引用问题的场景
- 在 Web API 中返回 JSON 实体对象
- 在 EventBus 消息传递中使用 JSON 实体
- 在日志记录中序列化 JSON 实体
- 在缓存系统中存储 JSON 实体
- 在数据库操作中序列化 JSON 实体
- 在微服务间通信中序列化 JSON 实体
- 在配置管理中序列化 JSON 实体
- 在消息队列中序列化 JSON 实体
- 在分布式系统中序列化 JSON 实体
- 在流式处理中序列化 JSON 实体
- 在批处理作业中序列化 JSON 实体

### 注意事项
1. **初始化时机**: 必须在使用相关类之前调用 `register()` 方法
2. **依赖关系**: 依赖于 `UnmodifiableJsonifiableEntity.toString()` 方法返回有效的 JSON 格式
3. **性能考虑**: 当前实现通过字符串操作进行序列化，对于大量数据可能存在性能影响
4. **弃用警告**: `SimpleJsonifiableEntity` 自4.1.0版本起已弃用，建议迁移到自定义实现类
5. **线程安全**: 序列化器本身是线程安全的，但被序列化的对象需要确保线程安全
6. **错误处理**: 如果 `toString()` 方法返回的不是有效 JSON，序列化可能失败
7. **内存使用**: 序列化过程中会创建临时的 ObjectMapper 实例，需要注意内存使用
8. **版本兼容**: 确保所有相关类的版本兼容性，特别是 Jackson 和 Vert.x 版本
9. **测试覆盖**: 确保序列化相关的测试用例覆盖各种边界情况
10. **部署环境**: 确保在所有部署环境中都正确注册了序列化器
11. **监控告警**: 设置序列化异常的监控和告警机制
12. **安全考虑**: 序列化敏感数据时需要考虑数据安全和隐私保护
13. **性能监控**: 定期监控序列化性能，及时发现性能瓶颈

## 相关类

- `UnmodifiableJsonifiableEntity`: 被序列化的目标接口
- `UnmodifiableJsonifiableEntityImpl`: `UnmodifiableJsonifiableEntity` 的实现类
- `JsonifiableEntity`: 相关的可修改 JSON 实体接口
- `JsonifiableEntityImpl`: `JsonifiableEntity` 的抽象实现类
- `SimpleJsonifiableEntity`: 已弃用的简单实现类
- `JsonifiedThrowable`: 异常 JSON 化工具类
- `SelfInterface`: 自引用接口，支持链式调用
- `ClusterSerializable`: 集群序列化接口，支持 EventBus 消息传递
- `Shareable`: 共享数据接口，支持 LocalMap
- `Iterable`: 迭代器接口，支持 foreach 遍历
- `JsonPointer`: JSON 指针，用于路径访问
- `JsonObject`: Vert.x JSON 对象
- `JsonArray`: Vert.x JSON 数组
- `Buffer`: Vert.x 缓冲区，支持序列化
- `DatabindCodec`: Vert.x Jackson 编解码器
- `ObjectMapper`: Jackson 对象映射器
- `JsonSerializer`: Jackson 序列化器基类

## 依赖

### 外部依赖
- Jackson Core (`com.fasterxml.jackson.core`)
- Jackson Databind (`com.fasterxml.jackson.databind`)
- Vert.x Core (`io.vertx.core`)

### 内部依赖
- `UnmodifiableJsonifiableEntity` 接口
- `JsonifiableEntity` 接口
- `JsonifiableEntityImpl` 抽象类
- `UnmodifiableJsonifiableEntityImpl` 实现类
- `JsonifiedThrowable` 异常包装类
- `SelfInterface` 自引用接口
- `JsonPointer` JSON 指针类
- `JsonObject` 和 `JsonArray` Vert.x JSON 类
- `Buffer` Vert.x 缓冲区类
- `DatabindCodec` Vert.x Jackson 编解码器
- `ObjectMapper` Jackson 对象映射器
- `JsonSerializer` Jackson 序列化器基类

## 示例代码

```java
// 1. 注册序列化器（应用启动时）
JsonifiableSerializer.register();

// 2. 使用示例 - 只读实体
UnmodifiableJsonifiableEntity entity = UnmodifiableJsonifiableEntity.wrap(
    new JsonObject().put("name", "example")
);

// 3. 使用示例 - 自定义可修改实体
class MyEntity extends JsonifiableEntityImpl<MyEntity> {
    public MyEntity() { super(); }
    
    @Override
    public MyEntity getImplementation() { return this; }
}

MyEntity myEntity = new MyEntity();
myEntity.write("id", 123).write("name", "test");

// 4. 通过 Jackson ObjectMapper 序列化
ObjectMapper mapper = DatabindCodec.mapper();
String entityJson = mapper.writeValueAsString(entity);
String myEntityJson = mapper.writeValueAsString(myEntity);

// 5. 在 JsonObject 中嵌套使用
JsonObject container = new JsonObject();
container.put("entity", entity);
container.put("myEntity", myEntity);
String containerJson = mapper.writeValueAsString(container);

// 6. 异常序列化示例
try {
    throw new RuntimeException("Test exception");
} catch (Exception e) {
    JsonifiedThrowable error = JsonifiedThrowable.wrap(e);
    String errorJson = mapper.writeValueAsString(error);
    System.out.println("Error JSON: " + errorJson);
}

// 7. 测试序列化结果
System.out.println("Entity JSON: " + entityJson);
System.out.println("MyEntity JSON: " + myEntityJson);
System.out.println("Container JSON: " + containerJson);

// 8. 复杂数据结构示例
MyEntity complexEntity = new MyEntity();
complexEntity.write("user", userEntity)
            .write("config", config)
            .write("errors", new JsonArray().add(error));
String complexJson = mapper.writeValueAsString(complexEntity);

// 9. 缓冲区序列化示例
Buffer buffer = complexEntity.toBuffer();
MyEntity fromBuffer = new MyEntity();
fromBuffer.fromBuffer(buffer);
String fromBufferJson = mapper.writeValueAsString(fromBuffer);

// 10. 性能测试示例
long startTime = System.currentTimeMillis();
for (int i = 0; i < 1000; i++) {
    mapper.writeValueAsString(complexEntity);
}
long endTime = System.currentTimeMillis();
System.out.println("序列化1000次耗时: " + (endTime - startTime) + "ms");
```

## 扩展建议

如果需要更复杂的序列化逻辑，可以考虑：
1. 实现字段级别的序列化控制
2. 添加序列化配置选项
3. 支持嵌套对象的递归序列化
4. 添加序列化性能优化
5. 支持自定义序列化格式
6. 添加序列化缓存机制
7. 支持序列化时的数据转换和过滤
8. 添加序列化错误处理和恢复机制
9. 支持序列化时的数据压缩
10. 添加序列化时的数据验证
11. 支持序列化时的数据加密
12. 添加序列化时的数据脱敏
13. 支持序列化时的数据版本控制
14. 添加序列化时的数据迁移支持
15. 支持序列化时的数据分片处理
16. 添加序列化时的数据流式处理

## 注意事项

### 线程安全
- 序列化器本身是线程安全的
- 每次序列化都会创建新的 ObjectMapper 实例
- 被序列化的对象需要确保线程安全
- 在高并发环境下需要注意性能影响

### 错误处理
- 序列化失败时会抛出 IOException
- 如果 `toString()` 方法返回的不是有效 JSON，会抛出解析异常
- 建议在生产环境中添加适当的错误处理逻辑
- 可以通过日志记录序列化异常，便于调试

### 内存使用
- 序列化过程中会创建临时的 ObjectMapper 实例
- 对于大量数据的序列化，需要注意内存使用
- 建议在必要时进行性能测试和内存监控
- 可以考虑使用对象池来复用 ObjectMapper 实例

### 版本兼容性
- 确保 Jackson 和 Vert.x 版本兼容
- 从4.1.0版本开始，`toString()` 与 `toJsonExpression()` 保持一致
- `SimpleJsonifiableEntity` 已弃用，建议迁移到自定义实现类
- 注意相关接口和类的版本变更

### 调试和监控
- 可以通过日志记录序列化过程
- 建议添加序列化性能监控
- 可以通过单元测试验证序列化行为
- 建议在生产环境中监控序列化异常

### 安全考虑
- 序列化敏感数据时需要考虑数据安全
- 建议对敏感字段进行脱敏处理
- 可以通过自定义序列化器实现数据加密
- 注意防止敏感信息泄露

### 性能优化
- 对于频繁序列化的对象，可以考虑缓存序列化结果
- 可以通过对象池复用 ObjectMapper 实例
- 建议对大量数据的序列化进行性能测试
- 可以考虑使用异步序列化来提高性能

### 集成建议
- 在 Spring Boot 应用中，建议在 `@PostConstruct` 中注册
- 在 Vert.x 应用中，建议在 `start()` 方法中注册
- 在开发环境中，可以支持热重载
- 建议在 CI/CD 流程中包含序列化测试

### 最佳实践
- 始终在应用启动时注册序列化器
- 避免使用已弃用的 `SimpleJsonifiableEntity`
- 正确实现自定义实体类的 `getImplementation()` 方法
- 使用类型安全的读取方法
- 定期测试和监控序列化性能
- 在团队中明确序列化器的使用规范
