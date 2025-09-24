package io.github.sinri.keel.core.servant.funnel;

import io.github.sinri.keel.core.verticles.KeelVerticleImpl;
import io.github.sinri.keel.logger.event.KeelEventLog;
import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenter;
import io.github.sinri.keel.logger.issue.recorder.KeelIssueRecorder;
import io.vertx.core.Future;
import io.vertx.core.Promise;

import javax.annotation.Nonnull;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

/**
 * Handle customized tasks in order.
 *
 * @since 3.0.0
 */
public class KeelFunnel extends KeelVerticleImpl {
    /**
     * The interrupt, to stop sleeping when idle time ends (a new task comes).
     */
    private final AtomicReference<Promise<Void>> interruptRef;
    private final Queue<Supplier<Future<Void>>> queue;
    private final AtomicLong sleepTimeRef;
    private final KeelIssueRecorder<KeelEventLog> funnelLogger;

    public KeelFunnel() {
        this.sleepTimeRef = new AtomicLong(1_000L);
        this.queue = new ConcurrentLinkedQueue<>();
        this.interruptRef = new AtomicReference<>();
        this.funnelLogger = buildFunnelLogger();
    }

    /**
     * @since 4.0.2
     */
    @Nonnull
    protected KeelIssueRecorder<KeelEventLog> buildFunnelLogger() {
        return KeelIssueRecordCenter.outputCenter().generateIssueRecorder("Funnel", KeelEventLog::new);
    }

    /**
     * @since 4.0.2
     */
    protected KeelIssueRecorder<KeelEventLog> getFunnelLogger() {
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
