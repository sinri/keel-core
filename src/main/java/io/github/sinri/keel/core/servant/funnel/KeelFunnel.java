package io.github.sinri.keel.core.servant.funnel;

import io.github.sinri.keel.base.verticles.AbstractKeelVerticle;
import io.github.sinri.keel.logger.api.event.EventRecorder;
import io.github.sinri.keel.logger.api.factory.RecorderFactory;
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
 * Handle customized tasks in order.
 *
 * @since 3.0.0
 */
public class KeelFunnel extends AbstractKeelVerticle {
    /**
     * The interrupt, to stop sleeping when idle time ends (a new task comes).
     */
    private final AtomicReference<Promise<Void>> interruptRef;
    private final Queue<Supplier<Future<Void>>> queue;
    private final AtomicLong sleepTimeRef;
    private final EventRecorder funnelLogger;

    public KeelFunnel() {
        this.sleepTimeRef = new AtomicLong(1_000L);
        this.queue = new ConcurrentLinkedQueue<>();
        this.interruptRef = new AtomicReference<>();
        this.funnelLogger = buildFunnelLogger();
    }

    protected RecorderFactory getRecorderFactory() {
        return Keel.getRecorderFactory();
    }

    /**
     * @since 4.0.2
     */
    @NotNull
    protected EventRecorder buildFunnelLogger() {
        return getRecorderFactory().createEventRecorder("Funnel");
    }

    /**
     * @since 4.0.2
     */
    protected EventRecorder getFunnelLogger() {
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
