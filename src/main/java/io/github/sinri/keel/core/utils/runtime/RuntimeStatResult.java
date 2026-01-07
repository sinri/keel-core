package io.github.sinri.keel.core.utils.runtime;

import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.NullMarked;

/**
 * Represents an interface for tracking runtime statistical results, allowing custom implementations
 * for various system metrics such as memory, CPU, or garbage collection statistics.
 *
 * @param <T> the type of the implementing class, used for comparison and data transformation.
 * @since 5.0.0
 */
@NullMarked
public interface RuntimeStatResult<T> {
    long statTime();

    /**
     * 获得与早先时间统计结果的对比结果
     *
     * @param start 早先时间统计的结果
     * @return 对比
     */
    T since(T start);

    JsonObject toJsonObject();
}
