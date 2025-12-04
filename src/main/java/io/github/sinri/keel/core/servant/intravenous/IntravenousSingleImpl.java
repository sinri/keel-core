package io.github.sinri.keel.core.servant.intravenous;

import io.github.sinri.keel.base.Keel;
import io.vertx.core.Future;
import org.jetbrains.annotations.NotNull;

import java.util.List;


/**
 * 对单个实体进行处理的静脉注入的实现
 *
 * @since 5.0.0
 */
class IntravenousSingleImpl<D> extends IntravenousBase<D> {
    private final @NotNull SingleDropProcessor<D> itemProcessor;

    public IntravenousSingleImpl(@NotNull Keel keel, @NotNull SingleDropProcessor<D> itemProcessor) {
        super(keel);
        this.itemProcessor = itemProcessor;
    }

    protected @NotNull Future<Void> handleDrops(@NotNull List<D> drops) {
        return getKeel().asyncCallIteratively(drops, drop -> Future.succeededFuture()
                                                                .compose(v -> this.itemProcessor.process(drop))
                                                                .recover(throwable -> {
                                                                    this.handleAllergy(throwable);
                                                                    return Future.succeededFuture();
                                                                }));
    }
}
