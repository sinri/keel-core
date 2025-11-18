package io.github.sinri.keel.core.servant.intravenous;

import io.github.sinri.keel.base.verticles.KeelVerticle;
import io.github.sinri.keel.base.verticles.KeelVerticleRunningStateEnum;
import io.vertx.core.Future;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static io.github.sinri.keel.base.KeelInstance.Keel;


/**
 * 静脉注入。
 * <p>
 * 陆续接收特定类型的对象，并按照 FIFO 的原则依次按批处理。
 *
 * @param <D> 处理对象的类型
 * @since 5.0.0
 */
public interface KeelIntravenous<D> extends KeelVerticle {
    static <T> KeelIntravenous<T> instant(@NotNull SingleDropProcessor<T> itemProcessor) {
        return new KeelIntravenousSingleImpl<>(itemProcessor);
    }

    static <T> KeelIntravenous<T> instantBatch(@NotNull MultiDropsProcessor<T> itemsProcessor) {
        return new KeelIntravenousBatchImpl<>(itemsProcessor);
    }

    void add(D drop);

    /**
     * 处理对象过程中发生异常时的回调。
     * <p>
     * 默认实现为无视异常。
     */
    default void handleAllergy(Throwable throwable) {
        // do nothing by default for the thrown exception
    }

    boolean isNoDropsLeft();

    /**
     * @return 是否已停止接收待处理对象。
     */
    boolean isStopped();

    /**
     * 通知本接口的实现立即停止接收待处理对象。
     */
    void shutdown();

    default Future<Void> shutdownAndAwait() {
        shutdown();
        return Keel.asyncCallRepeatedly(repeatedlyCallTask -> {
            if (isUndeployed()) {
                repeatedlyCallTask.stop();
                return Future.succeededFuture();
            } else {
                return Keel.asyncSleep(1000L);
            }
        });
    }

    /**
     * @return Whether the verticle is undeployed.
     */
    default boolean isUndeployed() {
        return this.getRunningState() == KeelVerticleRunningStateEnum.AFTER_RUNNING;
    }

    interface SingleDropProcessor<T> {
        Future<Void> process(T drop);
    }

    interface MultiDropsProcessor<T> {
        Future<Void> process(List<T> drops);
    }
}
