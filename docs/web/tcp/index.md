# Keel TCP Package Documentation

`io.github.sinri.keel.web.tcp`

## 概述

TCP 包提供了基于 Vert.x 的 TCP Socket 连接的封装和管理功能。该包主要包含 Socket 包装器、日志记录和数据片段处理等核心组件，为 TCP 通信提供了高级抽象和便利的操作接口。

## 核心组件

### 1. Socket 包装器

#### KeelAbstractSocketWrapper

抽象的 Socket 包装器类，提供了 TCP Socket 连接的基础功能和生命周期管理。

**主要特性：**
- 基于 Vert.x NetSocket 的高级封装
- 自动化的事件处理（连接、断开、异常等）
- 内置的日志记录和监控
- 线程安全的缓冲区管理
- 支持背压控制（写队列满时自动暂停）

**构造方法：**
```java
// 使用自动生成的 socketID
public KeelAbstractSocketWrapper(NetSocket socket)

// 使用自定义的 socketID  
public KeelAbstractSocketWrapper(NetSocket socket, String socketID)
```

**核心方法：**
```java
// 抽象方法，需要子类实现
protected abstract Future<Void> whenBufferComes(Buffer incomingBuffer);

// 生命周期回调方法
protected void whenReadToEnd()
protected void whenDrain()
protected void whenClose()
protected void whenExceptionOccurred(Throwable throwable)

// 写入操作
public Future<Void> write(String s)
public Future<Void> write(String s, String enc)
public Future<Void> write(Buffer buffer)

// 地址信息
public SocketAddress getRemoteAddress()
public SocketAddress getLocalAddress()
public String getRemoteAddressString()
public String getLocalAddressString()

// 连接控制
public Future<Void> close()
public KeelAbstractSocketWrapper setMaxSize(int maxSize)
```

**内部机制：**
- 使用 `KeelFunnel` 确保数据处理的线程安全性，所有数据处理任务按序排队执行
- 自动处理写队列满的情况，支持暂停和恢复读取操作
- 集成了完整的日志记录系统，包括连接状态和数据传输的详细记录
- 基于 WORKER 线程模型的 KeelFunnel 部署，适合处理阻塞 I/O 操作
- 自动生成唯一的 socketID，便于多连接环境下的日志追踪

#### KeelBasicSocketWrapper

`KeelAbstractSocketWrapper` 的基础实现类，提供了简单易用的配置接口。

**特性：**
- 通过函数式接口配置各种事件处理器
- 提供链式调用的配置方法
- 适合快速原型开发和简单场景

**构造方法：**
```java
// 使用自动生成的 socketID
public KeelBasicSocketWrapper(NetSocket socket)

// 使用自定义的 socketID
public KeelBasicSocketWrapper(NetSocket socket, String socketID)
```

**配置方法：**
```java
public KeelBasicSocketWrapper setIncomingBufferProcessor(Function<Buffer, Future<Void>> processor)
public KeelBasicSocketWrapper setReadToEndHandler(Handler<Void> handler)
public KeelBasicSocketWrapper setDrainHandler(Handler<Void> handler)
public KeelBasicSocketWrapper setCloseHandler(Handler<Void> handler)
public KeelBasicSocketWrapper setExceptionHandler(Consumer<Throwable> handler)
```

### 2. 日志记录

#### SocketIssueRecord

专门为 TCP Socket 通信设计的日志记录类，继承自 `KeelIssueRecord`。

**功能：**
- 专门的 Socket 通信日志记录
- 支持 Buffer 内容的十六进制编码记录
- 自动记录缓冲区大小信息
- 集成到 Keel 的统一日志系统

**使用示例：**
```java
// 记录缓冲区信息
socketIssueRecord.buffer(incomingBuffer);
```

**日志主题：**
- `TopicTcpSocket`: TCP Socket 相关的日志主题

**日志内容：**
- 缓冲区十六进制内容记录
- 缓冲区大小信息
- 连接建立、断开和异常信息
- 写队列状态变化记录

### 3. 数据片段处理 (piece 子包)

#### KeelPiece

数据片段的基础接口，定义了数据片段的标准行为。

```java
public interface KeelPiece {
    Buffer toBuffer();
}
```

#### KeelPieceKit

抽象的数据片段解析工具类，用于从连续的字节流中解析出完整的数据片段。

**设计目标：**
- 处理 TCP 流式数据的分包和粘包问题
- 提供线程安全的数据片段队列
- 支持自定义的数据片段解析逻辑

**核心功能：**
```java
// 接收新的缓冲区数据
public void accept(Buffer incomingBuffer)

// 获取解析完成的片段队列
public Queue<P> getPieceQueue()

// 抽象方法：从缓冲区解析第一个片段
protected abstract P parseFirstPieceFromBuffer()

// 工具方法
protected Buffer getBuffer()
protected void cutBuffer(int newStart)
```

**工作流程：**
1. 接收传入的 Buffer 数据
2. 将数据追加到内部缓冲区
3. 循环尝试从缓冲区解析完整的数据片段
4. 将解析成功的片段放入队列中
5. 保留未完成的数据继续等待后续数据

**注意事项：**
- `KeelPieceKit` 配合 `KeelFunnel` 使用，确保线程安全的数据处理
- 解析方法需要考虑数据分包和粘包的情况

## 使用场景

### 1. 简单 TCP 服务器
```java
NetServer server = vertx.createNetServer();
server.connectHandler(socket -> {
    KeelBasicSocketWrapper wrapper = new KeelBasicSocketWrapper(socket)
        .setIncomingBufferProcessor(buffer -> {
            // 处理接收到的数据
            return Future.succeededFuture();
        })
        .setCloseHandler(v -> {
            // 处理连接关闭
        });
});
```

### 2. 自定义协议解析
```java
public class MyProtocolWrapper extends KeelAbstractSocketWrapper {
    public MyProtocolWrapper(NetSocket socket) {
        super(socket);
    }
    
    @Override
    protected Future<Void> whenBufferComes(Buffer incomingBuffer) {
        // 实现自定义协议解析逻辑
        return Future.succeededFuture();
    }
}
```

### 3. 数据片段解析
```java
public class MyPieceKit extends KeelPieceKit<MyPiece> {
    @Override
    protected MyPiece parseFirstPieceFromBuffer() {
        // 实现自定义的片段解析逻辑
        // 例如：基于长度前缀、分隔符等
        return null; // 如果没有完整片段则返回 null
    }
}
```

## 设计特点

1. **异步处理**: 基于 Vert.x 的异步编程模型
2. **线程安全**: 使用 KeelFunnel 确保数据处理的线程安全
3. **背压控制**: 自动处理写队列满的情况
4. **完整的生命周期管理**: 从连接建立到关闭的完整事件处理
5. **可扩展性**: 通过抽象类和接口支持自定义实现
6. **日志集成**: 完整的日志记录和监控支持

## 版本历史

- **2.8**: 引入 KeelAbstractSocketWrapper 和 KeelBasicSocketWrapper
- **3.2.0**: 添加 SocketIssueRecord 日志记录功能
- **4.0.0**: 增强日志记录中心的配置选项

## 注意事项

1. **不要直接使用 NetSocket**: 应该通过包装器类来操作 Socket
2. **线程安全**: 所有的数据处理都通过 KeelFunnel 进行排队处理，确保按顺序执行
3. **资源管理**: Socket 关闭时会自动清理相关资源，包括 KeelFunnel 的卸载
4. **日志配置**: 默认使用静默日志中心，可通过重写 `getIssueRecordCenter()` 方法自定义
5. **背压处理**: 写队列满时会自动暂停读取，队列可写时自动恢复
6. **ID 管理**: 每个 Socket 包装器都有唯一的 socketID，用于日志追踪和问题定位
7. **线程模型**: KeelFunnel 使用 WORKER 线程模型，适合 I/O 密集型任务
