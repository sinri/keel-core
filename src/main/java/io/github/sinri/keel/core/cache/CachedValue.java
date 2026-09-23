package io.github.sinri.keel.core.cache;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * 在指定存活周期内缓存单个值。
 * <p>
 * 缓存值过期后，可以通过 {@link #set(Object)} 写入新值，也可以通过
 * {@link #getOrReload(Supplier)} 按需重新加载。重新加载操作会串行执行；当多个线程同时发现缓存失效时，
 * 只有第一个线程会调用加载器，其余线程复用新加载的值。
 *
 * @param <P> 缓存值的类型
 * @since 5.0.4
 */
@NullMarked
public class CachedValue<P> {
    private static final long DEFAULT_LIFETIME_IN_SECONDS = 10L;

    private final AtomicReference<@Nullable ValueExpirationPair<P>> cachedValueReference = new AtomicReference<>();
    private final ReentrantLock reloadLock = new ReentrantLock();
    private volatile long defaultLifetimeInSeconds = DEFAULT_LIFETIME_IN_SECONDS;

    private static long computeExpiresAt(long now, long lifeInSeconds) {
        if (lifeInSeconds <= 0) {
            throw new IllegalArgumentException("lifeInSeconds must be greater than 0");
        }
        try {
            long lifeInMillis = Math.multiplyExact(lifeInSeconds, 1_000L);
            return Math.addExact(now, lifeInMillis);
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException("lifeInSeconds is too large", e);
        }
    }

    /**
     * 获取默认缓存存活周期。
     *
     * @return 默认缓存存活周期，以秒计
     */
    public long getDefaultLifetimeInSeconds() {
        return defaultLifetimeInSeconds;
    }

    /**
     * 设置默认缓存存活周期。
     *
     * @param defaultLifetimeInSeconds 默认缓存存活周期，以秒计，必须大于零
     * @return 当前缓存实例
     * @throws IllegalArgumentException 存活周期不合法或换算后的过期时间溢出时
     */
    public CachedValue<P> setDefaultLifetimeInSeconds(long defaultLifetimeInSeconds) {
        computeExpiresAt(System.currentTimeMillis(), defaultLifetimeInSeconds);
        this.defaultLifetimeInSeconds = defaultLifetimeInSeconds;
        return this;
    }

    /**
     * 使用默认存活周期缓存一个值。
     *
     * @param value 要缓存的值
     * @return 当前缓存实例
     */
    public CachedValue<P> set(P value) {
        return set(value, defaultLifetimeInSeconds);
    }

    /**
     * 使用指定存活周期缓存一个值。
     *
     * @param value         要缓存的值，不可为 {@code null}
     * @param lifeInSeconds 存活周期，以秒计，必须大于零
     * @return 当前缓存实例
     * @throws NullPointerException     值为 {@code null} 时
     * @throws IllegalArgumentException 存活周期不合法或换算后的过期时间溢出时
     */
    public CachedValue<P> set(P value, long lifeInSeconds) {
        P nonNullValue = Objects.requireNonNull(value, "value");
        long expiresAt = computeExpiresAt(System.currentTimeMillis(), lifeInSeconds);
        cachedValueReference.set(new ValueExpirationPair<>(nonNullValue, expiresAt));
        return this;
    }

    /**
     * 获取当前有效的缓存值。
     *
     * @return 当前有效的缓存值；缓存为空或已过期时返回 {@code null}
     */
    public @Nullable P get() {
        ValueExpirationPair<P> valueExpirationPair = cachedValueReference.get();
        if (valueExpirationPair == null) {
            return null;
        }

        if (System.currentTimeMillis() >= valueExpirationPair.expiresAt()) {
            cachedValueReference.compareAndSet(valueExpirationPair, null);
            return null;
        }
        return valueExpirationPair.value();
    }

    /**
     * 获取当前有效的缓存值，缓存无效时返回给定的备用值。
     *
     * @param fallback 缓存无效时返回的备用值
     * @return 当前有效的缓存值或备用值
     */
    public @Nullable P getOrElse(@Nullable P fallback) {
        P value = get();
        return value == null ? fallback : value;
    }

    /**
     * 获取当前有效的缓存值；缓存无效时调用加载器生成新值并按默认存活周期缓存。
     *
     * @param loader 缓存值加载器，不可返回 {@code null}
     * @return 已缓存或新加载的值
     * @throws NullPointerException 加载器或加载结果为 {@code null} 时
     */
    public P getOrReload(Supplier<? extends P> loader) {
        Objects.requireNonNull(loader, "loader");

        P value = get();
        if (value != null) {
            return value;
        }

        reloadLock.lock();
        try {
            value = get();
            if (value != null) {
                return value;
            }

            P loaded = Objects.requireNonNull(loader.get(), "loader returned null");
            set(loaded);
            return loaded;
        } finally {
            reloadLock.unlock();
        }
    }

    /**
     * 移除当前缓存值。
     */
    public void clear() {
        cachedValueReference.set(null);
    }

    /**
     * 判断当前是否存在有效缓存值。
     *
     * @return 存在有效缓存值时为 {@code true}
     */
    public boolean isAvailable() {
        return get() != null;
    }

    private record ValueExpirationPair<P>(P value, long expiresAt) {
    }
}
