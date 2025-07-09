package io.github.sinri.keel.core.markdown;

import io.github.sinri.keel.facade.tesuto.unit.KeelUnitTest;
import org.commonmark.Extension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class KeelMarkdownKitTest extends KeelUnitTest {

    // ==================== 构造函数测试 ====================

    @Test
    @DisplayName("测试默认构造函数")
    void testDefaultConstructor() {
        KeelMarkdownKit kit = new KeelMarkdownKit();
        
        assertNotNull(kit);
        assertNotNull(kit.getExtensions());
        assertNotNull(kit.getMarkdownParser());
        assertNotNull(kit.getHtmlRenderer());
        
        // 默认应该包含 TablesExtension
        assertEquals(1, kit.getExtensions().size());
        assertInstanceOf(TablesExtension.class, kit.getExtensions().get(0));
        
        getUnitTestLogger().info("默认构造函数测试通过");
    }

    @Test
    @DisplayName("测试带扩展的构造函数")
    void testConstructorWithExtensions() {
        List<Extension> extensions = List.of(
                TablesExtension.create()
        );
        
        KeelMarkdownKit kit = new KeelMarkdownKit(extensions);
        
        assertNotNull(kit);
        assertNotNull(kit.getExtensions());
        assertEquals(1, kit.getExtensions().size());
        assertInstanceOf(TablesExtension.class, kit.getExtensions().get(0));
        
        getUnitTestLogger().info("带扩展的构造函数测试通过");
    }

    @Test
    @DisplayName("测试空扩展列表的构造函数")
    void testConstructorWithEmptyExtensions() {
        List<Extension> extensions = Collections.emptyList();
        
        KeelMarkdownKit kit = new KeelMarkdownKit(extensions);
        
        assertNotNull(kit);
        assertNotNull(kit.getExtensions());
        assertEquals(0, kit.getExtensions().size());
        
        getUnitTestLogger().info("空扩展列表构造函数测试通过");
    }

    // ==================== 扩展管理测试 ====================

    @Test
    @DisplayName("测试重置扩展")
    void testResetExtensions() {
        KeelMarkdownKit kit = new KeelMarkdownKit();
        
        // 初始状态应该有一个扩展
        assertEquals(1, kit.getExtensions().size());
        
        // 重置为空列表
        List<Extension> newExtensions = Collections.emptyList();
        KeelMarkdownKit result = kit.resetExtensions(newExtensions);
        
        // 验证返回的是同一个实例（链式调用）
        assertSame(kit, result);
        
        // 验证扩展已重置
        assertEquals(0, kit.getExtensions().size());
        
        getUnitTestLogger().info("重置扩展测试通过");
    }

    @Test
    @DisplayName("测试添加扩展")
    void testAppendExtensions() {
        KeelMarkdownKit kit = new KeelMarkdownKit();
        
        // 初始状态应该有一个扩展
        assertEquals(1, kit.getExtensions().size());
        
        // 添加新扩展会抛出异常，因为 List.of() 创建的列表是不可变的
        Extension newExtension = TablesExtension.create();
        assertThrows(UnsupportedOperationException.class, () -> {
            kit.appendExtensions(newExtension);
        });
        
        getUnitTestLogger().info("添加扩展测试通过 - 验证了不可变列表的行为");
    }

    @Test
    @DisplayName("测试多次添加扩展")
    void testAppendMultipleExtensions() {
        KeelMarkdownKit kit = new KeelMarkdownKit();
        
        // 初始状态
        assertEquals(1, kit.getExtensions().size());
        
        // 添加扩展会抛出异常，因为 List.of() 创建的列表是不可变的
        assertThrows(UnsupportedOperationException.class, () -> {
            kit.appendExtensions(TablesExtension.create());
        });
        
        getUnitTestLogger().info("多次添加扩展测试通过 - 验证了不可变列表的行为");
    }

    // ==================== Getter 方法测试 ====================

    @Test
    @DisplayName("测试获取扩展列表")
    void testGetExtensions() {
        List<Extension> extensions = List.of(
                TablesExtension.create()
        );
        
        KeelMarkdownKit kit = new KeelMarkdownKit(extensions);
        List<Extension> result = kit.getExtensions();
        
        assertNotNull(result);
        assertEquals(1, result.size());
        assertInstanceOf(TablesExtension.class, result.get(0));
        
        // 验证返回的是同一个引用（当前实现的行为）
        assertSame(extensions, result);
        
        getUnitTestLogger().info("获取扩展列表测试通过");
    }

    @Test
    @DisplayName("测试获取 Markdown 解析器")
    void testGetMarkdownParser() {
        KeelMarkdownKit kit = new KeelMarkdownKit();
        Parser parser = kit.getMarkdownParser();
        
        assertNotNull(parser);
        assertInstanceOf(Parser.class, parser);
        
        getUnitTestLogger().info("获取 Markdown 解析器测试通过");
    }

    @Test
    @DisplayName("测试获取 HTML 渲染器")
    void testGetHtmlRenderer() {
        KeelMarkdownKit kit = new KeelMarkdownKit();
        HtmlRenderer renderer = kit.getHtmlRenderer();
        
        assertNotNull(renderer);
        assertInstanceOf(HtmlRenderer.class, renderer);
        
        getUnitTestLogger().info("获取 HTML 渲染器测试通过");
    }

    // ==================== Markdown 转 HTML 测试 ====================

    @Test
    @DisplayName("测试基本 Markdown 转 HTML")
    void testConvertMarkdownToHtml_Basic() {
        KeelMarkdownKit kit = new KeelMarkdownKit();
        
        String markdown = "# Hello World\n\nThis is a **bold** text.";
        String html = kit.convertMarkdownToHtml(markdown);
        
        assertNotNull(html);
        assertTrue(html.contains("<h1>Hello World</h1>"));
        assertTrue(html.contains("<p>This is a <strong>bold</strong> text.</p>"));
        
        getUnitTestLogger().info("基本 Markdown 转 HTML 测试通过");
        getUnitTestLogger().info("输入: " + markdown);
        getUnitTestLogger().info("输出: " + html);
    }

    @Test
    @DisplayName("测试表格扩展功能")
    void testConvertMarkdownToHtml_WithTables() {
        KeelMarkdownKit kit = new KeelMarkdownKit();
        
        String markdown = "| Name | Age | City |\n" +
                         "|------|-----|------|\n" +
                         "| John | 25  | NYC  |\n" +
                         "| Jane | 30  | LA   |";
        
        String html = kit.convertMarkdownToHtml(markdown);
        
        assertNotNull(html);
        assertTrue(html.contains("<table>"));
        assertTrue(html.contains("<thead>"));
        assertTrue(html.contains("<tbody>"));
        assertTrue(html.contains("<th>Name</th>"));
        assertTrue(html.contains("<td>John</td>"));
        
        getUnitTestLogger().info("表格扩展功能测试通过");
        getUnitTestLogger().info("输出: " + html);
    }

    @Test
    @DisplayName("测试基本 Markdown 格式")
    void testConvertMarkdownToHtml_BasicFormatting() {
        KeelMarkdownKit kit = new KeelMarkdownKit();
        
        String markdown = "This is **bold** and *italic* text.";
        String html = kit.convertMarkdownToHtml(markdown);
        
        assertNotNull(html);
        assertTrue(html.contains("<strong>bold</strong>"));
        assertTrue(html.contains("<em>italic</em>"));
        
        getUnitTestLogger().info("基本 Markdown 格式测试通过");
        getUnitTestLogger().info("输出: " + html);
    }

    @Test
    @DisplayName("测试空字符串输入")
    void testConvertMarkdownToHtml_EmptyString() {
        KeelMarkdownKit kit = new KeelMarkdownKit();
        
        String html = kit.convertMarkdownToHtml("");
        
        assertNotNull(html);
        assertEquals("", html);
        
        getUnitTestLogger().info("空字符串输入测试通过");
    }

    @Test
    @DisplayName("测试 null 输入")
    void testConvertMarkdownToHtml_NullInput() {
        KeelMarkdownKit kit = new KeelMarkdownKit();
        
        // null 输入会抛出 NullPointerException
        assertThrows(NullPointerException.class, () -> {
            kit.convertMarkdownToHtml(null);
        });
        
        getUnitTestLogger().info("null 输入测试通过 - 验证了 NullPointerException 行为");
    }

    @Test
    @DisplayName("测试复杂 Markdown 内容")
    void testConvertMarkdownToHtml_ComplexContent() {
        KeelMarkdownKit kit = new KeelMarkdownKit();
        
        String markdown = "# Main Title\n\n" +
                         "## Subtitle\n\n" +
                         "This is a paragraph with **bold** and *italic* text.\n\n" +
                         "- List item 1\n" +
                         "- List item 2\n" +
                         "  - Nested item\n\n" +
                         "1. Numbered item 1\n" +
                         "2. Numbered item 2\n\n" +
                         "> This is a blockquote\n\n" +
                         "`inline code`\n\n" +
                         "```java\n" +
                         "public class Test {\n" +
                         "    public static void main(String[] args) {\n" +
                         "        System.out.println(\"Hello World\");\n" +
                         "    }\n" +
                         "}\n" +
                         "```";
        
        String html = kit.convertMarkdownToHtml(markdown);
        
        assertNotNull(html);
        assertTrue(html.contains("<h1>Main Title</h1>"));
        assertTrue(html.contains("<h2>Subtitle</h2>"));
        assertTrue(html.contains("<strong>bold</strong>"));
        assertTrue(html.contains("<em>italic</em>"));
        assertTrue(html.contains("<ul>"));
        assertTrue(html.contains("<ol>"));
        assertTrue(html.contains("<blockquote>"));
        assertTrue(html.contains("<code>"));
        assertTrue(html.contains("<pre>"));
        
        getUnitTestLogger().info("复杂 Markdown 内容测试通过");
        getUnitTestLogger().info("输出长度: " + html.length());
    }

    // ==================== 边界情况测试 ====================

    @Test
    @DisplayName("测试特殊字符处理")
    void testConvertMarkdownToHtml_SpecialCharacters() {
        KeelMarkdownKit kit = new KeelMarkdownKit();
        
        String markdown = "Text with <script>alert('xss')</script> and & < > \" ' characters";
        String html = kit.convertMarkdownToHtml(markdown);
        
        assertNotNull(html);
        // 验证 HTML 实体被正确转义
        assertTrue(html.contains("&lt;"));
        assertTrue(html.contains("&gt;"));
        assertTrue(html.contains("&amp;"));
        
        getUnitTestLogger().info("特殊字符处理测试通过");
        getUnitTestLogger().info("输出: " + html);
    }

    @Test
    @DisplayName("测试长文本处理")
    void testConvertMarkdownToHtml_LongText() {
        KeelMarkdownKit kit = new KeelMarkdownKit();
        
        StringBuilder longMarkdown = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longMarkdown.append("This is line ").append(i).append(" with some content.\n\n");
        }
        
        String html = kit.convertMarkdownToHtml(longMarkdown.toString());
        
        assertNotNull(html);
        assertTrue(html.length() > 0);
        assertTrue(html.contains("<p>"));
        
        getUnitTestLogger().info("长文本处理测试通过");
        getUnitTestLogger().info("输入长度: " + longMarkdown.length());
        getUnitTestLogger().info("输出长度: " + html.length());
    }

    @Test
    @DisplayName("测试链式调用")
    void testChainedCalls() {
        KeelMarkdownKit kit = new KeelMarkdownKit();
        
        // 测试链式调用 - resetExtensions 可以正常工作
        KeelMarkdownKit result = kit.resetExtensions(Collections.emptyList());
        
        assertSame(kit, result);
        assertEquals(0, kit.getExtensions().size());
        
        // 测试转换功能仍然正常工作
        String markdown = "# Test\n\n**bold** text";
        String html = kit.convertMarkdownToHtml(markdown);
        
        assertNotNull(html);
        assertTrue(html.contains("<h1>Test</h1>"));
        assertTrue(html.contains("<strong>bold</strong>"));
        
        getUnitTestLogger().info("链式调用测试通过");
    }

    // ==================== 性能测试 ====================

    @Test
    @DisplayName("测试多次转换的性能")
    void testMultipleConversions() {
        KeelMarkdownKit kit = new KeelMarkdownKit();
        
        String markdown = "# Test\n\nThis is a test content with **bold** and *italic* text.";
        
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < 100; i++) {
            String html = kit.convertMarkdownToHtml(markdown);
            assertNotNull(html);
            assertTrue(html.contains("<h1>Test</h1>"));
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        getUnitTestLogger().info("多次转换性能测试通过");
        getUnitTestLogger().info("100次转换耗时: " + duration + "ms");
        
        // 验证性能在合理范围内（通常应该在几毫秒内完成100次转换）
        assertTrue(duration < 1000, "转换性能应该在1秒内完成100次转换");
    }

    // ==================== 额外测试用例 ====================

    @Test
    @DisplayName("测试代码块渲染")
    void testCodeBlockRendering() {
        KeelMarkdownKit kit = new KeelMarkdownKit();
        
        String markdown = "```java\npublic class Test {\n    // comment\n}\n```";
        String html = kit.convertMarkdownToHtml(markdown);
        
        assertNotNull(html);
        assertTrue(html.contains("<pre>"));
        assertTrue(html.contains("<code"));
        assertTrue(html.contains("public class Test"));
        
        getUnitTestLogger().info("代码块渲染测试通过");
        getUnitTestLogger().info("输出: " + html);
    }

    @Test
    @DisplayName("测试链接渲染")
    void testLinkRendering() {
        KeelMarkdownKit kit = new KeelMarkdownKit();
        
        String markdown = "[Google](https://www.google.com)";
        String html = kit.convertMarkdownToHtml(markdown);
        
        assertNotNull(html);
        assertTrue(html.contains("<a href=\"https://www.google.com\">Google</a>"));
        
        getUnitTestLogger().info("链接渲染测试通过");
        getUnitTestLogger().info("输出: " + html);
    }

    @Test
    @DisplayName("测试图片渲染")
    void testImageRendering() {
        KeelMarkdownKit kit = new KeelMarkdownKit();
        
        String markdown = "![Alt text](image.jpg)";
        String html = kit.convertMarkdownToHtml(markdown);
        
        assertNotNull(html);
        assertTrue(html.contains("<img"));
        assertTrue(html.contains("src=\"image.jpg\""));
        assertTrue(html.contains("alt=\"Alt text\""));
        
        getUnitTestLogger().info("图片渲染测试通过");
        getUnitTestLogger().info("输出: " + html);
    }

    @Test
    @DisplayName("测试重置扩展为可修改列表")
    void testResetExtensionsWithMutableList() {
        KeelMarkdownKit kit = new KeelMarkdownKit();
        
        // 重置为可修改的列表
        List<Extension> mutableExtensions = new java.util.ArrayList<>();
        mutableExtensions.add(TablesExtension.create());
        
        KeelMarkdownKit result = kit.resetExtensions(mutableExtensions);
        assertSame(kit, result);
        assertEquals(1, kit.getExtensions().size());
        
        // 现在应该可以添加扩展了
        Extension newExtension = TablesExtension.create();
        KeelMarkdownKit appendResult = kit.appendExtensions(newExtension);
        assertSame(kit, appendResult);
        assertEquals(2, kit.getExtensions().size());
        
        getUnitTestLogger().info("重置扩展为可修改列表测试通过");
    }

    @Test
    @DisplayName("测试不同扩展组合")
    void testDifferentExtensionCombinations() {
        // 测试无扩展
        KeelMarkdownKit kit1 = new KeelMarkdownKit(Collections.emptyList());
        String html1 = kit1.convertMarkdownToHtml("# Title");
        assertTrue(html1.contains("<h1>Title</h1>"));
        
        // 测试只有表格扩展
        List<Extension> tableOnly = List.of(TablesExtension.create());
        KeelMarkdownKit kit2 = new KeelMarkdownKit(tableOnly);
        String html2 = kit2.convertMarkdownToHtml("| Col1 | Col2 |\n|------|------|\n| Data | Data |");
        assertTrue(html2.contains("<table>"));
        
        getUnitTestLogger().info("不同扩展组合测试通过");
    }

    @Test
    @DisplayName("测试边界情况")
    void testEdgeCases() {
        KeelMarkdownKit kit = new KeelMarkdownKit();
        
        // 测试单个字符
        String singleChar = kit.convertMarkdownToHtml("a");
        assertNotNull(singleChar);
        assertTrue(singleChar.contains("<p>a</p>"));
        
        // 测试只有空格
        String onlySpaces = kit.convertMarkdownToHtml("   ");
        assertNotNull(onlySpaces);
        // commonmark 对只有空格的输入会输出空字符串
        assertEquals("", onlySpaces);
        
        // 测试换行符
        String newlines = kit.convertMarkdownToHtml("\n\n");
        assertNotNull(newlines);
        // commonmark 对只有换行的输入也会输出空字符串
        assertEquals("", newlines);
        
        getUnitTestLogger().info("边界情况测试通过");
    }
}