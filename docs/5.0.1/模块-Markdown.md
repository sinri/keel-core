# 模块说明：Markdown（markdown）

**包**：`io.github.sinri.keel.core.markdown`  
**版本线**：5.0.1

## 职责

基于 **commonmark-java** 解析 Markdown，并渲染为 HTML；默认启用 **GFM Tables**（`TablesExtension`）。

## 主要类型

| 类型                | 说明               |
|-------------------|------------------|
| `KeelMarkdownKit` | 解析器 + HTML 渲染器封装 |

## 构造与扩展

- **无参构造**：默认扩展列表为 `TablesExtension.create()`。
- **三参构造**：`List<Extension> extensions`，以及对 `Parser.Builder`、`HtmlRenderer.Builder` 的 **可选回调**，用于插件式定制。

## 常用 API

- `convertMarkdownToHtml(String md)`
- `convertMarkdownToHtml(Reader mdReader)`（可能抛出 `IOException`）
- `getMarkdownParser()` / `getHtmlRenderer()`：需要更底层控制时使用。

## 依赖说明

本能力随 **keel-core** 传递依赖 **CommonMark**；JPMS 模块已 `requires transitive org.commonmark` 及相关扩展。

## 相关文档

- [CommonMark Java](https://github.com/commonmark/commonmark-java)
- [用户使用指导](./用户使用指导.md)
