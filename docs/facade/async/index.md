# KeelAsyncMixin

`KeelAsyncMixin` 是一个混入接口，为异步操作提供了结构化和便捷的工具方法。它基于 Vert.x Future 模式，提供了处理集合、迭代器、重复任务、独占访问等功能的异步方法。

## 概述

该接口自 4.0.0 版本引入，提供了以下主要功能：

- **并行处理**：对集合或迭代器中的元素进行并行异步处理
- **迭代处理**：按批次或逐个处理集合元素
- **重复执行**：重复执行异步任务直到满足停止条件
- **步进执行**：按步长执行指定次数的异步任务
- **独占执行**：使用锁机制确保异步任务的独占执行
- **Future 转换**：在不同类型的 Future 之间进行转换
- **阻塞代码执行**：在工作线程中执行阻塞代码

## 使用方式

### 引入依赖

首先需要引入 Keel 实例的静态导入：

```java
import static io.github.sinri.keel.facade.KeelInstance.Keel;
```

### 调用方法

通过 `io.github.sinri.keel.facade.KeelInstance#Keel` 来调用本接口的方法：

```java
// 示例：并行处理集合
List<String> items = Arrays.asList("item1", "item2", "item3");
Keel.parallelForAllSuccess(items, item -> {
    return processItem(item);
}).onSuccess(v -> {
    System.out.println("所有项目处理完成");
});

// 示例：异步睡眠
Keel.asyncSleep(1000)
    .compose(v -> {
        System.out.println("睡眠结束，继续执行");
        return Future.succeededFuture();
    });

// 示例：重复执行任务
Keel.asyncCallRepeatedly(task -> {
    return checkCondition()
        .compose(shouldContinue -> {
            if (!shouldContinue) {
                task.stop();
            }
            return Future.succeededFuture();
        });
});
```

## 方法分类

### 1. 并行处理方法

#### parallelForAllSuccess
并行执行所有任务，等待全部成功完成。

```java
// 处理集合
<T> Future<Void> parallelForAllSuccess(Iterable<T> collection, Function<T, Future<Void>> itemProcessor)

// 处理迭代器
<T> Future<Void> parallelForAllSuccess(Iterator<T> iterator, Function<T, Future<Void>> itemProcessor)
```

**使用示例：**
```java
List<String> urls = Arrays.asList("url1", "url2", "url3");
Keel.parallelForAllSuccess(urls, url -> {
    // 异步处理每个 URL
    return httpClient.get(url);
}).onSuccess(v -> {
    System.out.println("所有 URL 处理完成");
});
```

#### parallelForAnySuccess
并行执行所有任务，等待任意一个成功完成。

```java
<T> Future<Void> parallelForAnySuccess(Iterable<T> collection, Function<T, Future<Void>> itemProcessor)
<T> Future<Void> parallelForAnySuccess(Iterator<T> iterator, Function<T, Future<Void>> itemProcessor)
```

#### parallelForAllComplete
并行执行所有任务，等待全部完成（无论成功或失败）。

```java
<T> Future<Void> parallelForAllComplete(Iterable<T> collection, Function<T, Future<Void>> itemProcessor)
<T> Future<Void> parallelForAllComplete(Iterator<T> iterator, Function<T, Future<Void>> itemProcessor)
```

### 2. 重复执行方法

#### asyncCallRepeatedly
重复执行任务直到任务自身发出停止信号。

```java
Future<Void> asyncCallRepeatedly(Function<RepeatedlyCallTask, Future<Void>> processor)
```

**使用示例：**
```java
Keel.asyncCallRepeatedly(task -> {
    return checkCondition()
        .compose(shouldContinue -> {
            if (!shouldContinue) {
                task.stop(); // 停止重复执行
            }
            return Future.succeededFuture();
        });
});
```

### 3. 迭代处理方法

#### asyncCallIteratively (批处理)
按批次处理迭代器中的元素。

```java
// 带任务控制的批处理
<T> Future<Void> asyncCallIteratively(Iterator<T> iterator, 
    BiFunction<List<T>, RepeatedlyCallTask, Future<Void>> itemsProcessor, int batchSize)

// 简单批处理
<T> Future<Void> asyncCallIteratively(Iterator<T> iterator, 
    Function<List<T>, Future<Void>> itemsProcessor, int batchSize)
```

**使用示例：**
```java
Iterator<User> users = getUserIterator();
Keel.asyncCallIteratively(users, batch -> {
    // 批量处理用户数据
    return batchProcessUsers(batch);
}, 100); // 每批处理 100 个用户
```

#### asyncCallIteratively (逐个处理)
逐个处理迭代器中的元素。

```java
// 带任务控制的逐个处理
<T> Future<Void> asyncCallIteratively(Iterator<T> iterator, 
    BiFunction<T, RepeatedlyCallTask, Future<Void>> itemProcessor)

// 简单逐个处理
<T> Future<Void> asyncCallIteratively(Iterator<T> iterator, 
    Function<T, Future<Void>> itemProcessor)
```

### 4. 步进执行方法

#### asyncCallStepwise
按步长执行指定范围或次数的异步任务。

```java
// 指定范围和步长
Future<Void> asyncCallStepwise(long start, long end, long step, 
    BiFunction<Long, RepeatedlyCallTask, Future<Void>> processor)

// 指定次数（带任务控制）
Future<Void> asyncCallStepwise(long times, 
    BiFunction<Long, RepeatedlyCallTask, Future<Void>> processor)

// 指定次数（简单版本）
Future<Void> asyncCallStepwise(long times, Function<Long, Future<Void>> processor)
```

**使用示例：**
```java
// 处理 0 到 100，步长为 10
Keel.asyncCallStepwise(0, 100, 10, (step, task) -> {
    return processStep(step);
});

// 执行 50 次
Keel.asyncCallStepwise(50, step -> {
    return executeTask(step);
});
```

### 5. 无限循环执行

#### asyncCallEndlessly
无限循环执行异步任务。

```java
void asyncCallEndlessly(Supplier<Future<Void>> supplier)
```

**使用示例：**
```java
Keel.asyncCallEndlessly(() -> {
    return performPeriodicTask()
        .recover(throwable -> {
            // 处理错误，继续执行
            return Future.succeededFuture();
        });
});
```

### 6. 异步睡眠

#### asyncSleep
异步睡眠指定时间。

```java
Future<Void> asyncSleep(long time)
Future<Void> asyncSleep(long time, Promise<Void> interrupter)
```

**使用示例：**
```java
Keel.asyncSleep(5000) // 睡眠 5 秒
    .compose(v -> continueProcessing());

// 可中断的睡眠
Promise<Void> interrupter = Promise.promise();
Keel.asyncSleep(10000, interrupter)
    .onComplete(ar -> System.out.println("睡眠结束"));
// 在其他地方中断睡眠
interrupter.complete();
```

### 7. 独占执行

#### asyncCallExclusively
使用锁机制确保异步任务的独占执行。

```java
<T> Future<T> asyncCallExclusively(String lockName, long waitTimeForLock, 
    Supplier<Future<T>> exclusiveSupplier)

<T> Future<T> asyncCallExclusively(String lockName, 
    Supplier<Future<T>> exclusiveSupplier)
```

**使用示例：**
```java
Keel.asyncCallExclusively("resource-lock", () -> {
    return accessSharedResource();
}).onSuccess(result -> {
    System.out.println("独占访问完成: " + result);
});
```

### 8. Future 转换

#### asyncTransformCompletableFuture
将 CompletableFuture 转换为 Vert.x Future。

```java
<R> Future<R> asyncTransformCompletableFuture(CompletableFuture<R> completableFuture)
```

#### asyncTransformRawFuture
将原生 Java Future 转换为 Vert.x Future（使用轮询机制）。

```java
<R> Future<R> asyncTransformRawFuture(java.util.concurrent.Future<R> rawFuture, long sleepTime)
```

### 9. 阻塞代码执行

#### executeBlocking
在工作线程中执行阻塞代码。

```java
<T> Future<T> executeBlocking(Handler<Promise<T>> blockingCodeHandler)
```

**使用示例：**
```java
Keel.executeBlocking(promise -> {
    try {
        // 执行阻塞操作
        String result = performBlockingOperation();
        promise.complete(result);
    } catch (Exception e) {
        promise.fail(e);
    }
}).onSuccess(result -> {
    System.out.println("阻塞操作完成: " + result);
});
```

#### pseudoAwait
执行阻塞代码并同步等待结果。

```java
<T> T pseudoAwait(Handler<Promise<T>> blockingCodeHandler)
```

## RepeatedlyCallTask 内部类

`RepeatedlyCallTask` 是一个工具类，用于控制重复执行的任务。

### 主要方法

- `stop()`: 停止重复执行
- `start(RepeatedlyCallTask, Promise<Void>)`: 开始执行任务

### 特性

- 任务之间有 1 毫秒的延迟
- 可以通过调用 `stop()` 方法停止执行
- 如果任务失败，整个重复执行会停止

## 最佳实践

### 1. 错误处理
```java
Keel.parallelForAllSuccess(items, item -> {
    return processItem(item)
        .recover(throwable -> {
            logger.error("处理项目失败: " + item, throwable);
            return Future.succeededFuture(); // 继续处理其他项目
        });
});
```

### 2. 资源管理
```java
Keel.asyncCallExclusively("database-lock", () -> {
    return databaseOperation()
        .compose(result -> cleanup().map(result));
});
```

### 3. 批处理优化
```java
// 根据数据量调整批次大小
int batchSize = items.size() > 10000 ? 1000 : 100;
Keel.asyncCallIteratively(items.iterator(), batch -> {
    return processBatch(batch);
}, batchSize);
```

### 4. 超时控制
```java
Promise<Void> timeout = Promise.promise();
vertx.setTimer(30000, id -> timeout.complete()); // 30秒超时

Keel.asyncSleep(Long.MAX_VALUE, timeout)
    .compose(v -> longRunningTask());
```

## 版本历史

- **4.0.0**: 初始版本
- **4.0.2**: 添加并行处理方法
- **4.0.6**: 修复 `asyncTransformCompletableFuture` 方法名拼写错误
- **4.0.9**: 修复步进执行中的范围中断问题

## 注意事项

1. 所有方法都是基于 Vert.x 的事件循环模型，不会阻塞事件循环
2. 批处理大小应根据实际数据量和内存限制进行调整
3. 使用独占执行时要注意死锁问题
4. 长时间运行的任务应该提供适当的错误处理和恢复机制
5. `pseudoAwait` 方法会阻塞当前线程，应谨慎使用

