# 模块说明：邮件集成（SMTP）

**包**：`io.github.sinri.keel.core.integration.email.smtp`  
**版本线**：5.0.1

## 职责

将 **keel-base** 配置树中的 SMTP 节点转为 Vert.x **`MailConfig`**，并（历史上）提供简易发送封装。**推荐**直接使用 Vert.x *
*`MailClient`**。

## 主要类型

| 类型                  | 说明                                                                                         |
|---------------------|--------------------------------------------------------------------------------------------|
| `SmtpConfigElement` | 继承 `ConfigElement`，提供 `toMailConfig()` 及 `hostname`、`port`、`username`、`password`、`ssl` 等读取 |
| `KeelSmtpKit`       | **自 5.0.0 起 `@Deprecated`**：内部仍基于 `MailClient`，新代码请自行 `MailClient.create` / `createShared` |

## 推荐集成方式

1. 从配置中取出 SMTP 对应 **`ConfigElement`**，构造 **`SmtpConfigElement`**。
2. 调用 **`toMailConfig()`** 得到 **`MailConfig`**。
3. 使用 **`MailClient.create(vertx, config)`** 或 **`createShared(...)`** 发送 **`MailMessage`**。

`KeelSmtpKit(Vertx, String smtpName)` 一类构造会从 **`ConfigElement.root()`** 下路径 **`email.smtp.{smtpName}`** 读取配置（与
`KeelSmtpKit` 源码一致）；迁移时可对照该结构布置配置。

## 注意事项

- Vert.x **mail** 为传递依赖，版本与 **keel-core** 选用的 **Vert.x** 对齐。
- 证书、代理、池化等高级需求请查阅 **Vert.x MailClient** 文档。

## 相关文档

- [用户使用指导](./用户使用指导.md)
