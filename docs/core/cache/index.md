# package io.github.sinri.keel.core.cache

Keel 缓存包提供了多种缓存接口和实现，支持同步/异步操作、临时缓存和永久缓存等不同场景。

## 核心接口概览

### 1. KeelCacheInterface<K, V> - 基础缓存接口

基础的同步缓存接口，支持带过期时间的键值对缓存。

**主要特性：**
- 支持设置缓存项的生存时间（秒）
- 提供默认生存时间配置
- 支持缓存未命中时的回退值
- 支持缓存未命中时的生成器模式
- 自动清理过期缓存项

**典型用法：**

```java
// 创建缓存实例
KeelCacheInterface<String, String> cache = KeelCacheInterface.createDefaultInstance();

// 设置默认生存时间为60秒
cache.setDefaultLifeInSeconds(60);

// 保存缓存项（使用默认生存时间）
cache.save("user:123", "John Doe");

// 保存缓存项（指定生存时间为300秒）
cache.save("session:abc", "active", 300);

// 读取缓存项（未找到时返回null）
try {
    String user = cache.read("user:123");
    System.out.println("User: " + user);
} catch (NotCached e) {
    System.out.println("User not found in cache");
}

// 读取缓存项（未找到时返回默认值）
String status = cache.read("session:xyz", "inactive");

// 使用生成器模式（缓存未命中时自动生成并缓存）
String userData = cache.read("user:456", () -> {
    // 从数据库加载用户数据
    return loadUserFromDatabase("456");
}, 120);

// 手动清理过期缓存项
cache.cleanUp();

// 启动自动清理（每30秒清理一次）
cache.startEndlessCleanUp(30000);
```

### 2. KeelAsyncCacheInterface<K, V> - 异步缓存接口

基于 Vert.x Future 的异步缓存接口，适用于高并发场景。

**主要特性：**
- 所有操作返回 Future 对象
- 支持异步生成器模式
- 非阻塞操作

**典型用法：**

```java
// 创建异步缓存实例
KeelAsyncCacheInterface<String, User> asyncCache = KeelAsyncCacheInterface.createDefaultInstance();

// 异步保存
asyncCache.save("user:123", user, 300)
    .onSuccess(v -> System.out.println("User cached successfully"))
    .onFailure(err -> System.err.println("Failed to cache user: " + err.getMessage()));

// 异步读取
asyncCache.read("user:123")
    .onSuccess(user -> System.out.println("Found user: " + user.getName()))
    .onFailure(err -> {
        if (err instanceof NotCached) {
            System.out.println("User not found in cache");
        }
    });

// 异步读取（带默认值）
asyncCache.read("user:456", new User("Default User"))
    .onSuccess(user -> System.out.println("User: " + user.getName()));

// 异步生成器模式
asyncCache.read("user:789", key -> {
    // 异步从数据库加载
    return loadUserFromDatabaseAsync(key);
}, 180)
.onSuccess(user -> System.out.println("User loaded: " + user.getName()));
```

### 3. KeelEverlastingCacheInterface<K, V> - 永久缓存接口

不会过期的同步缓存接口，适用于配置数据、静态数据等场景。

**主要特性：**
- 缓存项不会自动过期
- 支持批量操作
- 支持完全替换缓存内容

**典型用法：**

```java
// 创建永久缓存实例
KeelEverlastingCacheInterface<String, Config> configCache = 
    KeelEverlastingCacheInterface.createDefaultInstance();

// 保存单个配置项
configCache.save("app.name", new Config("MyApp"));

// 批量保存配置项
Map<String, Config> configs = new HashMap<>();
configs.put("app.version", new Config("1.0.0"));
configs.put("app.debug", new Config("false"));
configCache.save(configs);

// 读取配置项
try {
    Config appName = configCache.read("app.name");
    System.out.println("App name: " + appName.getValue());
} catch (NotCached e) {
    System.out.println("Config not found");
}

// 读取配置项（带默认值）
Config debugMode = configCache.read("app.debug", new Config("true"));

// 批量删除
List<String> keysToRemove = Arrays.asList("old.config1", "old.config2");
configCache.remove(keysToRemove);

// 完全替换缓存内容
Map<String, Config> newConfigs = loadAllConfigsFromFile();
configCache.replaceAll(newConfigs);
```

### 4. KeelAsyncEverlastingCacheInterface<K, V> - 异步永久缓存接口

异步版本的永久缓存接口。

**典型用法：**

```java
// 创建异步永久缓存实例
KeelAsyncEverlastingCacheInterface<String, Config> asyncConfigCache = 
    KeelAsyncEverlastingCacheInterface.createDefaultInstance();

// 异步保存配置
asyncConfigCache.save("database.url", new Config("jdbc:mysql://localhost:3306/mydb"))
    .onSuccess(v -> System.out.println("Config saved"))
    .onFailure(err -> System.err.println("Failed to save config"));

// 异步读取配置
asyncConfigCache.read("database.url")
    .onSuccess(config -> System.out.println("DB URL: " + config.getValue()))
    .onFailure(err -> System.out.println("Config not found"));

// 异步批量操作
Map<String, Config> newConfigs = loadConfigsAsync();
asyncConfigCache.replaceAll(newConfigs)
    .onSuccess(v -> System.out.println("All configs updated"));
```

## 辅助类

### ValueWrapper<P> - 值包装器

用于包装缓存值，提供生存时间管理和软引用支持。

**主要特性：**
- 使用 SoftReference 存储值，支持内存不足时的垃圾回收
- 提供生存时间检查
- 线程安全的读写操作

### NotCached - 缓存未命中异常

当缓存中找不到指定键的值时抛出的异常。

### KeelTemporaryValue<P> - 临时值容器（已废弃）

单值的临时缓存容器，在 4.0.0 版本后已废弃，建议使用 KeelCacheInterface 替代。

## 实现类

- **KeelCacheAlef**: KeelCacheInterface 的默认实现，基于 ConcurrentHashMap
- **KeelCacheBet**: KeelAsyncCacheInterface 的默认实现
- **KeelCacheVet**: KeelEverlastingCacheInterface 的默认实现
- **KeelCacheGimel**: KeelAsyncEverlastingCacheInterface 的默认实现
- **KeelCacheDummy**: 空实现，用于测试或禁用缓存的场景

## 最佳实践

### 1. 选择合适的缓存类型

- **临时数据**：使用 KeelCacheInterface 或 KeelAsyncCacheInterface
- **配置数据**：使用 KeelEverlastingCacheInterface 或 KeelAsyncEverlastingCacheInterface
- **高并发场景**：优先选择异步接口

### 2. 合理设置生存时间

```java
// 用户会话：较短的生存时间
cache.save("session:" + sessionId, sessionData, 1800); // 30分钟

// 用户信息：中等生存时间
cache.save("user:" + userId, userData, 3600); // 1小时

// 静态配置：使用永久缓存
configCache.save("app.settings", settings);
```

### 3. 使用生成器模式避免缓存穿透

```java
// 推荐：使用生成器模式
String userData = cache.read("user:" + userId, () -> {
    return userService.loadUser(userId);
}, 3600);

// 不推荐：手动检查和设置
try {
    return cache.read("user:" + userId);
} catch (NotCached e) {
    String userData = userService.loadUser(userId);
    cache.save("user:" + userId, userData, 3600);
    return userData;
}
```

### 4. 定期清理过期缓存

```java
// 启动自动清理
cache.startEndlessCleanUp(60000); // 每分钟清理一次

// 或者手动清理
cache.cleanUp();
```

### 5. 异常处理

```java
// 同步缓存的异常处理
try {
    String value = cache.read("key");
    // 处理值
} catch (NotCached e) {
    // 处理缓存未命中
}

// 异步缓存的异常处理
asyncCache.read("key")
    .onSuccess(value -> {
        // 处理值
    })
    .onFailure(err -> {
        if (err instanceof NotCached) {
            // 处理缓存未命中
        } else {
            // 处理其他错误
        }
    });
```

## 性能考虑

1. **内存管理**：ValueWrapper 使用 SoftReference，在内存不足时会自动释放缓存值
2. **并发性能**：所有实现都基于 ConcurrentHashMap，支持高并发读写
3. **清理策略**：定期调用 cleanUp() 方法清理过期缓存，避免内存泄漏
4. **异步优势**：在 I/O 密集型应用中，异步缓存接口能显著提升性能

