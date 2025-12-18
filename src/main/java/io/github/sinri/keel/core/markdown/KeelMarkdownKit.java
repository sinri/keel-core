package io.github.sinri.keel.core.markdown;

import io.vertx.core.Handler;
import org.commonmark.Extension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.Reader;
import java.util.List;


/**
 * Markdown处理工具。
 * <p>
 * 本类功能基于 commonmark-java 实现。
 *
 * @see <a href="https://github.com/commonmark/commonmark-java">commonmark java readme</a>
 * @since 5.0.0 based on `org.commonmark`
 */
public final class KeelMarkdownKit {
    @NotNull
    private final Parser markdownParser;
    @NotNull
    private final HtmlRenderer htmlRenderer;

    public KeelMarkdownKit() {
        this(List.of(TablesExtension.create()), null, null);
    }

    public KeelMarkdownKit(
            @NotNull List<Extension> extensions,
            @Nullable Handler<Parser.Builder> parserBuilderHandler,
            @Nullable Handler<HtmlRenderer.Builder> htmlRendererBuilderHandler
    ) {
        Parser.Builder pb = Parser.builder().extensions(extensions);
        if (parserBuilderHandler != null) {
            parserBuilderHandler.handle(pb);
        }
        this.markdownParser = pb.build();

        HtmlRenderer.Builder hb = HtmlRenderer.builder().extensions(extensions);
        if (htmlRendererBuilderHandler != null) {
            htmlRendererBuilderHandler.handle(hb);
        }
        this.htmlRenderer = hb.build();
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

    @NotNull
    public String convertMarkdownToHtml(@NotNull Reader mdReader) throws IOException {
        Node document = markdownParser.parseReader(mdReader);
        return htmlRenderer.render(document);
    }

}
