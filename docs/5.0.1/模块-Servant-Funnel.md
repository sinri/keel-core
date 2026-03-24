# 模块说明：Servant — Funnel（烟囱）

**包**：`io.github.sinri.keel.core.servant.funnel`  
**版本线**：5.0.1

## 职责

在 **单 Verticle** 内将多个 **`Supplier<Future<Void>>`** 按 **FIFO** 串行执行，带有 **休眠间隔** 与 **新任务唤醒
** 机制，适合把零散异步任务排成一条管道、避免并发交织。

## 主要类型

| 类型       | 说明                                                            |
|----------|---------------------------------------------------------------|
| `Funnel` | 继承 `KeelVerticleBase`，通过 **`add(Supplier<Future<Void>>)`** 入队 |

## 典型用法思路

1. 部署 **`Funnel`** Verticle。
2. 将异步工作包装为 **`Supplier<Future<Void>>`**，调用 **`add(...)`** 入队。
3. 通过 **`setSleepTime(long)`** 调节空闲时的轮询/休眠节奏（过小会抛 `IllegalArgumentException`，见源码）。

## 注意事项

- 语义为 **单实例内串行**，非分布式队列。
- 与 **QueueDispatcher** 的区别：`Funnel` 侧重「通用异步任务管道」，队列包侧重「可部署的 `QueueTask` 与信号控制」。

## 相关文档

- [模块-Servant-Queue](./模块-Servant-Queue.md)
- [用户使用指导](./用户使用指导.md)
