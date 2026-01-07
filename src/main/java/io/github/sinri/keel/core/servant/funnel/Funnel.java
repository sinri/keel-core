package io.github.sinri.keel.core.servant.funnel;

import io.github.sinri.keel.base.verticles.KeelVerticleBase;
import io.github.sinri.keel.logger.api.LateObject;
import io.github.sinri.keel.logger.api.factory.LoggerFactory;
import io.github.sinri.keel.logger.api.logger.Logger;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;


/**
 * 烟囱。
 * <p>
 * 接受各种异步任务逻辑，按照 FIFO 原则依次执行。
 *
 * @since 5.0.0
 */
@NullMarked
public class Funnel extends KeelVerticleBase {
    /**
     * 休眠中出现新任务时，使用此内寄存的 Promise 唤醒。
     */
    private final AtomicReference<@Nullable Promise<Void>> interruptRef;

    private final Queue<Supplier<Future<Void>>> queue;

    private final AtomicLong sleepTimeRef;

    private final LateObject<Logger> lateFunnelLogger = new LateObject<>();

    public Funnel() {
        super();
        this.sleepTimeRef = new AtomicLong(1_000L);
        this.queue = new ConcurrentLinkedQueue<>();
        this.interruptRef = new AtomicReference<>();
    }


    private Logger buildFunnelLogger() {
        return LoggerFactory.getShared().createLogger("Funnel");
    }


    protected final Logger getFunnelLogger() {
        return lateFunnelLogger.get();
    }

    public void setSleepTime(long sleepTime) {
        if (sleepTime <= 1) {
            throw new IllegalArgumentException();
        }
        this.sleepTimeRef.set(sleepTime);
    }

    public void add(Supplier<Future<Void>> supplier) {
        queue.add(supplier);
        Promise<Void> currentInterrupt = getCurrentInterrupt();
        if (currentInterrupt != null) {
            currentInterrupt.tryComplete();
        }
    }

    @Nullable
    private Promise<Void> getCurrentInterrupt() {
        return this.interruptRef.get();
    }


    @Override
    protected Future<Void> startVerticle() {
        lateFunnelLogger.set(buildFunnelLogger());
        asyncCallEndlessly(this::executeCircle);
        return Future.succeededFuture();
    }


    private Future<Void> executeCircle() {
        this.interruptRef.set(null);
        getFunnelLogger().debug("funnel one circle start");
        return asyncCallRepeatedly(routineResult -> {
            // got one job to do, no matter if done
            return Future.succeededFuture()
                         .compose(ready -> {
                             Supplier<Future<Void>> supplier = queue.poll();
                             if (supplier == null) {
                                 // no job to do
                                 routineResult.stop();
                                 Supplier<Future<Void>> supplierTemp = Future::succeededFuture;
                                 return Future.succeededFuture(supplierTemp);
                             } else {
                                 return Future.succeededFuture(supplier);
                             }
                         })
                         .compose(Supplier::get);
        })
                .recover(throwable -> {
                    getFunnelLogger().error(log -> log.exception(throwable));
                    return Future.succeededFuture();
                })
                .eventually(() -> {
                    this.interruptRef.set(Promise.promise());
                    return asyncSleep(this.sleepTimeRef.get(), getCurrentInterrupt());
                });
    }
}
