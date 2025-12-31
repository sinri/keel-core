package io.github.sinri.keel.core.utils.io;

import io.github.sinri.keel.base.async.KeelAsyncMixin;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.streams.ReadStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

class AsyncOutputReadStreamImpl implements AsyncOutputReadStream, KeelAsyncMixin {
    private final @NotNull Vertx vertx;

    // Flow control state
    private final AtomicBoolean paused = new AtomicBoolean(true);  // Vert.x ReadStream 默认是暂停状态
    private final AtomicLong demand = new AtomicLong(0);
    private final AtomicBoolean reading = new AtomicBoolean(false);

    // Handlers
    private Handler<Throwable> exceptionHandler;
    private Handler<Buffer> handler;
    private Handler<Void> endHandler;

    // Reading state
    private InputStream inputStream;
    private Promise<Long> readOverPromise;
    private long totalBytesRead = 0;

    public AsyncOutputReadStreamImpl(@NotNull Vertx vertx) {
        this.vertx = vertx;
        // Default handlers
        this.exceptionHandler = t -> {};
        this.handler = b -> {};
        this.endHandler = v -> {};
    }

    @Override
    public void wrap(@NotNull InputStream inputStream) {
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
    public @NotNull Promise<Long> getReadOverPromise() {
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
        vertx.executeBlocking(() -> {
            try {
                // Read up to 8KB chunks
                // If demand is unlimited (Long.MAX_VALUE), read 8KB chunks
                // Otherwise read up to the remaining demand
                long currentDemand = demand.get();
                int chunkSize = (currentDemand == Long.MAX_VALUE) ? 8192 : (int) Math.min(8192, currentDemand);
                if (chunkSize <= 0) {
                    //System.out.println("readNextChunk demand is zero or less");
                    return null; // No demand
                }

                byte[] buffer = new byte[chunkSize];
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
        }, false).onComplete(ar -> {
            reading.set(false); // Clear reading flag

            if (ar.failed()) {
                // Handle error
                Throwable cause = ar.cause();
                vertx.runOnContext(v -> exceptionHandler.handle(cause));
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

            vertx.runOnContext(v -> {
                if (result.endOfStream) {
                    // End of stream reached
                    if (endHandler != null) {
                        endHandler.handle(null);
                    }
                    if (readOverPromise != null) {
                        readOverPromise.complete(totalBytesRead);
                    }
                } else {
                    // Data available
                    totalBytesRead += result.bytesRead;

                    // Only decrease demand if it's not unlimited
                    long currentDemand = demand.get();
                    if (currentDemand != Long.MAX_VALUE) {
                        demand.addAndGet(-result.bytesRead);
                    }

                    Buffer buffer = Buffer.buffer(result.data);
                    handler.handle(buffer);

                    // Continue reading if there's still demand and not paused
                    // For unlimited demand, continue until end of stream
                    if ((demand.get() > 0 || currentDemand == Long.MAX_VALUE) && !paused.get()) {
                        readNextChunk();
                    }
                }
            });
        });
    }

    /**
     * Internal class to represent read operation result
     */
    private static class ReadResult {
        final byte[] data;
        final int bytesRead;
        final boolean endOfStream;

        ReadResult(byte[] data, int bytesRead, boolean endOfStream) {
            this.data = data;
            this.bytesRead = bytesRead;
            this.endOfStream = endOfStream;
        }
    }

    @Override
    public ReadStream<Buffer> exceptionHandler(@Nullable Handler<Throwable> handler) {
        this.exceptionHandler = handler;
        return this;
    }

    @Override
    public ReadStream<Buffer> handler(@Nullable Handler<Buffer> handler) {
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
    public ReadStream<Buffer> endHandler(@Nullable Handler<Void> endHandler) {
        this.endHandler = endHandler;
        return this;
    }

    @Override
    public @NotNull Vertx getVertx() {
        return vertx;
    }
}
