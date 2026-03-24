# 模块说明：流切分（cutter）

**包**：`io.github.sinri.keel.core.cutter`  
**版本线**：5.0.1

## 职责

将 **Vert.x `Buffer` 流** 按自定义规则切成业务对象 `T`，再交给 **`Intravenous`
** 按 FIFO 处理；适用于 SSE、自定义分帧协议等「字节流 → 结构化片段」场景。

## 主要类型

| 类型                            | 说明                                                                                                          |
|-------------------------------|-------------------------------------------------------------------------------------------------------------|
| `IntravenouslyCutter<T>`      | 抽象 Verticle：接收 `acceptFromStream(Buffer)`、结束时 `stopHere()` / `stopHere(Throwable)`、等待 `waitForAllHandled()` |
| `IntravenouslyCutterOnString` | 内置实现：按 **两个换行符 `\n\n`** 切分为 `String`（面向 SSE 等文本流）                                                           |
| `CutterTimeout`               | 超时场景下的异常类型                                                                                                  |

## 推荐步骤（抽象基类文档摘要）

1. 部署切分器 Verticle 后，持续调用 **`acceptFromStream(Buffer)`** 喂入数据。
2. 流结束时调用 **`stopHere()`** 或 **`stopHere(throwable)`**。
3. 调用 **`waitForAllHandled()`** 等待切片全部进入并完成下游处理。

子类实现 **`List<T> cut()`**：从 **`getBufferRef()`** 当前缓冲区解析出尽可能多的 `T`，并 **更新 buffer 为剩余未消费部分
**（见基类 JavaDoc）。

## 与 Intravenous 的关系

构造函数传入 `Intravenous.SingleDropProcessor<T>` 与可选 **超时毫秒数**（`>0` 时内部会注册超时并可能以
`CutterTimeout` 结束）。内部通过 **`Intravenous.instant(...)`** 部署为 **WORKER** 线程模型。

## 注意事项

- `acceptFromStream` / `cut` 路径上存在 **同步块**，高吞吐时需自行压测。
- 超时行为与 **`Intravenous`** 生命周期见 [模块-Servant-Intravenous](./模块-Servant-Intravenous.md)。

## 相关文档

- [模块-Servant-Intravenous](./模块-Servant-Intravenous.md)
- [用户使用指导](./用户使用指导.md)
