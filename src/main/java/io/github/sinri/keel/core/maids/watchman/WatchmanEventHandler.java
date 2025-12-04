package io.github.sinri.keel.core.maids.watchman;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import org.jetbrains.annotations.NotNull;

/**
 * 更夫的事件处理器。
 *
 * @since 5.0.0
 */
public interface WatchmanEventHandler extends Handler<Long> {
    @NotNull
    default JsonObject config() {
        return new JsonObject();
    }
}
