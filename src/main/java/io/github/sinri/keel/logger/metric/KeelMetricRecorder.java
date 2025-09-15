package io.github.sinri.keel.logger.metric;

import io.vertx.core.Future;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

/**
 * @since 3.1.9 Technical Preview
 */
abstract public class KeelMetricRecorder implements Closeable {
    private final AtomicBoolean endSwitch = new AtomicBoolean(false);
    private final Queue<KeelMetricRecord> metricRecordQueue = new ConcurrentLinkedQueue<>();

    public void recordMetric(KeelMetricRecord metricRecord) {
        this.metricRecordQueue.add(metricRecord);
    }

    protected int bufferSize() {
        return 1000;
    }

    /**
     * Override this to change the topic of metric recorder.
     *
     * @since 4.0.0
     */
    protected String topic() {
        return "metric";
    }

    public void start() {
        Keel.asyncCallRepeatedly(routineResult -> Future.succeededFuture()
                                                        .compose(v -> {
                                                            List<KeelMetricRecord> buffer = new ArrayList<>();

                                                            while (true) {
                                                                KeelMetricRecord metricRecord = metricRecordQueue.poll();
                                                                if (metricRecord == null) break;

                                                                buffer.add(metricRecord);
                                                                if (buffer.size() >= bufferSize()) break;
                                                            }

                                                            if (buffer.isEmpty()) {
                                                                if (endSwitch.get()) {
                                                                    routineResult.stop();
                                                                    return Future.succeededFuture();
                                                                }
                                                                return Keel.asyncSleep(1000L);
                                                            } else {
                                                                // since 4.0.0 no various topics supported.
                                                                //                            Map<String, List<KeelMetricRecord>> map = groupByTopic(buffer);
                                                                //                            return Keel.asyncCallIteratively(map.keySet(), topic -> {
                                                                //                                return handleForTopic(topic, map.get(topic));
                                                                //                            });

                                                                return handleForTopic(topic(), buffer);
                                                            }
                                                        }));
    }

    /**
     * Marks the end of the metric recording process and closes any associated resources.
     * This method is deprecated and should not be used in newer implementations. Use
     * the {@link #close()} method instead to properly terminate the metric recorder.
     *
     * @throws IOException if an I/O error occurs during resource closure
     * @deprecated since version 4.1.3, use {@link #close()} instead
     */
    @Deprecated(since = "4.1.3", forRemoval = true)
    public final void end() throws IOException {
        close();
    }

    /**
     * Closes the metric recorder by marking the end of metric recording operations.
     * Once this method is invoked, the recorder will stop processing and no additional
     * metrics will be recorded.
     * <p>
     * This method should be called to properly terminate the recorder's lifecycle
     * and release associated resources.
     * <p>
     * The override method should call the super method to ensure proper resource closure.
     *
     * @throws IOException if an I/O error occurs during the close operation
     * @since 4.1.3
     */
    @Override
    public void close() throws IOException {
        endSwitch.set(true);
    }

    abstract protected Future<Void> handleForTopic(String topic, List<KeelMetricRecord> buffer);
}
