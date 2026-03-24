# 模块说明：Utils — 值与装箱（value）

**包**：`io.github.sinri.keel.core.utils.value`  
**版本线**：5.0.1

## 职责

提供轻量 **值容器** 与 **信封式编解码** 抽象，用于在 API 边界表达「可能尚未赋值」「需统一封装」等场景；加密包中的 *
*`AESValueEnveloping`** 等可与之组合。

## 主要类型

| 类型                     | 说明                                                       |
|------------------------|----------------------------------------------------------|
| `ValueBox<T>`          | 可变单值容器，泛型 `T` 可为可空类型参数                                   |
| `ValueEnveloping<R,E>` | 信封接口：读写字段与错误/元数据的统一形状（见 JavaDoc）                         |
| `AESValueEnveloping`   | 位于 **`encryption.aes`** 包，扩展 `ValueEnveloping`，面向 AES 场景 |

## 使用建议

- 优先阅读各接口/类上的 **JavaDoc**，确认 **线程安全** 与 **null 语义**。
- 不要把 `ValueBox` 当作分布式缓存或持久化结构使用。

## 相关文档

- [模块-加密-AES与RSA](./模块-加密-AES与RSA.md)
- [文档目录](index.md)
