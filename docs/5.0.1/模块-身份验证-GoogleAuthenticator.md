# 模块说明：身份验证 — Google Authenticator

**包**：

- `io.github.sinri.keel.core.utils.authenticator.googleauth`
- `...googleauth.sync`
- `...googleauth.async`

**版本线**：5.0.1

## 职责

实现与 **Google Authenticator（TOTP/HOTP）** 兼容的密钥生成、验证码校验、凭证仓储抽象；提供 **同步** 与 **异步（Vert.x Future）
** 两套接口。

## 主要类型（根包）

| 类型                               | 说明        |
|----------------------------------|-----------|
| `GoogleAuthenticatorConfig`      | 行为配置      |
| `GoogleAuthenticatorKey`         | 密钥材料      |
| `GoogleAuthenticatorQRGenerator` | 二维码内容生成等  |
| `KeyRepresentation`              | 密钥表示形式枚举  |
| `HmacHashFunction`               | HMAC 算法枚举 |
| `ReseedingSecureRandom`          | 安全随机数辅助   |
| `GoogleAuthenticatorException`   | 运行时异常     |

## 同步 API（sync）

| 类型                      | 说明      |
|-------------------------|---------|
| `IGoogleAuthenticator`  | 核心同步接口  |
| `ICredentialRepository` | 凭证持久化抽象 |
| `GoogleAuthenticator`   | 具体实现类   |

## 异步 API（async）

| 类型                           | 说明          |
|------------------------------|-------------|
| `AsyncIGoogleAuthenticator`  | Future 风格接口 |
| `AsyncICredentialRepository` | 异步凭证仓储      |
| `AsyncGoogleAuthenticator`   | 实现类         |

## 注意事项

- 密钥与备份码属于 **高敏感数据**，存储与传输需符合安全规范。
- 异步层与 Vert.x 调度结合，避免在 Event Loop 上执行阻塞加密实现。

## 相关文档

- 子目录 README：`src/main/java/io/github/sinri/keel/core/utils/authenticator/googleauth/sync/README.md`
- [文档目录](index.md)
