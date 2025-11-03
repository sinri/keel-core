package io.github.sinri.keel.core.helper;

import io.github.sinri.keel.core.TechnicalPreview;
import io.github.sinri.keel.core.helper.io.AsyncInputWriteStream;
import io.github.sinri.keel.core.helper.io.AsyncOutputReadStream;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.streams.ReadStream;
import io.vertx.core.streams.WriteStream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

/**
 * @since 4.1.5
 */
public class KeelIOHelper {
    private static final KeelIOHelper instance = new KeelIOHelper();

    private KeelIOHelper() {

    }

    static KeelIOHelper getInstance() {
        return instance;
    }

    @TechnicalPreview(since = "4.1.5")
    public ReadStream<Buffer> toReadStream(
            @Nonnull InputStream inputStream,
            @Nonnull Handler<ReadStream<Buffer>> handler,
            @Nullable Promise<Long> promise
    ) {
        var readStream = new AsyncOutputReadStream();
        readStream.pause();
        handler.handle(readStream);
        readStream.resume();

        var future = Keel.getVertx().executeBlocking(() -> {
            try {
                return inputStream.transferTo(readStream);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        if (promise != null) {
            future.onComplete(promise);
        }

        return readStream;
    }

    @TechnicalPreview(since = "4.1.5")
    public WriteStream<Buffer> toWriteStream(
            @Nonnull OutputStream outputStream,
            @Nonnull Handler<WriteStream<Buffer>> handler,
            @Nullable Promise<Void> promise
    ) {
        var writeStream = new AsyncInputWriteStream();
        handler.handle(writeStream);
        Future<Void> future = writeStream.wrap(outputStream);
        if (promise != null) {
            future.onComplete(promise);
        }
        return writeStream;
    }
}
