package io.github.sinri.keel.logger.metric;

import io.vertx.core.Future;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

/**
 * @since 3.1.9 Technical Preview
 */
abstract public class KeelMetricRecorder {
    private final AtomicBoolean endSwitch = new AtomicBoolean(false);
    private final Queue<KeelMetricRecord> metricRecordQueue = new ConcurrentLinkedQueue<>();

    public void recordMetric(KeelMetricRecord metricRecord) {
        this.metricRecordQueue.add(metricRecord);
    }

    protected int bufferSize() {
        return 1000;
    }

    /**
     * Override this to change topic of metric recorder.
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

    public void end() {
        endSwitch.set(true);
    }

    abstract protected Future<Void> handleForTopic(String topic, List<KeelMetricRecord> buffer);
}
