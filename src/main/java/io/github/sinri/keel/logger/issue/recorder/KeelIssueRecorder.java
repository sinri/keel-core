package io.github.sinri.keel.logger.issue.recorder;

import io.github.sinri.keel.logger.event.KeelEventLog;
import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenter;
import io.github.sinri.keel.logger.issue.record.KeelIssueRecord;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

/**
 * The recorder of issues. A special implementation, {@code  KeelIssueRecorder<KeelEventLog>} is just called logger.
 *
 * @param <T> The type of the certain implementation of the issue record used.
 * @since 3.1.10
 * @since 4.0.1 T is strict.
 */
public interface KeelIssueRecorder<T extends KeelIssueRecord<T>>
        extends KeelIssueRecorderCommonMixin<T>, KeelIssueRecorderJsonMixin<T> {
    /**
     * @since 4.0.0
     */
    static <T extends KeelIssueRecord<T>> KeelIssueRecorder<T> buildSilentIssueRecorder() {
        return new SilentIssueRecorder<>();
    }

    static <T extends KeelIssueRecord<T>> KeelIssueRecorder<T> build(
            @Nonnull KeelIssueRecordCenter issueRecordCenter,
            @Nonnull Supplier<T> issueRecordBuilder,
            @Nonnull String topic
    ) {
        return new KeelIssueRecorderImpl<>(issueRecordCenter, issueRecordBuilder, topic);
    }

    /**
     * @since 4.0.0
     */
    default KeelIssueRecorder<KeelEventLog> toEventLogger() {
        return build(
                this.issueRecordCenter(),
                () -> {
                    T t = this.issueRecordBuilder().get();
                    // if null returned, log is ignored.
                    if (t == null) {
                        return null;
                    }
                    return new KeelEventLog(t);
                },
                this.topic()
        );
    }


}
