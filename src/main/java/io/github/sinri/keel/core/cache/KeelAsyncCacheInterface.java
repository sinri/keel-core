package io.github.sinri.keel.core.cache;

import io.vertx.core.Future;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.function.Function;

import static io.github.sinri.keel.base.KeelInstance.Keel;


/**
 * 基于 {@link KeelAsyncCacheAlike} 提供异步方法的缓存接口.
 *
 * @param <K> 键的类型
 * @param <V> 值的类型
 * @since 5.0.0
 */
public interface KeelAsyncCacheInterface<K, V> extends KeelAsyncCacheAlike<K, V> {

    /**
     * 根据给定的键值对存入一条指定时长内有效的缓存记录。
     *
     * @param key           键
     * @param value         值
     * @param lifeInSeconds 存活周期，以秒计
     */
    @NotNull
    Future<Void> save(@NotNull K key, V value, long lifeInSeconds);

    /**
     * 根据给定的键，尝试获取值；如果无法找到有效的值，则利用给定的逻辑生成一个新值，以给定存活周期存入；返回找到的值或存入的值。
     *
     * @param key           键
     * @param generator     新建值的逻辑
     * @param lifeInSeconds 存活周期，以秒计
     * @return 异步返回的值
     */
    @NotNull
    Future<V> read(@NotNull K key, Function<K, Future<V>> generator, long lifeInSeconds);

    /**
     * 从缓存中移除指定键值对。
     *
     * @param key 键
     */
    @NotNull
    Future<Void> remove(@NotNull K key);

    /**
     * 从缓存中移除所有键值对。
     */
    @NotNull
    Future<Void> removeAll();

    /**
     * 清理缓存中的无效键值对。
     */
    @NotNull
    Future<Void> cleanUp();

    /**
     * 获取所有缓存中的有效的键的集合。
     *
     * @return 有效的键的集合
     */
    @NotNull
    Future<Set<K>> getCachedKeySet();

    /**
     * 启动一个不会停止清理循环，实现定期清理缓存中的无效内容。
     * <p>
     * 本方法不应自动启动，需要手动调用本方法来开启。
     * <p>
     * 如果这个缓存实例的生命周期是有限的（即会先于进程或所在 verticle 结束）那么，那么不能使用此方法，需要自行实现。
     *
     * @param sleepTime 清理周期，以毫秒计
     */
    default void startEndlessCleanUp(long sleepTime) {
        Keel.asyncCallEndlessly(() -> cleanUp().compose(cleaned -> Keel.asyncSleep(sleepTime)));
    }

}
