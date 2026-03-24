package io.github.sinri.keel.core.utils.io;

import io.github.sinri.keel.tesuto.KeelJUnit5Test;
import io.vertx.core.buffer.Buffer;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AsyncInputWriteStreamTest extends KeelJUnit5Test {

    public AsyncInputWriteStreamTest() {
        super();
    }

    @Test
    void testBasicWriteToOutputStream(VertxTestContext testContext) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        AsyncInputWriteStream stream = AsyncInputWriteStream.create(getKeel());
        stream.wrap(baos);

        String testData = "Hello, World!";
        stream.write(Buffer.buffer(testData)).onComplete(ar -> {
            testContext.verify(() -> {
                assertTrue(ar.succeeded());
                assertEquals(testData, baos.toString(StandardCharsets.UTF_8));
            });
            testContext.completeNow();
        });
    }

    @Test
    void testMultipleWrites(VertxTestContext testContext) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        AsyncInputWriteStream stream = AsyncInputWriteStream.create(getKeel());
        stream.wrap(baos);

        stream.write(Buffer.buffer("Hello, "))
              .compose(v -> stream.write(Buffer.buffer("World!")))
              .onComplete(ar -> {
                  testContext.verify(() -> {
                      assertTrue(ar.succeeded());
                      assertEquals("Hello, World!", baos.toString(StandardCharsets.UTF_8));
                  });
                  testContext.completeNow();
              });
    }

    @Test
    void testEndClosesOutputStream(VertxTestContext testContext) {
        AtomicBoolean closed = new AtomicBoolean(false);
        ByteArrayOutputStream baos = new ByteArrayOutputStream() {
            @Override
            public void close() throws IOException {
                super.close();
                closed.set(true);
            }
        };

        AsyncInputWriteStream stream = AsyncInputWriteStream.create(getKeel());
        stream.wrap(baos);

        stream.write(Buffer.buffer("data"))
              .compose(v -> stream.end())
              .onComplete(ar -> {
                  testContext.verify(() -> {
                      assertTrue(ar.succeeded());
                      assertTrue(closed.get(), "OutputStream should be closed after end()");
                      assertEquals("data", baos.toString(StandardCharsets.UTF_8));
                  });
                  testContext.completeNow();
              });
    }

    @Test
    void testWriteOverPromise(VertxTestContext testContext) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        AsyncInputWriteStream stream = AsyncInputWriteStream.create(getKeel());
        stream.wrap(baos);

        stream.writeOver().onComplete(ar -> {
            testContext.verify(() -> {
                assertTrue(ar.succeeded());
                assertEquals("test data", baos.toString(StandardCharsets.UTF_8));
            });
            testContext.completeNow();
        });

        stream.write(Buffer.buffer("test data"))
              .compose(v -> stream.end());
    }

    @Test
    void testWriteQueueFull(VertxTestContext testContext) {
        AsyncInputWriteStream stream = AsyncInputWriteStream.create(getKeel());
        stream.setWriteQueueMaxSize(2);

        // Buffer writes (no OutputStream wrapped)
        stream.write(Buffer.buffer("a"));
        stream.write(Buffer.buffer("b"));

        testContext.verify(() -> {
            assertTrue(stream.writeQueueFull(), "Queue should be full after 2 writes with maxSize=2");
        });
        testContext.completeNow();
    }

    @Test
    void testDrainHandlerOnlyFiresOnTransition(VertxTestContext testContext) {
        AsyncInputWriteStream stream = AsyncInputWriteStream.create(getKeel());
        stream.setWriteQueueMaxSize(2);

        AtomicInteger drainCount = new AtomicInteger(0);
        stream.drainHandler(v -> drainCount.incrementAndGet());

        // Write without filling queue - drainHandler should NOT fire spuriously
        stream.write(Buffer.buffer("a"));

        // Give event loop time to process any queued runOnContext calls
        getKeel().setTimer(200, timerId -> {
            testContext.verify(() -> {
                assertEquals(0, drainCount.get(), "drainHandler should not fire when queue was never full");
            });
            testContext.completeNow();
        });
    }

    @Test
    void testExceptionHandler(VertxTestContext testContext) {
        OutputStream failingStream = new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                throw new IOException("Simulated write error");
            }
        };

        AsyncInputWriteStream stream = AsyncInputWriteStream.create(getKeel());
        stream.exceptionHandler(t -> {
            testContext.verify(() -> {
                // The IOException is wrapped in a RuntimeException by executeBlocking
                Throwable cause = t.getCause() != null ? t.getCause() : t;
                assertTrue(cause.getMessage().contains("Simulated write error"));
            });
            testContext.completeNow();
        });
        stream.wrap(failingStream);

        stream.write(Buffer.buffer("data"));
    }

    @Test
    void testDiscardWriteAfterClose(VertxTestContext testContext) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        AsyncInputWriteStream stream = AsyncInputWriteStream.create(getKeel());
        stream.wrap(baos);

        stream.write(Buffer.buffer("before"))
              .compose(v -> stream.end())
              .compose(v -> stream.write(Buffer.buffer("after-close")))
              .onComplete(ar -> {
                  testContext.verify(() -> {
                      assertTrue(ar.succeeded());
                      assertEquals("before", baos.toString(StandardCharsets.UTF_8));
                  });
                  testContext.completeNow();
              });
    }

    @Test
    void testBufferingBeforeWrap(VertxTestContext testContext) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        AsyncInputWriteStream stream = AsyncInputWriteStream.create(getKeel());

        // Write data before wrapping an OutputStream
        stream.write(Buffer.buffer("buffered "));
        stream.write(Buffer.buffer("data"));

        // Now wrap - buffered data should be transferred
        stream.wrap(baos);

        // Give time for async transfer
        getKeel().setTimer(500, timerId -> {
            testContext.verify(() -> {
                assertEquals("buffered data", baos.toString(StandardCharsets.UTF_8));
            });
            testContext.completeNow();
        });
    }
}
