package io.github.sinri.keel.core.utils.io;

import io.github.sinri.keel.base.async.Keel;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.streams.ReadStream;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@NullMarked
class AsyncOutputReadStreamImpl implements AsyncOutputReadStream {
    private final Keel keel;

    private static final int CHUNK_SIZE = 8192;

    // Flow control state (demand counts chunks, not bytes, per Vert.x ReadStream contract)
    private final AtomicBoolean paused = new AtomicBoolean(true);  // Vert.x ReadStream 默认是暂停状态
    private final AtomicLong demand = new AtomicLong(0);
    private final AtomicBoolean reading = new AtomicBoolean(false);

    // Handlers
    private Handler<Throwable> exceptionHandler;
    private Handler<Buffer> handler;
    private Handler<Void> endHandler;

    // Reading state
    private @Nullable InputStream inputStream;
    private @Nullable Promise<Long> readOverPromise;
    private long totalBytesRead = 0;

    public AsyncOutputReadStreamImpl(Keel keel) {
        this.keel = keel;
        // Default handlers
        this.exceptionHandler = t -> {
        };
        this.handler = b -> {
        };
        this.endHandler = v -> {
        };
    }

    @Override
    public void wrap(InputStream inputStream) {
        if (this.inputStream != null) {
            throw new IllegalStateException("Stream has already been wrapped");
        }
        this.inputStream = inputStream;
        this.readOverPromise = Promise.promise();
        this.totalBytesRead = 0;

        // Start reading asynchronously
        readNextChunk();
    }

    @Override
    public Promise<Long> getReadOverPromise() {
        if (readOverPromise == null) {
            throw new IllegalStateException("Stream has not been wrapped yet");
        }
        return readOverPromise;
    }

    /**
     * Asynchronously read the next chunk from the InputStream.
     * This method ensures non-blocking operation by checking flow control state
     * and using executeBlocking for actual IO operations.
     */
    private void readNextChunk() {
        // System.out.println("readNextChunk start");
        // Check if we should read (has demand and not paused and not already reading)
        if (demand.get() <= 0 || paused.get() || reading.get() || inputStream == null) {
            // System.out.println("readNextChunk should not read, died");
            return;
        }

        // Mark as reading to prevent concurrent reads
        if (!reading.compareAndSet(false, true)) {
            //System.out.println("readNextChunk read locked, died");
            return;
        }

        // Use executeBlocking for the actual IO operation
        // Wrap with timeout to prevent indefinite blocking (e.g., when waiting for System.in input)
        Promise<ReadResult> resultPromise = Promise.promise();
        keel.executeBlocking(() -> {
            try {
                byte[] buffer = new byte[CHUNK_SIZE];
                int bytesRead = inputStream.read(buffer);

                if (bytesRead == -1) {
                    // End of stream
                    return new ReadResult(null, 0, true);
                } else {
                    // Data read
                    byte[] data = new byte[bytesRead];
                    System.arraycopy(buffer, 0, data, 0, bytesRead);
                    return new ReadResult(data, bytesRead, false);
                }
            } catch (IOException e) {
                throw new RuntimeException("IO error while reading", e);
            }
        }, false).onComplete(resultPromise);

        // Set timeout - if operation takes too long, it will be retried
        long timeoutId = keel.setTimer(60000, timerId -> {
            // Timeout - fail the promise and trigger retry
            if (!resultPromise.future().isComplete()) {
                resultPromise.fail(new java.util.concurrent.TimeoutException("Read operation timed out after 60 seconds"));
            }
        });

        resultPromise.future().onComplete(ar -> {
            // Cancel the timeout timer
            keel.cancelTimer(timeoutId);
            reading.set(false); // Clear reading flag

            if (ar.failed()) {
                // Check if it's a timeout
                Throwable cause = ar.cause();
                boolean isTimeout = cause instanceof java.util.concurrent.TimeoutException
                        || (cause != null && cause.getMessage() != null && cause.getMessage().contains("timeout"));

                if (isTimeout) {
                    // Timeout - retry later (non-blocking)
                    // This prevents the worker thread from being blocked indefinitely
                    keel.setTimer(100, timerId -> {
                        if (!paused.get() && demand.get() > 0) {
                            readNextChunk();
                        }
                    });
                    return;
                }

                // Handle other errors
                keel.runOnContext(v -> exceptionHandler.handle(cause));
                if (readOverPromise != null) {
                    readOverPromise.fail(cause);
                }
                return;
            }

            ReadResult result = ar.result();
            if (result == null) {
                // No demand or other condition, try again later
                readNextChunk();
                return;
            }

            keel.runOnContext(new Handler<Void>() {
                @Override
                public void handle(Void v) {
                    if (result.endOfStream) {
                        // End of stream reached
                        endHandler.handle(v);
                        if (readOverPromise != null) {
                            readOverPromise.complete(totalBytesRead);
                        }
                    } else {
                        // Data available
                        totalBytesRead += result.bytesRead;

                        // Decrease demand by 1 chunk (not by bytes), unless unlimited
                        long currentDemand = demand.get();
                        if (currentDemand != Long.MAX_VALUE) {
                            demand.decrementAndGet();
                        }

                        byte[] resultData = result.data;
                        if (resultData != null) {
                            Buffer buffer = Buffer.buffer(resultData);
                            handler.handle(buffer);
                        }

                        // Continue reading if there's still demand and not paused
                        if (demand.get() > 0 && !paused.get()) {
                            AsyncOutputReadStreamImpl.this.readNextChunk();
                        }
                    }
                }
            });
        });
    }

    @Override
    public ReadStream<Buffer> exceptionHandler(Handler<Throwable> handler) {
        this.exceptionHandler = handler;
        return this;
    }

    @Override
    public ReadStream<Buffer> handler(Handler<Buffer> handler) {
        this.handler = handler;
        return this;
    }

    @Override
    public ReadStream<Buffer> pause() {
        paused.set(true);
        return this;
    }

    @Override
    public ReadStream<Buffer> resume() {
        if (paused.compareAndSet(true, false)) {
            // Set unlimited demand (Vert.x ReadStream resume() means unlimited demand)
            demand.set(Long.MAX_VALUE);
            // Start reading
            readNextChunk();
        }
        return this;
    }

    @Override
    public ReadStream<Buffer> fetch(long amount) {
        if (amount <= 0) {
            return this;
        }

        long newDemand = demand.addAndGet(amount);

        // If not paused and not currently reading, start reading
        if (!paused.get() && !reading.get()) {
            readNextChunk();
        }

        return this;
    }

    @Override
    public ReadStream<Buffer> endHandler(Handler<Void> endHandler) {
        this.endHandler = endHandler;
        return this;
    }

    /**
     * Internal class to represent read operation result
     */
    @NullMarked
    private static class ReadResult {
        final byte @Nullable [] data;
        final int bytesRead;
        final boolean endOfStream;

        ReadResult(byte @Nullable [] data, int bytesRead, boolean endOfStream) {
            this.data = data;
            this.bytesRead = bytesRead;
            this.endOfStream = endOfStream;
        }
    }
}
