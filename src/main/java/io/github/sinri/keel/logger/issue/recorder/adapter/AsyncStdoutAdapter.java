package io.github.sinri.keel.logger.issue.recorder.adapter;

import io.github.sinri.keel.core.servant.intravenous.KeelIntravenous;
import io.github.sinri.keel.logger.issue.record.KeelIssueRecord;
import io.github.sinri.keel.logger.issue.recorder.render.KeelIssueRecordRender;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.ThreadingModel;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

/**
 * @since 3.1.10
 */
public class AsyncStdoutAdapter implements KeelIssueRecorderAdapter {
    private static final AsyncStdoutAdapter instance = new AsyncStdoutAdapter();
    private final KeelIntravenous<WrappedIssueRecord> intravenous;
    private volatile boolean closed;

    private AsyncStdoutAdapter() {
        this.intravenous = KeelIntravenous.instantBatch(items -> Keel.asyncCallIteratively(items, item -> this.writeOneIssueRecord(item.topic, item.issueRecord)));
        this.intravenous.deployMe(new DeploymentOptions().setThreadingModel(ThreadingModel.WORKER));
        closed = false;
    }

    public static AsyncStdoutAdapter getInstance() {
        return instance;
    }

    private Future<Void> writeOneIssueRecord(@Nonnull String topic, @Nonnull KeelIssueRecord<?> issueRecord) {
        String s = this.issueRecordRender().renderIssueRecord(topic, issueRecord);
        System.out.println(s);
        return Future.succeededFuture();
    }

    @Override
    public KeelIssueRecordRender<String> issueRecordRender() {
        return KeelIssueRecordRender.renderForString();
    }

    @Override
    public void record(@Nonnull String topic, @Nullable KeelIssueRecord<?> issueRecord) {
        if (issueRecord != null) {
            this.intravenous.add(new WrappedIssueRecord(topic, issueRecord));
        }
    }

    @Override
    public Future<Void> gracefullyClose() {
        return this.intravenous.shutdownAndAwait()
                               .compose(v -> {
                                   closed = true;
                                   return Future.succeededFuture();
                               });
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    /**
     * @since 4.0.0
     */
    private static class WrappedIssueRecord {
        public final KeelIssueRecord<?> issueRecord;
        public final String topic;

        private WrappedIssueRecord(String topic, KeelIssueRecord<?> issueRecord) {
            this.issueRecord = issueRecord;
            this.topic = topic;
        }
    }
}
