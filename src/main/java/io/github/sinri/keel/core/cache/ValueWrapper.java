package io.github.sinri.keel.core.cache;

import javax.annotation.Nullable;
import java.lang.ref.SoftReference;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A wrapper class that holds a value with a limited lifetime.
 * The value is stored using a SoftReference to allow garbage collection when memory is low.
 *
 * @param <P> The type of value to be wrapped
 * @since 2.5 moved from inner class to here
 * @since 3.2.15 value use SoftReference
 */
public class ValueWrapper<P> {
    private final SoftReference<P> value;
    private final long death;
    private final long birth;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * Creates a new ValueWrapper with the specified value and lifetime.
     *
     * @param value The value to wrap
     * @param lifeInSeconds The lifetime of the value in seconds
     */
    public ValueWrapper(P value, long lifeInSeconds) {
        this.value = new SoftReference<>(value);
        this.birth = System.currentTimeMillis();
        this.death = this.birth + lifeInSeconds * 1000L;
    }

    /**
     * Gets the birth timestamp of the value.
     *
     * @return The birth timestamp in milliseconds
     */
    public long getBirth() {
        return birth;
    }

    /**
     * Gets the death timestamp of the value.
     *
     * @return The death timestamp in milliseconds
     */
    public long getDeath() {
        return death;
    }

    /**
     * Gets the remaining lifetime of the value in milliseconds.
     *
     * @return The remaining lifetime in milliseconds, or 0 if the value is dead
     */
    public long getRemainingLifetime() {
        long now = System.currentTimeMillis();
        return Math.max(0, death - now);
    }

    private boolean isInAlivePeriod() {
        long now = System.currentTimeMillis();
        boolean alive = now < this.death && now >= this.birth;
        if (!alive) {
            lock.writeLock().lock();
            try {
                value.clear();
            } finally {
                lock.writeLock().unlock();
            }
        }
        return alive;
    }

    /**
     * Gets the wrapped value if it is still alive and available.
     *
     * @return The wrapped value, or null if the value is dead or has been garbage collected
     */
    @Nullable
    public P getValue() {
        lock.readLock().lock();
        try {
            if (isInAlivePeriod()) {
                return value.get();
            }
            return null;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Checks if the value is currently available.
     * A value is available if it is within its lifetime period and has not been garbage collected.
     *
     * @return true if the value is available, false otherwise
     */
    public boolean isAvailable() {
        lock.readLock().lock();
        try {
            if (isInAlivePeriod()) {
                return getValue() != null;
            }
            return false;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Checks if the value is not available.
     * A value is not available if it is outside its lifetime period or has been garbage collected.
     *
     * @return true if the value is not available, false otherwise
     * @since 4.0.2
     */
    public boolean isNotAvailable() {
        return !isAvailable();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ValueWrapper<?> that = (ValueWrapper<?>) o;
        return death == that.death && birth == that.birth && Objects.equals(value.get(), that.value.get());
    }

    @Override
    public int hashCode() {
        return Objects.hash(value.get(), death, birth);
    }
}
