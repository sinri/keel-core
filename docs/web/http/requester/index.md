# Keel Web HTTP Requester 文档

## 概述

Keel Web HTTP Requester 是一个基于 Vert.x 的 HTTP 客户端工具包，提供了简化的 Web 请求处理能力。它与 `KeelInstance` 紧密集成，提供了统一的 Web 客户端管理和响应处理机制。

## 核心组件

### 1. KeelWebRequestMixin

`KeelWebRequestMixin` 是核心接口，提供了 Web 客户端的统一访问入口。

#### 主要方法

- **`useWebClient(WebClientOptions, Function<WebClient, Future<T>>)`**
  - 创建并使用配置化的 WebClient
  - 自动管理客户端生命周期（使用后自动关闭）
  - 基于 `KeelInstance.Keel.getVertx()` 创建客户端

- **`useWebClient(Function<WebClient, Future<T>>)`**
  - 使用默认配置的 WebClient
  - 简化版本，适用于大多数场景

- **`useHttpClient(HttpClientOptions, Function<HttpClient, Future<T>>)`**
  - 创建并使用低级别的 HttpClient
  - 提供更精细的控制能力

#### 使用示例

```java
// 使用默认配置的 WebClient
Future<String> result = useWebClient(client -> {
    return client.get(80, "example.com", "/api/data")
                 .send()
                 .map(response -> response.bodyAsString());
});

// 使用自定义配置的 WebClient
WebClientOptions options = new WebClientOptions()
    .setConnectTimeout(5000)
    .setIdleTimeout(10000);

Future<JsonObject> jsonResult = useWebClient(options, client -> {
    return client.post(443, "api.example.com", "/submit")
                 .ssl(true)
                 .sendJsonObject(requestData)
                 .map(response -> response.bodyAsJsonObject());
});
```

### 2. 响应提取器（Extractor）

响应提取器提供了标准化的响应验证和内容提取机制。

#### KeelWebResponseExtractor<T>

抽象基类，定义了响应提取的基本结构：

- **属性**：
  - `responseStatusCode`: 响应状态码
  - `responseBody`: 响应体
  - `requestMethod`: 请求方法（可选）
  - `requestTarget`: 请求目标（可选）
  - `requestBody`: 请求体（可选）

- **核心方法**：
  - `abstract T extract() throws ReceivedUnexpectedResponse`: 提取响应内容

#### 具体实现类

1. **KeelWebResponseExtractorOnNormalStatus**
   - 验证响应状态码为 200
   - 返回原始 Buffer 响应体
   - 适用于基本的状态码验证

```java
// 使用示例
KeelWebResponseExtractorOnNormalStatus extractor = 
    new KeelWebResponseExtractorOnNormalStatus(response);
Buffer content = extractor.extract(); // 如果状态码非200则抛出异常
```

2. **KeelWebResponseExtractorOnJsonObjectFormat**
   - 继承状态码验证（200）
   - 额外验证响应体为有效的 JSON 对象格式
   - 返回解析后的 JsonObject

```java
// 使用示例
KeelWebResponseExtractorOnJsonObjectFormat extractor = 
    new KeelWebResponseExtractorOnJsonObjectFormat(response);
JsonObject jsonContent = extractor.extract(); // 验证状态码和JSON格式
```

3. **KeelWebResponseExtractorOnOKCode**
   - 继承 JSON 格式验证
   - 额外验证 JSON 中的 `code` 字段值为 "OK"
   - 适用于具有标准化响应格式的 API

```java
// 使用示例
KeelWebResponseExtractorOnOKCode extractor = 
    new KeelWebResponseExtractorOnOKCode(response);
JsonObject validContent = extractor.extract(); // 验证状态码、JSON格式和业务代码
```

### 3. 异常处理体系

提供了层次化的异常处理机制，便于精确的错误处理。

#### ReceivedUnexpectedResponse

基础异常类，包含：
- `responseStatusCode`: 响应状态码
- `responseBody`: 响应体
- `toJsonObject()`: 结构化的错误信息输出

#### 具体异常类型

1. **ReceivedAbnormalStatusResponse**
   - 响应状态码非 200 时抛出
   - 构造函数：`ReceivedAbnormalStatusResponse(int responseStatusCode, Buffer responseBody)`

2. **ReceivedUnexpectedFormatResponse**
   - 响应体格式不符合预期时抛出
   - 通常在 JSON 解析失败时使用

3. **ReceivedFailedResponse**
   - 业务层面的失败响应（code 字段非 "OK"）
   - 提供 `getResponseBodyAsJsonObject()` 方法便于获取结构化响应

#### 异常处理示例

```java
try {
    JsonObject result = extractor.extract();
    // 处理成功响应
} catch (ReceivedAbnormalStatusResponse e) {
    // 处理HTTP状态码异常
    logger.error("HTTP status error: " + e.getResponseStatusCode());
} catch (ReceivedUnexpectedFormatResponse e) {
    // 处理响应格式异常
    logger.error("Response format error: " + e.getMessage());
} catch (ReceivedFailedResponse e) {
    // 处理业务逻辑失败
    JsonObject errorDetails = e.getResponseBodyAsJsonObject();
    logger.error("Business logic error: " + errorDetails);
}
```

## 与 KeelInstance 的集成

### 集成机制

`KeelInstance` 实现了 `KeelWebRequestMixin` 接口，使得全局实例 `KeelInstance.Keel` 直接具备 Web 请求能力：

```java
public final class KeelInstance implements 
    KeelHelpersInterface, 
    KeelClusterKit, 
    KeelAsyncMixin, 
    KeelWebRequestMixin {
    
    public final static KeelInstance Keel = new KeelInstance();
    // ...
}
```

### Vertx 实例管理

- Web 客户端基于 `KeelInstance.getVertx()` 创建
- 支持集群模式和单机模式
- 提供了完整的 Vertx 生命周期管理

### 使用模式

```java
// 直接通过 Keel 实例使用
Future<JsonObject> apiResponse = Keel.useWebClient(client -> {
    return client.get(443, "api.service.com", "/endpoint")
                 .ssl(true)
                 .send()
                 .compose(response -> {
                     KeelWebResponseExtractorOnOKCode extractor = 
                         new KeelWebResponseExtractorOnOKCode(response);
                     return Future.succeededFuture(extractor.extract());
                 });
});

// 在自定义类中通过 Mixin 使用
public class MyService implements KeelWebRequestMixin {
    public Future<String> fetchData() {
        return useWebClient(client -> {
            return client.get(80, "data.service.com", "/api/data")
                         .send()
                         .compose(response -> {
                             KeelWebResponseExtractorOnNormalStatus extractor =
                                 new KeelWebResponseExtractorOnNormalStatus(response);
                             Buffer content = extractor.extract();
                             return Future.succeededFuture(content.toString());
                         });
        });
    }
}
```

## 最佳实践

### 1. 客户端生命周期管理

- 始终使用 `useWebClient` 或 `useHttpClient` 方法
- 避免手动创建和管理客户端实例
- 利用自动关闭机制防止资源泄漏

### 2. 响应验证策略

- 根据 API 特性选择合适的提取器
- 简单场景使用 `KeelWebResponseExtractorOnNormalStatus`
- JSON API 使用 `KeelWebResponseExtractorOnJsonObjectFormat`
- 标准化 API 使用 `KeelWebResponseExtractorOnOKCode`

### 3. 异常处理

- 捕获具体的异常类型进行差异化处理
- 利用异常的 `toJsonObject()` 方法进行结构化日志记录
- 为业务异常提供合适的降级处理

### 4. 配置管理

- 通过 `WebClientOptions` 配置超时、SSL 等参数
- 利用 `KeelInstance.getConfiguration()` 进行配置管理
- 考虑连接池和重用策略

## 扩展指南

### 自定义提取器

```java
public class CustomExtractor extends KeelWebResponseExtractor<MyDataType> {
    public CustomExtractor(HttpResponse<Buffer> response) {
        super(response);
    }
    
    @Override
    public MyDataType extract() throws ReceivedUnexpectedResponse {
        // 自定义验证和提取逻辑
        if (getResponseStatusCode() != 200) {
            throw new ReceivedAbnormalStatusResponse(getResponseStatusCode(), getResponseBody());
        }
        
        // 自定义数据转换
        return parseCustomFormat(getResponseBody());
    }
}
```

### 自定义异常

```java
public class CustomApiException extends ReceivedUnexpectedResponse {
    public CustomApiException(String customMessage, int statusCode, Buffer body) {
        super(customMessage, statusCode, body);
    }
    
    // 添加自定义方法
    public String getCustomErrorCode() {
        // 从响应体中提取自定义错误码
        return extractErrorCode(getResponseBody());
    }
}
```

## 版本历史

- **4.0.3**: 引入响应提取器和异常处理体系
- **4.0.1**: 添加 HttpClient 支持和 WebClientOptions 参数
- **3.2.19**: 修复跨 Verticle 丢失问题
- **3.2.18**: 初始版本的 useWebClient 方法

## 相关组件

- [KeelInstance](../../facade/README.md): 核心实例管理
- [Vertx集成](../../core/README.md): 底层 Vertx 支持
- [配置系统](../../facade/configuration/README.md): 配置管理
- [日志系统](../../logger/README.md): 日志和问题记录
