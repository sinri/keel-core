package io.github.sinri.keel.core.maids.gatling;

import io.vertx.core.Future;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * 多管加特林的配置。
 *
 * @since 5.0.0
 */
@NullMarked
public class GatlingOptions {
    public static final long MAX_AVERAGE_REST_INTERVAL = Integer.MAX_VALUE * 2L;

    private final String gatlingName;
    private int barrels;
    private long averageRestInterval;
    private Supplier<Future<@Nullable Bullet>> bulletLoader;

    public GatlingOptions(String gatlingName) {
        this.gatlingName = gatlingName;
        this.barrels = 1;
        this.averageRestInterval = 1000;
        this.bulletLoader = () -> Future.succeededFuture(null);
    }

    /**
     * @return 加特林机枪名称（集群中各节点之间的识别同一组加特林机枪类的实例用）
     */
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
     * @param barrels 枪管数量（并发任务数），至少为 1
     */
    public GatlingOptions setBarrels(int barrels) {
        if (barrels < 1) {
            throw new IllegalArgumentException("barrels must be at least 1");
        }
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
     * @param averageRestInterval 弹带更换平均等待时长（没有新任务时的休眠期，单位毫秒），范围为
     *                            [2, MAX_AVERAGE_REST_INTERVAL]
     */
    public GatlingOptions setAverageRestInterval(long averageRestInterval) {
        if (averageRestInterval < 2) {
            throw new IllegalArgumentException("averageRestInterval must be at least 2");
        }
        if (averageRestInterval > MAX_AVERAGE_REST_INTERVAL) {
            throw new IllegalArgumentException(
                    "averageRestInterval must be no greater than " + MAX_AVERAGE_REST_INTERVAL
            );
        }
        this.averageRestInterval = averageRestInterval;
        return this;
    }

    /**
     * @return 供弹器（新任务生成器）
     */
    public Supplier<Future<@Nullable Bullet>> getBulletLoader() {
        return Objects.requireNonNull(bulletLoader);
    }

    /**
     * @param bulletLoader 供弹器（新任务生成器）
     */
    public GatlingOptions setBulletLoader(Supplier<Future<@Nullable Bullet>> bulletLoader) {
        this.bulletLoader = bulletLoader;
        return this;
    }
}
