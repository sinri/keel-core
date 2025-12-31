package io.github.sinri.keel.core.cutter;

import io.github.sinri.keel.base.Keel;
import io.github.sinri.keel.core.servant.intravenous.Intravenous;
import io.vertx.core.buffer.Buffer;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;


/**
 * 数据流切分处理器的字符串切片实现。
 * <p>
 * 本类主要面向 Server Sent Events 协议下的数据流切分，数据流为字符流，可通过两个换行符({@code \n\n})切分为字符串片段分别处理。
 *
 * @since 5.0.0
 */
public class IntravenouslyCutterOnString extends IntravenouslyCutter<String> {

    /**
     * 数据流切分处理器的字符串切片实现构造函数，带超时机制。
     *
     * @param stringSingleDropProcessor 切分出的字符串文本片段处理器，由内置的{@link Intravenous}实例调用
     * @param timeout                   以毫秒计的数据流接收处理总时间限制
     */
    public IntravenouslyCutterOnString(@NotNull Keel keel, @NotNull Intravenous.SingleDropProcessor<String> stringSingleDropProcessor, long timeout) {
        super(keel, stringSingleDropProcessor, timeout);
    }

    /**
     * 数据流切分处理器的字符串切片实现构造函数，不带超时机制。
     *
     * @param stringSingleDropProcessor 切分出的字符串文本片段处理器，由内置的{@link Intravenous}实例调用
     */
    public IntravenouslyCutterOnString(@NotNull Keel keel, @NotNull Intravenous.SingleDropProcessor<String> stringSingleDropProcessor) {
        this(keel, stringSingleDropProcessor, 0);
    }

    @Override
    protected @NotNull List<@NotNull String> cut() {
        List<String> list = new ArrayList<>();
        while (true) {
            String s0 = getBufferRef().get().toString(StandardCharsets.UTF_8);
            int index = s0.indexOf("\n\n");
            if (index < 0) break;

            var s1 = s0.substring(0, index);
            var s2 = s0.substring(index + 2);
            getBufferRef().set(Buffer.buffer(s2.getBytes(StandardCharsets.UTF_8)));

            list.add(s1);
        }
        return list;
    }
}
