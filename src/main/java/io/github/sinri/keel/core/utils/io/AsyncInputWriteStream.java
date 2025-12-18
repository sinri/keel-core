package io.github.sinri.keel.core.utils.io;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.streams.WriteStream;
import org.jetbrains.annotations.NotNull;

import java.io.OutputStream;

/**
 * 一个Vertx体系下 {@link WriteStream} 的实现，其将{@link OutputStream}封装来实现流式异步写入。
 *
 * @since 5.0.0
 */
public interface AsyncInputWriteStream extends WriteStream<Buffer> {
    static AsyncInputWriteStream create(Vertx vertx) {
        return new AsyncInputWriteStreamImpl2(vertx);
    }

    void wrap(@NotNull OutputStream os);

    @NotNull
    Promise<Void> getWriteOverPromise();

    @NotNull
    default Future<Void> writeOver() {
        return getWriteOverPromise().future();
    }
}
