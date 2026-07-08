package io.github.sinri.keel.core.markdown;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class KeelMarkdownKitTest {
    @Test
    void convertMarkdownToHtmlRendersBasicMarkdown() {
        KeelMarkdownKit kit = new KeelMarkdownKit();

        String html = kit.convertMarkdownToHtml("# Title\n\nHello **Keel**.");

        assertTrue(html.contains("<h1>Title</h1>"));
        assertTrue(html.contains("<p>Hello <strong>Keel</strong>.</p>"));
    }

    @Test
    void convertMarkdownToHtmlRendersGfmTablesByDefault() {
        KeelMarkdownKit kit = new KeelMarkdownKit();

        String html = kit.convertMarkdownToHtml("""
                                                | Name | Value |
                                                | --- | --- |
                                                | keel | core |
                                                """);

        assertTrue(html.contains("<table>"));
        assertTrue(html.contains("<th>Name</th>"));
        assertTrue(html.contains("<td>keel</td>"));
    }
}
