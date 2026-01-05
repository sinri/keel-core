package io.github.sinri.keel.core.cache;

import io.vertx.core.Future;
import org.jspecify.annotations.NullMarked;

import java.util.Set;
import java.util.function.Function;


/**
 * 基于 {@link KeelAsyncCacheAlike} 提供异步方法的缓存接口.
 *
 * @param <K> 键的类型
 * @param <V> 值的类型
 * @since 5.0.0
 */
@NullMarked
public interface KeelAsyncCacheInterface<K, V> extends KeelAsyncCacheAlike<K, V> {

    /**
     * 根据给定的键值对存入一条指定时长内有效的缓存记录。
     *
     * @param key           键
     * @param value         值
     * @param lifeInSeconds 存活周期，以秒计
     */
    Future<Void> save(K key, V value, long lifeInSeconds);

    /**
     * 根据给定的键，尝试获取值；如果无法找到有效的值，则利用给定的逻辑生成一个新值，以给定存活周期存入；返回找到的值或存入的值。
     *
     * @param key           键
     * @param generator     新建值的逻辑
     * @param lifeInSeconds 存活周期，以秒计
     * @return 异步返回的值
     */
    Future<V> read(K key, Function<K, Future<V>> generator, long lifeInSeconds);

    /**
     * 从缓存中移除指定键值对。
     *
     * @param key 键
     */
    Future<Void> remove(K key);

    /**
     * 从缓存中移除所有键值对。
     */
    Future<Void> removeAll();

    /**
     * 清理缓存中的无效键值对。
     * <p>
     * 本方法需要适时调用以避免内存泄露。
     */
    Future<Void> cleanUp();

    /**
     * 获取所有缓存中的有效的键的集合。
     *
     * @return 有效的键的集合
     */
    Future<Set<K>> getCachedKeySet();

}
