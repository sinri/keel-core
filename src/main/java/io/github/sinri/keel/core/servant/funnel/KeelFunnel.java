package io.github.sinri.keel.core.servant.funnel;

import io.github.sinri.keel.core.verticles.KeelVerticleImplWithEventLogger;
import io.github.sinri.keel.logger.event.KeelEventLogger;
import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenter;
import io.vertx.core.Future;
import io.vertx.core.Promise;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

/**
 * @since 3.0.0
 */
public class KeelFunnel extends KeelVerticleImplWithEventLogger {
    /**
     * The interrupt, to stop sleeping when idle time ends (a new task comes).
     */
    private final AtomicReference<Promise<Void>> interruptRef;
    private final Queue<Supplier<Future<Void>>> queue;
    private final AtomicLong sleepTimeRef;

    public KeelFunnel() {
        this.sleepTimeRef = new AtomicLong(1_000L);
        this.queue = new ConcurrentLinkedQueue<>();
        this.interruptRef = new AtomicReference<>();
    }

    /**
     * @since 3.2.0
     */
    @Override
    protected KeelEventLogger buildEventLogger() {
        return KeelIssueRecordCenter.outputCenter().generateEventLogger("Funnel");
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
    protected void startAsKeelVerticle(Promise<Void> startPromise) {
        Keel.asyncCallRepeatedly(repeatedlyCallTask -> {
            this.interruptRef.set(null);

            return Keel.asyncCallRepeatedly(routineResult -> {
                        Supplier<Future<Void>> supplier = queue.poll();
                        if (supplier == null) {
                            // no job to do
                            routineResult.stop();
                            return Future.succeededFuture();
                        }

                        // got one job to do, no matter if done
                        return Future.succeededFuture()
                                .compose(v -> {
                                    return supplier.get();
                                })
                                .compose(v -> {
                                    //getLogger().debug("funnel done");
                                    return Future.succeededFuture();
                                }, throwable -> {
                                    getLogger().exception(throwable, r -> r.message("funnel task error"));
                                    return Future.succeededFuture();
                                });
                    })
                    .andThen(ar -> {
                        this.interruptRef.set(Promise.promise());

                        Keel.asyncSleep(this.sleepTimeRef.get(), getCurrentInterrupt())
                                .andThen(slept -> {
                                    repeatedlyCallTask.stop();
                                });
                    });
        });
        startPromise.complete();
    }
}
