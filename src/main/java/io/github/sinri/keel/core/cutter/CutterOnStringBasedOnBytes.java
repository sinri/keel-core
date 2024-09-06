package io.github.sinri.keel.core.cutter;

import io.github.sinri.keel.facade.async.KeelAsyncKit;
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

/**
 * @since 3.2.18
 */
public class CutterOnStringBasedOnBytes implements Cutter<String> {
    final Queue<Buffer> writeBuffer = new LinkedList<>();
    //final List<Byte> readBuffer = new LinkedList<>();
    private Buffer readBuffer = Buffer.buffer();
    private final ReentrantReadWriteLock lock;
    private Handler<String> componentHandler;
    private int retainRepeat = 5;
    private long retainTime = 100L;

    public CutterOnStringBasedOnBytes() {
        lock = new ReentrantReadWriteLock(true);
    }

    public CutterOnStringBasedOnBytes setRetainRepeat(int retainRepeat) {
        this.retainRepeat = retainRepeat;
        return this;
    }

    public CutterOnStringBasedOnBytes setRetainTime(long retainTime) {
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
            //e.printStackTrace();
        }
    }

    private void doWriteExclusively(Handler<Void> v) {
        lock.writeLock().lock();
        try {
            v.handle(null);
        } finally {
            lock.writeLock().unlock();
            //e.printStackTrace();
        }
    }

    @Override
    public Future<Void> end() {
        //Keel.getLogger().info("into io.github.sinri.keel.core.cutter.CutterOnStringBasedOnBytes.end");

        AtomicReference<String> chunkRef = new AtomicReference<>();
        AtomicInteger counter = new AtomicInteger(this.retainRepeat);
        return KeelAsyncKit.repeatedlyCall(routineResult -> {
                    return KeelAsyncKit.sleep(this.retainTime)
                            .compose(v -> {
                                return KeelAsyncKit.repeatedlyCall(routineResultForRead -> {
                                            doReadExclusively(vv -> {
                                                var s = cutWithDelimiter(false);
                                                chunkRef.set(s);
                                            });
                                            var s = chunkRef.get();
                                            if (s != null) {
                                                if (!s.isEmpty() && !s.isBlank()) {
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
                    if (s != null && !s.isEmpty() && !s.isBlank()) {
                        handleComponent(s);
                    }
                    //Keel.getLogger().info("finish io.github.sinri.keel.core.cutter.CutterOnStringBasedOnBytes.end");
                    return Future.succeededFuture();
                });
    }

    @Override
    public void handle(Buffer piece) {
        //Keel.getLogger().info("into io.github.sinri.keel.core.cutter.CutterOnStringBasedOnBytes.handle");

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
        if (s != null && !s.isEmpty() && !s.isBlank()) {
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
//                byte[] bytes = buffer.getBytes();
//                for (byte b : bytes) {
//                    readBuffer.add(b);
//                }
                readBuffer.appendBuffer(buffer);
            }
        }

        if (readBuffer.length() == 0) {
            return null;
        }

//        byte[] bytes = new byte[readBuffer.size()];
//        for (int i = 0; i < readBuffer.size(); i++) {
//            bytes[i] = readBuffer.get(i);
//        }
//        var s = new String(bytes, StandardCharsets.UTF_8);

        var s = readBuffer.toString(StandardCharsets.UTF_8);

        int place = s.indexOf(getDelimiter());
        if (place == -1) {
            if (asTail) return s;
            else return null;
        }

        String head = s.substring(0, place);

//        List<Byte> subList = readBuffer.subList(
//                head.getBytes(StandardCharsets.UTF_8).length
//                        + getDelimiter().getBytes(StandardCharsets.UTF_8).length,
//                readBuffer.size()
//        );
//        var tempList = new ArrayList<>(subList);
//        readBuffer.clear();
//        readBuffer.addAll(tempList);

        readBuffer = readBuffer.getBuffer(
                head.getBytes(StandardCharsets.UTF_8).length
                        + getDelimiter().getBytes(StandardCharsets.UTF_8).length,
                readBuffer.length()
        );

        return head;
    }
}
