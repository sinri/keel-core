package io.github.sinri.keel.core.helper;

import io.github.sinri.keel.core.TechnicalPreview;
import io.github.sinri.keel.core.helper.io.AsyncInputWriteStream;
import io.github.sinri.keel.core.helper.io.AsyncOutputReadStream;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.streams.ReadStream;
import io.vertx.core.streams.WriteStream;

import javax.annotation.Nonnull;
import java.io.InputStream;
import java.io.OutputStream;

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
    public AsyncOutputReadStream toReadStream(@Nonnull InputStream inputStream, @Nonnull Handler<ReadStream<Buffer>> handler) {
        var readStream = AsyncOutputReadStream.create();
        readStream.pause();
        handler.handle(readStream);
        readStream.resume();
        readStream.wrap(inputStream);
        return readStream;
    }

    @TechnicalPreview(since = "4.1.5")
    public AsyncInputWriteStream toWriteStream(@Nonnull OutputStream outputStream, @Nonnull Handler<WriteStream<Buffer>> handler) {
        var writeStream = AsyncInputWriteStream.create();
        handler.handle(writeStream);
        writeStream.wrap(outputStream);
        return writeStream;
    }
}
