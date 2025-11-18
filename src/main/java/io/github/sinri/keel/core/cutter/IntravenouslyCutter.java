package io.github.sinri.keel.core.cutter;

import io.github.sinri.keel.core.servant.intravenous.KeelIntravenous;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 数据流切分处理器。
 * <p>
 * 基本使用：<br>
 * 调用{@link IntravenouslyCutter#acceptFromStream(Buffer)}方法从流接收数据。<br>
 * 当流结束时，调用{@link IntravenouslyCutter#stopHere()}方法通知切分器停止接收数据，
 * 并调用{@link IntravenouslyCutter#waitForAllHandled()}等待流切片处理结束。
 *
 * @param <T> 组成可处理的流的实体类型
 * @since 5.0.0
 */
public interface IntravenouslyCutter<T> {
    /**
     * 针对从流中切分出的一部分数据转化为对应实体的一个实例的处理器。
     *
     * @return 单个目标实体处理器
     */
    KeelIntravenous.SingleDropProcessor<T> getSingleDropProcessor();

    /**
     * 从流接收一部分数据。
     *
     * @param buffer 从流中获取到的缓冲存储下来的数据块
     */
    void acceptFromStream(@NotNull Buffer buffer);

    /**
     * 当数据流到终点时，调用本方法通知切分处理器停止接收流数据并处理完毕已收到的数据块。
     * <p>
     * 调用本方法也意味着读取流本身没有出现任何异常。
     */
    default void stopHere() {
        stopHere(null);
    }

    /**
     * 当数据流到终点或遇到异常时，调用本方法通知切分处理器停止接收流数据并处理完毕已收到的数据块。
     *
     * @param throwable 使数据流切分处理器停止工作的异常
     */
    void stopHere(@Nullable Throwable throwable);

    /**
     * 等待所有接收到的数据流内容被处理完毕。
     *
     * @return 异步返回处理结果
     */
    Future<Void> waitForAllHandled();

    /**
     * 切分处理器总处理时间超时异常。
     */
    final class Timeout extends Exception {
        public Timeout() {
            super("This IntravenouslyCutter instance met timeout");
        }
    }
}
