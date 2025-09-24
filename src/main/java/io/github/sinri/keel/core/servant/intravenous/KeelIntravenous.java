package io.github.sinri.keel.core.servant.intravenous;

import io.github.sinri.keel.core.verticles.KeelVerticle;
import io.vertx.core.Future;

import javax.annotation.Nonnull;
import java.util.List;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

/**
 * Handle tasks to process objects of a certain type in order.
 * <p>
 * Extracted since 4.0.7.
 *
 * @param <D> the type of the object to be processed.
 * @since 4.0.7
 */
public interface KeelIntravenous<D> extends KeelVerticle {
    static <T> KeelIntravenous<T> instant(@Nonnull SingleDropProcessor<T> itemProcessor) {
        return new KeelIntravenousSingleImpl<>(itemProcessor);
    }

    static <T> KeelIntravenous<T> instantBatch(@Nonnull MultiDropsProcessor<T> itemsProcessor) {
        return new KeelIntravenousBatchImpl<>(itemsProcessor);
    }

    void add(D drop);

    /**
     * The exception occurred when handling drops, override this method to handle.
     * By default, the exception would be omitted.
     */
    default void handleAllergy(Throwable throwable) {
        // do nothing by default for the thrown exception
    }

    /**
     * @since 4.0.11
     */
    boolean isNoDropsLeft();

    /**
     * When all the drops reported, i.e. no more drops would be accepted.
     *
     * @since 4.0.11
     */
    boolean isStopped();

    void shutdown();

    /**
     * @since 4.1.3
     */
    default Future<Void> shutdownAndAwait() {
        shutdown();
        return Keel.asyncCallRepeatedly(repeatedlyCallTask -> {
            if (isUndeployed()) {
                repeatedlyCallTask.stop();
                return Future.succeededFuture();
            } else {
                return Keel.asyncSleep(1000L);
            }
        });
    }

    /**
     * @return Whether the verticle is undeployed.
     * @since 4.0.11
     */
    boolean isUndeployed();

    interface SingleDropProcessor<T> {
        Future<Void> process(T drop);
    }

    interface MultiDropsProcessor<T> {
        Future<Void> process(List<T> drops);
    }
}
