package io.github.sinri.keel.core.servant.intravenous;

import io.github.sinri.keel.core.verticles.KeelVerticle;
import io.vertx.core.Future;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Handle tasks to process certain type of objects in order.
 * <p>
 * Extracted since 4.0.7.
 * </p>
 *
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
     * When the verticle is undeployed.
     *
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
