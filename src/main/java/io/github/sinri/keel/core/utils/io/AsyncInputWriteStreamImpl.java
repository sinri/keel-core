package io.github.sinri.keel.core.utils.io;

import io.github.sinri.keel.base.async.Keel;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.streams.WriteStream;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A non-blocking implementation of AsyncInputWriteStream for Vert.x.
 * <p>
 * This class provides a fully asynchronous WriteStream that can buffer data
 * and transfer it to an OutputStream using Vert.x's executeBlocking mechanism.
 * Unlike the blocking InputStream-based implementation, this version uses
 * asynchronous handlers and promises throughout.
 * <p>
 * Data written to this stream is buffered asynchronously and can be consumed
 * through registered data handlers or transferred to an OutputStream.
 *
 * @since 5.0.0
 */
@NullMarked
class AsyncInputWriteStreamImpl implements AsyncInputWriteStream {

    private final Keel keel;
    private final Context context;
    private final ConcurrentLinkedQueue<PendingWrite> buffer = new ConcurrentLinkedQueue<>();
    private final AtomicBoolean closed = new AtomicBoolean(false);

    private volatile int maxSize = 1000;
    private volatile int maxBufferSize = Integer.MAX_VALUE;
    private volatile @Nullable OutputStream wrappedOutputStream;
    private Handler<Void> drainHandler = __ -> {
    };
    private Handler<Throwable> exceptionHandler = __ -> {
    };
    private Handler<Buffer> dataHandler = __ -> {
    };
    private @Nullable Promise<Void> writeOverPromise;

    public AsyncInputWriteStreamImpl(Keel keel) {
        this.keel = keel;
        this.context = keel.getOrCreateContext();
    }

    /**
     * Wraps an OutputStream and transfers all subsequent data directly to it.
     * Any buffered data will be transferred first.
     *
     * @param os OutputStream to transfer data to
     */
    @Override
    public void wrap(OutputStream os) {
        this.wrappedOutputStream = os;
        this.writeOverPromise = Promise.promise();

        // Transfer any existing buffered data
        if (!buffer.isEmpty()) {
            context.executeBlocking(() -> {
                       try {
                           transferBufferedData(os);
                           return null;
                       } catch (Exception e) {
                           throw new RuntimeException("Failed to transfer buffered data", e);
                       }
                   })
                   .onFailure(t -> {
                       exceptionHandler.handle(t);
                       // Don't fail the promise yet, future writes might still succeed
                   });
        }
    }

    /**
     * Transfers all buffered data to the OutputStream.
     */
    private void transferBufferedData(OutputStream os) throws IOException {
        PendingWrite write;
        while ((write = buffer.poll()) != null) {
            if (write.data != null) {
                byte[] bytes = write.data.getBytes();
                os.write(bytes);
                os.flush();
            }
            write.completion.tryComplete();
        }
    }

    /**
     * Gets the promise that completes when all data has been written and the stream is closed.
     *
     * @return Promise that completes on write completion
     */
    @Override
    public Promise<Void> getWriteOverPromise() {
        if (writeOverPromise == null) {
            writeOverPromise = Promise.promise();
        }
        return writeOverPromise;
    }

    /**
     * Sets the exception handler for this stream.
     *
     * @param handler Exception handler
     * @return This stream instance
     */
    @Override
    public WriteStream<Buffer> exceptionHandler(@Nullable Handler<Throwable> handler) {
        this.exceptionHandler = handler != null ? handler : __ -> {
        };
        return this;
    }

    /**
     * Writes data asynchronously. If an OutputStream is wrapped, data is written directly to it.
     * Otherwise, data is buffered until an OutputStream is wrapped or the stream is closed.
     *
     * @param data Buffer to write (null indicates end of stream)
     * @return Future that completes when the data is processed
     */
    @Override
    public Future<Void> write(@Nullable Buffer data) {
        if (closed.get() && data != null) {
            return Future.succeededFuture(); // Discard data if closed, but allow end-of-stream marker
        }

        Promise<Void> promise = Promise.promise();

        OutputStream localWrappedOutputStream = this.wrappedOutputStream;
        if (localWrappedOutputStream != null) {
            // Write directly to wrapped OutputStream in blocking context
            context.executeBlocking(() -> {
                       try {
                           if (data == null) {
                               // End of stream - close the output stream
                               localWrappedOutputStream.close();
                           } else {
                               // Split large buffers if needed
                               for (int start = 0; start < data.length(); ) {
                                   int chunkSize = Math.min(maxBufferSize, data.length() - start);
                                   Buffer chunk = data.getBuffer(start, start + chunkSize);
                                   start += chunkSize;

                                   byte[] bytes = chunk.getBytes();
                                   localWrappedOutputStream.write(bytes);
                                   localWrappedOutputStream.flush();

                                   // Notify data handler
                                   final Buffer chunkToHandle = chunk;
                                   context.runOnContext(new Handler<Void>() {
                                       @Override
                                       public void handle(Void event) {
                                           dataHandler.handle(chunkToHandle);
                                       }
                                   });
                               }
                           }
                           return null;
                       } catch (Exception e) {
                           throw new RuntimeException("Failed to write data to OutputStream", e);
                       }
                   })
                   .onFailure(t -> {
                       exceptionHandler.handle(t);
                       promise.fail(t);
                   })
                   .onSuccess(__ -> {
                       // Complete the write promise
                       promise.complete();
                       // For end-of-stream, also complete the writeOver promise
                       if (data == null && writeOverPromise != null) {
                           writeOverPromise.complete();
                       }
                   });
        } else {
            // Buffer the data
            if (data == null) {
                // End of stream
                buffer.add(new PendingWrite(null, promise));
                closed.set(true);
                if (writeOverPromise != null) {
                    writeOverPromise.complete();
                }
            } else {
                // Split large buffers if needed
                for (int start = 0; start < data.length(); ) {
                    int chunkSize = Math.min(maxBufferSize, data.length() - start);
                    Buffer chunk = data.getBuffer(start, start + chunkSize);
                    start += chunkSize;

                    boolean isLastChunk = start >= data.length();
                    Promise<Void> chunkPromise = isLastChunk ? promise : Promise.promise();
                    buffer.add(new PendingWrite(chunk, chunkPromise));

                    // Notify data handler asynchronously
                    final Buffer chunkToHandle = chunk;
                    context.runOnContext(new Handler<Void>() {
                        @Override
                        public void handle(Void event) {
                            dataHandler.handle(chunkToHandle);
                        }
                    });
                }
            }
        }

        // Check if we should trigger drain handler
        checkDrain();

        return promise.future();
    }

    /**
     * Ends the stream, marking it as closed.
     *
     * @return Future that completes when the stream is fully closed
     */
    @Override
    public Future<Void> end() {
        closed.set(true);
        return write(null);
    }

    /**
     * Sets the maximum write queue size.
     *
     * @param maxSize Maximum queue size
     * @return This stream instance
     */
    @Override
    public WriteStream<Buffer> setWriteQueueMaxSize(int maxSize) {
        this.maxSize = maxSize;
        checkDrain();
        return this;
    }

    /**
     * Sets the maximum buffer chunk size.
     *
     * @param maxSize Maximum chunk size
     * @return This stream instance
     */
    public AsyncInputWriteStreamImpl setMaxChunkSize(int maxSize) {
        this.maxBufferSize = maxSize;
        return this;
    }

    /**
     * Checks if the write queue is full.
     *
     * @return true if the queue is full
     */
    @Override
    public boolean writeQueueFull() {
        return buffer.size() >= maxSize;
    }

    /**
     * Sets the drain handler to be called when the write queue is no longer full.
     *
     * @param handler Drain handler
     * @return This stream instance
     */
    @Override
    public WriteStream<Buffer> drainHandler(@Nullable Handler<Void> handler) {
        this.drainHandler = handler != null ? handler : __ -> {
        };
        return this;
    }

    /**
     * Sets a data handler to be called when new data is written to the stream.
     *
     * @param handler Data handler
     * @return This stream instance
     */
    public AsyncInputWriteStreamImpl dataHandler(@Nullable Handler<Buffer> handler) {
        this.dataHandler = handler != null ? handler : __ -> {
        };
        return this;
    }

    /**
     * Transfers all buffered data to the given OutputStream.
     * This method should be called from a blocking execution context.
     *
     * @param os OutputStream to write to
     * @throws IOException if an I/O error occurs
     */
    private void transferTo(OutputStream os) throws IOException {
        PendingWrite write;
        while ((write = buffer.poll()) != null) {
            if (write.data != null) {
                // Write buffer data
                byte[] bytes = write.data.getBytes();
                os.write(bytes);
                os.flush();
            }
            // Complete the write promise
            write.completion.tryComplete();
        }
    }

    /**
     * Checks if the drain handler should be triggered.
     */
    private void checkDrain() {
        if (!writeQueueFull()) {
            context.runOnContext(new Handler<Void>() {
                @Override
                public void handle(Void event) {
                    drainHandler.handle(event);
                }
            });
        }
    }

    /**
     * Represents a pending write operation.
     */
    @NullMarked
    private static class PendingWrite {
        final @Nullable Buffer data;
        final Promise<Void> completion;

        PendingWrite(@Nullable Buffer data, Promise<Void> completion) {
            this.data = data;
            this.completion = completion;
        }
    }
}
