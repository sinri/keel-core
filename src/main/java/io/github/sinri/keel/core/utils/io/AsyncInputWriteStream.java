package io.github.sinri.keel.core.utils.io;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.streams.WriteStream;
import org.jspecify.annotations.NullMarked;

import java.io.OutputStream;

/**
 * 一个Vertx体系下 {@link WriteStream} 的实现，其将{@link OutputStream}封装来实现流式异步写入。
 *
 * @since 5.0.0
 */
@NullMarked
public interface AsyncInputWriteStream extends WriteStream<Buffer> {
    static AsyncInputWriteStream create(Vertx vertx) {
        return new AsyncInputWriteStreamImpl(vertx);
    }

    void wrap(OutputStream os);

    Promise<Void> getWriteOverPromise();

    default Future<Void> writeOver() {
        return getWriteOverPromise().future();
    }
}
