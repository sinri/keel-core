package io.github.sinri.keel.core.servant.intravenous;

import io.github.sinri.keel.base.Keel;
import io.vertx.core.Future;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * 对指定实体进行批量处理的静脉注入的实现。
 *
 * @since 5.0.0
 */
@NullMarked
class IntravenousBatchImpl<D extends @Nullable Object> extends IntravenousBase<D> {

    private final MultiDropsProcessor<D> itemsProcessor;

    public IntravenousBatchImpl(Keel keel, MultiDropsProcessor<D> itemsProcessor) {
        super(keel);
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
