package io.github.sinri.keel.core.cache;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.lang.ref.SoftReference;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A wrapper class that holds a value with a limited lifetime.
 * The value is stored using a SoftReference to allow garbage collection when memory is low.
 *
 * @since 5.0.0
 */
@NullMarked
class ValueWrapper<P> {
    private final SoftReference<P> value;
    private final long death;
    private final long birth;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * Creates a new ValueWrapper with the specified value and lifetime.
     *
     * @param value         The value to wrap
     * @param lifeInSeconds The lifetime of the value in seconds
     */
    public ValueWrapper(P value, long lifeInSeconds) {
        this.value = new SoftReference<>(value);
        this.birth = System.currentTimeMillis();
        this.death = computeDeath(this.birth, lifeInSeconds);
    }

    /**
     * Computes the expiration timestamp from a birth timestamp and a positive lifetime in seconds.
     * <p>
     * The lifetime must be greater than zero. The calculation is checked in both steps: converting seconds to
     * milliseconds and adding the result to the birth timestamp. If either step would overflow a {@code long}, this
     * method rejects the lifetime instead of returning an invalid expiration timestamp.
     *
     * @param birth         the birth timestamp in milliseconds
     * @param lifeInSeconds the positive lifetime in seconds
     * @return the expiration timestamp in milliseconds
     * @throws IllegalArgumentException when {@code lifeInSeconds} is not positive, or when the computed expiration
     *                                  timestamp would overflow
     */
    static long computeDeath(long birth, long lifeInSeconds) {
        if (lifeInSeconds <= 0) {
            throw new IllegalArgumentException("lifeInSeconds must be greater than 0");
        }
        try {
            long lifeInMillis = Math.multiplyExact(lifeInSeconds, 1000L);
            return Math.addExact(birth, lifeInMillis);
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException("lifeInSeconds is too large", e);
        }
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

    /**
     * Checks if the current time is within the value's alive period.
     *
     * @return true if the current time is within the value's alive period, false otherwise
     */
    private boolean isInAlivePeriod() {
        long now = System.currentTimeMillis();
        return now < this.death && now >= this.birth;
    }

    /**
     * @return the value read
     */
    @Nullable
    private P readWithReadLock() {
        lock.readLock().lock();
        try {
            return value.get();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Clears the value if it's not in alive period, should be called when {@link ValueWrapper#isInAlivePeriod()} is
     * checked as not true.
     * This method acquires a write lock and should not be called while holding a read lock.
     *
     */
    private void clearWithWriteLock() {
        lock.writeLock().lock();
        try {
            value.clear();
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Gets the wrapped value if it is still alive and available.
     *
     * @return The wrapped value, or null if the value is dead or has been garbage collected
     */
    @Nullable
    public P getValue() {
        if (!isInAlivePeriod()) {
            // with write lock if it needs
            clearWithWriteLock();
            return null;
        } else {
            // with read lock
            return readWithReadLock();
        }
    }

    /**
     * Checks if the value is currently available.
     * A value is available if it is within its lifetime period and has not been garbage collected.
     *
     * @return true if the value is available, false otherwise
     */
    public boolean isAvailable() {
        return getValue() != null;
    }

    /**
     * Checks if the value is not available.
     * A value is not available if it is outside its lifetime period or has been garbage collected.
     *
     * @return true if the value is not available, false otherwise
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
