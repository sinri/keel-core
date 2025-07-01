package io.github.sinri.keel.facade.tesuto.unit;

import io.github.sinri.keel.logger.event.KeelEventLog;
import io.github.sinri.keel.logger.issue.recorder.KeelIssueRecorder;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;

import java.util.function.Supplier;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

/**
 * @since 4.0.12
 */
public interface KeelUnitTestCore {
    /**
     * Override this method to be executed before each `test*` method.
     */
    default void setUp() {
    }

    /**
     * Override this method to be executed after each `test*` method.
     */
    default void tearDown() {
    }

    KeelIssueRecorder<KeelEventLog> getUnitTestLogger();

    /**
     * Executes the given test handler asynchronously and waits for its completion.
     *
     * <p>This method is designed to be used in testing scenarios where an asynchronous operation
     * needs to be awaited. It ensures that the test does not proceed until the promise handled by
     * the testHandler is completed.</p>
     *
     * @param testHandler a handler that receives a Promise and performs some asynchronous operations
     * @throws RuntimeException if an error occurs during the execution of the testHandler
     */
    default void async(Handler<Promise<Void>> testHandler) {
        //        Promise<Void> promise = Promise.promise();
        //        testHandler.handle(promise);
        //        Keel.await(promise.future());
        Keel.pseudoAwait(testHandler);
    }

    /**
     * Executes the given supplier asynchronously and waits for its completion.
     *
     * <p>This method is designed to be used in testing scenarios where an asynchronous operation
     * needs to be awaited. It ensures that the test does not proceed until the future returned by
     * the supplier is completed.</p>
     *
     * @param testSupplier a supplier that provides a {@link Future} which will be awaited
     * @throws NullPointerException if the provided supplier or the future it returns is null
     */
    default void async(Supplier<Future<Void>> testSupplier) {
        Keel.await(testSupplier.get());
    }
}
