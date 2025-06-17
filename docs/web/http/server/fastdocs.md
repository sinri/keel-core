# FastDocs 文档生成工具

## 概述

FastDocs 是 Keel 框架中的一个轻量级文档生成和展示工具包，用于快速构建基于 Markdown 的文档网站。它提供了一个完整的解决方案，能够将 Markdown 文件转换为美观的 HTML 页面，并提供目录导航、面包屑导航等功能。

## 核心特性

- **Markdown 渲染**: 自动将 Markdown 文件转换为格式化的 HTML 页面
- **目录导航**: 自动生成文档目录树，支持嵌套结构
- **面包屑导航**: 显示当前页面在文档结构中的位置
- **响应式设计**: 适配不同屏幕尺寸的设备
- **静态资源支持**: 支持图片、CSS 等静态文件的访问
- **可定制样式**: 基于 GitHub Markdown 样式，可自定义标题和页脚

## 包结构

```
io.github.sinri.keel.web.http.fastdocs/
├── KeelFastDocsKit.java              # 主入口类，处理路由和请求分发
├── page/
│   ├── CataloguePageBuilder.java     # 目录页面构建器
│   ├── MarkdownPageBuilder.java      # Markdown 页面构建器
│   ├── MarkdownCssBuilder.java       # CSS 样式构建器
│   ├── PageBuilderOptions.java       # 页面构建选项配置
│   └── FastDocsContentResponder.java # 内容响应接口
└── package-info.java
```

## 核心组件

### 1. KeelFastDocsKit

主入口类，负责：
- 路由配置和请求处理
- 请求类型识别和分发
- 静态资源访问控制

**主要方法**：
- `installToRouter()`: 静态方法，快速安装到 Router
- `processRouterRequest()`: 处理所有进入的请求
- `setDocumentSubject()`: 设置文档主题
- `setFooterText()`: 设置页脚文本

### 2. PageBuilderOptions

配置类，包含页面构建所需的所有选项：

```java
public class PageBuilderOptions {
    public String rootURLPath;           // 根 URL 路径
    public String rootMarkdownFilePath;  // Markdown 文件根路径  
    public String fromDoc;               // 来源文档参数
    public RoutingContext ctx;           // Vert.x 路由上下文
    public String markdownContent;       // Markdown 内容
    public String subjectOfDocuments;    // 文档主题
    public String footerText;            // 页脚文本
}
```

### 3. MarkdownPageBuilder

Markdown 页面构建器，负责：
- 将 Markdown 内容转换为 HTML
- 生成完整的 HTML 页面结构
- 添加导航和样式

**核心功能**：
- 页面标题生成
- 面包屑导航构建
- 目录嵌入（iframe 方式）
- 响应式布局

### 4. CataloguePageBuilder

目录页面构建器，负责：
- 扫描文档目录结构
- 生成树形目录导航
- 支持 JAR 内和文件系统两种模式

**特性**：
- 自动递归扫描目录
- 支持嵌入式（JAR 内）和外部文件系统
- 树形结构展示
- 可折叠的目录层级

### 5. MarkdownCssBuilder

CSS 样式构建器，负责：
- 提供 GitHub Markdown 样式
- 缓存 CSS 内容以提高性能
- 响应 CSS 请求

## 使用方法

### 基本用法

```java
// 方法 1: 使用静态安装方法（推荐）
KeelFastDocsKit.installToRouter(
    router,
    "/docs/",                    // URL 基础路径
    "documentation/",            // Markdown 文件目录
    "项目文档",                   // 文档主题
    "版权所有 © 2024"            // 页脚文本
);

// 方法 2: 手动实例化
KeelFastDocsKit fastDocs = new KeelFastDocsKit("/docs/", "documentation/")
    .setDocumentSubject("项目文档")
    .setFooterText("版权所有 © 2024");

router.route("/docs/*").handler(fastDocs::processRouterRequest);
```

### 目录结构示例

```
resources/
└── documentation/
    ├── index.md              # 首页文档
    ├── quickstart/
    │   ├── index.md         # 快速开始首页
    │   ├── installation.md  # 安装指南
    │   └── first-steps.md   # 第一步
    ├── advanced/
    │   ├── index.md         # 高级功能首页
    │   ├── configuration.md # 配置说明
    │   └── api-reference.md # API 参考
    └── images/
        └── logo.png         # 静态图片资源
```

### URL 路由规则

- `GET /docs/` 或 `GET /docs` → 重定向到 `index.md`
- `GET /docs/*.md` → 渲染对应的 Markdown 页面
- `GET /docs/catalogue` → 显示目录页面
- `GET /docs/catalogue?from_doc=path` → 显示指定文档的目录
- `GET /docs/markdown.css` → 返回 CSS 样式文件
- `GET /docs/images/*` → 返回静态资源文件

## 页面布局

FastDocs 生成的页面采用固定布局：

```
┌─────────────────────────────────────────┐
│              Header (固定顶部)            │
├─────────────┬───────────────────────────┤
│   Catalogue │                           │
│   (左侧固定) │     Main Content         │
│             │     (Markdown 渲染)       │
│             │                           │
├─────────────┴───────────────────────────┤
│              Footer (固定底部)            │
└─────────────────────────────────────────┘
```

## 配置说明

### 1. 路径配置

- **rootURLPath**: Web 访问的根路径，如 `/docs/`
- **rootMarkdownFilePath**: 资源文件中 Markdown 文件的根目录，如 `documentation/`

### 2. 样式定制

FastDocs 使用 GitHub Markdown 样式作为基础，位于：
- `src/main/resources/web-fastdocs-css/github-markdown.4.0.0.min.css`

### 3. 静态资源

支持的静态资源类型：
- 图片文件：`.png`, `.jpg`, `.gif`, `.svg` 等
- 样式文件：`.css`
- 脚本文件：`.js`
- 其他文件：根据 StaticHandler 配置

## 依赖关系

FastDocs 依赖于以下 Keel 组件：

1. **KeelMarkdownKit**: Markdown 到 HTML 的转换
2. **Vert.x Web**: HTTP 服务和路由
3. **Vert.x Core**: 异步操作支持

## 最佳实践

### 1. 文档组织

- 每个目录都应有 `index.md` 作为入口
- 使用有意义的文件名和目录名
- 保持目录层级不要过深（建议不超过 4 层）

### 2. Markdown 编写

- 使用标准的 Markdown 语法
- 图片路径使用相对路径
- 添加适当的标题层级

### 3. 性能优化

- CSS 内容会被自动缓存
- 目录结构会被缓存（在应用重启前）
- 静态资源通过 StaticHandler 高效处理

### 4. 部署注意事项

- 确保 Markdown 文件在 `resources` 目录下
- JAR 部署时会自动检测嵌入模式
- 开发时支持外部文件系统模式

## 示例项目

参考测试用例：`DSHttpServer.java`

```java
@Override
protected void configureRoutes(Router router) {
    KeelFastDocsKit.installToRouter(
        router,
        "/fastdocs/",
        "web_root/fastdocs/",
        "Dyson Sphere FastDocs",
        "Copyright 2022 Sinri Edogawa"
    );
}
```

## 版本历史

- **1.12**: 初始版本
- **3.0.0**: 测试通过，稳定版本

## 相关文档

- [KeelMarkdownKit 文档](../../core/markdown.md)
- [HTTP 服务器文档](./index.md)
- [Vert.x Web 官方文档](https://vertx.io/docs/vertx-web/java/)
