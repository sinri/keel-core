package io.github.sinri.keel.core.servant.intravenous;

import io.github.sinri.keel.core.verticles.KeelVerticleImplWithEventLogger;
import io.github.sinri.keel.logger.event.KeelEventLogger;
import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenter;
import io.vertx.core.Future;
import io.vertx.core.Promise;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

/**
 * @param <T>
 * @since 3.0.1 redesigned from the original KeelIntravenous
 */
abstract public class KeelIntravenousBase<T> extends KeelVerticleImplWithEventLogger {
    private final Queue<T> queue;
    private final AtomicReference<Promise<Void>> interruptRef;
    protected long sleepTime = 1_000L;
    protected int batchSize = 1;
    private boolean queueAcceptTask = false;

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
    protected void startAsKeelVerticle(Promise<Void> startPromise) {
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

        startPromise.complete();
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
     * @since 3.2.0
     */
    @Override
    protected KeelEventLogger buildEventLogger() {
        return KeelIssueRecordCenter.silentCenter().generateEventLogger(getClass().getName());
    }

}
