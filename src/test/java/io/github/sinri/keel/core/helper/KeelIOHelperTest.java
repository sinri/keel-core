package io.github.sinri.keel.core.helper;

import io.github.sinri.keel.facade.tesuto.unit.KeelJUnit5Test;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(VertxExtension.class)
class KeelIOHelperTest extends KeelJUnit5Test {

    /**
     * The constructor would run after {@code @BeforeAll} annotated method.
     */
    public KeelIOHelperTest(Vertx vertx) {
        super(vertx);
    }

    @Test
    @DisplayName("KeelIOHelper is a singleton")
    void testSingleton() {
        var a = KeelIOHelper.getInstance();
        var b = KeelIOHelper.getInstance();
        assertNotNull(a);
        assertSame(a, b);
    }

    @Test
    @DisplayName("toReadStream should transfer InputStream to ReadStream and complete promise with byte count")
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testToReadStream_success(VertxTestContext testContext) {
        var helper = KeelIOHelper.getInstance();
        String content = "Hello Vert.x Streams!".repeat(100);
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        InputStream is = new ByteArrayInputStream(bytes);

        Promise<Long> transferred = Promise.promise();
        ByteArrayOutputStream collector = new ByteArrayOutputStream();

        helper.toReadStream(is, rs -> {
            rs.exceptionHandler(testContext::failNow);
            rs.handler(buf -> {
                try {
                    collector.write(buf.getBytes());
                } catch (IOException e) {
                    testContext.failNow(e);
                }
            });
            rs.endHandler(v -> {
                // end of stream reached
            });
        }, transferred);

        transferred.future().onComplete(ar -> {
            if (ar.failed()) {
                testContext.failNow(ar.cause());
                return;
            }
            long count = ar.result();
            byte[] collected = collector.toByteArray();
            assertEquals(bytes.length, count, "transferTo should report the exact byte count");
            assertArrayEquals(bytes, collected, "ReadStream should receive identical data");
            testContext.completeNow();
        });
    }

    @Test
    @DisplayName("toReadStream should propagate failure when InputStream throws IOException")
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testToReadStream_failure(VertxTestContext testContext) {
        var helper = KeelIOHelper.getInstance();

        // An InputStream that fails immediately
        InputStream failingIs = new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("boom");
            }
        };

        Promise<Long> transferred = Promise.promise();
        AtomicReference<Throwable> rsError = new AtomicReference<>();

        helper.toReadStream(failingIs, rs -> {
            rs.exceptionHandler(rsError::set);
            rs.handler(buf -> {
            }); // no-op
            rs.endHandler(v -> {
            });
        }, transferred);

        transferred.future().onComplete(ar -> {
            assertTrue(ar.failed(), "Promise should fail when InputStream throws");
            // AsyncOutputReadStream may not receive an error from transferTo failure directly,
            // so rsError might be null. We assert the failure path primarily via the promise.
            testContext.completeNow();
        });
    }

    @Test
    @DisplayName("toWriteStream should transfer WriteStream content into OutputStream and complete promise")
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testToWriteStream_success(VertxTestContext testContext) {
        var helper = KeelIOHelper.getInstance();
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        Promise<Void> completed = Promise.promise();
        var ws = helper.toWriteStream(os, writeStream -> {
            writeStream.exceptionHandler(testContext::failNow);
        }, completed);

        String s1 = "Alpha";
        String s2 = "Beta";
        String s3 = "Gamma";
        ws.write(Buffer.buffer(s1));
        ws.write(Buffer.buffer(s2));
        ws.write(Buffer.buffer(s3))
          .compose(v -> ws.end()); // signal end-of-stream so wrap can finish

        completed.future().onComplete(ar -> {
            if (ar.failed()) {
                testContext.failNow(ar.cause());
                return;
            }
            String result = os.toString(StandardCharsets.UTF_8);
            assertEquals(s1 + s2 + s3, result);
            testContext.completeNow();
        });
    }

    @Test
    @DisplayName("toWriteStream should fail promise when OutputStream throws IOException")
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testToWriteStream_failure(VertxTestContext testContext) {
        var helper = KeelIOHelper.getInstance();
        // OutputStream that throws on any write
        OutputStream failingOs = new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                throw new IOException("nope");
            }
        };

        Promise<Void> completed = Promise.promise();
        AtomicReference<Throwable> wsError = new AtomicReference<>();
        var ws = helper.toWriteStream(failingOs, writeStream -> {
            writeStream.exceptionHandler(wsError::set);
        }, completed);

        ws.write(Buffer.buffer("data"))
          .compose(v -> ws.end());

        completed.future().onComplete(ar -> {
            assertTrue(ar.failed(), "Promise should fail when OutputStream fails");
            // exceptionHandler should be invoked by AsyncInputWriteStream.wrap onFailure
            assertNotNull(wsError.get(), "WriteStream exceptionHandler should receive the error");
            testContext.completeNow();
        });
    }
}