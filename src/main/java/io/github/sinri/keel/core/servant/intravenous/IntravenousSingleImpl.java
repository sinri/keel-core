package io.github.sinri.keel.core.servant.intravenous;

import io.vertx.core.Future;
import org.jspecify.annotations.NullMarked;

import java.util.List;


/**
 * 对单个实体进行处理的静脉注入的实现
 *
 * @since 5.0.0
 */
@NullMarked
class IntravenousSingleImpl<D> extends IntravenousBase<D> {
    private final SingleDropProcessor<D> itemProcessor;

    public IntravenousSingleImpl(SingleDropProcessor<D> itemProcessor) {
        super();
        this.itemProcessor = itemProcessor;
    }

    protected Future<Void> handleDrops(List<D> drops) {
        return getKeel().asyncCallIteratively(
                drops,
                drop -> Future.succeededFuture()
                              .compose(v -> this.itemProcessor.process(drop))
                              .recover(throwable -> {
                                  this.handleAllergy(throwable);
                                  return Future.succeededFuture();
                              })
        );
    }
}
