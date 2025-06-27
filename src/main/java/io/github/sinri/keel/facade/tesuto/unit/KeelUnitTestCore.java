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
     * @deprecated use {@link Future#await()} for this scenario.
     */
    @Deprecated(since = "4.1.0")
    default void async(Handler<Promise<Void>> testHandler) {
        Keel.pseudoAwait(testHandler);
    }

    /**
     * @deprecated use {@link Future#await()} for this scenario.
     */
    @Deprecated(since = "4.1.0")
    default void async(Supplier<Future<Void>> testSupplier) {
        testSupplier.get().await();

        //        Keel.pseudoAwait(p -> testSupplier.get().andThen(ar -> {
        //            if (ar.succeeded()) {
        //                p.complete();
        //            } else {
        //                p.fail(ar.cause());
        //            }
        //        }));
    }
}
