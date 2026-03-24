# 模块说明：Servant — Queue（单节点队列）

**包**：`io.github.sinri.keel.core.servant.queue`  
**版本线**：5.0.1  
**部署语义**：**仅单节点**（类注释明确说明）

## 职责

**`QueueDispatcher`** 作为队列管理 Verticle：循环读取 **运行信号**、在 **工作池** 未满时 **寻找下一个 `QueueTask` 并部署
**；**`QueueTask`** 表示一项可部署的业务任务 Verticle。

## 核心类型

| 类型                            | 说明                                                                   |
|-------------------------------|----------------------------------------------------------------------|
| `QueueDispatcher`             | 抽象基类：实现 **`NextQueueTaskSeeker`**、**`QueueSignalReader`**            |
| `QueueTask`                   | 抽象任务 Verticle：`getTaskReference()`、`getTaskCategory()`、**`run()`** 等 |
| `NextQueueTaskSeeker`         | **`seekNextTask()`**：找出一个 **已锁定** 的任务；无任务返回 empty                    |
| `QueueSignalReader`           | **`readSignal()`**：返回 **`QueueSignal.RUN` / `STOP`**                 |
| `QueueWorkerPoolManager`      | 并发工作数管理；**`buildQueueWorkerPoolManager()`** 可重写（默认 `0` 表示不限制，见源码）    |
| `QueueSignal` / `QueueStatus` | 信号与队列状态枚举                                                            |

## 生命周期摘要

- **`beforeQueueStart()`**：启动前清理，默认成功。
- **`startVerticle`**：进入 **`RUNNING`**，启动 **`routine()`** 循环。
- 每轮：**`readSignal()`** → 若 **STOP** 则停止；若 **RUN** 则在 **`asyncCallRepeatedly`** 中 **`seekNextTask()`** 并 *
  *`task.deployMe(keel)`**。
- 无任务时按 **`getWaitingPeriodInMsWhenTaskFree()`**（默认 10 秒）定时再次 **`routine()`**。

## 部署方式

**`QueueDispatcher.deployMe(Keel)`** 使用 **`ThreadingModel.WORKER`**。

## 注意事项

- **`seekNextTask` 的契约**：返回非空任务时应 **已完成业务侧锁定**，调度器日志中假设「信任 seeker 已加锁」。
- 多副本部署同一队列且无外部协调时 **可能重复消费**，请勿在集群多实例上共用同一逻辑队列键。

## 相关文档

- [模块-Servant-Funnel](./模块-Servant-Funnel.md)
- [用户使用指导](./用户使用指导.md)
