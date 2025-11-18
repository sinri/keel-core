package io.github.sinri.keel.core.servant.funnel;

import io.github.sinri.keel.base.verticles.AbstractKeelVerticle;
import io.github.sinri.keel.logger.api.factory.LoggerFactory;
import io.github.sinri.keel.logger.api.logger.Logger;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import org.jetbrains.annotations.NotNull;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static io.github.sinri.keel.base.KeelInstance.Keel;


/**
 * 烟囱。
 * <p>
 * 接受各种异步任务逻辑，按照 FIFO 原则依次执行。
 *
 * @since 5.0.0
 */
public class KeelFunnel extends AbstractKeelVerticle {
    /**
     * 休眠中出现新任务时，使用此内寄存的 Promise 唤醒。
     */
    private final AtomicReference<Promise<Void>> interruptRef;
    private final Queue<Supplier<Future<Void>>> queue;
    private final AtomicLong sleepTimeRef;
    private final Logger funnelLogger;

    public KeelFunnel() {
        this.sleepTimeRef = new AtomicLong(1_000L);
        this.queue = new ConcurrentLinkedQueue<>();
        this.interruptRef = new AtomicReference<>();
        this.funnelLogger = buildFunnelLogger();
    }

    protected LoggerFactory getLoggerFactory() {
        return Keel.getLoggerFactory();
    }

    /**
     * @since 4.0.2
     */
    @NotNull
    protected Logger buildFunnelLogger() {
        return getLoggerFactory().createLogger("Funnel");
    }

    /**
     * @since 4.0.2
     */
    protected Logger getFunnelLogger() {
        return funnelLogger;
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

    private Promise<Void> getCurrentInterrupt() {
        return this.interruptRef.get();
    }


    @Override
    protected Future<Void> startVerticle() {
        Keel.asyncCallEndlessly(this::executeCircle);
        return Future.succeededFuture();
    }

    private Future<Void> executeCircle() {
        this.interruptRef.set(null);
        funnelLogger.debug("funnel one circle start");
        return Keel.asyncCallRepeatedly(routineResult -> {
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
                       funnelLogger.exception(throwable);
                       return Future.succeededFuture();
                   })
                   .eventually(() -> {
                       this.interruptRef.set(Promise.promise());
                       return Keel.asyncSleep(this.sleepTimeRef.get(), getCurrentInterrupt());
                   });
    }
}
