package io.github.sinri.keel.core;

import javax.annotation.Nonnull;


/**
 * A generic interface that provides a method to get the implementation instance.
 * This is useful for fluent interfaces or mixins where the implementing class
 * needs to return `this` to allow method chaining.
 *
 * @param <T> the type of the implementing class
 * @since 3.1.10
 */
public interface SelfInterface<T> {
    /**
     * @return the implementation instance, such as `this` in implemented class.
     */
    @Nonnull
    T getImplementation();
}
