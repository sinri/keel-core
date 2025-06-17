# Keel UDP 模块文档

## 概述

`io.github.sinri.keel.web.udp` 包提供了基于 Vert.x 的 UDP 网络通信功能，支持 UDP 数据报的发送和接收。该模块主要包含 UDP 传输器和相关的日志记录功能。

## 核心组件

### 1. KeelUDPTransceiver

主要的 UDP 传输器类，负责 UDP 数据包的收发。

#### 功能特性

- **双向通信**：支持 UDP 数据包的发送和接收
- **异步处理**：基于 Vert.x 的异步编程模型
- **日志记录**：集成问题记录器，记录传输过程中的事件和错误
- **自定义处理**：支持自定义数据包处理逻辑

#### 主要方法

```java
// 构造函数
public KeelUDPTransceiver(
    DatagramSocket udpServer,
    int port,
    @Nonnull KeelIssueRecordCenter issueRecordCenter
)

// 设置数据包处理器
public KeelUDPTransceiver setDatagramSocketConsumer(
    BiConsumer<SocketAddress, Buffer> datagramSocketConsumer
)

// 开始监听
public Future<Object> listen()

// 发送数据包
public Future<Void> send(Buffer buffer, int targetPort, String targetAddress)

// 关闭传输器
public Future<Void> close()
```

#### 使用示例

```java
import io.github.sinri.keel.web.udp.KeelUDPTransceiver;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;

// 创建 Vertx 实例和 DatagramSocket
Vertx vertx = Vertx.vertx();
DatagramSocket socket = vertx.createDatagramSocket();

// 创建 UDP 传输器
KeelUDPTransceiver transceiver = new KeelUDPTransceiver(
    socket, 
    8080, 
    issueRecordCenter
);

// 设置数据包处理器
transceiver.setDatagramSocketConsumer((sender, buffer) -> {
    System.out.println("收到来自 " + sender + " 的数据: " + buffer.toString());
    
    // 回复数据包
    Buffer response = Buffer.buffer("收到数据");
    transceiver.send(response, sender.port(), sender.host());
});

// 开始监听
transceiver.listen()
    .onSuccess(v -> System.out.println("UDP 服务器启动成功"))
    .onFailure(err -> System.err.println("启动失败: " + err.getMessage()));
```

### 2. DatagramIssueRecord

专门用于记录 UDP 数据报相关事件的日志记录类。

#### 功能特性

- **继承自 KeelIssueRecord**：复用基础日志功能
- **数据包记录**：记录发送和接收的数据包详细信息
- **十六进制编码**：将二进制数据编码为十六进制字符串便于查看

#### 主要方法

```java
// 记录发送的数据包
public DatagramIssueRecord bufferSent(
    @Nonnull Buffer buffer, 
    @Nonnull String address, 
    int port
)

// 记录接收的数据包
public DatagramIssueRecord bufferReceived(
    @Nonnull Buffer buffer, 
    @Nonnull String address, 
    int port
)
```

#### 日志格式

日志记录包含以下信息：
- **地址信息**：目标或源地址和端口
- **缓冲区内容**：十六进制编码的数据内容
- **缓冲区大小**：数据包的字节长度
- **操作类型**：`sent_to` 或 `received_from`

## 版本历史

- **@since 2.8**：首次引入，原名 `KeelUDPServer`
- **@since 3.2.0**：添加问题记录器支持，引入 `DatagramIssueRecord`
- **@since 4.0.0**：构造函数参数 `issueRecorder` 改为 `issueRecordCenter`

## 依赖关系

该模块依赖以下 Keel 组件：
- `io.github.sinri.keel.logger.issue` - 日志记录功能
- `io.github.sinri.keel.facade.KeelInstance` - 核心工具类

以及外部依赖：
- **Vert.x Core** - 提供异步网络通信能力
- **Vert.x Datagram** - UDP 数据报支持

## 最佳实践

1. **资源管理**：使用完毕后及时调用 `close()` 方法释放资源
2. **异常处理**：合理处理 Future 的成功和失败回调
3. **数据包大小**：注意 UDP 数据包大小限制（通常为 65507 字节）
4. **网络地址**：确保目标地址和端口的有效性
5. **日志级别**：根据需要配置合适的日志记录级别

## 注意事项

- UDP 是无连接协议，不保证数据包的到达顺序和可靠性
- 大数据包可能被网络层分片，需要在应用层处理
- 防火墙和 NAT 可能影响 UDP 通信
- 建议在生产环境中添加适当的错误处理和重试机制
