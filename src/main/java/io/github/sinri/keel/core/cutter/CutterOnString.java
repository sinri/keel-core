package io.github.sinri.keel.core.cutter;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;

import javax.annotation.Nullable;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

/**
 * @since 3.2.18 greatly changed.
 */
public class CutterOnString implements Cutter<String> {
    final Queue<Buffer> writeBuffer = new LinkedList<>();
    private final ReentrantReadWriteLock lock;
    private Buffer readBuffer = Buffer.buffer();
    private Handler<String> componentHandler;
    private int retainRepeat = 5;
    private long retainTime = 100L;

    public CutterOnString() {
        lock = new ReentrantReadWriteLock(true);
    }

    @Override
    public Cutter<String> setRetainRepeat(int retainRepeat) {
        this.retainRepeat = retainRepeat;
        return this;
    }

    @Override
    public Cutter<String> setRetainTime(long retainTime) {
        this.retainTime = retainTime;
        return this;
    }

    @Override
    public Cutter<String> setComponentHandler(Handler<String> componentHandler) {
        this.componentHandler = componentHandler;
        return this;
    }

    private void handleComponent(String s) {
        //Keel.getLogger().info("CutterOnStringBasedOnBytes.handleComponent: \n$$$" + s + "$$$\n");
        componentHandler.handle(s);
    }

    private void doReadExclusively(Handler<Void> v) {
        lock.readLock().lock();
        try {
            v.handle(null);
        } finally {
            lock.readLock().unlock();
        }
    }

    private void doWriteExclusively(Handler<Void> v) {
        lock.writeLock().lock();
        try {
            v.handle(null);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public Future<Void> end() {
        AtomicReference<String> chunkRef = new AtomicReference<>();
        AtomicInteger counter = new AtomicInteger(this.retainRepeat);
        return Keel.asyncCallRepeatedly(routineResult -> {
                    return Keel.asyncSleep(this.retainTime)
                            .compose(v -> {
                                return Keel.asyncCallRepeatedly(routineResultForRead -> {
                                            doReadExclusively(vv -> {
                                                var s = cutWithDelimiter(false);
                                                chunkRef.set(s);
                                            });
                                            var s = chunkRef.get();
                                            if (s != null) {
                                                if (!s.isBlank()) {
                                                    handleComponent(s);
                                                }
                                            } else {
                                                routineResultForRead.stop();
                                            }
                                            return Future.succeededFuture();
                                        })
                                        .compose(vv -> {
                                            if (writeBuffer.isEmpty()) {
                                                counter.decrementAndGet();
                                                if (counter.get() <= 0) {
                                                    routineResult.stop();
                                                }
                                            }
                                            return Future.succeededFuture();
                                        });
                            });
                })
                .compose(v -> {
                    doReadExclusively(vv -> {
                        var s = cutWithDelimiter(true);
                        chunkRef.set(s);
                    });
                    var s = chunkRef.get();
                    if (s != null && !s.isBlank()) {
                        handleComponent(s);
                    }
                    //Keel.getLogger().info("finish io.github.sinri.keel.core.cutter.CutterOnStringBasedOnBytes.end");
                    return Future.succeededFuture();
                });
    }

    @Override
    public void handle(Buffer piece) {
        // Keel.getLogger().warning("into io.github.sinri.keel.core.cutter.CutterOnStringBasedOnBytes.handle "+piece.toString());

        AtomicBoolean containsDelimiter = new AtomicBoolean(false);
        doWriteExclusively(v -> {
            writeBuffer.add(piece);

            String string = piece.toString(StandardCharsets.UTF_8);
            boolean contains = string.contains(getDelimiter());
            containsDelimiter.set(contains);
        });

        AtomicReference<String> chunkRef = new AtomicReference<>();
        doReadExclusively(v -> {
            if (containsDelimiter.get()) {
                String chunk = cutWithDelimiter(false);
                chunkRef.set(chunk);
            }
        });

        var s = chunkRef.get();
        if (s != null && !s.isBlank()) {
            handleComponent(s);
        }

        //Keel.getLogger().info("stop io.github.sinri.keel.core.cutter.CutterOnStringBasedOnBytes.handle");
    }

    public String getDelimiter() {
        return "\n\n";
    }

    /**
     * This method is run in synchronized(buffer) block.
     */
    @Nullable
    private String cutWithDelimiter(boolean asTail) {
        if (!writeBuffer.isEmpty()) {
            while (true) {
                Buffer buffer = writeBuffer.poll();
                if (buffer == null) break;
                readBuffer.appendBuffer(buffer);
            }
        }

        if (readBuffer.length() == 0) {
            return null;
        }

        var s = readBuffer.toString(StandardCharsets.UTF_8);

        int place = s.indexOf(getDelimiter());
        if (place == -1) {
            if (asTail) return s;
            else return null;
        }

        String head = s.substring(0, place);

        readBuffer = readBuffer.getBuffer(
                head.getBytes(StandardCharsets.UTF_8).length
                        + getDelimiter().getBytes(StandardCharsets.UTF_8).length,
                readBuffer.length()
        );

        return head;
    }
}
