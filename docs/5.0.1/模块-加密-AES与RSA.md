# 模块说明：加密 — AES 与 RSA

**包**：

- `io.github.sinri.keel.core.utils.encryption.aes`
- `io.github.sinri.keel.core.utils.encryption.rsa`

**版本线**：5.0.1

## 职责

提供常用 **AES** 模式（ECB/CBC、PKCS5/PKCS7/无填充等）与 **RSA** 密钥对、加解密封装；与 **`ValueEnveloping`** 结合的类型见 *
*`AESValueEnveloping`**。

## AES 主要类型

| 类型                                                                      | 说明             |
|-------------------------------------------------------------------------|----------------|
| `KeelAes`                                                               | AES 操作接口       |
| `KeelAesBase`                                                           | 实现基类           |
| `KeelAesEcbPkcs5Padding`、`KeelAesEcbPkcs7Padding`、`KeelAesEcbNoPadding` | ECB 变体         |
| `KeelAesCbcPkcs5Padding`、`KeelAesCbcPkcs7Padding`                       | CBC 变体         |
| `KeelAesUsingPkcs5Padding`、`KeelAesUsingPkcs7Padding`                   | 填充策略抽象分支       |
| `AESValueEnveloping`                                                    | 与值信封结合的 AES 抽象 |

包内 **`README.md`** 含示例与注意点，建议一并阅读。

## RSA 主要类型

| 类型               | 说明                      |
|------------------|-------------------------|
| `KeelRSAKeyPair` | 密钥对封装                   |
| `KeelRSA`        | 继承密钥对，提供运算入口（见 JavaDoc） |

## 模块导出范围

**`module-info.java` 当前 export `aes` 与 `rsa`。**  
**`bcrypt` 等包未 export**，应用模块不应依赖未导出包中的类型。

## 安全提示

- 密钥长度、模式、IV、随机数来源须符合当前安全基线；避免误用 **ECB** 处理结构化明文。
- 仅依赖本库不能替代完整威胁建模与合规要求。

## 相关文档

- `src/main/java/io/github/sinri/keel/core/utils/encryption/aes/README.md`
- [模块-Utils-值与装箱](./模块-Utils-值与装箱.md)
- [文档目录](index.md)
