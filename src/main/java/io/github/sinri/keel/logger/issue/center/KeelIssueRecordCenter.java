package io.github.sinri.keel.logger.issue.center;

import io.github.sinri.keel.logger.event.KeelEventLog;
import io.github.sinri.keel.logger.event.KeelEventLogger;
import io.github.sinri.keel.logger.issue.record.KeelIssueRecord;
import io.github.sinri.keel.logger.issue.recorder.KeelIssueRecorder;
import io.github.sinri.keel.logger.issue.recorder.adapter.KeelIssueRecorderAdapter;
import io.github.sinri.keel.logger.issue.recorder.adapter.SilentAdapter;
import io.github.sinri.keel.logger.issue.recorder.adapter.SyncStdoutAdapter;
import io.vertx.core.Handler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Supplier;

/**
 * @since 3.1.10 Technical Preview
 */
public interface KeelIssueRecordCenter {
    /**
     * @since 3.2.7 Use a static singleton impl.
     */
    static KeelIssueRecordCenter outputCenter() {
        //return KeelIssueRecordCenterAsSync.getInstanceWithStdout();
        return build(SyncStdoutAdapter.getInstance());
    }

    static KeelIssueRecordCenter silentCenter() {
        //return KeelIssueRecordCenterAsSilent.getInstance();
        return build(SilentAdapter.getInstance());
    }

    @Deprecated(since = "4.0.0", forRemoval = true)
    static <X extends KeelIssueRecord<?>> KeelIssueRecorder<X> createSilentIssueRecorder() {
        return silentCenter().generateIssueRecorder("Silent", () -> null);
    }

    /**
     * @param adapter the KeelIssueRecorderAdapter instance.
     * @return the built KeelIssueRecordCenter instance.
     * @since 4.0.0
     */
    static KeelIssueRecordCenter build(@Nonnull KeelIssueRecorderAdapter adapter) {
        return new KeelIssueRecordCenter() {
            @Nonnull
            @Override
            public KeelIssueRecorderAdapter getAdapter() {
                return adapter;
            }
        };
    }

    @Nonnull
    KeelIssueRecorderAdapter getAdapter();

    /**
     * @param issueRecordBuilder Sample for silent: {@code Supplier<T> issueRecordBuilder= () -> null;}
     */
    @Nonnull
    default <T extends KeelIssueRecord<?>> KeelIssueRecorder<T> generateIssueRecorder(@Nonnull String topic, @Nonnull Supplier<T> issueRecordBuilder) {
        return KeelIssueRecorder.build(this, issueRecordBuilder, topic);
    }

    @Nonnull
    default KeelEventLogger generateEventLogger(@Nonnull String topic) {
        return KeelEventLogger.from(generateIssueRecorderForEventLogger(topic));
    }

    @Nonnull
    default KeelEventLogger generateEventLogger(@Nonnull String topic, @Nullable Handler<KeelEventLog> templateEventLogEditor) {
        return KeelEventLogger.from(generateIssueRecorderForEventLogger(topic), templateEventLogEditor);
    }

    @Nonnull
    default KeelIssueRecorder<KeelEventLog> generateIssueRecorderForEventLogger(@Nonnull String topic) {
        return generateIssueRecorder(topic, () -> new KeelEventLog(topic));
    }

}
