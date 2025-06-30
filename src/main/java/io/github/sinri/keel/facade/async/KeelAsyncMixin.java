package io.github.sinri.keel.facade.async;

import io.github.sinri.keel.core.verticles.KeelVerticle;
import io.vertx.core.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

/**
 * KeelAsyncMixin is a mixin interface that provides utility methods for
 * performing asynchronous operations in a more
 * structured and convenient manner. It includes methods for processing
 * collections and iterators, handling repeated
 * and iterative tasks, and managing exclusive access to resources.
 *
 * @since 4.0.0
 */
public interface KeelAsyncMixin {
    /**
     * Executes a given function in parallel for all items in the provided
     * collection, returning a future that completes
     * when all individual futures produced by the function have successfully
     * completed.
     *
     * @param <T>           the type of elements in the collection
     * @param collection    the iterable collection of items to process
     * @param itemProcessor the function to apply to each item, which returns a
     *                      future
     * @return a Future that completes with Void when all the futures returned by
     *         the itemProcessor have succeeded
     * @since 4.0.2
     */
    default <T> Future<Void> parallelForAllSuccess(@Nonnull Iterable<T> collection,
            @Nonnull Function<T, Future<Void>> itemProcessor) {
        return parallelForAllSuccess(collection.iterator(), itemProcessor);
    }

    /**
     * Executes a given function in parallel for all items in the provided iterator,
     * returning a future that completes
     * when all individual futures produced by the function have successfully
     * completed.
     *
     * @param <T>           the type of elements in the iterator
     * @param iterator      the iterator of items to process
     * @param itemProcessor the function to apply to each item, which returns a
     *                      future
     * @return a Future that completes with Void when all the futures returned by
     *         the itemProcessor have succeeded
     * @since 4.0.2
     */
    default <T> Future<Void> parallelForAllSuccess(@Nonnull Iterator<T> iterator,
            @Nonnull Function<T, Future<Void>> itemProcessor) {
        List<Future<Void>> futures = new ArrayList<>();
        while (iterator.hasNext()) {
            Future<Void> f = itemProcessor.apply(iterator.next());
            futures.add(f);
        }
        if (futures.isEmpty()) {
            return Future.succeededFuture();
        }
        return Future.all(futures)
                .mapEmpty();
    }

    /**
     * Executes a given function in parallel for all items in the provided
     * collection, returning a future that completes
     * when any of the individual futures produced by the function has successfully
     * completed.
     *
     * @param <T>           the type of elements in the collection
     * @param collection    the iterable collection of items to process
     * @param itemProcessor the function to apply to each item, which returns a
     *                      future
     * @return a Future that completes with Void when any of the futures returned by
     *         the itemProcessor have succeeded
     * @since 4.0.2
     */
    default <T> Future<Void> parallelForAnySuccess(@Nonnull Iterable<T> collection,
            @Nonnull Function<T, Future<Void>> itemProcessor) {
        return parallelForAnySuccess(collection.iterator(), itemProcessor);
    }

    /**
     * Executes a given function in parallel for all items in the provided iterator,
     * returning a future that completes
     * when any of the individual futures produced by the function has successfully
     * completed.
     *
     * @param <T>           the type of elements in the iterator
     * @param iterator      the iterator of items to process
     * @param itemProcessor the function to apply to each item, which returns a
     *                      future
     * @return a Future that completes with Void when any of the futures returned by
     *         the itemProcessor have succeeded
     * @since 4.0.2
     */
    default <T> Future<Void> parallelForAnySuccess(@Nonnull Iterator<T> iterator,
            @Nonnull Function<T, Future<Void>> itemProcessor) {
        List<Future<Void>> futures = new ArrayList<>();
        while (iterator.hasNext()) {
            Future<Void> f = itemProcessor.apply(iterator.next());
            futures.add(f);
        }
        if (futures.isEmpty()) {
            return Future.succeededFuture();
        }
        return Future.any(futures)
                .mapEmpty();
    }

    /**
     * Executes a given function in parallel for all items in the provided
     * collection, returning a future that completes
     * when all individual futures produced by the function have completed,
     * regardless of success or failure.
     *
     * @param <T>           the type of elements in the collection
     * @param collection    the iterable collection of items to process
     * @param itemProcessor the function to apply to each item, which returns a
     *                      future
     * @return a Future that completes with Void when all the futures returned by
     *         the itemProcessor have completed
     * @since 4.0.2
     */
    default <T> Future<Void> parallelForAllComplete(@Nonnull Iterable<T> collection,
            @Nonnull Function<T, Future<Void>> itemProcessor) {
        return parallelForAllComplete(collection.iterator(), itemProcessor);
    }

    /**
     * Executes a given function in parallel for all items in the provided iterator,
     * returning a future that completes
     * when all individual futures produced by the function have completed,
     * regardless of success or failure.
     *
     * @param <T>           the type of elements in the iterator
     * @param iterator      the iterator of items to process
     * @param itemProcessor the function to apply to each item, which returns a
     *                      future
     * @return a Future that completes with Void when all the futures returned by
     *         the itemProcessor have completed
     * @since 4.0.2
     */
    default <T> Future<Void> parallelForAllComplete(@Nonnull Iterator<T> iterator,
            @Nonnull Function<T, Future<Void>> itemProcessor) {
        List<Future<Void>> futures = new ArrayList<>();
        while (iterator.hasNext()) {
            Future<Void> f = itemProcessor.apply(iterator.next());
            futures.add(f);
        }
        if (futures.isEmpty()) {
            return Future.succeededFuture();
        }
        return Future.join(futures)
                .mapEmpty();
    }

    /**
     * Initiates a task that will be called repeatedly until the task itself signals
     * to stop.
     *
     * @param repeatedlyCallTask the task to be executed repeatedly, which includes
     *                           a processor function and a stopping
     *                           condition
     * @return a Future that completes with Void when the repeatedly called task has
     *         finished its execution
     */
    private Future<Void> asyncCallRepeatedly(@Nonnull RepeatedlyCallTask repeatedlyCallTask) {
        Promise<Void> promise = Promise.promise();
        RepeatedlyCallTask.start(repeatedlyCallTask, promise);
        return promise.future();
    }

    /**
     * Initiates a task that will be called repeatedly until the task itself signals
     * to stop.
     *
     * @param processor the function that defines the task to be executed and
     *                  returns a future. The function is provided
     *                  with
     *                  a {@link RepeatedlyCallTask} instance, which can be used to
     *                  signal the task to stop by calling
     *                  its
     *                  {@code stop()} method.
     * @return a Future that completes with Void when the repeatedly called task has
     *         finished its execution.
     */
    default Future<Void> asyncCallRepeatedly(@Nonnull Function<RepeatedlyCallTask, Future<Void>> processor) {
        return asyncCallRepeatedly(new RepeatedlyCallTask(processor));
    }

    /**
     * Processes items from the given iterator in batches, invoking a provided
     * processor function for each batch.
     * The method continues to process items until the iterator is exhausted. The
     * size of each batch is determined
     * by the batchSize parameter. If the iterator has fewer items than the
     * specified batch size, the remaining items
     * are processed as the final batch.
     *
     * @param <T>            the type of elements in the iterator
     * @param iterator       the iterator of items to process
     * @param itemsProcessor the function to apply to each batch of items, which
     *                       returns a future
     * @param batchSize      the number of items to process in each batch
     * @return a Future that completes with Void when all the futures returned by
     *         the itemsProcessor have completed
     */
    default <T> Future<Void> asyncCallIteratively(
            @Nonnull Iterator<T> iterator,
            @Nonnull BiFunction<List<T>, RepeatedlyCallTask, Future<Void>> itemsProcessor,
            int batchSize) {
        if (batchSize <= 0)
            throw new IllegalArgumentException("batchSize must be greater than 0");

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

    /**
     * Asynchronously processes items from the provided iterator in batches.
     *
     * @param iterator       the iterator that provides the items to be processed
     * @param itemsProcessor a function that takes a list of items and returns a
     *                       future representing the asynchronous
     *                       processing of these items
     * @param batchSize      the number of items to process in each batch
     * @return a future that completes when all items have been processed
     */
    default <T> Future<Void> asyncCallIteratively(
            @Nonnull Iterator<T> iterator,
            @Nonnull Function<List<T>, Future<Void>> itemsProcessor,
            int batchSize) {
        return asyncCallIteratively(
                iterator,
                (ts, repeatedlyCallTask) -> itemsProcessor.apply(ts),
                batchSize);
    }

    /**
     * Asynchronously processes items from the given iterable in batches.
     *
     * @param <T>            the type of elements in the iterable
     * @param iterable       the iterable containing items to be processed
     * @param itemsProcessor a function that takes a list of items and a task, and
     *                       returns a {@code Future<Void>}
     * @param batchSize      the size of each batch to process
     * @return a Future representing the completion of the processing
     */
    default <T> Future<Void> asyncCallIteratively(
            @Nonnull Iterable<T> iterable,
            @Nonnull BiFunction<List<T>, RepeatedlyCallTask, Future<Void>> itemsProcessor,
            int batchSize) {
        return asyncCallIteratively(iterable.iterator(), itemsProcessor, batchSize);
    }

    /**
     * Asynchronously processes items from an iterator using a provided item
     * processor function.
     * The processing continues until the iterator is exhausted.
     *
     * @param <T>           the type of elements in the iterator
     * @param iterator      the iterator to process items from
     * @param itemProcessor a function that takes an item from the iterator and a
     *                      RepeatedlyCallTask,
     *                      and returns a {@link Future<Void>} representing the
     *                      asynchronous processing of the item
     * @return a {@link Future<Void>} that completes when all items have been
     *         processed or fails if any processing step
     *         fails
     */
    default <T> Future<Void> asyncCallIteratively(
            @Nonnull Iterator<T> iterator,
            @Nonnull BiFunction<T, RepeatedlyCallTask, Future<Void>> itemProcessor) {
        return asyncCallRepeatedly(routineResult -> Future.succeededFuture()
                .compose(v -> {
                    if (iterator.hasNext()) {
                        return itemProcessor.apply(iterator.next(), routineResult);
                    } else {
                        routineResult.stop();
                        return Future.succeededFuture();
                    }
                }));
    }

    /**
     * Processes each item in the given iterator asynchronously and iteratively.
     *
     * @param <T>           the type of elements in the iterator
     * @param iterator      the iterator containing items to be processed
     * @param itemProcessor a function that takes an item from the iterator and
     *                      returns a Future representing the
     *                      asynchronous processing of that item
     * @return a Future that completes when all items have been processed, or fails
     *         if any item processing fails
     */
    default <T> Future<Void> asyncCallIteratively(
            @Nonnull Iterator<T> iterator,
            @Nonnull Function<T, Future<Void>> itemProcessor) {
        return asyncCallIteratively(
                iterator,
                (t, repeatedlyCallTask) -> itemProcessor.apply(t));
    }

    /**
     * Asynchronously processes each item in the provided iterable using the
     * specified item processor function.
     * The items are processed sequentially, ensuring that the next item is only
     * processed after the current one has
     * completed.
     *
     * @param iterable      an iterable containing the items to be processed
     * @param itemProcessor a function that takes an item from the iterable and
     *                      returns a Future representing the
     *                      asynchronous processing of the item
     * @return a Future that completes when all items in the iterable have been
     *         processed
     */
    default <T> Future<Void> asyncCallIteratively(
            @Nonnull Iterable<T> iterable,
            @Nonnull Function<T, Future<Void>> itemProcessor) {
        return asyncCallIteratively(iterable.iterator(), itemProcessor);
    }

    /**
     * Asynchronously processes each item in the given iterable using the provided
     * item processor function.
     *
     * @param <T>           the type of elements in the iterable
     * @param iterable      the iterable containing items to be processed
     * @param itemProcessor a function that takes an item and a task, and returns a
     *                      Future representing the asynchronous
     *                      processing of the item
     * @return a Future that completes when all items have been processed
     */
    default <T> Future<Void> asyncCallIteratively(
            @Nonnull Iterable<T> iterable,
            @Nonnull BiFunction<T, RepeatedlyCallTask, Future<Void>> itemProcessor) {
        return asyncCallIteratively(iterable.iterator(), itemProcessor);
    }

    /**
     * Executes a series of asynchronous calls in a stepwise manner, from the start
     * value to the end value, with a
     * specified step.
     * As of 4.0.9, fix the bug about range break.
     *
     * @param start     the starting value for the stepwise execution
     * @param end       the ending value for the stepwise execution, must be equal
     *                  to or greater than `start`; if the pointer is stepped to be
     *                  equal to or greater than `end`, the task will be stopped.
     * @param step      the step size to increment or decrement at each call, must
     *                  be greater than 0.
     * @param processor a function that processes the current value and a task,
     *                  returning a Future
     * @return a Future that completes when all stepwise calls have been processed
     * @throws IllegalArgumentException if the step is 0
     */
    default Future<Void> asyncCallStepwise(long start, long end, long step,
            BiFunction<Long, RepeatedlyCallTask, Future<Void>> processor) {
        if (step <= 0)
            throw new IllegalArgumentException("step must be greater than 0");
        if (start > end)
            throw new IllegalArgumentException("start must not be greater than end");
        AtomicLong ptr = new AtomicLong(start);
        return asyncCallRepeatedly(task -> Future.succeededFuture()
                .compose(vv -> processor.apply(ptr.get(), task)
                        .compose(v -> {
                            long y = ptr.addAndGet(step);
                            if (y >= end) {
                                task.stop();
                            }
                            return Future.succeededFuture();
                        })));
    }

    /**
     * Executes a given processor function stepwise for a specified number of times.
     *
     * @param times     the total number of times to execute the processor function
     * @param processor the function to be executed, which takes the current step
     *                  and a task as arguments and returns a
     *                  Future
     * @return a Future that completes when all steps have been processed
     */
    default Future<Void> asyncCallStepwise(long times, BiFunction<Long, RepeatedlyCallTask, Future<Void>> processor) {
        if (times <= 0) {
            return Future.succeededFuture();
        }
        return asyncCallStepwise(0, times, 1, processor);
    }

    /**
     * Executes a given asynchronous task multiple times in a stepwise manner.
     *
     * @param times     the number of times to execute the task
     * @param processor the function that returns a future, representing the
     *                  asynchronous task to be executed
     * @return a Future that completes when all invocations of the processor have
     *         completed
     */
    default Future<Void> asyncCallStepwise(long times, Function<Long, Future<Void>> processor) {
        if (times <= 0) {
            return Future.succeededFuture();
        }
        return asyncCallStepwise(0, times, 1, (aLong, repeatedlyCallTask) -> processor.apply(aLong));
    }

    /**
     * Repeatedly calls the given supplier to execute an asynchronous job.
     * The job is executed in a loop until it is manually stopped.
     * Each call to the supplier should return a {@code Future<Void>} which
     * represents the completion of the asynchronous task.
     * 
     * <p>
     * <strong>Note:</strong> This method will continue executing even if the
     * supplier
     * fails, as failures are converted to success to keep the loop running.
     * If you need to stop on failure, use {@link #asyncCallRepeatedly(Function)}
     * instead.
     *
     * @param supplier An async job handler that provides a {@code Future<Void>}.
     * @since 3.0.1
     */
    default void asyncCallEndlessly(@Nonnull Supplier<Future<Void>> supplier) {
        asyncCallRepeatedly(routineResult -> Future.succeededFuture()
                .compose(v -> supplier.get())
                .eventually(Future::succeededFuture)); // Convert failures to success to keep loop running
    }

    /**
     * Asynchronously sleeps for a specified amount of time.
     *
     * @param time the duration to sleep in milliseconds
     * @return a Future that completes after the specified time has elapsed
     */
    default Future<Void> asyncSleep(long time) {
        return asyncSleep(time, null);
    }

    /**
     * Asynchronously sleeps for a specified amount of time, with an optional
     * interrupter.
     *
     * @param time        the duration to sleep in milliseconds. If less than 1, it
     *                    will be set to 1.
     * @param interrupter an optional Promise that, when completed, will cancel the
     *                    sleep and complete the returned
     *                    Future.
     * @return a Future that completes after the specified time has elapsed, or is
     *         interrupted by the interrupter.
     */
    default Future<Void> asyncSleep(long time, @Nullable Promise<Void> interrupter) {
        Promise<Void> promise = Promise.promise();
        time = Math.max(1, time);
        long timer_id = Keel.getVertx().setTimer(time, timerID -> promise.complete());
        if (interrupter != null) {
            interrupter.future().onSuccess(interrupted -> {
                Keel.getVertx().cancelTimer(timer_id);
                promise.tryComplete();
            });
        }
        return promise.future();
    }

    /**
     * Executes a supplier asynchronously while ensuring exclusive access to a given
     * lock.
     *
     * @param <T>               the type of the result returned by the supplier
     * @param lockName          the name of the lock to be used for ensuring
     *                          exclusivity
     * @param waitTimeForLock   the maximum time in milliseconds to wait for
     *                          acquiring the lock
     * @param exclusiveSupplier the supplier that provides a future, which will be
     *                          executed exclusively
     * @return a future representing the asynchronous computation result
     */
    default <T> Future<T> asyncCallExclusively(@Nonnull String lockName, long waitTimeForLock,
            @Nonnull Supplier<Future<T>> exclusiveSupplier) {
        return Keel.getVertx().sharedData()
                .getLockWithTimeout(lockName, waitTimeForLock)
                .compose(lock -> Future.succeededFuture()
                        .compose(v -> exclusiveSupplier.get())
                        .andThen(ar -> lock.release()));
    }

    /**
     * Executes the given supplier asynchronously with an exclusive lock.
     *
     * @param <T>               the type of the result produced by the supplier
     * @param lockName          the name of the lock to be used for exclusivity
     * @param exclusiveSupplier the supplier that produces a future, which will be
     *                          executed exclusively
     * @return a future representing the asynchronous computation
     */
    default <T> Future<T> asyncCallExclusively(@Nonnull String lockName,
            @Nonnull Supplier<Future<T>> exclusiveSupplier) {
        return asyncCallExclusively(lockName, 1_000L, exclusiveSupplier);
    }

    /**
     * Transforms a given CompletableFuture into a Vert.x Future.
     *
     * @param completableFuture the CompletableFuture to be transformed, must not be
     *                          null
     * @return a Future that completes or fails based on the completion of the
     *         provided CompletableFuture
     * @since 4.0.6 fix naming mistake.
     */
    default <R> Future<R> asyncTransformCompletableFuture(@Nonnull CompletableFuture<R> completableFuture) {
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

    /**
     * Transforms a raw Java Future into a Vert.x Future, with an asynchronous
     * polling mechanism to check the completion
     * of the raw Future.
     *
     * @param rawFuture the raw Java Future to be transformed
     * @param sleepTime the time in milliseconds to wait between each poll
     * @param <R>       the type of the result
     * @return a Vert.x Future that will complete or fail based on the outcome of
     *         the raw Future
     */
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
                            promise.fail(new Exception(
                                    getClass().getName() +
                                            ".asyncTransformRawFuture failed with rawFuture is not done."));
                        }
                    } else {
                        promise.fail(ar.cause());
                    }
                })
                .recover(throwable -> Future.succeededFuture())
                .compose(v -> promise.future());
    }

    /**
     * Executes the provided blocking code handler in a separate worker verticle to
     * avoid blocking the event loop.
     * The method ensures that the blocking operation is performed in a non-blocking
     * manner, allowing the event loop to
     * continue processing other tasks. Once the blocking operation is complete, the
     * result or any failure is propagated
     * back to the caller through the returned Future.
     *
     * @param <T>                 the type of the result
     * @param blockingCodeHandler the handler that contains the blocking code to be
     *                            executed. It receives a Promise
     *                            which should be completed with the result or
     *                            failed with an error.
     * @return a Future that will be completed with the result of the blocking
     *         operation, or failed if an error occurs.
     */
    default <T> Future<T> executeBlocking(@Nonnull Handler<Promise<T>> blockingCodeHandler) {
        Promise<T> promise = Promise.promise();

        KeelVerticle verticle = KeelVerticle.instant(() -> {
            blockingCodeHandler.handle(promise);
            return Future.succeededFuture();
        });
        return verticle.deployMe(new DeploymentOptions().setThreadingModel(ThreadingModel.WORKER))
                .compose(
                        deploymentId -> {
                            return promise.future()
                                    .onComplete(ar -> verticle.undeployMe());
                        },
                        deploymentFailure -> {
                            // If deployment fails, fail the promise and return failed future
                            promise.tryFail(deploymentFailure);
                            return Future.failedFuture(deploymentFailure);
                        });
    }

    /**
     * Creates a {@link CompletableFuture} that will be completed based on the
     * result of the provided blocking code
     * handler.
     *
     * @param <T>                 the type of the result
     * @param blockingCodeHandler the handler containing the blocking code to be
     *                            executed, which returns a promise
     * @return a new {@link CompletableFuture} that will be completed with the
     *         result of the blocking code or
     *         exceptionally if an error occurs
     */
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

    /**
     * Executes the provided blocking code handler and waits for its completion.
     *
     * @param <T>                 the type of the result
     * @param blockingCodeHandler a handler that receives a Promise and performs
     *                            some blocking operations
     * @return the result of the blocking operation
     * @throws RuntimeException if an InterruptedException or ExecutionException
     *                          occurs during the execution
     */
    default <T> T pseudoAwait(Handler<Promise<T>> blockingCodeHandler) {
        CompletableFuture<T> completableFuture = createPseudoAwaitCompletableFuture(blockingCodeHandler);
        try {
            return completableFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Executes the provided blocking code handler and waits for its completion with
     * a timeout.
     *
     * @param <T>                 the type of the result
     * @param blockingCodeHandler a handler that receives a Promise and performs
     *                            some blocking operations
     * @param timeout             the maximum time to wait for the blocking
     *                            operation to complete
     * @param timeoutUnit         the unit of time for the timeout
     * @return the result of the blocking operation
     * @throws RuntimeException if an InterruptedException, ExecutionException, or
     *                          TimeoutException occurs during the
     *                          execution
     */
    default <T> T pseudoAwait(Handler<Promise<T>> blockingCodeHandler, long timeout, TimeUnit timeoutUnit) {
        CompletableFuture<T> completableFuture = createPseudoAwaitCompletableFuture(blockingCodeHandler);
        try {
            return completableFuture.get(timeout, timeoutUnit);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Safely awaits the completion of a future, with appropriate handling for
     * different thread contexts.
     *
     * <p>
     * <strong>Warning:</strong> This method is primarily designed for testing
     * scenarios.
     * It should NOT be called from event loop threads in production code as it will
     * block the event loop.
     *
     * <p>
     * Behavior:
     * <ul>
     * <li>In Java 21+ with virtual threads: Uses {@link Future#await()}</li>
     * <li>In event loop thread: Logs warning and uses blocking wait (NOT
     * recommended)</li>
     * <li>In other threads: Uses blocking wait safely</li>
     * </ul>
     *
     * @param <T>    the type of the result
     * @param future the future to await
     * @return the result of the future
     * @throws NullPointerException if the provided future is null
     * @since 4.1.0
     */
    default <T> T await(@Nonnull Future<T> future) {
        boolean isOnEventLoop = Context.isOnEventLoopThread();
        boolean hasVirtualThreads = Keel.reflectionHelper().isVirtualThreadsAvailable();

        // 在事件循环线程中调用时给出警告
        if (isOnEventLoop) {
            Keel.getLogger().warning(r -> r
                    .message("await called from event loop thread - this will block the event loop!")
                    .context("thread", Thread.currentThread().getName())
                    .context("recommendation", "Use Future.compose() or call from test thread instead"));
        }

        // Java 21+ 环境下且不在事件循环线程中，使用虚拟线程的await
        if (hasVirtualThreads && !isOnEventLoop) {
            return Future.await(future);
        } else {
            // 降级到使用CompletableFuture的方式（无超时）
            return pseudoAwait(future::onComplete);
        }
    }

    /**
     * A utility class designed to repeatedly execute a task until it is explicitly
     * stopped.
     * The task is represented by a {@link Function} that takes an instance of this
     * class and
     * returns a {@link Future<Void>}. The task will continue to be executed with a
     * delay of 1 millisecond
     * between each execution, unless the stop method is called or the task itself
     * fails.
     *
     * @see #start(RepeatedlyCallTask, Promise)
     * @see #stop()
     */
    final class RepeatedlyCallTask {
        @Nonnull
        private final Function<RepeatedlyCallTask, Future<Void>> processor;
        private volatile boolean toStop = false;

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
