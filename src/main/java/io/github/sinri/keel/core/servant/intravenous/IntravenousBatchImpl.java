package io.github.sinri.keel.core.servant.intravenous;

import io.github.sinri.keel.base.Keel;
import io.vertx.core.Future;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 对指定实体进行批量处理的静脉注入的实现。
 *
 * @since 5.0.0
 */
class IntravenousBatchImpl<D> extends IntravenousBase<D> {

    private final @NotNull MultiDropsProcessor<D> itemsProcessor;

    public IntravenousBatchImpl(@NotNull Keel keel, @NotNull MultiDropsProcessor<D> itemsProcessor) {
        super(keel);
        this.itemsProcessor = itemsProcessor;
    }

    protected @NotNull Future<Void> handleDrops(@NotNull List<@Nullable D> drops) {
        return Future.succeededFuture()
                     .compose(v -> this.itemsProcessor.process(drops))
                     .recover(throwable -> {
                         handleAllergy(throwable);
                         return Future.succeededFuture();
                     });
    }
}
