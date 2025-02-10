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
import java.util.concurrent.atomic.AtomicLong;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

/**
 * @since 3.1.10
 */
abstract public class AliyunSLSIssueAdapter implements KeelIssueRecorderAdapter {
    private final Map<String, Queue<KeelIssueRecord<?>>> issueRecordQueueMap = new ConcurrentHashMap<>();


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
        Keel.asyncCallRepeatedly(routineResult -> {
                if (isStopped()) {
                    Keel.getIssueRecorder().warning("AliyunSLSIssueAdapter routine to stop");
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
            })
            .onFailure(throwable -> {
                Keel.getIssueRecorder().exception(throwable, "AliyunSLSIssueAdapter routine exception");
            });
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
