package io.github.sinri.keel.core.markdown;

import org.commonmark.Extension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.jetbrains.annotations.NotNull;

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
    @NotNull
    private List<Extension> extensions;
    @NotNull
    private Parser markdownParser;
    @NotNull
    private HtmlRenderer htmlRenderer;

    public KeelMarkdownKit() {
        this(List.of(TablesExtension.create()));
    }

    public KeelMarkdownKit(@NotNull List<Extension> extensions) {
        this.extensions = extensions;
        this.markdownParser = Parser.builder()
                                    .extensions(extensions)
                                    .build();
        this.htmlRenderer = HtmlRenderer.builder()
                                        .extensions(extensions)
                                        .build();
    }

    public KeelMarkdownKit resetExtensions(@NotNull List<Extension> extensions) {
        this.extensions = extensions;
        this.markdownParser = Parser.builder()
                                    .extensions(extensions)
                                    .build();
        this.htmlRenderer = HtmlRenderer.builder()
                                        .extensions(extensions)
                                        .build();
        return this;
    }

    public KeelMarkdownKit appendExtensions(@NotNull Extension extension) {
        this.extensions.add(extension);
        this.markdownParser = Parser.builder()
                                    .extensions(extensions)
                                    .build();
        this.htmlRenderer = HtmlRenderer.builder()
                                        .extensions(extensions)
                                        .build();
        return this;
    }

    @NotNull
    public List<Extension> getExtensions() {
        return extensions;
    }

    public @NotNull Parser getMarkdownParser() {
        return markdownParser;
    }

    public @NotNull HtmlRenderer getHtmlRenderer() {
        return htmlRenderer;
    }

    @NotNull
    public String convertMarkdownToHtml(@NotNull String md) {
        Node document = markdownParser.parse(md);
        return htmlRenderer.render(document);
    }
}
