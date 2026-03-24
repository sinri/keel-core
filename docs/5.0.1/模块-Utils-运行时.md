# 模块说明：Utils — 运行时（runtime）

**包**：`io.github.sinri.keel.core.utils.runtime`  
**版本线**：5.0.1

## 职责

采集 **GC、CPU、JVM 内存** 等指标，提供 **单次快照 record** 与 **周期性监控 Verticle**；依赖 **OSHI**（模块
`requires com.github.oshi`）等实现底层探测。

## 主要类型

| 类型                                                              | 说明                                                                         |
|-----------------------------------------------------------------|----------------------------------------------------------------------------|
| `KeelRuntimeMonitor`                                            | 继承 `KeelVerticleBase`：按 **`interval`** 周期调用 **`Handler<MonitorSnapshot>`** |
| `MonitorSnapshot`                                               | 一次采样的聚合快照（含与上次对比的差分）                                                       |
| `GCStatResult`、`CPUTimeResult`、`JVMMemoryResult`、`MemoryResult` | 各维度统计 **record**                                                           |
| `RuntimeStatResult<T>`                                          | 统计结果通用接口                                                                   |

根包 **`RuntimeUtils`** 提供静态快照方法（如 **`getGCSnapshot`**、**`getCPUTimeSnapshot`**、**`makeJVMMemorySnapshot`**），供
**`KeelRuntimeMonitor`** 与业务直接使用。

## KeelRuntimeMonitor 用法思路

1. 构造时传入 **采样间隔（毫秒）** 与 **`Handler<MonitorSnapshot>`**。
2. 部署该 Verticle；在 handler 中上报指标或写入日志。
3. 首次采样可能产生「空差分」占位，随后为相邻两次差值（见源码）。

## 注意事项

- 采样有 **开销**，生产环境请设置合理 **interval**。
- 权限不足或容器环境可能导致部分 OSHI 数据不可用，需容错。

## 相关文档

- [模块-Utils-通用工具](./模块-Utils-通用工具.md)
- [文档目录](index.md)
