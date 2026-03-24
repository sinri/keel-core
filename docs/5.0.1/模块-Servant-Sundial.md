# 模块说明：Servant — Sundial（日晷）

**包**：`io.github.sinri.keel.core.servant.sundial`  
**版本线**：5.0.1  
**部署语义**：**单节点** Cron/分钟级调度

## 职责

在 **单个 Vert.x 实例** 内，约 **每分钟** 对齐时钟，结合 **`KeelCronExpression`** 判断 **`SundialPlan`** 是否命中；命中则以
**`DeploymentOptions`**（含 **`ThreadingModel`**）**部署独立 Verticle** 执行计划逻辑。

## 核心类型

| 类型            | 说明                                                                                           |
|---------------|----------------------------------------------------------------------------------------------|
| `Sundial`     | 抽象 Verticle：实现 **`fetchPlans()`** 返回计划集合或 `null`（`null` 表示不更新快照）                             |
| `SundialPlan` | 计划：`key()`、`cronExpression()`、`execute(Keel, Calendar, logger)`、`expectedThreadingModel()` 等 |

## 计划刷新

内部通过 **`asyncCallExclusively`** 与固定锁名周期性调用 **`fetchPlans()`**，对 **`planMap` 做增删**（见源码
`refreshPlans`）。

## 部署方式

**`Sundial.deployMe(Keel)`** 固定为 **`ThreadingModel.WORKER`**。

## 与 Watchman 的对照

|      | Sundial        | CronWatchman / PureWatchman |
|------|----------------|-----------------------------|
| 拓扑   | 单节点            | 集群协调                        |
| 典型用途 | 本机定时、开发或小规模单实例 | 多节点仅一处执行                    |

## 相关文档

- [模块-Maids-Watchman](./模块-Maids-Watchman.md)
- [模块-Utils-Cron](./模块-Utils-Cron.md)
- [用户使用指导](./用户使用指导.md)
