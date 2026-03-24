# 模块说明：Maids — Gatling（加特林）

**包**：`io.github.sinri.keel.core.maids.gatling`  
**版本线**：5.0.1  
**运行环境**：Vert.x **集群**（SharedData 锁、Counter 等）

## 职责

在集群中 **并发执行** 从「供弹器」加载的任务单元（**`Bullet`**），通过共享锁避免多节点重复取同一任务，并支持 **独占锁集合
** 避免任务冲突。

## 主要类型

| 类型               | 说明                                                                                                        |
|------------------|-----------------------------------------------------------------------------------------------------------|
| `Gatling`        | 抽象 Verticle：循环 `loadOneBullet` → `fireBullet`，控制 **barrels** 并发度                                          |
| `GatlingOptions` | **`gatlingName`**、`barrels`（并发管数）、`averageRestInterval`（无任务休眠）、`bulletLoader`（`Supplier<Future<Bullet>>`） |
| `Bullet`         | 抽象任务：`bulletID()`、`exclusiveLockSet()`、`fire()`、`ejectShell(AsyncResult)`                                 |

## 行为摘要

- 加载任务前对 **`KeelGatling-{gatlingName}-Load`** 取 **SharedData Lock**，在锁内执行 **`bulletLoader`**。
- 对 `Bullet.exclusiveLockSet()` 中每个名称使用 **Counter** 实现互斥；若冲突则释放并失败该次发射。
- 并发度受 **`barrels`** 与内部计数器约束；无子弹时按随机化后的 **`averageRestInterval`** 休眠。

## 扩展与部署说明

`Gatling` 为 **抽象类**，子类需实现 **`buildGatlingLogger()`**。**构造器可见性以你所使用的发行版源码为准
**；若无法在应用模块中直接继承，请将 **JavaDoc / 源码** 作为唯一依据或向维护者确认扩展方式。

## 注意事项

- **`Bullet`** 的 `fire` / `ejectShell` 异常路径应配合日志与重试策略。
- 与 **Pleiades**、**Watchman** 的协同见 [Maids README](../../src/main/java/io/github/sinri/keel/core/maids/README.md)。

## 相关文档

- [模块-Maids-Pleiades](./模块-Maids-Pleiades.md)
- [模块-Maids-Watchman](./模块-Maids-Watchman.md)
