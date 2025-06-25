# Core Cutter 组件

## 概述

`cutter` 包提供了一个流式数据切分处理框架，用于处理从数据流中接收的缓冲区数据，并按照指定的分隔符进行切分和处理。该组件基于 Vert.x 的异步机制，配合 Keel 的 `intravenous` 组件实现高效的流数据处理。

## 核心概念

### 设计原理

Cutter 组件采用了"静脉输液"的设计理念，将流数据处理比作医疗输液过程：
- **数据流** 如同输液管中的液体
- **切分器** 如同控制输液速度的调节器
- **处理器** 如同接收输液的患者

### 主要特性

- **流式处理**: 支持实时处理流数据，无需等待完整数据
- **分隔符切分**: 使用双换行符 `\n\n` 作为默认分隔符
- **异步处理**: 基于 Vert.x 异步机制，提供非阻塞处理能力
- **超时控制**: 支持设置处理超时时间，防止长时间阻塞
- **异常处理**: 完善的异常处理机制，支持异常传播

## 核心接口

### IntravenouslyCutter<T>

流式数据切分器的核心接口，定义了处理流数据的基本规范。

```java
public interface IntravenouslyCutter<T> {
    // 获取单个数据处理器
    KeelIntravenous.SingleDropProcessor<T> getSingleDropProcessor();
    
    // 接收流数据
    void acceptFromStream(Buffer t);
    
    // 停止接收数据
    void stopHere();
    void stopHere(Throwable throwable);
    
    // 等待所有数据处理完成
    Future<Void> waitForAllHandled();
}
```

#### 使用流程

1. **创建实例**: 创建一个 `IntravenouslyCutter` 实例
2. **接收数据**: 循环调用 `acceptFromStream(Buffer)` 处理每个数据块
3. **停止接收**: 当没有更多数据时调用 `stopHere()`
4. **等待完成**: 调用 `waitForAllHandled()` 等待所有数据处理完成

#### 异常处理

- **Timeout**: 当处理超时时抛出的异常
- **异常传播**: 通过 `stopHere(Throwable)` 传递异常，影响最终结果

## 具体实现

### IntravenouslyCutterOnString

专门处理字符串数据的切分器实现，是目前唯一的具体实现类。

#### 特性

- **字符编码**: 使用 UTF-8 编码处理字符串
- **分隔符**: 使用双换行符 `\n\n` 分割数据
- **缓冲机制**: 内部维护缓冲区，支持数据的累积和切分
- **线程安全**: 使用同步机制确保线程安全

#### 构造方法

```java
// 带超时的构造方法
public IntravenouslyCutterOnString(
    KeelIntravenous.SingleDropProcessor<String> processor,
    long timeout
)

// 无超时的构造方法
public IntravenouslyCutterOnString(
    KeelIntravenous.SingleDropProcessor<String> processor
)
```

#### 工作原理

1. **数据接收**: 通过 `acceptFromStream()` 接收 Buffer 数据
2. **缓冲累积**: 将新数据追加到内部缓冲区
3. **切分处理**: 查找双换行符，切分出完整的数据片段
4. **异步处理**: 将切分后的字符串传递给处理器异步处理
5. **资源清理**: 完成后清理资源和取消定时器

#### 关键实现细节

```java
private String cutWithDelimiter() {
    String s0 = buffer.get().toString(StandardCharsets.UTF_8);
    int index = s0.indexOf("\n\n");
    if (index < 0) return null;
    
    var s1 = s0.substring(0, index);
    var s2 = s0.substring(index + 2);
    buffer.set(Buffer.buffer(s2.getBytes(StandardCharsets.UTF_8)));
    return s1;
}
```

## 使用示例

### 基本用法

```java
// 创建数据处理器
KeelIntravenous.SingleDropProcessor<String> processor = data -> {
    // 处理每个切分后的字符串
    System.out.println("处理数据: " + data);
    return Future.succeededFuture();
};

// 创建切分器
IntravenouslyCutterOnString cutter = new IntravenouslyCutterOnString(processor);

// 模拟接收流数据
cutter.acceptFromStream(Buffer.buffer("数据1\n\n数据2"));
cutter.acceptFromStream(Buffer.buffer("\n\n数据3"));

// 停止接收并等待完成
cutter.stopHere();
cutter.waitForAllHandled()
      .onSuccess(v -> System.out.println("所有数据处理完成"))
      .onFailure(err -> System.err.println("处理失败: " + err.getMessage()));
```

### 带超时的用法

```java
// 创建带5秒超时的切分器
IntravenouslyCutterOnString cutter = new IntravenouslyCutterOnString(
    processor, 
    5000  // 5秒超时
);

// 其他使用方式相同
```

## 适用场景

### 日志流处理
- 实时处理日志文件流
- 按条目分割和处理日志

### 网络数据处理
- HTTP 分块传输处理
- TCP 流数据分包处理

### 文件解析
- 大文件分块读取和处理
- 结构化数据流解析

## 注意事项

1. **分隔符限制**: 当前实现仅支持双换行符作为分隔符
2. **内存管理**: 对于大数据块，需要注意内存使用
3. **超时设置**: 合理设置超时时间，避免过短或过长
4. **异常处理**: 及时处理和传播异常，确保系统稳定性
5. **资源清理**: 确保调用 `waitForAllHandled()` 完成资源清理

## 版本历史

- **4.0.11**: 初始版本，提供基本的流数据切分功能
- **4.0.12**: 
  - 添加超时功能
  - 改进异常处理机制
  - 增强 `stopHere(Throwable)` 方法
  - 优化资源清理逻辑

## 相关组件

- **[intravenous](../servant/intravenous/index.md)**: 提供底层的异步处理能力
- **[KeelInstance](../../facade/index.md)**: 提供 Vert.x 实例和基础设施
