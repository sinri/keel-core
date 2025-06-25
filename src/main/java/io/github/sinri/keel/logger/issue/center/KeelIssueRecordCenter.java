package io.github.sinri.keel.logger.issue.center;

import io.github.sinri.keel.logger.issue.record.KeelIssueRecord;
import io.github.sinri.keel.logger.issue.recorder.KeelIssueRecorder;
import io.github.sinri.keel.logger.issue.recorder.adapter.KeelIssueRecorderAdapter;
import io.github.sinri.keel.logger.issue.recorder.adapter.SilentAdapter;
import io.github.sinri.keel.logger.issue.recorder.adapter.SyncStdoutAdapter;

import javax.annotation.Nonnull;
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
        //return build(SyncStdoutAdapter.getInstance());
        return Holder.outputCenter;
    }

    static KeelIssueRecordCenter silentCenter() {
        //return KeelIssueRecordCenterAsSilent.getInstance();
        //return build(SilentAdapter.getInstance());
        return Holder.silentCenter;
    }

    /**
     * @param adapter the KeelIssueRecorderAdapter instance.
     * @return the built KeelIssueRecordCenter instance.
     * @since 4.0.0
     */
    static KeelIssueRecordCenter build(@Nonnull KeelIssueRecorderAdapter adapter) {
        return () -> adapter;
    }

    @Nonnull
    KeelIssueRecorderAdapter getAdapter();

    /**
     * @param issueRecordBuilder Sample for silent: {@code Supplier<T> issueRecordBuilder= () -> null;}
     */
    @Nonnull
    default <T extends KeelIssueRecord<T>> KeelIssueRecorder<T> generateIssueRecorder(
            @Nonnull String topic, @Nonnull Supplier<T> issueRecordBuilder
    ) {
        return KeelIssueRecorder.build(this, issueRecordBuilder, topic);
    }

    class Holder {
        final static KeelIssueRecordCenter outputCenter = build(SyncStdoutAdapter.getInstance());
        final static KeelIssueRecordCenter silentCenter = build(SilentAdapter.getInstance());
    }

}
