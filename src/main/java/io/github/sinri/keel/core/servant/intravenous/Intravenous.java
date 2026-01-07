package io.github.sinri.keel.core.servant.intravenous;

import io.github.sinri.keel.base.verticles.KeelVerticleBase;
import io.github.sinri.keel.base.verticles.KeelVerticleRunningStateEnum;
import io.vertx.core.Future;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;


/**
 * 静脉注入。
 * <p>
 * 陆续接收特定类型的对象，并按照 FIFO 的原则依次按批处理。
 *
 * @param <D> 处理对象的类型
 * @since 5.0.0
 */
@NullMarked
public abstract class Intravenous<D extends @Nullable Object> extends KeelVerticleBase {

    public static <T extends @Nullable Object> Intravenous<T> instant(SingleDropProcessor<T> itemProcessor) {
        return new IntravenousSingleImpl<>(itemProcessor);
    }


    public static <T extends @Nullable Object> Intravenous<T> instantBatch(MultiDropsProcessor<T> itemsProcessor) {
        return new IntravenousBatchImpl<>(itemsProcessor);
    }

    abstract public void add(D drop);

    /**
     * 处理对象过程中发生异常时的回调。
     * <p>
     * 默认实现为无视异常。
     */
    protected void handleAllergy(Throwable throwable) {
        // do nothing by default for the thrown exception
    }

    public abstract boolean isNoDropsLeft();

    /**
     * @return 是否已停止接收待处理对象。
     */
    public abstract boolean isStopped();

    /**
     * 通知本接口的实现立即停止接收待处理对象。
     */
    public abstract void shutdown();


    public Future<Void> shutdownAndAwait() {
        shutdown();
        return asyncCallRepeatedly(repeatedlyCallTask -> {
            if (isUndeployed()) {
                repeatedlyCallTask.stop();
                return Future.succeededFuture();
            } else {
                return asyncSleep(1000L);
            }
        });
    }

    /**
     * @return Whether the verticle is undeployed.
     */
    public final boolean isUndeployed() {
        return this.getRunningState() == KeelVerticleRunningStateEnum.AFTER_RUNNING;
    }

    @NullMarked
    public interface SingleDropProcessor<T extends @Nullable Object> {
        Future<Void> process(T drop);
    }

    @NullMarked
    public interface MultiDropsProcessor<T extends @Nullable Object> {
        Future<Void> process(List<T> drops);
    }
}
