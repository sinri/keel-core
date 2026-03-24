package io.github.sinri.keel.core.utils.io;

import io.github.sinri.keel.tesuto.KeelJUnit5Test;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class AsyncOutputReadStreamTest extends KeelJUnit5Test {

    public AsyncOutputReadStreamTest() {
        super();
    }

    @Test
    void testBasicRead(VertxTestContext testContext) {
        String testData = "Hello, World! This is a test.";
        byte[] testBytes = testData.getBytes(StandardCharsets.UTF_8);
        InputStream inputStream = new ByteArrayInputStream(testBytes);

        AsyncOutputReadStream stream = AsyncOutputReadStream.create(getKeel());
        StringBuilder result = new StringBuilder();
        AtomicInteger handlerCallCount = new AtomicInteger(0);

        stream.handler(buffer -> {
            handlerCallCount.incrementAndGet();
            result.append(buffer.toString(StandardCharsets.UTF_8));
        });

        stream.endHandler(v -> {
            testContext.verify(() -> {
                assertEquals(testData, result.toString());
                assertEquals(1, handlerCallCount.get());
            });
            testContext.completeNow();
        });

        stream.exceptionHandler(testContext::failNow);

        stream.wrap(inputStream);
        stream.resume();
    }

    @Test
    void testPauseAndResume(VertxTestContext testContext) {
        StringBuilder result = new StringBuilder();
        AtomicInteger pauseCount = new AtomicInteger(0);
        AtomicInteger resumeCount = new AtomicInteger(0);

        AsyncOutputReadStream stream = AsyncOutputReadStream.create(getKeel());

        stream.handler(buffer -> {
            result.append(buffer.toString(StandardCharsets.UTF_8));
        });

        stream.endHandler(v -> {
            testContext.verify(() -> {
                assertEquals("ABC", result.toString());
            });
            testContext.completeNow();
        });

        stream.exceptionHandler(testContext::failNow);

        // Create a slowly readable input stream
        InputStream slowStream = new SlowInputStream("ABC");
        stream.wrap(slowStream);

        // Initially paused
        stream.pause();
        pauseCount.incrementAndGet();

        // Resume after a short delay
        getKeel().setTimer(100, timerId -> {
            stream.resume();
            resumeCount.incrementAndGet();

            // Pause again
            getKeel().setTimer(100, timerId2 -> {
                stream.pause();
                pauseCount.incrementAndGet();

                // Resume again
                getKeel().setTimer(100, timerId3 -> {
                    stream.resume();
                    resumeCount.incrementAndGet();
                });
            });
        });
    }

    @Test
    void testEndOfStream(VertxTestContext testContext) {
        byte[] testBytes = new byte[0]; // Empty stream
        InputStream inputStream = new ByteArrayInputStream(testBytes);

        AsyncOutputReadStream stream = AsyncOutputReadStream.create(getKeel());
        AtomicInteger endHandlerCalled = new AtomicInteger(0);

        stream.handler(buffer -> {
            // Should not be called for empty stream
            testContext.failNow("Handler should not be called for empty stream");
        });

        stream.endHandler(v -> {
            testContext.verify(() -> {
                assertEquals(1, endHandlerCalled.incrementAndGet());
            });
            testContext.completeNow();
        });

        stream.exceptionHandler(testContext::failNow);

        stream.wrap(inputStream);
        stream.resume();
    }

    @Test
    void testExceptionHandler(VertxTestContext testContext) {
        InputStream failingStream = new InputStream() {
            @Override
            public int read() {
                throw new RuntimeException("Simulated read error");
            }
        };

        AsyncOutputReadStream stream = AsyncOutputReadStream.create(getKeel());
        AtomicInteger exceptionHandlerCalled = new AtomicInteger(0);

        stream.handler(buffer -> {
            testContext.failNow("Handler should not be called when exception occurs");
        });

        stream.endHandler(v -> {
            testContext.failNow("End handler should not be called when exception occurs");
        });

        stream.exceptionHandler(throwable -> {
            testContext.verify(() -> {
                assertEquals(1, exceptionHandlerCalled.incrementAndGet());
                assertEquals("Simulated read error", throwable.getMessage());
            });
            testContext.completeNow();
        });

        stream.wrap(failingStream);
        stream.resume();
    }

    @Test
    void testReadOverPromise(VertxTestContext testContext) {
        String testData = "Test data for promise";
        byte[] testBytes = testData.getBytes(StandardCharsets.UTF_8);
        InputStream inputStream = new ByteArrayInputStream(testBytes);

        AsyncOutputReadStream stream = AsyncOutputReadStream.create(getKeel());

        stream.handler(buffer -> {
            // Just consume the data
        });

        stream.endHandler(v -> {
            // Check the promise after stream ends using future
            stream.readOver().onComplete(ar -> {
                testContext.verify(() -> {
                    assertTrue(ar.succeeded());
                    assertEquals(testBytes.length, ar.result());
                });
                testContext.completeNow();
            });
        });

        stream.exceptionHandler(testContext::failNow);

        stream.wrap(inputStream);
        stream.resume();
    }

    @Test
    void testMultipleChunks(VertxTestContext testContext) {
        // Create data larger than default chunk size (8KB)
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            sb.append("Chunk").append(i).append("\n");
        }
        String testData = sb.toString();
        byte[] testBytes = testData.getBytes(StandardCharsets.UTF_8);
        InputStream inputStream = new ByteArrayInputStream(testBytes);

        AsyncOutputReadStream stream = AsyncOutputReadStream.create(getKeel());
        StringBuilder result = new StringBuilder();
        AtomicInteger chunkCount = new AtomicInteger(0);

        stream.handler(buffer -> {
            chunkCount.incrementAndGet();
            result.append(buffer.toString(StandardCharsets.UTF_8));
        });

        stream.endHandler(v -> {
            testContext.verify(() -> {
                assertEquals(testData, result.toString());
                assertTrue(chunkCount.get() > 1, "Should have multiple chunks");
            });
            testContext.completeNow();
        });

        stream.exceptionHandler(testContext::failNow);

        stream.wrap(inputStream);
        stream.resume();
    }

    @Test
    void testWrapThrowsIfAlreadyWrapped(VertxTestContext testContext) {
        AsyncOutputReadStream readStream = AsyncOutputReadStream.create(getKeel());

        InputStream inputStream1 = new ByteArrayInputStream(new byte[0]);
        InputStream inputStream2 = new ByteArrayInputStream(new byte[0]);

        readStream.wrap(inputStream1);

        testContext.verify(() -> {
            assertThrows(IllegalStateException.class, () -> readStream.wrap(inputStream2));
        });

        testContext.completeNow();
    }

    @Test
    void testGetReadOverPromiseThrowsIfNotWrapped(VertxTestContext testContext) {
        AsyncOutputReadStream stream = AsyncOutputReadStream.create(getKeel());

        testContext.verify(() -> {
            assertThrows(IllegalStateException.class, stream::getReadOverPromise);
        });

        testContext.completeNow();
    }

    /**
     * A slow input stream that simulates reading with delays
     */
    private static class SlowInputStream extends InputStream {
        private final String data;
        private int position = 0;

        SlowInputStream(String data) {
            this.data = data;
        }

        @Override
        public int read() {
            if (position >= data.length()) {
                return -1;
            }
            try {
                Thread.sleep(50); // Small delay between reads
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return data.charAt(position++);
        }
    }
}
