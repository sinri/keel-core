package io.github.sinri.keel.core.servant.intravenous;

import io.github.sinri.keel.core.verticles.KeelVerticleImpl;
import io.github.sinri.keel.facade.async.KeelAsyncMixin;
import io.vertx.core.Future;
import io.vertx.core.Promise;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

/**
 * @since 4.0.7
 * @param <D>
 */
abstract class KeelIntravenousBase<D> extends KeelVerticleImpl implements KeelIntravenous<D> {
    private final Queue<D> queue;
    private final AtomicReference<Promise<Void>> interrupterRef = new AtomicReference<>();
    private final AtomicBoolean stoppedRef = new AtomicBoolean(false);

    public KeelIntravenousBase() {
        this.queue = new ConcurrentLinkedQueue<>();
    }

    @Override
    public void add(D drop) {
        if (stoppedRef.get()) {
            throw new IllegalStateException("Can't add drop to a stopped intravenous");
        }
        synchronized (queue) {
            queue.add(drop);
        }
        Promise<Void> interrupter = interrupterRef.get();
        if (interrupter != null) {
            interrupter.tryComplete();
        }
    }

    @Override
    public void shutdown() {
        stoppedRef.set(true);
    }

    @Override
    protected Future<Void> startVerticle() {
        this.interrupterRef.set(null);
        Keel.asyncCallRepeatedly(this::handleRoutine)
            .onComplete(ar -> {
                this.undeployMe();
            });
        return Future.succeededFuture();
    }

    private Future<Void> handleRoutine(KeelAsyncMixin.RepeatedlyCallTask repeatedlyCallTask) {
        this.interrupterRef.set(Promise.promise());

        boolean toStop = this.stoppedRef.get();

        List<D> buffer = new ArrayList<>();
        if (!queue.isEmpty()) {
            synchronized (queue) {
                while (true) {
                    D drop = queue.poll();
                    if (drop != null) {
                        buffer.add(drop);
                    } else {
                        break;
                    }
                }
            }
        }
        return Future.succeededFuture()
                     .compose(v -> {
                         if (!buffer.isEmpty()) {
                             return handleDrops(buffer);
                         } else {
                             return Future.succeededFuture();
                         }
                     })
                     .compose(v -> {
                         if (toStop) {
                             repeatedlyCallTask.stop();
                             return Future.succeededFuture();
                         } else {
                             // wait for next `add` call, or just sleep 1 second
                             return Future.any(
                                                  Keel.asyncSleep(1000L),
                                                  this.interrupterRef.get().future()
                                          )
                                          .compose(compositeFuture -> {
                                              return Future.succeededFuture();
                                          });
                         }
                     });
    }

    abstract protected Future<Void> handleDrops(List<D> drops);
}
