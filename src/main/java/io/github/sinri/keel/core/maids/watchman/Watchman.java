package io.github.sinri.keel.core.maids.watchman;

import io.github.sinri.keel.base.verticles.KeelVerticle;
import io.vertx.core.Handler;
import org.jetbrains.annotations.NotNull;

/**
 * 更夫。
 * 在 Vert.x 集群下实现的定时任务调度器。
 * <p>
 * 每个定时任务都会随机在集群内的某个节点里运行。
 * <p>
 * 在集群中运行时，实例实现应当一致。
 *
 * @since 5.0.0
 */
public interface Watchman extends KeelVerticle {

    /**
     * @return 更夫的名称
     */
    @NotNull
    String watchmanName();

    /**
     * @return 定时任务调度周期
     */
    long interval();

    /**
     * @return 每个定时任务周期调用的触发器
     */
    @NotNull
    Handler<Long> regularHandler();

}
