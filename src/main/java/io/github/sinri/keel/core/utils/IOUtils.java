package io.github.sinri.keel.core.utils;

import io.github.sinri.keel.base.annotations.TechnicalPreview;
import io.github.sinri.keel.base.async.Keel;
import io.github.sinri.keel.core.utils.io.AsyncInputWriteStream;
import io.github.sinri.keel.core.utils.io.AsyncOutputReadStream;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.streams.ReadStream;
import io.vertx.core.streams.WriteStream;
import org.jspecify.annotations.NullMarked;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * IO工具类
 *
 * @since 5.0.0
 */
@NullMarked
public class IOUtils {
    private IOUtils() {
    }

    @TechnicalPreview(since = "5.0.0")
    public static AsyncOutputReadStream toReadStream(Keel keel, InputStream inputStream, Handler<ReadStream<Buffer>> handler) {
        var readStream = AsyncOutputReadStream.create(keel);
        readStream.pause();
        handler.handle(readStream);
        readStream.resume();
        readStream.wrap(inputStream);
        return readStream;
    }

    @TechnicalPreview(since = "5.0.0")
    public static AsyncInputWriteStream toWriteStream(Keel keel, OutputStream outputStream, Handler<WriteStream<Buffer>> handler) {
        var writeStream = AsyncInputWriteStream.create(keel);
        handler.handle(writeStream);
        writeStream.wrap(outputStream);
        return writeStream;
    }
}
