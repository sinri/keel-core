package io.github.sinri.keel.core.markdown;

import io.vertx.core.Handler;
import org.commonmark.Extension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

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
@NullMarked
public final class KeelMarkdownKit {
    private final Parser markdownParser;
    private final HtmlRenderer htmlRenderer;

    public KeelMarkdownKit() {
        this(List.of(TablesExtension.create()), null, null);
    }

    public KeelMarkdownKit(
            List<Extension> extensions,
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

    public String convertMarkdownToHtml(Reader mdReader) throws IOException {
        Node document = markdownParser.parseReader(mdReader);
        return htmlRenderer.render(document);
    }

}
