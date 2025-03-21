package io.github.sinri.keel.core.verticles;

import io.vertx.core.Future;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

/**
 * A concrete implementation of the {@link KeelVerticle} interface, extending from {@link KeelVerticleImpl}.
 * This class is designed to wrap a supplier that provides a future representing the start operation of the verticle.
 * <p>
 * The primary responsibility of this class is to manage the startup process by delegating the start operation to the
 * provided supplier. It ensures that the `startVerticle` method, which is part of the lifecycle management, calls the
 * supplier to get the future and completes the startup process.
 */ /*
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
