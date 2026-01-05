package io.github.sinri.keel.core.cache;

import io.vertx.core.Future;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Objects;


/**
 * 提供异步步读写方法的缓存接口。
 * <p>
 * 缓存的键不可为空。
 * 一个有效缓存记录的值不可为空。
 *
 * @param <K> 缓存的键的类型
 * @param <V> 缓存的值的类型
 * @since 5.0.0
 */
@NullMarked
public interface KeelAsyncCacheAlike<K, V> {
    /**
     * 根据给定的键值对异步存入一条缓存记录。
     * <p>
     * 如果值为null，将移除键对应的缓存记录，因为null不是有效的缓存值。
     *
     * @param k 键
     * @param v 值
     * @return 异步执行结果
     */
    Future<Void> save(K k, @Nullable V v);

    /**
     * 根据给定的键，尝试异步获取缓存的值；如果无法找到有效的缓存值，则返回给定的默认值。
     *
     * @param k 键
     * @param v 默认值，用于无法找到有效缓存时
     * @return 异步返回的给定键对应的有效缓存或给定的默认值
     */
    Future<@Nullable V> read(K k, @Nullable V v);

    /**
     * 根据给定的键，尝试异步获取缓存的值，如果无法找到有效的缓存值，则抛出 {@link NotCached} 异常。
     *
     * @param k 键
     * @return 异步返回的给定键对应的有效缓存，或失败时给出{@link NotCached}异常。
     */
    default Future<V> read(K k) {
        return read(k, null)
                .compose(v -> {
                    try {
                        V vv = Objects.requireNonNull(v);
                        return Future.succeededFuture(vv);
                    } catch (NullPointerException nullPointerException) {
                        return Future.failedFuture(new NotCached(k.toString()));
                    }
                });
    }
}
