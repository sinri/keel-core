package io.github.sinri.keel.core.servant.intravenous;

import io.vertx.core.Future;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static io.github.sinri.keel.base.KeelInstance.Keel;


/**
 * 对单个实体进行处理的静脉注入的实现
 *
 * @since 5.0.0
 */
class KeelIntravenousSingleImpl<D> extends KeelIntravenousBase<D> {
    private final @NotNull SingleDropProcessor<D> itemProcessor;

    public KeelIntravenousSingleImpl(@NotNull SingleDropProcessor<D> itemProcessor) {
        super();
        this.itemProcessor = itemProcessor;
    }

    protected @NotNull Future<Void> handleDrops(@NotNull List<D> drops) {
        return Keel.asyncCallIteratively(drops, drop -> Future.succeededFuture()
                                                              .compose(v -> this.itemProcessor.process(drop))
                                                              .recover(throwable -> {
                                                                  this.handleAllergy(throwable);
                                                                  return Future.succeededFuture();
                                                              }));
    }
}
