package io.github.sinri.keel.core.cutter;

import io.github.sinri.keel.base.verticles.KeelVerticleBase;
import io.github.sinri.keel.core.servant.intravenous.Intravenous;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.ThreadingModel;
import io.vertx.core.buffer.Buffer;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;


/**
 * 数据流切分处理器。
 * <p>
 * 基本使用：<br>
 * 1. 调用{@link IntravenouslyCutter#acceptFromStream(Buffer)}方法从流接收数据。<br>
 * 2. 当流结束时，调用{@link IntravenouslyCutter#stopHere()}方法或{@link IntravenouslyCutter#stopHere(Throwable)}通知切分器停止接收数据；<br>
 * 3. 调用{@link IntravenouslyCutter#waitForAllHandled()}等待流切片处理结束。
 *
 * @param <T> 组成可处理的流的实体类型
 * @since 5.0.0
 */
@NullMarked
public abstract class IntravenouslyCutter<T> extends KeelVerticleBase {

    private final AtomicReference<Buffer> bufferRef;
    private final Intravenous<T> intravenous;
    private final AtomicBoolean readStopRef = new AtomicBoolean(false);
    private final AtomicReference<@Nullable Throwable> stopCause = new AtomicReference<>();
    //    private final Keel keel;
    //    private final Vertx vertx;
    private final long timeout;
    private @Nullable Long timeoutTimer;

    public IntravenouslyCutter(
            Intravenous.SingleDropProcessor<T> singleDropProcessor,
            long timeout
    ) {
        this.bufferRef = new AtomicReference<>(Buffer.buffer());
        this.timeout = timeout;
        this.intravenous = Intravenous.instant(singleDropProcessor);
    }

    @Override
    protected Future<?> startVerticle() {
        return this.intravenous.deployMe(
                           getVertx(),
                           new DeploymentOptions().setThreadingModel(ThreadingModel.WORKER)
                   )
                               .andThen(ar -> {
                                   if (timeout > 0) {
                                       timeoutTimer = vertx.setTimer(timeout, timer -> {
                                           this.timeoutTimer = timer;
                                           this.stopHere(new CutterTimeout());
                                       });
                                   }
                               });
    }

    public final void acceptFromStream(Buffer incomingBuffer) {
        synchronized (this.bufferRef) {
            this.bufferRef.get().appendBuffer(incomingBuffer);

            List<T> list;
            synchronized (bufferRef) {
                list = cut();
            }
            for (var t : list) {
                intravenous.add(t);
            }
        }
    }

    public final void stopHere() {
        this.stopHere(null);
    }

    public final void stopHere(@Nullable Throwable throwable) {
        if (!readStopRef.get()) {
            List<T> list;
            synchronized (bufferRef) {
                list = cut();
            }
            for (var t : list) {
                intravenous.add(t);
            }

            if (timeoutTimer != null) {
                long x = timeoutTimer;
                getVertx().cancelTimer(x);
                timeoutTimer = null;
            }
            stopCause.set(throwable);
            readStopRef.set(true);
        }
    }

    public final Future<Void> waitForAllHandled() {
        return asyncCallRepeatedly(repeatedlyCallTask -> {
            if (!this.readStopRef.get()) {
                return asyncSleep(200L);
            }
            if (!intravenous.isNoDropsLeft()) {
                return asyncSleep(100L);
            }
            intravenous.shutdown();
            if (!intravenous.isUndeployed()) {
                return asyncSleep(100L);
            }
            repeatedlyCallTask.stop();
            return Future.succeededFuture();
        })
                .compose(v -> {
                    Throwable throwable = stopCause.get();
                    if (throwable != null) {
                        return Future.failedFuture(throwable);
                    }
                    return Future.succeededFuture();
                });
    }

    protected final AtomicReference<Buffer> getBufferRef() {
        return bufferRef;
    }

    /**
     * 自{@link IntravenouslyCutter#getBufferRef()}读取到 buffer 后，从头开始解析出尽可能多的目标切片实体，然后更新 buffer 为余下部分。
     * <p>
     * 本方法已被限定在同步块内调用以保证线程安全。
     *
     * @return 目标切片实体列表
     */
    abstract protected List<T> cut();
}
