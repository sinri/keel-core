package io.github.sinri.keel.logger.issue.recorder;

import io.github.sinri.keel.logger.KeelLogLevel;
import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenter;
import io.github.sinri.keel.logger.issue.record.KeelIssueRecord;
import io.vertx.core.Handler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * @since 4.0.0
 */
public final class SilentIssueRecorder<T extends KeelIssueRecord<T>> implements KeelIssueRecorder<T> {
    private final List<KeelIssueRecorder<T>> bypassIssueRecorders = new ArrayList<>();

    @Nonnull
    @Override
    public KeelLogLevel getVisibleLevel() {
        return KeelLogLevel.SILENT;
    }

    @Override
    public void setVisibleLevel(@Nonnull KeelLogLevel level) {

    }

    @Nonnull
    @Override
    public KeelIssueRecordCenter issueRecordCenter() {
        return KeelIssueRecordCenter.silentCenter();
    }

    @Nonnull
    @Override
    public Supplier<T> issueRecordBuilder() {
        return () -> null;
    }

    @Override
    public void addBypassIssueRecorder(@Nonnull KeelIssueRecorder<T> bypassIssueRecorder) {
        this.bypassIssueRecorders.add(bypassIssueRecorder);
    }

    @Nonnull
    @Override
    public List<KeelIssueRecorder<T>> getBypassIssueRecorders() {
        return bypassIssueRecorders;
    }

    @Nonnull
    @Override
    public String topic() {
        return "silent";
    }

    @Nullable
    @Override
    public Handler<T> getRecordFormatter() {
        return null;
    }

    @Override
    public void setRecordFormatter(@Nullable Handler<T> handler) {

    }

    @Override
    public void record(@Nonnull Handler<T> issueHandler) {
        // do nothing
    }
}
