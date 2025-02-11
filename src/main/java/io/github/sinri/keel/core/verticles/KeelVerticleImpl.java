package io.github.sinri.keel.core.verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;

/**
 * @since 4.0.2 remove the logging embeddings.
 */
public abstract class KeelVerticleImpl extends AbstractVerticle implements KeelVerticle {


    @Override
    public final void start() {
    }

    @Override
    public final void start(Promise<Void> startPromise) {
        Future.succeededFuture()
              .compose(v -> {
                  start();
                  return startVerticle();
              })
              .andThen(ar -> {
                  if (ar.succeeded()) {
                      startPromise.complete();
                  } else {
                      startPromise.fail(ar.cause());
                  }
              });
    }

    protected abstract Future<Void> startVerticle();
}
