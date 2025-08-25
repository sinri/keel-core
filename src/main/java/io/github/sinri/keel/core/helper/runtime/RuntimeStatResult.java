package io.github.sinri.keel.core.helper.runtime;

import io.vertx.core.json.JsonObject;

/**
 * Represents an interface for tracking runtime statistical results, allowing custom implementations
 * for various system metrics such as memory, CPU, or garbage collection statistics.
 *
 * @param <T> the type of the implementing class, used for comparison and data transformation.
 * @since 2.9.4
 */
public interface RuntimeStatResult<T> {
    long getStatTime();

    T since(T start);

    JsonObject toJsonObject();
}
