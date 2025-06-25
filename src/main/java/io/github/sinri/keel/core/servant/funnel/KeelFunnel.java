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
    private KeelIssueRecorder<KeelEventLog> funnelLogger;

    public KeelFunnel() {
        this.sleepTimeRef = new AtomicLong(1_000L);
        this.queue = new ConcurrentLinkedQueue<>();
        this.interruptRef = new AtomicReference<>();
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
        this.funnelLogger = buildFunnelLogger();
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
                                        .compose(v -> supplier.get())
                                        .compose(v -> {
                                            //getLogger().debug("funnel done");
                                            return Future.succeededFuture();
                                        }, throwable -> {
                                            getFunnelLogger().exception(throwable, r -> r.message("funnel task " +
                                                    "error"));
                                            return Future.succeededFuture();
                                        });
                       })
                       .andThen(ar -> {
                           this.interruptRef.set(Promise.promise());

                           Keel.asyncSleep(this.sleepTimeRef.get(), getCurrentInterrupt())
                               .andThen(slept -> repeatedlyCallTask.stop());
                       });
        });

        return Future.succeededFuture();
    }
}
