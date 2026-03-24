# 模块说明：Servant — Intravenous（静脉）

**包**：`io.github.sinri.keel.core.servant.intravenous`  
**版本线**：5.0.1

## 职责

以 Verticle 形式 **FIFO** 接收类型 **`D`** 的「滴液」对象，按 **单条** 或 **批量** 处理器消费；支持 **shutdown**、*
*shutdownAndAwait** 等生命周期控制。

## 工厂方法

| 方法                                                 | 说明     |
|----------------------------------------------------|--------|
| `Intravenous.instant(SingleDropProcessor<T>)`      | 单条处理实现 |
| `Intravenous.instantBatch(MultiDropsProcessor<T>)` | 批量处理实现 |

具体实现类为包内 **`IntravenousSingleImpl` / `IntravenousBatchImpl`**，通过上述静态方法创建。

## 主要抽象 API

- **`add(D drop)`**：注入待处理项。
- **`isNoDropsLeft()`** / **`isStopped()`**：状态查询。
- **`shutdown()`**、**`shutdownAndAwait()`**：停止接收并等待处理收尾。
- **`handleAllergy(Throwable)`**：处理中异常回调，默认空实现，可覆盖。

## 部署与线程模型

作为 **`KeelVerticleBase`** 子类，通过 **`deployMe(Keel, DeploymentOptions)`** 部署。**`IntravenouslyCutter`** 内部将 *
*`Intravenous`** 以 **WORKER** 模型部署（见 cutter 模块）。

## 相关文档

- [模块-流切分-Cutter](./模块-流切分-Cutter.md)
- [用户使用指导](./用户使用指导.md)
