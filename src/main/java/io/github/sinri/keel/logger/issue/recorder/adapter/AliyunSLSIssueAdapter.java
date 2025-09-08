package io.github.sinri.keel.logger.issue.recorder.adapter;

import io.github.sinri.keel.logger.issue.record.KeelIssueRecord;
import io.github.sinri.keel.logger.issue.recorder.render.KeelIssueRecordRender;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

/**
 * @since 3.1.10
 */
abstract public class AliyunSLSIssueAdapter implements KeelIssueRecorderAdapter {
    private final Map<String, Queue<KeelIssueRecord<?>>> issueRecordQueueMap = new ConcurrentHashMap<>();
    private final AtomicReference<CountDownLatch> countDownLatchAtomicReference = new AtomicReference<>(null);

    @Override
    public void record(@Nonnull String topic, @Nullable KeelIssueRecord<?> issueRecord) {
        if (issueRecord != null) {
            this.fetchQueue(topic).add(issueRecord);
        }
    }

    @Nonnull
    private Queue<KeelIssueRecord<?>> fetchQueue(@Nonnull String topic) {
        return this.issueRecordQueueMap.computeIfAbsent(topic, x -> new ConcurrentLinkedQueue<>());
    }

    public final void start() {
        Future.succeededFuture()
              .compose(v1 -> {
                  countDownLatchAtomicReference.set(new CountDownLatch(1));
                  return Future.succeededFuture();
              })
              .compose(v2 -> {
                  return Keel.asyncCallRepeatedly(routineResult -> {
                      if (isStopped()) {
                          Keel.getLogger().warning("AliyunSLSIssueAdapter routine to stop");
                          routineResult.stop();
                          return Future.succeededFuture();
                      }

                      Set<String> topics = Collections.unmodifiableSet(this.issueRecordQueueMap.keySet());
                      return Keel.asyncCallIteratively(topics, this::handleForTopic)
                                 .compose(v -> {
                                     AtomicLong total = new AtomicLong(0);
                                     return Keel.asyncCallIteratively(topics, topic -> {
                                                    total.addAndGet(this.issueRecordQueueMap.get(topic).size());
                                                    return Future.succeededFuture();
                                                })
                                                .compose(vv -> {
                                                    if (total.get() == 0) {
                                                        return Keel.asyncSleep(500L);
                                                    } else {
                                                        return Future.succeededFuture();
                                                    }
                                                });
                                 });
                  });
              })
              .onFailure(throwable -> Keel.getLogger().exception(throwable, "AliyunSLSIssueAdapter routine exception"))
              .andThen(ar -> {
                  countDownLatchAtomicReference.get().countDown();
              });
    }


    /**
     * Waits for a recording process to complete by blocking until a {@link CountDownLatch} associated with
     * the recording process reaches zero or is interrupted. If no latch is present, no operation occurs.
     * <p>
     * This method retrieves the {@link CountDownLatch} instance from an atomic reference. If the latch exists,
     * it invokes the {@code await()} method on the latch, blocking the current thread until the latch reaches
     * a zero count or the thread is interrupted. If the thread is interrupted during waiting, the interruption
     * is logged using the Keel logging system.
     * <p>
     * This method is typically used to synchronize or ensure that recording-related operations are complete
     * before proceeding further in the application logic.
     *
     * @since 4.1.3
     */
    protected final void awaitRecording() {
        CountDownLatch countDownLatch = countDownLatchAtomicReference.get();
        if (countDownLatch != null) {
            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                Keel.getLogger().exception(e, "Awaiting recording interrupted");
            }
        }
    }

    protected int bufferSize() {
        return 1000;
    }

    private Future<Void> handleForTopic(@Nonnull final String topic) {
        Queue<KeelIssueRecord<?>> keelIssueRecords = this.issueRecordQueueMap.get(topic);
        List<KeelIssueRecord<?>> buffer = new ArrayList<>();
        while (true) {
            KeelIssueRecord<?> x = keelIssueRecords.poll();
            if (x == null) {
                break;
            }
            buffer.add(x);
            if (buffer.size() >= bufferSize()) {
                break;
            }
        }
        if (buffer.isEmpty()) return Future.succeededFuture();
        return handleIssueRecordsForTopic(topic, buffer);
    }

    abstract protected Future<Void> handleIssueRecordsForTopic(@Nonnull final String topic,
                                                               @Nonnull final List<KeelIssueRecord<?>> buffer);

    @Override
    public KeelIssueRecordRender<JsonObject> issueRecordRender() {
        return KeelIssueRecordRender.renderForJsonObject();
    }

}
