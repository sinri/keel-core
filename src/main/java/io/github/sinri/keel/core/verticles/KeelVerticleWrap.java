package io.github.sinri.keel.core.verticles;

import io.vertx.core.Future;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

/**
 * @since 4.0.2
 */
public final class KeelVerticleWrap extends KeelVerticleImpl {

    @Nonnull
    private final Supplier<Future<Void>> startFutureSupplier;

    KeelVerticleWrap(@Nonnull Supplier<Future<Void>> startFutureSupplier) {
        this.startFutureSupplier = startFutureSupplier;
    }

    @Override
    protected Future<Void> startVerticle() {
        return this.startFutureSupplier.get();
    }
}
