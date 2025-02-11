package io.github.sinri.keel.core.servant.intravenous;

import io.github.sinri.keel.core.verticles.KeelVerticleImpl;
import io.github.sinri.keel.logger.event.KeelEventLog;
import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenter;
import io.github.sinri.keel.logger.issue.recorder.KeelIssueRecorder;
import io.vertx.core.Future;
import io.vertx.core.Promise;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

/**
 * @since 3.0.1 redesigned from the original KeelIntravenous
 */
abstract public class KeelIntravenousBase<T> extends KeelVerticleImpl {
    private final Queue<T> queue;
    private final AtomicReference<Promise<Void>> interruptRef;
    protected long sleepTime = 1_000L;
    protected int batchSize = 1;
    private boolean queueAcceptTask = false;
    private KeelIssueRecorder<KeelEventLog> intravenousLogger;

    public KeelIntravenousBase() {
        this.queue = new ConcurrentLinkedQueue<>();
        this.interruptRef = new AtomicReference<>();
    }

    abstract protected Future<Void> process(List<T> list);

    public void add(T t) {
        if (!queueAcceptTask) {
            throw new IllegalStateException("shutdown declared");
        }

        queue.add(t);
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
        this.intravenousLogger = buildIntravenousIssueRecorder();

        queueAcceptTask = true;

        int configuredBatchSize = getBatchSize();

        Keel.asyncCallRepeatedly(repeatedlyCallTask -> {
            this.interruptRef.set(null);

            return Keel.asyncCallRepeatedly(routineResult -> {
                           List<T> buffer = new ArrayList<>();
                           while (true) {
                               T t = queue.poll();
                               if (t != null) {
                                   buffer.add(t);
                                   if (buffer.size() >= configuredBatchSize) {
                                       break;
                                   }
                               } else {
                                   break;
                               }
                           }
                           if (buffer.isEmpty()) {
                               routineResult.stop();
                               return Future.succeededFuture();
                           }

                           // got one job to do, no matter if done
                           return Future.succeededFuture()
                                        .compose(v -> {
                                            return this.process(buffer);
                                        })
                                        .compose(v -> {
                                            return Future.succeededFuture();
                                        }, throwable -> {
                                            return Future.succeededFuture();
                                        });
                       })
                       .andThen(ar -> {
                           this.interruptRef.set(Promise.promise());

                           Keel.asyncSleep(sleptTime(), getCurrentInterrupt())
                               .andThen(slept -> {
                                   repeatedlyCallTask.stop();
                               });
                       });
        });

        return Future.succeededFuture();
    }

    protected int getBatchSize() {
        return batchSize;
    }

    protected long sleptTime() {
        return sleepTime;
    }

    /**
     * @since 3.0.12
     */
    public void declareShutdown() {
        // declare shutdown, to avoid new tasks coming.
        this.queueAcceptTask = false;
    }

    /**
     * @return Async result is done after this intravenous instance undeploy.
     * @since 3.0.12
     */
    public Future<Void> shutdown() {
        declareShutdown();
        // waiting for the queue clear
        return Keel.asyncCallRepeatedly(routineResult -> {
                       if (this.queue.isEmpty()) {
                           routineResult.stop();
                           return Future.succeededFuture();
                       } else {
                           return Keel.asyncSleep(100L);
                       }
                   })
                   .compose(allTasksInQueueIsConsumed -> {
                       return this.undeployMe();
                   });
    }

    /**
     * @since 4.0.2
     */
    @Nonnull
    protected KeelIssueRecorder<KeelEventLog> buildIntravenousIssueRecorder() {
        return KeelIssueRecordCenter.silentCenter().generateEventLogger(getClass().getName());
    }

    /**
     * @since 4.0.2
     */
    public KeelIssueRecorder<KeelEventLog> getIntravenousLogger() {
        return intravenousLogger;
    }
}
