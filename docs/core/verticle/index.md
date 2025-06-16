# Keel Verticles 包文档

## 概述

`io.github.sinri.keel.core.verticles` 包提供了基于 Vert.x 的 Verticle 扩展实现，为 Keel 框架提供了统一的 Verticle 管理和生命周期控制机制。该包包含了多个类，从基础接口到具体实现，为不同场景提供了灵活的 Verticle 解决方案。

## 核心类分析

### 1. KeelVerticle (接口)

**作用**: 扩展了 Vert.x 的 `Verticle` 接口，提供了额外的功能和便利方法。

**主要特性**:
- 提供部署和卸载的便利方法
- 支持配置管理和 Verticle 信息获取
- 提供静态工厂方法创建即时 Verticle

**核心方法**:
- `deploymentID()`: 获取部署 ID
- `config()`: 获取配置信息
- `getVerticleInfo()`: 获取 Verticle 详细信息
- `deployMe(DeploymentOptions)`: 部署当前 Verticle
- `undeployMe()`: 卸载当前 Verticle

**静态工厂方法**:
- `instant(Supplier<Future<Void>>)`: 创建基于 Future 供应商的即时 Verticle
- `instant(Function<Promise<Void>, Future<Void>>)`: 创建支持停止控制的即时 Verticle

### 2. KeelVerticleImpl (抽象类) ⭐ **推荐使用**

**作用**: `KeelVerticle` 的标准抽象实现，提供了结构化的 Verticle 实现方式。

**设计特点**:
- 继承自 `AbstractVerticle`，实现 `KeelVerticle` 接口
- 提供了标准的生命周期管理
- 强制子类实现 `startVerticle()` 方法
- 自动处理启动过程的异步操作

**生命周期管理**:
- `start()`: 被标记为 final，不允许重写
- `start(Promise<Void>)`: 处理异步启动逻辑
- `startVerticle()`: 抽象方法，子类必须实现

### 3. KeelVerticleWrap (具体类)

**作用**: 包装器类，用于将函数式接口转换为 Verticle 实现。

**使用场景**:
- 快速创建简单的 Verticle
- 将现有的异步逻辑包装为 Verticle
- 支持自管理生命周期的 Verticle

**构造方式**:
- 基于 `Supplier<Future<Void>>` 的简单包装
- 基于 `Function<Promise<Void>, Future<Void>>` 的高级包装（支持停止控制）

### 4. 已弃用的实现类

以下类已被标记为 `@Deprecated`，建议使用 `KeelVerticleImpl` 替代：

- **KeelVerticleImplPure**: 纯净实现，已在 4.0.0 版本弃用
- **KeelVerticleImplWithEventLogger**: 带事件日志的实现，计划移除
- **KeelVerticleImplWithIssueRecorder**: 带问题记录器的实现，已弃用

## 使用指南

### 基本使用模式

#### 1. 继承 KeelVerticleImpl（推荐）

```java
public class MyBusinessVerticle extends KeelVerticleImpl {
    
    @Override
    protected Future<Void> startVerticle() {
        // 实现具体的启动逻辑
        return setupHttpServer()
            .compose(v -> initializeDatabase())
            .compose(v -> startBackgroundTasks());
    }
    
    private Future<Void> setupHttpServer() {
        return vertx.createHttpServer()
            .requestHandler(request -> {
                request.response().end("Hello from MyBusinessVerticle!");
            })
            .listen(8080)
            .mapEmpty();
    }
    
    private Future<Void> initializeDatabase() {
        // 数据库初始化逻辑
        return Future.succeededFuture();
    }
    
    private Future<Void> startBackgroundTasks() {
        // 启动后台任务
        vertx.setPeriodic(5000, id -> {
            System.out.println("Background task running...");
        });
        return Future.succeededFuture();
    }
}
```

#### 2. 使用即时 Verticle

```java
// 简单的即时 Verticle
KeelVerticle simpleVerticle = KeelVerticle.instant(() -> {
    System.out.println("Simple verticle started!");
    return Future.succeededFuture();
});

// 带停止控制的即时 Verticle
KeelVerticle advancedVerticle = KeelVerticle.instant(stopPromise -> {
    System.out.println("Advanced verticle started!");
    
    // 设置定时器，5秒后自动停止
    vertx.setTimer(5000, id -> {
        System.out.println("Auto-stopping verticle...");
        stopPromise.complete();
    });
    
    return Future.succeededFuture();
});
```

### 部署和管理

#### 1. 基本部署

```java
public class VerticleDeploymentExample {
    
    public void deployVerticles() {
        MyBusinessVerticle verticle = new MyBusinessVerticle();
        
        // 使用默认选项部署
        DeploymentOptions options = new DeploymentOptions()
            .setInstances(2)
            .setConfig(new JsonObject().put("port", 8080));
            
        verticle.deployMe(options)
            .onSuccess(deploymentId -> {
                System.out.println("Verticle deployed with ID: " + deploymentId);
                
                // 获取 Verticle 信息
                JsonObject info = verticle.getVerticleInfo();
                System.out.println("Verticle info: " + info.encodePrettily());
            })
            .onFailure(throwable -> {
                System.err.println("Failed to deploy verticle: " + throwable.getMessage());
            });
    }
    
    public void undeployVerticle(KeelVerticle verticle) {
        verticle.undeployMe()
            .onSuccess(v -> System.out.println("Verticle undeployed successfully"))
            .onFailure(throwable -> System.err.println("Failed to undeploy: " + throwable.getMessage()));
    }
}
```

#### 2. 批量部署管理

```java
public class VerticleManager {
    private final List<KeelVerticle> deployedVerticles = new ArrayList<>();
    
    public Future<Void> deployAllVerticles() {
        List<Future<String>> deploymentFutures = Arrays.asList(
            new DatabaseVerticle().deployMe(new DeploymentOptions()),
            new HttpServerVerticle().deployMe(new DeploymentOptions().setInstances(4)),
            new MessageProcessorVerticle().deployMe(new DeploymentOptions())
        );
        
        return Future.all(deploymentFutures)
            .compose(compositeFuture -> {
                System.out.println("All verticles deployed successfully");
                return Future.succeededFuture();
            });
    }
    
    public Future<Void> shutdownAll() {
        List<Future<Void>> undeployFutures = deployedVerticles.stream()
            .map(KeelVerticle::undeployMe)
            .collect(Collectors.toList());
            
        return Future.all(undeployFutures).mapEmpty();
    }
}
```

### 高级使用场景

#### 1. 微服务架构中的 Verticle

```java
public class MicroserviceVerticle extends KeelVerticleImpl {
    
    @Override
    protected Future<Void> startVerticle() {
        return Future.all(
            startHttpApi(),
            startEventBusConsumers(),
            startHealthCheck(),
            registerService()
        ).mapEmpty();
    }
    
    private Future<Void> startHttpApi() {
        Router router = Router.router(vertx);
        
        router.get("/api/health").handler(ctx -> {
            ctx.response().end(new JsonObject()
                .put("status", "UP")
                .put("timestamp", System.currentTimeMillis())
                .encode());
        });
        
        router.get("/api/info").handler(ctx -> {
            ctx.response().end(getVerticleInfo().encode());
        });
        
        return vertx.createHttpServer()
            .requestHandler(router)
            .listen(config().getInteger("port", 8080))
            .mapEmpty();
    }
    
    private Future<Void> startEventBusConsumers() {
        vertx.eventBus().consumer("service.request", message -> {
            // 处理服务请求
            JsonObject request = (JsonObject) message.body();
            processRequest(request)
                .onSuccess(result -> message.reply(result))
                .onFailure(throwable -> message.fail(500, throwable.getMessage()));
        });
        
        return Future.succeededFuture();
    }
    
    private Future<JsonObject> processRequest(JsonObject request) {
        // 业务逻辑处理
        return Future.succeededFuture(new JsonObject().put("result", "processed"));
    }
    
    private Future<Void> startHealthCheck() {
        vertx.setPeriodic(30000, id -> {
            // 定期健康检查
            performHealthCheck()
                .onFailure(throwable -> {
                    System.err.println("Health check failed: " + throwable.getMessage());
                });
        });
        return Future.succeededFuture();
    }
    
    private Future<Void> performHealthCheck() {
        // 实现健康检查逻辑
        return Future.succeededFuture();
    }
    
    private Future<Void> registerService() {
        // 服务注册逻辑
        return Future.succeededFuture();
    }
}
```

#### 2. 工作流处理 Verticle

```java
public class WorkflowProcessorVerticle extends KeelVerticleImpl {
    private WorkQueue<WorkItem> workQueue;
    
    @Override
    protected Future<Void> startVerticle() {
        this.workQueue = new WorkQueue<>();
        
        return Future.all(
            startWorkConsumer(),
            startWorkProducer(),
            startMonitoring()
        ).mapEmpty();
    }
    
    private Future<Void> startWorkConsumer() {
        vertx.setPeriodic(1000, id -> {
            WorkItem item = workQueue.poll();
            if (item != null) {
                processWorkItem(item)
                    .onSuccess(result -> System.out.println("Work item processed: " + result))
                    .onFailure(throwable -> System.err.println("Work item failed: " + throwable.getMessage()));
            }
        });
        return Future.succeededFuture();
    }
    
    private Future<Void> startWorkProducer() {
        vertx.eventBus().consumer("workflow.submit", message -> {
            JsonObject workData = (JsonObject) message.body();
            WorkItem item = new WorkItem(workData);
            workQueue.offer(item);
            message.reply("accepted");
        });
        return Future.succeededFuture();
    }
    
    private Future<String> processWorkItem(WorkItem item) {
        // 实现工作项处理逻辑
        return Future.succeededFuture("completed");
    }
    
    private Future<Void> startMonitoring() {
        vertx.setPeriodic(10000, id -> {
            System.out.println("Queue size: " + workQueue.size());
        });
        return Future.succeededFuture();
    }
    
    // 简单的工作队列实现
    private static class WorkQueue<T> {
        private final Queue<T> queue = new ConcurrentLinkedQueue<>();
        
        public void offer(T item) { queue.offer(item); }
        public T poll() { return queue.poll(); }
        public int size() { return queue.size(); }
    }
    
    private static class WorkItem {
        private final JsonObject data;
        
        public WorkItem(JsonObject data) {
            this.data = data;
        }
        
        public JsonObject getData() { return data; }
    }
}
```

## 最佳实践

### 1. 选择合适的基类
- **推荐使用 `KeelVerticleImpl`**: 适用于大多数场景，提供清晰的生命周期管理
- **使用即时 Verticle**: 适用于简单的、一次性的任务
- **避免使用已弃用的类**: 不要使用带 `@Deprecated` 标记的实现类

### 2. 错误处理
```java
@Override
protected Future<Void> startVerticle() {
    return initializeResources()
        .recover(throwable -> {
            // 启动失败时的恢复逻辑
            System.err.println("Failed to initialize: " + throwable.getMessage());
            return cleanupResources()
                .compose(v -> Future.failedFuture(throwable));
        });
}
```

### 3. 配置管理
```java
@Override
protected Future<Void> startVerticle() {
    JsonObject config = config();
    int port = config.getInteger("server.port", 8080);
    String host = config.getString("server.host", "localhost");
    boolean enableSsl = config.getBoolean("server.ssl.enabled", false);
    
    // 使用配置启动服务
    return startServer(host, port, enableSsl);
}
```

### 4. 资源清理
```java
@Override
public void stop(Promise<Void> stopPromise) {
    cleanupResources()
        .onComplete(ar -> {
            if (ar.succeeded()) {
                stopPromise.complete();
            } else {
                stopPromise.fail(ar.cause());
            }
        });
}

private Future<Void> cleanupResources() {
    return Future.all(
        closeDatabase(),
        shutdownHttpServer(),
        cancelTimers()
    ).mapEmpty();
}
```

## 版本兼容性

- **当前推荐**: `KeelVerticleImpl` (自 4.0.2 版本)
- **即时 Verticle**: `KeelVerticle.instant()` 方法 (自 4.0.2 版本)
- **已弃用**: 所有带特定功能的实现类 (计划在未来版本中移除)

## 总结

Keel Verticles 包提供了完整的 Verticle 生命周期管理解决方案，从简单的即时 Verticle 到复杂的业务逻辑 Verticle，都有相应的实现方式。通过使用推荐的 `KeelVerticleImpl` 基类，开发者可以专注于业务逻辑的实现，而将生命周期管理交给框架处理。
