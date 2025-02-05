package io.github.sinri.keel.logger.event;

import io.github.sinri.keel.logger.KeelLogLevel;
import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenter;
import io.github.sinri.keel.logger.issue.recorder.KeelIssueRecorder;
import io.vertx.core.Handler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * @since 3.2.0 The brand new KeelEventLogger based on KeelIssueRecorder.
 * @since 4.0.0 refine to class
 */
public final class KeelEventLogger implements KeelIssueRecorder<KeelEventLog> {
    private final @Nonnull KeelIssueRecordCenter issueRecordCenter;
    private KeelLogLevel level;
    private final @Nonnull String topic;
    private final List<KeelIssueRecorder<KeelEventLog>> bypassIssueRecorders;
    private @Nullable Handler<KeelEventLog> recordFormatter;

    public KeelEventLogger(
            @Nonnull KeelIssueRecordCenter issueRecordCenter,
            @Nullable Handler<KeelEventLog> recordFormatter,
            @Nonnull String topic
    ) {
        this.issueRecordCenter = issueRecordCenter;
        this.level = KeelLogLevel.INFO;
        this.topic = topic;
        this.bypassIssueRecorders = new ArrayList<>();
    }

    public static KeelEventLogger from(@Nonnull KeelIssueRecorder<KeelEventLog> issueRecorder) {
        return new KeelEventLogger(issueRecorder.issueRecordCenter(), null, issueRecorder.topic());
    }

    public static KeelEventLogger from(@Nonnull KeelIssueRecorder<KeelEventLog> issueRecorder, @Nullable Handler<KeelEventLog> recordFormatter) {
        return new KeelEventLogger(issueRecorder.issueRecordCenter(), recordFormatter, issueRecorder.topic());
    }

    @Nonnull
    @Override
    public KeelLogLevel getVisibleLevel() {
        return level;
    }

    @Override
    public void setVisibleLevel(@Nonnull KeelLogLevel level) {
        this.level = level;
    }

    @Nonnull
    @Override
    public KeelIssueRecordCenter issueRecordCenter() {
        return this.issueRecordCenter;
    }

    @Nonnull
    @Override
    public Supplier<KeelEventLog> issueRecordBuilder() {
        return KeelEventLog::new;
    }

    @Override
    public void addBypassIssueRecorder(@Nonnull KeelIssueRecorder<KeelEventLog> bypassIssueRecorder) {
        this.bypassIssueRecorders.add(bypassIssueRecorder);
    }

    @Nonnull
    @Override
    public List<KeelIssueRecorder<KeelEventLog>> getBypassIssueRecorders() {
        return this.bypassIssueRecorders;
    }

    @Nonnull
    @Override
    public String topic() {
        return topic;
    }

    @Nullable
    @Override
    public Handler<KeelEventLog> getRecordFormatter() {
        return this.recordFormatter;
    }

    @Override
    public void setRecordFormatter(@Nullable Handler<KeelEventLog> handler) {
        this.recordFormatter = handler;
    }
}
