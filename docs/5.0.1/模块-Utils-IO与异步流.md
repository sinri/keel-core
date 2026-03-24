# 模块说明：Utils — IO 与异步流（utils.io + IOUtils）

**包**：`io.github.sinri.keel.core.utils.io`；桥接入口另见 **`io.github.sinri.keel.core.utils.IOUtils`**  
**版本线**：5.0.1

## 职责

把 **阻塞** 的 **`InputStream` / `OutputStream`** 适配为 Vert.x **`ReadStream<Buffer>` / `WriteStream<Buffer>`**，读写在
**worker** 上执行，避免阻塞 Event Loop。

## 主要类型

| 类型                      | 说明                                                                                                 |
|-------------------------|----------------------------------------------------------------------------------------------------|
| `AsyncOutputReadStream` | 工厂 **`create(Keel)`** → **`wrap(InputStream)`**；读完关注 **`readOver()`** / **`getReadOverPromise()`** |
| `AsyncInputWriteStream` | 工厂 **`create(Keel)`** → **`wrap(OutputStream)`**；写完关注 **`writeOver()`**                            |
| `IOUtils`               | **`@TechnicalPreview`**：`toReadStream` / `toWriteStream` 先装配 handler 再 `wrap`                      |

## IOUtils 推荐顺序（读流）

1. **`AsyncOutputReadStream.create(keel)`** 并 **`pause()`**。
2. 将实例交给 handler（例如 **`pipe`** 到目标 `WriteStream`）。
3. **`wrap(inputStream)`** 后 **`resume()`**。

写流：`create` → handler 装配 → **`wrap(outputStream)`**。

## 使用注意

- **阻塞读无法被简单取消**：超时或失败路径下，底层 **`InputStream.read`** 仍可能占用 worker，详见仓库 **`docs/项目设计与实现分析.md`
  ** 与实现类 JavaDoc。
- **`InputStream` 线程安全**、EOF、关闭顺序需由业务保证。
- **`chunkSize`、pause/resume** 等行为以 **`AsyncOutputReadStreamImpl`** 等实现为准。

## 相关文档

- [用户使用指导](./用户使用指导.md)
- [项目设计与实现分析](../项目设计与实现分析.md)
