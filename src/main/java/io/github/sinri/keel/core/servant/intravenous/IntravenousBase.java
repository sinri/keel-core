package io.github.sinri.keel.core.servant.intravenous;

import io.github.sinri.keel.base.async.RepeatedlyCallTask;
import io.github.sinri.keel.core.utils.value.ValueBox;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import org.jspecify.annotations.NullMarked;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * 静脉注入的基本实现。
 *
 * @since 5.0.0
 */
@NullMarked
abstract class IntravenousBase<D> extends Intravenous<D> {
    private final Queue<D> queue;
    private final ValueBox<Promise<Void>> interrupterBox = new ValueBox<>();
    private final AtomicBoolean stoppedRef = new AtomicBoolean(false);

    public IntravenousBase() {
        super();
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
        Promise<Void> interrupter = interrupterBox.getValue();
        if (interrupter != null) {
            interrupter.tryComplete();
        }
    }

    @Override
    public void shutdown() {
        stoppedRef.set(true);
    }

    @Override
    public boolean isNoDropsLeft() {
        return queue.isEmpty();
    }

    @Override
    public boolean isStopped() {
        return stoppedRef.get();
    }

    @Override
    protected Future<Void> startVerticle() {
        this.interrupterBox.setValue(null);
        asyncCallRepeatedly(this::handleRoutine)
                .onComplete(ar -> this.undeployMe());
        return Future.succeededFuture();
    }

    private Future<Void> handleRoutine(RepeatedlyCallTask repeatedlyCallTask) {
        this.interrupterBox.setValue(Promise.promise());

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
                             Promise<Void> promise = this.interrupterBox.getNonNullValue();
                             if (promise == null) {
                                 return asyncSleep(1000L);
                             } else {
                                 return Future.any(
                                                      asyncSleep(1000L),
                                                      promise.future()
                                              )
                                              .compose(compositeFuture -> Future.succeededFuture());
                             }
                         }
                     });
    }

    abstract protected Future<Void> handleDrops(List<D> drops);
}
