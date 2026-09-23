# 模块说明：流切分（cutter）

**包**：`io.github.sinri.keel.core.cutter`  
**版本线**：5.0.1（下述换行与异常处理行为已同步至 5.0.4 修复）

## 职责

将 **Vert.x `Buffer` 流** 按自定义规则切成业务对象 `T`，再交给 **`Intravenous`
** 按 FIFO 处理；适用于 SSE、自定义分帧协议等「字节流 → 结构化片段」场景。

## 主要类型

| 类型                            | 说明                                                                                                          |
|-------------------------------|-------------------------------------------------------------------------------------------------------------|
| `IntravenouslyCutter<T>`      | 抽象 Verticle：接收 `acceptFromStream(Buffer)`、结束时 `stopHere()` / `stopHere(Throwable)`、等待 `waitForAllHandled()` |
| `IntravenouslyCutterOnString` | 内置实现：按空行切分为 `String`，支持 LF、CRLF、CR 及混合换行（面向 SSE 等文本流）                                                           |
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

## 字符串切分与失败语义

- 完整片段形成后才解码 UTF-8，返回字符串内部的行结束符统一为 `\n`，不含末尾分隔换行。
- CRLF 可跨输入 Buffer；中文、表情等 UTF-8 字符跨 Buffer 时保留原始字节，不提前解码。
- EOF 不强制交付未形成空行边界的片段；`getBufferRef()` 仍保留未完成部分的原始字节。
- 仅负责切分，不解析 `data:` 等 SSE 字段；保留原有 LF 输入的空片段行为。
- 片段按 FIFO 串行处理。处理器同步抛错或返回失败 Future 后，后续片段继续处理；
  `waitForAllHandled()` 在处理结束后返回首个处理异常。
- `stopHere(throwable)` 指定的异常及 `CutterTimeout` 优先于处理异常返回。
  此异常收集仅作用于切分器，不改变其他 `Intravenous` 使用者。

## 注意事项

- `acceptFromStream` / `cut` 路径上存在 **同步块**，高吞吐时需自行压测。
- 超时行为与 **`Intravenous`** 生命周期见 [模块-Servant-Intravenous](./模块-Servant-Intravenous.md)。

## 相关文档

- [模块-Servant-Intravenous](./模块-Servant-Intravenous.md)
- [用户使用指导](./用户使用指导.md)
