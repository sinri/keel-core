package io.github.sinri.keel.core.markdown;

import org.commonmark.Extension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import java.util.List;


/**
 * Markdown处理工具。
 * <p>
 * 本类功能基于 commonmark-java 实现。
 *
 * @see <a href="https://github.com/commonmark/commonmark-java">commonmark java readme</a>
 * @since 5.0.0 based on `org.commonmark`
 */
public class KeelMarkdownKit {
    private List<Extension> extensions;
    private Parser markdownParser;
    private HtmlRenderer htmlRenderer;

    public KeelMarkdownKit() {
        extensions = List.of(TablesExtension.create());
        markdownParser = Parser.builder()
                               .extensions(extensions)
                               .build();
        htmlRenderer = HtmlRenderer.builder()
                                   .extensions(extensions)
                                   .build();
    }

    public KeelMarkdownKit(List<Extension> extensions) {
        this.extensions = extensions;
        markdownParser = Parser.builder()
                               .extensions(extensions)
                               .build();
        htmlRenderer = HtmlRenderer.builder()
                                   .extensions(extensions)
                                   .build();
    }

    public KeelMarkdownKit resetExtensions(List<Extension> extensions) {
        this.extensions = extensions;
        markdownParser = Parser.builder()
                               .extensions(extensions)
                               .build();
        htmlRenderer = HtmlRenderer.builder()
                                   .extensions(extensions)
                                   .build();
        return this;
    }

    public KeelMarkdownKit appendExtensions(Extension extension) {
        this.extensions.add(extension);
        markdownParser = Parser.builder()
                               .extensions(extensions)
                               .build();
        htmlRenderer = HtmlRenderer.builder()
                                   .extensions(extensions)
                                   .build();
        return this;
    }

    public List<Extension> getExtensions() {
        return extensions;
    }

    public Parser getMarkdownParser() {
        return markdownParser;
    }

    public HtmlRenderer getHtmlRenderer() {
        return htmlRenderer;
    }

    public String convertMarkdownToHtml(String md) {
        Node document = markdownParser.parse(md);
        return htmlRenderer.render(document);
    }
}
