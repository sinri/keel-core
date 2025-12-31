package io.github.sinri.keel.core.cache;


import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 提供同步读写方法的缓存接口。
 * <p>
 * 缓存的键不可为空。
 * 一个有效缓存记录的值不可为空。
 *
 * @param <K> 缓存的键的类型
 * @param <V> 缓存的值的类型
 * @since 5.0.0
 */
public interface KeelSyncCacheAlike<K, V> {
    /**
     * 根据给定的键值对存入一条缓存记录。
     * <p>
     * 如果值为null，将移除键对应的缓存记录，因为null不是有效的缓存值。
     *
     * @param key   键
     * @param value 值
     */
    void save(@NotNull K key, @Nullable V value);

    /**
     * 根据给定的键，尝试获取缓存的值；如果无法找到有效的缓存值，则返回给定的默认值。
     *
     * @param key           键
     * @param fallbackValue 默认值，用于无法找到有效缓存时
     * @return 给定键对应的有效缓存，或给定的默认值
     */
    @Nullable V read(@NotNull K key, @Nullable V fallbackValue);

    /**
     * 根据给定的键，尝试获取缓存的值，如果无法找到有效的缓存值，则抛出 {@link NotCached} 异常。
     *
     * @param key 键
     * @return 给定键对应的有效缓存
     * @throws NotCached 无法找到有效缓存时
     */
    default @NotNull V read(@NotNull K key) throws NotCached {
        var v = read(key, null);
        if (v == null) {
            throw new NotCached(key.toString());
        }
        return v;
    }
}
