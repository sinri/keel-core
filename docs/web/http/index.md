# Keel HTTP 包分析大纲

## 概述

Keel HTTP 包 (`io.github.sinri.keel.web.http`) 提供了完整的 HTTP 服务器和客户端功能，基于 Vert.x 框架构建，包含请求处理、响应管理、预处理链、文档生成等核心功能。

## 包结构

### 1. 核心服务器 (Core Server)
- **`KeelHttpServer.java`** - HTTP 服务器抽象基类
  - 继承自 `KeelVerticleImpl`
  - 提供 HTTP 服务器配置和生命周期管理
  - 支持路由配置抽象方法 `configureRoutes(Router router)`
  - 内置异常处理和日志记录
  - 支持优雅关闭和主服务标识

### 2. 请求处理模块 (Requester)
**目录**: `requester/`

#### 2.1 核心接口
- **`KeelWebRequestMixin.java`** - Web 请求混入接口
  - 提供 WebClient 和 HttpClient 的便捷使用方法
  - 支持自定义配置选项
  - 自动资源管理和清理

#### 2.2 响应提取器 (Extractor)
**子目录**: `requester/extractor/`
- **`KeelWebResponseExtractor.java`** - 响应提取器基类
- **`KeelWebResponseExtractorOnOKCode.java`** - 基于 OK 状态码的提取器
- **`KeelWebResponseExtractorOnNormalStatus.java`** - 基于正常状态的提取器
- **`KeelWebResponseExtractorOnJsonObjectFormat.java`** - JSON 对象格式提取器

#### 2.3 错误处理 (Error)
**子目录**: `requester/error/`
- **`ReceivedUnexpectedResponse.java`** - 意外响应异常
- **`ReceivedFailedResponse.java`** - 失败响应异常
- **`ReceivedUnexpectedFormatResponse.java`** - 格式异常响应
- **`ReceivedAbnormalStatusResponse.java`** - 异常状态响应

### 3. 接收处理模块 (Receptionist)
**目录**: `receptionist/`

#### 3.1 核心组件
- **`KeelWebReceptionist.java`** - Web 接收器抽象基类
  - 处理路由上下文和请求生命周期
  - 提供成功/失败响应方法
  - 集成日志记录和问题追踪
  - 支持用户认证和会话管理
- **`KeelWebFutureReceptionist.java`** - 异步接收器
- **`KeelWebReceptionistLoader.java`** - 接收器加载器

#### 3.2 API 元数据
- **`ApiMeta.java`** - API 元数据定义
- **`ApiMetaContainer.java`** - API 元数据容器
- **`AbstractRequestBody.java`** - 抽象请求体基类

#### 3.3 响应器 (Responder)
**子目录**: `receptionist/responder/`
- **`KeelWebResponder.java`** - Web 响应器接口
- **`AbstractKeelWebResponder.java`** - 抽象响应器基类
- **`KeelWebResponderCommonApiImpl.java`** - 通用 API 响应器实现

#### 3.4 日志记录
- **`ReceptionistIssueRecord.java`** - 接收器问题记录

### 4. 预处理链模块 (PreHandler)
**目录**: `prehandler/`

#### 4.1 处理链管理
- **`PreHandlerChain.java`** - 预处理器链主类
  - 管理多种类型的处理器：平台、安全策略、协议升级、多租户、认证、授权等
  - 按权重顺序执行处理器
  - 支持失败处理器配置

#### 4.2 专用处理器
- **`KeelPlatformHandler.java`** - 平台处理器
  - 请求 ID 生成和管理
  - 请求开始时间记录
- **`KeelAuthenticationHandler.java`** - 认证处理器
  - 用户身份验证逻辑
  - 集成认证服务

#### 4.3 元数据
- **`PreHandlerChainMeta.java`** - 预处理链元数据

### 5. 快速文档模块 (FastDocs)
**目录**: `fastdocs/`

#### 5.1 核心功能
- **`KeelFastDocsKit.java`** - 快速文档工具包
  - 提供 Markdown 文档的 HTTP 服务
  - 支持目录生成和静态资源处理
  - 自动路由配置和请求处理

#### 5.2 页面构建器 (Page Builders)
**子目录**: `fastdocs/page/`
- **`MarkdownPageBuilder.java`** - Markdown 页面构建器
- **`CataloguePageBuilder.java`** - 目录页面构建器
- **`MarkdownCssBuilder.java`** - Markdown CSS 构建器
- **`PageBuilderOptions.java`** - 页面构建选项
- **`FastDocsContentResponder.java`** - 快速文档内容响应器

## 主要功能特性

### 1. HTTP 服务器管理
- 基于 Vert.x 的异步 HTTP 服务器
- 支持配置化端口和服务器选项
- 自动异常处理和日志记录
- 优雅启动和关闭机制

### 2. 请求/响应处理
- 统一的请求接收和处理框架
- 多种响应格式支持（JSON、HTML 等）
- 完善的错误处理和异常管理
- 自动请求追踪和日志记录

### 3. 中间件支持
- 可配置的预处理器链
- 支持认证、授权、跨域等常见中间件
- 自定义处理器扩展机制
- 按权重排序的处理器执行

### 4. 文档生成
- 自动 Markdown 文档服务
- 目录结构自动生成
- CSS 样式定制支持
- 静态资源处理

### 5. 客户端支持
- WebClient 和 HttpClient 封装
- 自动资源管理
- 响应提取和错误处理
- 异步操作支持

## 设计模式

### 1. 模板方法模式
- `KeelHttpServer` 定义服务器生命周期模板
- 子类实现具体的路由配置逻辑

### 2. 建造者模式
- 各种 Builder 类用于构建页面和响应
- 支持链式调用和灵活配置

### 3. 责任链模式
- `PreHandlerChain` 实现请求处理的责任链
- 支持动态添加和配置处理器

### 4. 混入模式
- `KeelWebRequestMixin` 提供可复用的请求功能
- 支持接口默认方法实现

## 扩展点

### 1. 自定义处理器
- 实现 `Handler<RoutingContext>` 接口
- 添加到 `PreHandlerChain` 中相应的处理器列表

### 2. 自定义响应器
- 继承 `AbstractKeelWebResponder`
- 实现特定的响应格式和逻辑

### 3. 自定义接收器
- 继承 `KeelWebReceptionist`
- 实现具体的业务处理逻辑

### 4. 文档定制
- 扩展页面构建器
- 自定义 CSS 和模板

## 依赖关系

- **核心依赖**: Vert.x Core, Vert.x Web
- **日志系统**: Keel Logger 模块
- **配置管理**: Keel Facade 模块
- **工具类**: Keel Core Helper 模块

## 使用场景

1. **Web API 服务** - 构建 RESTful API 服务
2. **Web 应用** - 开发完整的 Web 应用程序
3. **微服务** - 构建微服务架构中的 HTTP 服务
4. **文档服务** - 提供项目文档的 Web 访问
5. **代理服务** - 实现 HTTP 代理和转发功能
