package io.github.sinri.keel.core.servant.intravenous;

import io.vertx.core.Future;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 对指定实体进行批量处理的静脉注入的实现。
 *
 * @since 5.0.0
 */
class KeelIntravenousBatchImpl<D> extends KeelIntravenousBase<D> {

    private final @NotNull MultiDropsProcessor<D> itemsProcessor;

    public KeelIntravenousBatchImpl(@NotNull MultiDropsProcessor<D> itemsProcessor) {
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
