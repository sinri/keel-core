package io.github.sinri.keel.core.maids.watchman;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.NullMarked;

/**
 * 更夫的事件处理器。
 *
 * @since 5.0.0
 */
@NullMarked
public interface WatchmanEventHandler extends Handler<Long> {
    default JsonObject config() {
        return new JsonObject();
    }
}
