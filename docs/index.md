# Keel 框架文档目录

Keel 是一个基于 Vert.x 生态系统的 Java 框架，专为 Web 应用、任务处理等项目而设计。本文档提供了 Keel 框架各模块的完整说明和使用指南。

## 框架概述

Keel 框架是一个功能完整的异步 Java 框架，构建在 Vert.x 4.5.11 之上，提供了 Web 服务、数据库集成、日志记录、缓存管理等全方位的解决方案。框架采用响应式编程模型，支持高并发、高性能的应用开发。

* 当前版本: 4.0.13
* License: GNU GENERAL PUBLIC LICENSE version 3

## Maven 坐标

```xml
<dependency>
    <groupId>io.github.sinri</groupId>
    <artifactId>Keel</artifactId>
    <version>4.0.13</version>
</dependency>
```

## 核心模块 (Core)

核心模块提供了 Keel 框架的基础功能和工具类，是其他所有模块的基础。

### 核心功能
框架的核心抽象和基础组件。详细说明请参考 [核心功能文档](./core/index.md)。

### 辅助工具类
完整的工具类集合，包含日常开发所需的各种工具。详细说明请参考 [辅助工具类文档](./core/helper/index.md)：

- **[KeelBinaryHelper](./core/helper/KeelBinaryHelper.md)** - 二进制数据处理，支持十六进制、Base64、Base32 编码
- **[KeelDateTimeHelper](./core/helper/KeelDateTimeHelper.md)** - 日期时间处理，支持多种格式、时区转换、Cron 表达式
- **[KeelFileHelper](./core/helper/KeelFileHelper.md)** - 文件系统操作，支持异步文件操作、JAR 文件处理、压缩文件操作
- **[KeelJsonHelper](./core/helper/KeelJsonHelper.md)** - JSON 数据处理，支持深度读写、键链访问、格式化输出
- **[KeelNetHelper](./core/helper/KeelNetHelper.md)** - 网络工具，支持 IP 地址处理、本机信息获取
- **[KeelReflectionHelper](./core/helper/KeelReflectionHelper.md)** - 反射工具，支持注解处理、类扫描、动态加载
- **[KeelDigestHelper](./core/helper/KeelDigestHelper.md)** - 摘要算法，支持 MD5、SHA 系列、HMAC 算法
- **[KeelCryptographyHelper](./core/helper/KeelCryptographyHelper.md)** - 加密工具，支持 AES、RSA 加密
- **[KeelAuthenticationHelper](./core/helper/KeelAuthenticationHelper.md)** - 身份认证，支持 BCrypt 密码哈希、Google Authenticator
- **[KeelRandomHelper](./core/helper/KeelRandomHelper.md)** - 随机数生成器

### JSON 处理
JSON 数据操作和管理。详细说明请参考 [JSON 处理文档](./core/json/index.md)：

- **[JsonifiableEntity](./core/json/JsonifiableEntity.md)** - JSON 可序列化实体
- **[UnmodifiableJsonifiableEntity](./core/json/UnmodifiableJsonifiableEntity.md)** - 不可变 JSON 实体
- **[IETF-RFC-6901](./core/json/IETF-RFC-6901.md)** - JSON 指针规范实现

### 缓存系统
完整的缓存解决方案，支持多种缓存策略和存储后端。详细说明请参考 [缓存系统文档](./core/cache/index.md)。

### Verticle 管理
Vert.x Verticle 的高级封装和管理工具。详细说明请参考 [Verticle 管理文档](./core/verticle/index.md)。

### 服务组件
各种后台服务组件。详细说明请参考 [服务组件文档](./core/servant/index.md)：

- **[Funnel](./core/servant/funnel/)** - 漏斗服务，用于流量控制
- **[Intravenous](./core/servant/intravenous/)** - 静脉注射服务，用于依赖注入
- **[Queue](./core/servant/queue/)** - 队列服务，消息队列处理
- **[Sundial](./core/servant/sundial/)** - 日晷服务，定时任务管理

### 核心抽象
- **[SelfInterface](./core/SelfInterface.md)** - 自引用接口
- **[ValueBox](./core/ValueBox.md)** - 值包装器

## 门面模块 (Facade)

门面模块提供了统一的访问入口和配置管理。

### Facade 概述
KeelInstance 作为框架的核心门面类，提供统一的入口点访问框架的各种功能。详细说明请参考 [Facade 概述文档](./facade/index.md)。

### 异步支持
异步编程环境下的逻辑支持封装。详细说明请参考 [异步支持文档](./facade/async/index.md)。

### 配置管理
配置项的封装和管理。详细说明请参考 [配置管理文档](./facade/configuration/index.md)。

### 测试框架
完整的测试体系封装。详细说明请参考 [测试框架文档](./facade/tesuto/index.md)。

## 集成模块 (Integration)

集成模块提供与第三方系统和服务的集成功能。

### MySQL 数据库
完整的 MySQL 数据库集成解决方案。详细说明请参考 [MySQL 数据库文档](./integration/mysql/index.md)：

- **[基础组件](./integration/mysql/base.md)** - 数据源配置和连接管理
- **[SQL 语句](./integration/mysql/statement.md)** - SQL 语句构建器
- **[查询条件](./integration/mysql/condition.md)** - 查询条件构建
- **[操作封装](./integration/mysql/action.md)** - 数据库操作抽象
- **[结果处理](./integration/mysql/result.md)** - 查询结果处理

### Office 文档处理
基于 Apache POI 的文档处理。详细说明请参考 [Office 文档处理文档](./integration/poi/index.md)：

- **[CSV 处理](./integration/poi/csv/index.md)** - CSV 文件读写处理
- **[Excel 处理](./integration/poi/excel/index.md)** - Excel 文件读写处理

## 日志系统 (Logger)

### Logger 概述
功能完整的日志记录系统，提供结构化日志记录、事件记录、指标收集等功能。详细说明请参考 [Logger 概述文档](./logger/index.md)：

- **日志级别管理** - DEBUG、INFO、NOTICE、WARNING、ERROR、FATAL、SILENT
- **问题记录系统** - KeelIssueRecord、KeelIssueRecordCenter、KeelIssueRecorder
- **事件日志系统** - KeelEventLog、KeelEventLogger
- **指标记录系统** - KeelMetricRecord、KeelMetricRecorder
- **输出适配器** - 支持多种输出方式（控制台、文件、远程服务）
- **渲染器** - 支持字符串和 JSON 格式渲染

## Web 模块

Web 模块是 Keel 框架的网络通信核心，提供完整的网络协议支持。

### Web 概述
基于 Vert.x 的异步网络通信模块，支持 HTTP、TCP、UDP 三大协议。详细说明请参考 [Web 概述文档](./web/index.md)。

### HTTP 服务
完整的 HTTP 服务器和客户端功能。详细说明请参考 [HTTP 服务文档](./web/http/index.md)：

- **[HTTP 服务器](./web/http/server/index.md)** - HTTP 服务器实现
- **[FastDocs 文档服务](./web/http/server/fastdocs.md)** - 自动化 Markdown 文档服务
- **[HTTP 客户端](./web/http/requester/index.md)** - HTTP 请求客户端

### TCP 通信
TCP Socket 连接的高级封装和管理。详细说明请参考 [TCP 通信文档](./web/tcp/index.md)。

### UDP 通信
UDP 数据报的收发功能。详细说明请参考 [UDP 通信文档](./web/udp/index.md)。

## 技术特性

### 异步编程模型
- 基于 Vert.x 的响应式编程
- Future/Promise 模式的异步操作
- 事件循环驱动的高并发处理

### 统一的日志系统
- 集成 Keel Logger 模块
- 专门的问题记录器（IssueRecord）
- 分级别的日志管理和过滤

### 可扩展的架构
- 抽象类和接口设计
- 插件化的中间件系统
- 自定义协议处理支持

### 高性能优化
- 零拷贝的缓冲区操作
- 背压控制和流量管理
- 连接池和资源复用

## 第三方依赖

Keel 框架引用吸收了多个优秀的第三方库：

- **[Vert.x](https://vertx.io)** 4.5.11 - 异步应用框架
- **[CommonMark](https://github.com/commonmark/commonmark-java)** 0.21.0 - Markdown 处理
- **[GoogleAuth](https://github.com/wstrange/GoogleAuth)** - Google 双因子认证
- **[OSHI](https://github.com/oshi/oshi)** 6.4.9 - 系统和硬件信息
- **[Apache POI](https://github.com/apache/poi)** 5.4.0 - Office 文档处理
- **[Excel Streaming Reader](https://github.com/pjfanning/excel-streaming-reader)** 5.0.3 - Excel 流式读取
- **[JOL](https://github.com/openjdk/jol)** 0.17 - Java 对象布局

## 快速开始

### 推荐项目模板
考虑使用 [Dry Dock of Keel](https://github.com/sinri/DryDockOfKeel) 快速构建应用程序！

### 学习资源
查看 [DryDock Lesson](https://github.com/sinri/DryDockLession) 了解如何使用 DryDock 和 Keel 快速开发。

## 分支说明

- **latest**: Keel 的最新推送版本，可能尚未发布
- **p3**: Keel 3.x 的最新发布版本

## 最佳实践

### 资源管理
- 及时释放网络连接和相关资源
- 使用连接池避免频繁创建连接
- 合理配置超时时间

### 错误处理
- 使用 Future 的 onSuccess 和 onFailure 回调
- 实现优雅的异常处理和恢复机制
- 记录详细的错误日志便于排查

### 性能优化
- 避免阻塞操作影响事件循环
- 合理使用缓冲区避免内存泄漏
- 配置适当的背压控制参数

### 安全考虑
- 实现适当的认证和授权机制
- 验证和过滤用户输入数据
- 使用 HTTPS 保护敏感数据传输

## 版本兼容性

- **最低要求**: Java 8+，Vert.x 4.x
- **推荐版本**: Java 11+，最新稳定版 Vert.x
- **向后兼容**: 主要 API 保持向后兼容性

---

## 贡献和支持

欢迎提交 Issue 和 Pull Request 来改进 Keel 框架。更多信息请访问项目的 GitHub 仓库。
