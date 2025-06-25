package io.github.sinri.keel.core.servant.intravenous;

import io.vertx.core.Future;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * @since 4.0.7
 */
class KeelIntravenousBatchImpl<D> extends KeelIntravenousBase<D> {

    private final @Nonnull MultiDropsProcessor<D> itemsProcessor;

    public KeelIntravenousBatchImpl(@Nonnull MultiDropsProcessor<D> itemsProcessor) {
        super();
        this.itemsProcessor = itemsProcessor;
    }

    protected Future<Void> handleDrops(List<D> drops) {
        return Future.succeededFuture()
                     .compose(v -> this.itemsProcessor.process(drops))
                     .recover(throwable -> {
                         handleAllergy(throwable);
                         return Future.succeededFuture();
                     });
    }
}
