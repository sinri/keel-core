package io.github.sinri.keel.facade.async;

import io.github.sinri.keel.core.verticles.KeelVerticle;
import io.github.sinri.keel.core.verticles.KeelVerticleImplPure;
import io.vertx.core.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

/**
 * @since 3.3.0
 */
public interface KeelAsyncMixin {

    private Future<Void> asyncCallRepeatedly(@Nonnull RepeatedlyCallTask repeatedlyCallTask) {
        Promise<Void> promise = Promise.promise();
        RepeatedlyCallTask.start(repeatedlyCallTask, promise);
        return promise.future();
    }

    default Future<Void> asyncCallRepeatedly(@Nonnull Function<RepeatedlyCallTask, Future<Void>> processor) {
        return asyncCallRepeatedly(new RepeatedlyCallTask(processor));
    }

    default <T> Future<Void> asyncCallIteratively(
            @Nonnull Iterator<T> iterator,
            @Nonnull BiFunction<List<T>, RepeatedlyCallTask, Future<Void>> itemsProcessor,
            int batchSize
    ) {
        if (batchSize <= 0) throw new IllegalArgumentException("batchSize must be greater than 0");

        return asyncCallRepeatedly(repeatedlyCallTask -> {
            List<T> buffer = new ArrayList<>();

            while (buffer.size() < batchSize) {
                if (iterator.hasNext()) {
                    buffer.add(iterator.next());
                } else {
                    break;
                }
            }

            if (buffer.isEmpty()) {
                repeatedlyCallTask.stop();
                return Future.succeededFuture();
            }

            return itemsProcessor.apply(buffer, repeatedlyCallTask);
        });
    }

    default <T> Future<Void> asyncCallIteratively(
            @Nonnull Iterator<T> iterator,
            @Nonnull Function<List<T>, Future<Void>> itemsProcessor,
            int batchSize
    ) {
        return asyncCallIteratively(
                iterator,
                (ts, repeatedlyCallTask) -> itemsProcessor.apply(ts),
                batchSize
        );
    }

    default <T> Future<Void> asyncCallIteratively(
            @Nonnull Iterable<T> iterable,
            @Nonnull BiFunction<List<T>, RepeatedlyCallTask, Future<Void>> itemsProcessor,
            int batchSize
    ) {
        return asyncCallIteratively(iterable.iterator(), itemsProcessor, batchSize);
    }

    default <T> Future<Void> asyncCallIteratively(
            @Nonnull Iterator<T> iterator,
            @Nonnull BiFunction<T, RepeatedlyCallTask, Future<Void>> itemProcessor
    ) {
        return asyncCallRepeatedly(routineResult -> {
            return Future.succeededFuture()
                    .compose(v -> {
                        if (iterator.hasNext()) {
                            return itemProcessor.apply(iterator.next(), routineResult);
                        } else {
                            routineResult.stop();
                            return Future.succeededFuture();
                        }
                    });
        });
    }

    default <T> Future<Void> asyncCallIteratively(
            @Nonnull Iterator<T> iterator,
            @Nonnull Function<T, Future<Void>> itemProcessor
    ) {
        return asyncCallIteratively(
                iterator,
                (t, repeatedlyCallTask) -> itemProcessor.apply(t)
        );
    }

    default <T> Future<Void> asyncCallIteratively(
            @Nonnull Iterable<T> iterator,
            @Nonnull Function<T, Future<Void>> itemProcessor
    ) {
        return asyncCallIteratively(iterator.iterator(), itemProcessor);
    }

    default <T> Future<Void> asyncCallIteratively(
            @Nonnull Iterable<T> iterable,
            @Nonnull BiFunction<T, RepeatedlyCallTask, Future<Void>> itemProcessor
    ) {
        return asyncCallIteratively(iterable.iterator(), itemProcessor);
    }

    default Future<Void> asyncCallStepwise(long start, long end, long step, BiFunction<Long, RepeatedlyCallTask, Future<Void>> processor) {
        if (step == 0) throw new IllegalArgumentException("step must not be 0");
        AtomicLong ptr = new AtomicLong(start);
        return asyncCallRepeatedly(task -> {
            return Future.succeededFuture()
                    .compose(vv -> {
                        return processor.apply(ptr.get(), task)
                                .compose(v -> {
                                    long y = ptr.addAndGet(step);
                                    if (y >= end) {
                                        task.stop();
                                    }
                                    return Future.succeededFuture();
                                });
                    });
        });
    }

    default Future<Void> asyncCallStepwise(long times, BiFunction<Long, RepeatedlyCallTask, Future<Void>> processor) {
        return asyncCallStepwise(0, times, 1, processor);
    }

    default Future<Void> asyncCallStepwise(long times, Function<Long, Future<Void>> processor) {
        return asyncCallStepwise(0, times, 1, (aLong, repeatedlyCallTask) -> processor.apply(aLong));
    }

    /**
     * @param supplier An async job handler, results a Void Future.
     * @since 3.0.1
     */
    default void asyncCallEndlessly(@Nonnull Supplier<Future<Void>> supplier) {
        asyncCallRepeatedly(routineResult -> {
            return Future.succeededFuture()
                    .compose(v -> supplier.get())
                    .eventually(() -> Future.succeededFuture());
        });
    }

    default Future<Void> asyncSleep(long time) {
        return asyncSleep(time, null);
    }

    default Future<Void> asyncSleep(long time, @Nullable Promise<Void> interrupter) {
        Promise<Void> promise = Promise.promise();
        if (time < 1) time = 1;
        long timer_id = Keel.getVertx().setTimer(time, timerID -> {
            promise.complete();
        });
        if (interrupter != null) {
            interrupter.future().onSuccess(interrupted -> {
                Keel.getVertx().cancelTimer(timer_id);
                promise.tryComplete();
            });
        }
        return promise.future();
    }

    default <T> Future<T> asyncCallExclusively(@Nonnull String lockName, long waitTimeForLock, @Nonnull Supplier<Future<T>> exclusiveSupplier) {
        return Keel.getVertx().sharedData()
                .getLockWithTimeout(lockName, waitTimeForLock)
                .compose(lock -> Future.succeededFuture()
                        .compose(v -> exclusiveSupplier.get())
                        .andThen(ar -> lock.release())
                );
    }

    default <T> Future<T> asyncCallExclusively(@Nonnull String lockName, @Nonnull Supplier<Future<T>> exclusiveSupplier) {
        return asyncCallExclusively(lockName, 1_000L, exclusiveSupplier);
    }

    default <R> Future<R> asyncTransfromCompletableFuture(@Nonnull CompletableFuture<R> completableFuture) {
        Promise<R> promise = Promise.promise();
        completableFuture.whenComplete((r, t) -> {
            if (t != null) {
                promise.fail(t);
            } else {
                promise.complete(r);
            }
        });
        return promise.future();
    }

    default <R> Future<R> asyncTransformRawFuture(@Nonnull java.util.concurrent.Future<R> rawFuture, long sleepTime) {
        Promise<R> promise = Promise.promise();
        return asyncCallRepeatedly(repeatedlyCallTask -> {
            if (rawFuture.isDone() || rawFuture.isCancelled()) {
                repeatedlyCallTask.stop();
                return Future.succeededFuture();
            } else {
                return asyncSleep(sleepTime);
            }
        })
                .andThen(ar -> {
                    if (ar.succeeded()) {
                        if (rawFuture.isDone()) {
                            try {
                                R r = rawFuture.get();
                                promise.complete(r);
                            } catch (InterruptedException | ExecutionException e) {
                                promise.fail(e);
                            }
                        } else {
                            promise.fail(new Exception("io.github.sinri.keel.core.async.KeelAsyncMixin.vertxizedRawFuture failed with rawFuture is not done."));
                        }
                    } else {
                        promise.fail(ar.cause());
                    }
                })
                .recover(throwable -> {
                    return Future.succeededFuture();
                })
                .compose(v -> {
                    return promise.future();
                });
    }

    default <T> Future<T> executeBlocking(@Nonnull Handler<Promise<T>> blockingCodeHandler) {
        Promise<T> promise = Promise.promise();
        KeelVerticle verticle = new KeelVerticleImplPure() {

            @Override
            protected void startAsPureKeelVerticle() {
                blockingCodeHandler.handle(promise);
                promise.future().onComplete(ar -> this.undeployMe());
            }
        };
        return verticle.deployMe(new DeploymentOptions().setThreadingModel(ThreadingModel.WORKER))
                .compose(deploymentId -> promise.future());
    }

    private <T> CompletableFuture<T> createPseudoAwaitCompletableFuture(Handler<Promise<T>> blockingCodeHandler) {
        CompletableFuture<T> completableFuture = new CompletableFuture<>();
        executeBlocking(blockingCodeHandler)
                .andThen(ar -> {
                    if (ar.succeeded()) {
                        completableFuture.complete(ar.result());
                    } else {
                        completableFuture.completeExceptionally(ar.cause());
                    }
                });
        return completableFuture;
    }

    default <T> T pseudoAwait(Handler<Promise<T>> blockingCodeHandler) {
        CompletableFuture<T> completableFuture = createPseudoAwaitCompletableFuture(blockingCodeHandler);
        try {
            return completableFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    final class RepeatedlyCallTask {
        @Nonnull
        private final Function<RepeatedlyCallTask, Future<Void>> processor;
        private boolean toStop = false;

        public RepeatedlyCallTask(@Nonnull Function<RepeatedlyCallTask, Future<Void>> processor) {
            this.processor = processor;
        }

        public static void start(@Nonnull RepeatedlyCallTask thisTask, @Nonnull Promise<Void> finalPromise) {
            Future.succeededFuture()
                    .compose(v -> {
                        if (thisTask.toStop) {
                            return Future.succeededFuture();
                        }
                        return thisTask.processor.apply(thisTask);
                    })
                    .andThen(shouldStopAR -> {
                        if (shouldStopAR.succeeded()) {
                            if (thisTask.toStop) {
                                finalPromise.complete();
                            } else {
                                Keel.getVertx().setTimer(1L, x -> start(thisTask, finalPromise));
                            }
                        } else {
                            finalPromise.fail(shouldStopAR.cause());
                        }
                    });
        }

        public void stop() {
            toStop = true;
        }
    }
}
