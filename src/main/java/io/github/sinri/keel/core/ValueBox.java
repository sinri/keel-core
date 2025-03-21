package io.github.sinri.keel.core;

import javax.annotation.Nullable;

/**
 * A generic container for a value that can be set with an optional expiration time.
 * <p>
 * The class allows setting and getting the value, checking if the value is set,
 * and clearing the value. If an expiration time is provided, the value will
 * be automatically cleared after the specified duration.
 *
 * @param <T> the type of the value to be stored in the box
 * @since 3.0.19
 */
public class ValueBox<T> {
    private T value;
    private boolean valueAlreadySet;
    /**
     * @since 3.1.0
     *         When expire is equal or less than zero, never expire;
     *         Or as the milliseconds to reserve the value.
     */
    private long expire = 0;

    /**
     * Constructs a new instance of ValueBox and clears its internal state.
     * <p>
     * This constructor initializes the ValueBox with no value, setting the internal
     * state to indicate that no value is set and any expiration time is reset.
     */
    public ValueBox() {
        this.clear();
    }

    /**
     * Constructs a new instance of ValueBox with the specified value and no expiration time.
     *
     * @param value the initial value to be stored in the box, can be null
     */
    public ValueBox(@Nullable T value) {
        this(value, 0);
    }

    /**
     * Constructs a new instance of ValueBox with the specified value and an optional expiration time.
     *
     * @param value    the initial value to be stored in the box, can be null
     * @param lifetime the duration in milliseconds after which the value will expire and be cleared;
     *                 if 0 or negative, the value will not expire
     */
    public ValueBox(@Nullable T value, long lifetime) {
        this.setValue(value, lifetime);
    }

    public ValueBox<T> clear() {
        this.value = null;
        this.valueAlreadySet = false;
        this.expire = 0;
        return this;
    }

    /**
     * If the value is already set, and, not expired if lifetime declared.
     * When checked, if the value set but expired, it would be cleaned.
     */
    public boolean isValueAlreadySet() {
        if (!valueAlreadySet) return false;
        if (expire <= 0) {
            return true;
        }
        if (expire > System.currentTimeMillis()) {
            return true;
        }
        this.clear();
        return false;
    }

    /**
     * Retrieves the value stored in the ValueBox if it has been set and not expired.
     *
     * @return the value of type T if it is already set and not expired, otherwise throws an
     *         {@link IllegalStateException}
     */
    @Nullable
    public T getValue() {
        if (isValueAlreadySet()) return value;
        else throw new IllegalStateException("Value is not set yet");
    }

    /**
     * Sets the value in the ValueBox with no expiration time.
     *
     * @param value the value to be stored in the box, can be null
     * @return the current instance of ValueBox for method chaining
     */
    public ValueBox<T> setValue(@Nullable T value) {
        return this.setValue(value, 0);
    }

    /**
     * Retrieves the value stored in the ValueBox if it has been set and not expired.
     * If the value is not set or has expired, returns the provided fallback value.
     *
     * @param fallbackForInvalid the value to return if the stored value is not set or has expired
     * @return the value of type T if it is already set and not expired, otherwise the fallback value
     * @since 3.1.0
     */
    @Nullable
    public T getValueOrElse(@Nullable T fallbackForInvalid) {
        if (isValueAlreadySet()) return value;
        else return fallbackForInvalid;
    }

    /**
     * Sets the value in the ValueBox with an optional expiration time.
     *
     * @param value     the value to be stored in the box, can be null
     * @param lifetime  the duration in milliseconds after which the value will expire and be cleared;
     *                  if 0 or negative, the value will not expire
     * @return the current instance of ValueBox for method chaining
     */
    public ValueBox<T> setValue(@Nullable T value, long lifetime) {
        this.value = value;
        this.valueAlreadySet = true;
        if (lifetime > 0) {
            this.expire = System.currentTimeMillis() + lifetime;
        } else {
            this.expire = 0;
        }
        return this;
    }

    /**
     * Checks if the value in the ValueBox is set to null.
     *
     * @return true if the value is set and is null, false otherwise
     */
    public boolean isValueSetToNull() {
        return this.isValueAlreadySet() && this.getValue() == null;
    }
}
