package io.github.sinri.keel.test.lab.queue;

import io.github.sinri.keel.core.servant.queue.KeelQueueTask;
import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenter;
import io.vertx.core.Future;

import javax.annotation.Nonnull;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

public class TestQueueTask extends KeelQueueTask {
    String id;
    int life;

    public TestQueueTask(String id, int life) {
        this.id = id;
        this.life = life;
    }

    @Nonnull
    @Override
    public String getTaskReference() {
        return id;
    }

    @Nonnull
    @Override
    public String getTaskCategory() {
        return "TEST";
    }

    @Override
    protected Future<Void> run() {
        getQueueTaskIssueRecorder().setRecordFormatter(r -> r.context("id", id).context("life", life));

        getQueueTaskIssueRecorder().info(r -> r.message("START"));
        return Keel.asyncSleep(this.life * 1000L)
                .eventually(() -> {
                    getQueueTaskIssueRecorder().info(r -> r.message("END"));
                    return Future.succeededFuture();
                });
    }

    @Override
    protected KeelIssueRecordCenter getIssueRecordCenter() {
        return KeelIssueRecordCenter.outputCenter();
    }
}
