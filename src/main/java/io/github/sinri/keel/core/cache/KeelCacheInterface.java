package io.github.sinri.keel.core.cache;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.function.Function;

import static io.github.sinri.keel.base.KeelInstance.Keel;


/**
 * 基于 {@link KeelSyncCacheAlike} 提供同步方法的缓存接口.
 *
 * @param <K> 键的类型
 * @param <V> 值的类型
 * @since 5.0.0
 */
public interface KeelCacheInterface<K, V> extends KeelSyncCacheAlike<K, V> {
    /**
     * 获取一个默认实现实例
     *
     * @param <K> 键的类型
     * @param <V> 值的类型
     * @return 本接口的默认实现实例
     */
    @NotNull
    static <K, V> KeelCacheInterface<K, V> createDefaultInstance() {
        return new KeelCacheImpl<>();
    }

    /**
     * 获取一个伪同步缓存实现实例，该实例不会缓存任何记录。
     *
     * @param <K> 键的类型
     * @param <V> 值的类型
     * @return 本接口的伪实现实例
     */
    @NotNull
    static <K, V> KeelCacheInterface<K, V> getDummyInstance() {
        return new KeelCacheDummy<>();
    }

    /**
     * @return 默认的缓存记录存活周期，以秒计。
     */
    long getDefaultLifeInSeconds();

    /**
     * 设置默认的缓存记录存活周期，以秒计。
     *
     * @param lifeInSeconds 默认的缓存记录存活周期，以秒计
     * @return 本接口实例
     */
    @NotNull
    KeelCacheInterface<K, V> setDefaultLifeInSeconds(long lifeInSeconds);

    /**
     * 根据给定的键值对存入一条指定时长内有效的缓存记录。
     *
     * @param key           键
     * @param value         值
     * @param lifeInSeconds 本条缓存记录的存活周期，以秒计
     */
    void save(@NotNull K key, V value, long lifeInSeconds);

    /**
     * 根据给定的键值对存入一条默认时长内有效的缓存记录。
     *
     * @param key   键
     * @param value 值
     */
    @Override
    default void save(@NotNull K key, @Nullable V value) {
        save(key, value, getDefaultLifeInSeconds());
    }


    /**
     * 根据给定的键，尝试获取值；如果无法找到有效的值，则利用给定的逻辑生成一个新值，以默认存活周期存入；返回找到的值或存入的值。
     *
     * @param key         键
     * @param computation 新值生成逻辑，生成的结果不应为 null
     * @return 找到的值或新建而存入的值
     */
    @NotNull
    default V computeIfAbsent(@NotNull K key, @NotNull Function<K, V> computation) {
        return computeIfAbsent(key, computation, getDefaultLifeInSeconds());
    }

    /**
     * 根据给定的键，尝试获取值；如果无法找到有效的值，则利用给定的逻辑生成一个新值，以给定存活周期存入；返回找到的值或存入的值。
     *
     * @param key           键
     * @param computation   新值生成逻辑，生成的结果不应为 null
     * @param lifeInSeconds 存活周期，以秒计
     * @return 找到的值或新建而存入的值
     */
    @NotNull
    V computeIfAbsent(@NotNull K key, @NotNull Function<K, V> computation, long lifeInSeconds);

    /**
     * 从缓存中移除一个记录。
     *
     * @param key 键
     */
    void remove(@NotNull K key);

    /**
     * 从缓存中移除所有记录。
     */
    void removeAll();

    /**
     * 从缓存中清理掉所有无效记录（值为空、过期等情况）。
     */
    void cleanUp();

    /**
     * 获取所有缓存中的有效的键的集合。
     *
     * @return 有效的键的集合
     */
    @NotNull
    Set<K> getCachedKeySet();

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
        Keel.asyncCallEndlessly(() -> {
            cleanUp();
            return Keel.asyncSleep(sleepTime);
        });
    }
}
