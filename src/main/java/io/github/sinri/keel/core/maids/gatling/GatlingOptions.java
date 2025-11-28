package io.github.sinri.keel.core.maids.gatling;

import io.vertx.core.Future;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * 多管加特林的配置。
 *
 * @since 5.0.0
 */
public class GatlingOptions {
    @NotNull
    private final String gatlingName;
    private int barrels;
    private long averageRestInterval;
    @NotNull
    private Supplier<Future<Bullet>> bulletLoader;

    public GatlingOptions(@NotNull String gatlingName) {
        this.gatlingName = gatlingName;
        this.barrels = 1;
        this.averageRestInterval = 1000;
        this.bulletLoader = () -> Future.succeededFuture(null);
    }

    /**
     * @return 加特林机枪名称（集群中各节点之间的识别同一组加特林机枪类的实例用）
     */
    @NotNull
    public String getGatlingName() {
        return gatlingName;
    }

    /**
     * @return 枪管数量（并发任务数）
     */
    public int getBarrels() {
        return barrels;
    }

    /**
     * @param barrels 枪管数量（并发任务数）
     */
    @NotNull
    public GatlingOptions setBarrels(int barrels) {
        this.barrels = barrels;
        return this;
    }

    /**
     * @return 弹带更换平均等待时长（没有新任务时的休眠期，单位毫秒）
     */
    public long getAverageRestInterval() {
        return averageRestInterval;
    }

    /**
     * @param averageRestInterval 弹带更换平均等待时长（没有新任务时的休眠期，单位毫秒）
     */
    @NotNull
    public GatlingOptions setAverageRestInterval(long averageRestInterval) {
        this.averageRestInterval = averageRestInterval;
        return this;
    }

    /**
     * @return 供弹器（新任务生成器）
     */
    @NotNull
    public Supplier<Future<Bullet>> getBulletLoader() {
        return Objects.requireNonNull(bulletLoader);
    }

    /**
     * @param bulletLoader 供弹器（新任务生成器）
     */
    @NotNull
    public GatlingOptions setBulletLoader(@NotNull Supplier<Future<Bullet>> bulletLoader) {
        this.bulletLoader = bulletLoader;
        return this;
    }
}
