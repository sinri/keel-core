package io.github.sinri.keel.core.maids.gatling;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import org.jspecify.annotations.NullMarked;

import java.util.Set;

/**
 * 多管加特林的任务。
 *
 * @since 5.0.0
 */
@NullMarked
abstract public class Bullet {
    abstract public String bulletID();

    abstract protected Set<String> exclusiveLockSet();

    abstract protected Future<Object> fire();

    abstract protected Future<Void> ejectShell(AsyncResult<Object> fired);
}
