package io.github.sinri.keel.core.maids.watchman;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

/**
 * 更夫的事件处理器。
 * @since 5.0.0
 */
public interface KeelWatchmanEventHandler extends Handler<Long> {
    default JsonObject config() {
        return new JsonObject();
    }
}
