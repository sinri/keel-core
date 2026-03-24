# 模块说明：Maids — Watchman（更夫）

**包**：`io.github.sinri.keel.core.maids.watchman`  
**版本线**：5.0.1  
**运行环境**：Vert.x **集群**（依赖 EventBus、SharedData 锁等）

## 职责

在集群中 **按周期** 触发定时逻辑；每个周期通过分布式协调保证 **大致只有一个节点** 执行（与单节点 `Sundial` 对照）。

## 主要类型

| 类型                     | 说明                                                                                                                          |
|------------------------|-----------------------------------------------------------------------------------------------------------------------------|
| `Watchman`             | 接口：扩展 `KeelVerticle`，提供 `watchmanName()`、`interval()`、`regularHandler()`                                                    |
| `PureWatchman`         | 固定间隔调度：静态方法 **`deploy(Keel, String watchmanName, Handler<Options> optionsHandler)`**                                        |
| `CronWatchman`         | Cron 风格：静态方法 **`deploy(Keel, String watchmanName, Function<String, Future<Void>> cronTabUpdateStartup)`**，行为类似集群版 `Sundial` |
| `WatchmanEventHandler` | 继承 `Handler<Long>` 的定时回调类型                                                                                                  |

`WatchmanImpl` 为包内基类，业务侧通常只接触 **`PureWatchman`** / **`CronWatchman`**。

## PureWatchman 要点

- 在 **`Options`** 回调中设置 **`interval`** 与 **`handler`**（`WatchmanEventHandler`）。
- 部署时使用 **`ThreadingModel.WORKER`**（见 `deploy` 实现）。

## CronWatchman 要点

- 启动时通过 **`cronTabUpdateStartup`** 维护集群 AsyncMap 中的 Cron 任务表（细节见类与 JavaDoc）。
- 触发时按当前时间与各条 Cron 表达式匹配后调用已注册处理器。

## 注意事项

- 集群内 **各节点实现应一致**；消息体须 **可序列化**。
- 更完整的集群语义与协作关系见 [Maids README](../../src/main/java/io/github/sinri/keel/core/maids/README.md)。

## 相关文档

- [模块-Servant-Sundial](./模块-Servant-Sundial.md)（单节点对照）
- [模块-Utils-Cron](./模块-Utils-Cron.md)
- [文档目录](index.md)
