package io.github.sinri.keel.core.cutter;

import io.github.sinri.keel.core.servant.intravenous.Intravenous;
import io.vertx.core.buffer.Buffer;
import org.jspecify.annotations.NullMarked;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;


/**
 * 数据流切分处理器的字符串切片实现。
 * <p>
 * 本类主要面向 Server Sent Events 协议下的数据流切分，以空行作为片段边界。
 * 支持 LF、CRLF、CR 及混合换行，片段内部换行统一为 LF，返回值不含末尾的分隔换行。
 * <p>
 * 仅在完整片段形成后解码 UTF-8；未完成片段保留原始字节，流结束不会强制交付。
 * 本类只负责切分，不解析 SSE 字段；连续两个行结束符之间的空片段仍交由调用方处理，以兼容原有 LF 切分行为。
 *
 * @since 5.0.0
 */
@NullMarked
public class IntravenouslyCutterOnString extends IntravenouslyCutter<String> {
    private int scanOffset;
    private int lineStart;
    private int contentEnd;
    private boolean skipLf;
    private boolean hasLineEnding;


    /**
     * 数据流切分处理器的字符串切片实现构造函数。
     *
     * @param stringSingleDropProcessor 切分出的字符串文本片段处理器，由内置的{@link Intravenous}实例调用
     */
    public IntravenouslyCutterOnString(Intravenous.SingleDropProcessor<String> stringSingleDropProcessor, long timeout) {
        super(stringSingleDropProcessor, timeout);
    }

    public IntravenouslyCutterOnString(Intravenous.SingleDropProcessor<String> stringSingleDropProcessor) {
        this(stringSingleDropProcessor, 0);
    }

    @Override
    protected List<String> cut() {
        List<String> list = new ArrayList<>();
        Buffer buffer = getBufferRef().get();
        int consumed = 0;
        while (scanOffset < buffer.length()) {
            int index = scanOffset++;
            byte value = buffer.getByte(index);
            if (skipLf) {
                skipLf = false;
                if (value == '\n') {
                    // The LF belongs to the preceding CR, even across calls.
                    lineStart = scanOffset;
                    if (consumed == index) {
                        consumed = scanOffset;
                        contentEnd = scanOffset;
                    }
                    continue;
                }
            }
            if (value != '\r' && value != '\n') continue;

            skipLf = value == '\r';
            if (index == lineStart && hasLineEnding) {
                String event = buffer.getString(consumed, contentEnd, StandardCharsets.UTF_8.name());
                list.add(event.replace("\r\n", "\n").replace('\r', '\n'));
                consumed = scanOffset;
                contentEnd = scanOffset;
                hasLineEnding = false;
            } else {
                contentEnd = index;
                hasLineEnding = true;
            }
            lineStart = scanOffset;
        }
        if (consumed > 0) {
            getBufferRef().set(buffer.getBuffer(consumed, buffer.length()));
            scanOffset -= consumed;
            lineStart -= consumed;
            contentEnd -= consumed;
        }
        return list;
    }
}
