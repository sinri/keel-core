# Keel Web 模块文档

## 概述

Keel Web 模块 (`io.github.sinri.keel.web`) 是 Keel 框架的网络通信核心模块，提供了完整的网络协议支持，包括 HTTP、TCP 和 UDP 三大主要协议的实现。该模块基于 Vert.x 异步框架构建，为构建高性能的网络应用程序提供了强大的基础设施。

## 模块架构

Keel Web 模块采用分层架构设计，按照网络协议类型组织代码结构：

```
io.github.sinri.keel.web/
├── http/          # HTTP 协议支持
├── tcp/           # TCP Socket 通信
└── udp/           # UDP 数据报通信
```

## 核心子模块

### 1. HTTP 模块 (`http/`)

**功能定位**：提供完整的 HTTP 服务器和客户端功能

**主要特性**：
- 基于 Vert.x 的异步 HTTP 服务器实现
- 完整的请求/响应处理框架
- 可配置的中间件处理链
- 自动化文档生成功能
- 统一的错误处理和日志记录

**核心组件**：
- **服务器核心**：`KeelHttpServer` - HTTP 服务器抽象基类
- **请求处理**：`requester/` - HTTP 客户端请求功能
- **接收处理**：`receptionist/` - HTTP 服务器端请求处理
- **预处理链**：`prehandler/` - 中间件和过滤器
- **文档服务**：`fastdocs/` - 自动化 Markdown 文档服务

**详细文档**：[HTTP 模块文档](./http/index.md)

### 2. TCP 模块 (`tcp/`)

**功能定位**：提供 TCP Socket 连接的高级封装和管理

**主要特性**：
- 基于 Vert.x NetSocket 的抽象封装
- 自动化的连接生命周期管理
- 线程安全的数据缓冲区处理
- 背压控制和流量管理
- 数据片段解析和协议处理

**核心组件**：
- **抽象包装器**：`KeelAbstractSocketWrapper` - Socket 基础抽象类
- **基础实现**：`KeelBasicSocketWrapper` - 简单配置实现
- **日志记录**：`SocketIssueRecord` - 专用日志记录
- **数据片段**：`piece/` - 数据流解析工具

**详细文档**：[TCP 模块文档](./tcp/index.md)

### 3. UDP 模块 (`udp/`)

**功能定位**：提供 UDP 数据报的收发功能

**主要特性**：
- 基于 Vert.x DatagramSocket 的封装
- 异步的数据包收发处理
- 自定义数据包处理器支持
- 完整的日志记录功能

**核心组件**：
- **传输器**：`KeelUDPTransceiver` - UDP 数据包收发器
- **日志记录**：`DatagramIssueRecord` - UDP 专用日志记录

**详细文档**：[UDP 模块文档](./udp/index.md)

## 技术特性

### 1. 异步编程模型
- 基于 Vert.x 的响应式编程
- Future/Promise 模式的异步操作
- 事件循环驱动的高并发处理

### 2. 统一的日志系统
- 集成 Keel Logger 模块
- 专门的问题记录器（IssueRecord）
- 分级别的日志管理和过滤

### 3. 可扩展的架构
- 抽象类和接口设计
- 插件化的中间件系统
- 自定义协议处理支持

### 4. 高性能优化
- 零拷贝的缓冲区操作
- 背压控制和流量管理
- 连接池和资源复用

## 设计模式

### 1. 模板方法模式
- `KeelHttpServer` 定义服务器生命周期模板
- `KeelAbstractSocketWrapper` 定义 Socket 处理模板

### 2. 建造者模式
- 各种 Builder 类用于构建复杂对象
- 支持链式调用和灵活配置

### 3. 责任链模式
- `PreHandlerChain` 实现请求处理的责任链
- 支持动态添加和配置处理器

### 4. 混入模式
- Mixin 接口提供可复用的功能
- 避免代码重复和提高模块化

## 典型使用场景

### 1. Web API 服务
```java
public class MyApiServer extends KeelHttpServer {
    @Override
    protected void configureRoutes(Router router) {
        router.get("/api/users").handler(new UserListReceptionist());
        router.post("/api/users").handler(new UserCreateReceptionist());
    }
}
```

### 2. TCP 服务器
```java
NetServer server = vertx.createNetServer();
server.connectHandler(socket -> {
    new KeelBasicSocketWrapper(socket)
        .setIncomingBufferProcessor(this::processMessage)
        .setCloseHandler(this::onConnectionClosed);
});
```

### 3. UDP 服务
```java
KeelUDPTransceiver transceiver = new KeelUDPTransceiver(socket, port, issueRecordCenter)
    .setDatagramSocketConsumer(this::handleDatagramPacket);
transceiver.listen();
```

### 4. 文档服务
```java
KeelFastDocsKit.setup(router, "docs", "/docs/path");
```

## 依赖关系

### 核心依赖
- **Vert.x Core**：提供异步网络通信基础
- **Vert.x Web**：HTTP 路由和中间件支持
- **Keel Core**：核心工具类和辅助功能
- **Keel Logger**：统一日志系统
- **Keel Facade**：配置管理和实例控制

### 可选依赖
- **Vert.x Clustering**：集群模式支持
- **Vert.x Metrics**：性能监控集成

## 最佳实践

### 1. 资源管理
- 及时释放网络连接和相关资源
- 使用连接池避免频繁创建连接
- 合理配置超时时间

### 2. 错误处理
- 使用 Future 的 onSuccess 和 onFailure 回调
- 实现优雅的异常处理和恢复机制
- 记录详细的错误日志便于排查

### 3. 性能优化
- 避免阻塞操作影响事件循环
- 合理使用缓冲区避免内存泄漏
- 配置适当的背压控制参数

### 4. 安全考虑
- 实现适当的认证和授权机制
- 验证和过滤用户输入数据
- 使用 HTTPS 保护敏感数据传输

## 版本兼容性

- **最低要求**：Java 8+，Vert.x 4.x
- **推荐版本**：Java 11+，最新稳定版 Vert.x
- **向后兼容**：主要 API 保持向后兼容性

## 扩展指南

### 1. 自定义 HTTP 处理器
继承相应的基类并实现抽象方法：
- `KeelWebReceptionist` - 服务器端请求处理
- `AbstractKeelWebResponder` - 响应格式定制
- `Handler<RoutingContext>` - 中间件处理

### 2. 自定义 TCP 协议
继承 `KeelAbstractSocketWrapper` 实现协议解析：
```java
public class MyProtocolWrapper extends KeelAbstractSocketWrapper {
    @Override
    protected Future<Void> whenBufferComes(Buffer buffer) {
        // 实现协议解析逻辑
        return Future.succeededFuture();
    }
}
```

### 3. 自定义数据片段解析
继承 `KeelPieceKit` 实现数据分包处理：
```java
public class MyPieceKit extends KeelPieceKit<MyPiece> {
    @Override
    protected MyPiece parseFirstPieceFromBuffer() {
        // 实现片段解析逻辑
        return null;
    }
}
```

## 社区支持

- **问题反馈**：通过 GitHub Issues 报告问题
- **功能请求**：提交 Feature Request 建议新功能
- **贡献代码**：欢迎提交 Pull Request 参与开发
- **文档改进**：帮助完善文档和示例代码

---

*更多详细信息请参考各子模块的专门文档。*
