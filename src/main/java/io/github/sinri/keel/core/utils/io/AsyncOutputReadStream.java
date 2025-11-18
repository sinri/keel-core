package io.github.sinri.keel.core.utils.io;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.streams.ReadStream;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;

/**
 * 一个Vertx体系下 {@link ReadStream} 的实现，其将 {@link InputStream} 封装来实现异步流式读取。
 *
 * @since 5.0.0
 */
public interface AsyncOutputReadStream extends ReadStream<Buffer> {
    static AsyncOutputReadStream create() {
        return new AsyncOutputReadStreamImpl();
    }

    void wrap(@NotNull InputStream inputStream);

    @NotNull
    Promise<Long> getReadOverPromise();

    @NotNull
    default Future<Long> readOver() {
        return getReadOverPromise().future();
    }
}
