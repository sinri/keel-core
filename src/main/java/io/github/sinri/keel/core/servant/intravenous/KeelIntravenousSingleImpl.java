package io.github.sinri.keel.core.servant.intravenous;

import io.vertx.core.Future;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static io.github.sinri.keel.base.KeelInstance.Keel;


/**
 * A new implementation as of 4.0.7, no sleep anymore.
 *
 * @since 4.0.7
 */
class KeelIntravenousSingleImpl<D> extends KeelIntravenousBase<D> {
    private final @NotNull SingleDropProcessor<D> itemProcessor;

    public KeelIntravenousSingleImpl(@NotNull SingleDropProcessor<D> itemProcessor) {
        super();
        this.itemProcessor = itemProcessor;
    }

    protected Future<Void> handleDrops(List<D> drops) {
        return Keel.asyncCallIteratively(drops, drop -> Future.succeededFuture()
                                                          .compose(v -> this.itemProcessor.process(drop))
                                                          .recover(throwable -> {
                         this.handleAllergy(throwable);
                         return Future.succeededFuture();
                     }));
    }
}
