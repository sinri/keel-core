# Intravenous

## 概述

Intravenous 包提供了一个用于按顺序处理特定类型对象任务的框架。该包实现了一个基于 Vert.x Verticle 的异步任务处理系统，支持单个任务处理和批量任务处理两种模式。

**版本信息**: 自 4.0.7 版本引入

## 核心接口

### KeelIntravenous<D>

主要接口，继承自 `KeelVerticle`，定义了任务处理的核心功能。

#### 静态工厂方法

```java
// 创建单任务处理实例
static <T> KeelIntravenous<T> instant(@Nonnull SingleDropProcessor<T> itemProcessor)

// 创建批量任务处理实例  
static <T> KeelIntravenous<T> instantBatch(@Nonnull MultiDropsProcessor<T> itemsProcessor)
```

#### 核心方法

- `void add(D drop)` - 添加待处理的任务对象
- `void handleAllergy(Throwable throwable)` - 处理任务执行过程中的异常（默认忽略）
- `boolean isNoDropsLeft()` - 检查是否还有未处理的任务 (since 4.0.11)
- `boolean isStopped()` - 检查是否已停止接收新任务 (since 4.0.11)
- `void shutdown()` - 停止接收新任务
- `boolean isUndeployed()` - 检查 Verticle 是否已卸载 (since 4.0.11)

#### 内部接口

**SingleDropProcessor<T>**
```java
Future<Void> process(T drop)
```
用于处理单个任务对象的处理器接口。

**MultiDropsProcessor<T>**
```java
Future<Void> process(List<T> drops)
```
用于批量处理任务对象的处理器接口。

## 实现类

### KeelIntravenousBase<D>

抽象基类，继承自 `KeelVerticleImpl`，实现了 `KeelIntravenous` 接口的核心功能。

#### 核心特性

- **线程安全的队列**: 使用 `ConcurrentLinkedQueue` 存储待处理任务
- **原子状态管理**: 使用 `AtomicBoolean` 管理停止和卸载状态
- **中断机制**: 通过 `AtomicReference<Promise<Void>>` 实现任务添加时的即时唤醒
- **异步循环处理**: 基于 Vert.x 的异步重复调用机制

#### 处理流程

1. 启动 Verticle 时开始异步循环处理
2. 每次循环检查队列中的任务并批量取出
3. 调用抽象方法 `handleDrops()` 处理任务
4. 如果没有任务且未停止，等待新任务或超时（1秒）
5. 停止时结束循环并卸载 Verticle

### KeelIntravenousSingleImpl<D>

单任务处理实现类，继承自 `KeelIntravenousBase`。

#### 特性

- **逐个处理**: 使用 `Keel.asyncCallIteratively()` 逐个处理任务列表中的每个对象
- **异常处理**: 捕获处理过程中的异常并调用 `handleAllergy()` 方法
- **无睡眠设计**: 4.0.7 版本的新实现，移除了睡眠机制

### KeelIntravenousBatchImpl<D>

批量任务处理实现类，继承自 `KeelIntravenousBase`。

#### 特性

- **批量处理**: 将所有待处理任务作为一个列表传递给处理器
- **异常处理**: 统一处理整个批次的异常
- **高效处理**: 适用于可以批量优化的任务场景

## 使用示例

### 单任务处理

```java
// 创建单任务处理器
KeelIntravenous.SingleDropProcessor<String> processor = (message) -> {
    System.out.println("Processing: " + message);
    return Future.succeededFuture();
};

// 创建 Intravenous 实例
KeelIntravenous<String> intravenous = KeelIntravenous.instant(processor);

// 部署 Verticle
Keel.getVertx().deployVerticle(intravenous);

// 添加任务
intravenous.add("Task 1");
intravenous.add("Task 2");

// 停止接收新任务
intravenous.shutdown();
```

### 批量任务处理

```java
// 创建批量任务处理器
KeelIntravenous.MultiDropsProcessor<String> batchProcessor = (messages) -> {
    System.out.println("Processing batch of " + messages.size() + " messages");
    // 批量处理逻辑
    return Future.succeededFuture();
};

// 创建批量 Intravenous 实例
KeelIntravenous<String> batchIntravenous = KeelIntravenous.instantBatch(batchProcessor);

// 部署和使用
Keel.getVertx().deployVerticle(batchIntravenous);
batchIntravenous.add("Message 1");
batchIntravenous.add("Message 2");
batchIntravenous.add("Message 3");
```

### 异常处理

```java
KeelIntravenous<String> intravenous = KeelIntravenous.instant(message -> {
    if (message.contains("error")) {
        throw new RuntimeException("Processing failed");
    }
    return Future.succeededFuture();
}) {
    @Override
    public void handleAllergy(Throwable throwable) {
        System.err.println("Task processing failed: " + throwable.getMessage());
        // 自定义异常处理逻辑
    }
};
```

## 设计模式

### 生产者-消费者模式

Intravenous 实现了经典的生产者-消费者模式：
- **生产者**: 通过 `add()` 方法添加任务的代码
- **消费者**: 内部的异步处理循环
- **缓冲区**: 线程安全的 `ConcurrentLinkedQueue`

### 策略模式

通过 `SingleDropProcessor` 和 `MultiDropsProcessor` 接口，允许用户定义不同的任务处理策略。

### 模板方法模式

`KeelIntravenousBase` 定义了任务处理的框架，具体的处理逻辑由子类实现。

## 注意事项

1. **线程安全**: 所有公共方法都是线程安全的
2. **异常处理**: 默认情况下异常会被忽略，建议重写 `handleAllergy()` 方法
3. **生命周期管理**: 需要手动调用 `shutdown()` 来停止接收新任务
4. **资源清理**: Verticle 会在处理完所有任务后自动卸载
5. **状态检查**: 可以通过 `isNoDropsLeft()`、`isStopped()`、`isUndeployed()` 方法检查当前状态

## Legacy 包

`legacy` 子包包含了旧版本的实现，主要用于向后兼容。新项目建议使用当前版本的实现。