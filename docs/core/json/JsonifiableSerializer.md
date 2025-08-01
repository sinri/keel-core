# JsonifiableSerializer 类文档

## 概述

`JsonifiableSerializer` 是 Keel 框架中为 [JsonSerializable](JsonSerializable.md) 接口提供 Jackson Databind 序列化支持的核心类。它实现了 Jackson 的自定义序列化器，确保所有实现了 `JsonSerializable` 接口的类都能在 Jackson 序列化过程中被正确处理。

## 设计思想

### 统一序列化标准

[JsonifiableSerializer](src/main/java/io/github/sinri/keel/core/json/JsonifiableSerializer.java#L24-L38) 的设计目标是提供一个统一的序列化标准，解决以下问题：

1. **接口一致性**：确保所有 `JsonSerializable` 实现类都使用相同的序列化逻辑
2. **Jackson 集成**：与 Jackson 框架无缝集成，支持所有 Jackson 序列化场景
3. **性能优化**：避免反射和复杂的对象图遍历，直接使用 `toString()` 方法

### 装饰器模式应用

该类采用了装饰器模式，在不修改原始类的情况下，为其添加了 Jackson 序列化能力：

```java
public class JsonifiableSerializer extends JsonSerializer<JsonSerializable>
```

这种设计确保了：
- **非侵入性**：不需要修改现有的业务类
- **可扩展性**：可以轻松添加新的序列化逻辑
- **可测试性**：序列化逻辑独立，便于单元测试

### 静态注册机制

提供了静态注册方法，简化了序列化器的配置：

```java
public static void register()
```

这种设计提供了：
- **全局配置**：一次注册，全局生效
- **简化使用**：无需复杂的配置代码
- **向后兼容**：不影响现有的序列化行为

## 核心方法

### register() 方法

```java
public static void register()
```

- **功能**：将序列化器注册到 Jackson 的 ObjectMapper 中
- **调用时机**：必须在程序启动时，在任何 `JsonSerializable` 类被序列化之前调用
- **设计考虑**：使用 Vert.x 的 DatabindCodec 确保与 Vert.x 生态系统的兼容性

### serialize() 方法

```java
@Override
public void serialize(JsonSerializable value, JsonGenerator gen, SerializerProvider serializers) throws IOException
```

- **功能**：将 `JsonSerializable` 对象序列化为 JSON
- **参数**：
  - `value`：要序列化的对象
  - `gen`：Jackson 的 JSON 生成器
  - `serializers`：序列化提供者
- **实现逻辑**：调用对象的 `toString()` 方法，然后将结果解析为 JSON 节点

## 使用方法指导

### 基本配置

```java
// 在应用程序启动时注册序列化器
public class Application {
    public static void main(String[] args) {
        // 必须在任何 JsonSerializable 对象被序列化之前注册
        JsonifiableSerializer.register();
        
        // 启动应用程序
        startApplication();
    }
}
```

### 在 Vert.x 应用中使用

```java
public class MainVerticle extends AbstractVerticle {
    @Override
    public void start() {
        // 注册序列化器
        JsonifiableSerializer.register();
        
        // 创建 HTTP 服务器
        Router router = Router.router(vertx);
        
        // 路由处理
        router.get("/api/data").handler(this::handleGetData);
        
        vertx.createHttpServer()
            .requestHandler(router)
            .listen(8080);
    }
    
    private void handleGetData(RoutingContext context) {
        // 创建 JsonSerializable 对象
        MyDataObject data = new MyDataObject("张三", 25);
        
        // 直接返回，Jackson 会自动使用注册的序列化器
        context.response()
            .putHeader("Content-Type", "application/json")
            .end(Json.encode(data));
    }
}
```

### 在 Spring Boot 应用中使用

```java
@Configuration
public class JacksonConfig {
    
    @PostConstruct
    public void configureJackson() {
        JsonifiableSerializer.register();
    }
}

// 或者使用 Bean 配置
@Configuration
public class JacksonConfig {
    
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        
        // 注册序列化器
        SimpleModule module = new SimpleModule();
        module.addSerializer(JsonSerializable.class, new JsonifiableSerializer());
        mapper.registerModule(module);
        
        return mapper;
    }
}
```

### 序列化示例

```java
// 定义实现 JsonSerializable 的类
public class User implements JsonSerializable {
    private String name;
    private int age;
    
    public User(String name, int age) {
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

// 使用序列化器
public class UserService {
    
    public String getUserAsJson(User user) {
        // 注册序列化器（通常在应用启动时完成）
        JsonifiableSerializer.register();
        
        // 使用 Jackson 序列化
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(user);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("序列化失败", e);
        }
    }
}
```

### 在 EventBus 中使用

```java
public class EventBusExample {
    
    public void sendMessage() {
        // 注册序列化器
        JsonifiableSerializer.register();
        
        // 创建消息对象
        MessageData message = new MessageData("Hello", "World");
        
        // 发送到 EventBus，自动序列化
        vertx.eventBus().send("message.address", message);
    }
    
    public void receiveMessage() {
        vertx.eventBus().consumer("message.address", message -> {
            // 消息会自动反序列化
            MessageData data = (MessageData) message.body();
            System.out.println("收到消息: " + data);
        });
    }
}
```

## 最佳实践

### 1. 注册时机

```java
// 推荐：在应用启动时注册
public class ApplicationBootstrap {
    
    public static void initialize() {
        // 1. 注册序列化器（最先执行）
        JsonifiableSerializer.register();
        
        // 2. 其他初始化
        initializeDatabase();
        initializeCache();
        initializeServices();
    }
}
```

### 2. 错误处理

```java
public class SafeSerializer {
    
    public static String safeSerialize(JsonSerializable obj) {
        try {
            JsonifiableSerializer.register();
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            // 降级到手动序列化
            return obj.toJsonExpression();
        }
    }
}
```

### 3. 性能优化

```java
public class OptimizedSerializer {
    private static final ObjectMapper mapper = new ObjectMapper();
    
    static {
        JsonifiableSerializer.register();
    }
    
    public static String serialize(JsonSerializable obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("序列化失败", e);
        }
    }
}
```

### 4. 测试支持

```java
@Test
public void testSerialization() {
    // 确保在测试中注册序列化器
    JsonifiableSerializer.register();
    
    User user = new User("张三", 25);
    ObjectMapper mapper = new ObjectMapper();
    
    try {
        String json = mapper.writeValueAsString(user);
        assertTrue(json.contains("张三"));
        assertTrue(json.contains("25"));
    } catch (JsonProcessingException e) {
        fail("序列化失败: " + e.getMessage());
    }
}
```

## 注意事项

### 1. 注册时机

- **必须在序列化之前注册**：如果序列化器未注册，Jackson 会使用默认的序列化逻辑
- **全局注册**：注册后对所有 `JsonSerializable` 实现类生效
- **一次性注册**：通常只需要在应用启动时注册一次

### 2. 性能考虑

- **toString() 依赖**：序列化器依赖 `toString()` 方法，确保该方法返回有效的 JSON
- **缓存友好**：如果对象是不可变的，考虑缓存 `toString()` 结果
- **内存使用**：序列化过程会创建临时对象，注意内存使用

### 3. 错误处理

- **异常处理**：序列化失败时会抛出 IOException
- **降级策略**：考虑提供降级到手动序列化的策略
- **日志记录**：记录序列化错误以便调试

### 4. 兼容性

- **向后兼容**：不影响现有的序列化行为
- **Vert.x 集成**：与 Vert.x 的 DatabindCodec 完全兼容
- **Jackson 版本**：支持 Jackson 2.x 版本

## 相关接口

- [JsonSerializable](JsonSerializable.md)：序列化接口定义
- [JsonObjectConvertible](JsonObjectConvertible.md)：JSON 对象转换能力
- [UnmodifiableJsonifiableEntity](UnmodifiableJsonifiableEntity.md)：只读 JSON 实体
- [JsonifiableEntity](JsonifiableEntity.md)：完整 JSON 实体接口
