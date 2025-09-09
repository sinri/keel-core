package io.github.sinri.keel.logger.issue.recorder.adapter;

import io.github.sinri.keel.logger.issue.record.KeelIssueRecord;
import io.github.sinri.keel.logger.issue.recorder.render.KeelIssueRecordRender;
import io.vertx.core.Future;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @since 3.1.10
 */
public class SyncStdoutAdapter implements KeelIssueRecorderAdapter {
    private static final SyncStdoutAdapter instance = new SyncStdoutAdapter();
    private volatile boolean closed = false;

    private SyncStdoutAdapter() {

    }

    public static SyncStdoutAdapter getInstance() {
        return instance;
    }

    @Override
    public KeelIssueRecordRender<String> issueRecordRender() {
        return KeelIssueRecordRender.renderForString();
    }

    @Override
    public void record(@Nonnull String topic, @Nullable KeelIssueRecord<?> issueRecord) {
        if (issueRecord != null && !closed) {
            String s = this.issueRecordRender().renderIssueRecord(topic, issueRecord);
            System.out.println(s);
        }
    }

    @Override
    public Future<Void> gracefullyClose() {
        closed = true;
        return Future.succeededFuture();
    }

    @Override
    public boolean isClosed() {
        return closed;
    }
}
