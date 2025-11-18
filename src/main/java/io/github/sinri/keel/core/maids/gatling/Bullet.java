package io.github.sinri.keel.core.maids.gatling;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * 多管加特林的任务。
 *
 * @since 5.0.0
 */
abstract public class Bullet {
    @NotNull
    abstract public String bulletID();

    @NotNull
    abstract protected Set<String> exclusiveLockSet();

    @NotNull
    abstract protected Future<Object> fire();

    @NotNull
    abstract protected Future<Void> ejectShell(AsyncResult<Object> fired);
}
