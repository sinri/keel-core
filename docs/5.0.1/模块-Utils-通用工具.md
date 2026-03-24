# 模块说明：Utils — 通用工具（utils 根包）

**包**：`io.github.sinri.keel.core.utils`  
**版本线**：5.0.1

## 职责

提供与框架无关或弱耦合的常用工具：字符串、JSON、文件、时间、摘要、网络、反射、运行时入口、加解密辅助等。**IO 桥接
**见分册 [模块-Utils-IO与异步流](./模块-Utils-IO与异步流.md)。

## 主要公开类（按名速查）

| 类                     | 典型用途                                                              |
|-----------------------|-------------------------------------------------------------------|
| `StringUtils`         | 字符串处理                                                             |
| `JsonUtils`           | JSON 与对象映射（Jackson 等，细节见 JavaDoc）                                 |
| `FileUtils`           | 文件路径与读写辅助                                                         |
| `TimeUtils`           | 时间日期工具                                                            |
| `DigestUtils`         | 摘要/哈希                                                             |
| `RandomUtils`         | 随机数/抽样                                                            |
| `NetUtils`            | 网络相关                                                              |
| `ReflectionUtils`     | 反射辅助                                                              |
| `BinaryUtils`         | 字节与编码                                                             |
| `CryptographyUtils`   | 加解密通用辅助                                                           |
| `AuthenticationUtils` | 认证相关辅助                                                            |
| `RuntimeUtils`        | JVM/进程级只读快照（与 `utils.runtime` 包配合）                                |
| `IOUtils`             | **`@TechnicalPreview`**：`InputStream`/`OutputStream` 与 Vert.x 流桥接 |

## JPMS

模块对 **`io.github.sinri.keel.core.utils`** 有 **`opens`**，供部分反射场景使用；常规调用仅依赖 **public API** 即可。

## 相关文档

- [模块-Utils-IO与异步流](./模块-Utils-IO与异步流.md)
- [模块-Utils-运行时](./模块-Utils-运行时.md)
- [文档目录](index.md)
