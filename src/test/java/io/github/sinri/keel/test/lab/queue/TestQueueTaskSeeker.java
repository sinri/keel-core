package io.github.sinri.keel.test.lab.queue;

import io.github.sinri.keel.core.servant.queue.KeelQueueNextTaskSeeker;
import io.github.sinri.keel.core.servant.queue.KeelQueueTask;
import io.github.sinri.keel.core.servant.queue.QueueManageIssueRecord;
import io.github.sinri.keel.logger.issue.recorder.KeelIssueRecorder;
import io.vertx.core.Future;

import java.util.UUID;

public class TestQueueTaskSeeker implements KeelQueueNextTaskSeeker {
    private final KeelIssueRecorder<QueueManageIssueRecord> issueRecorder;

    public TestQueueTaskSeeker(KeelIssueRecorder<QueueManageIssueRecord> issueRecorder) {
        this.issueRecorder = issueRecorder;
    }

    @Override
    public KeelIssueRecorder<QueueManageIssueRecord> getIssueRecorder() {
        return this.issueRecorder;
    }

    @Override
    public Future<KeelQueueTask> seekNextTask() {
        return Future.succeededFuture()
                .compose(v -> {
                    int rest = (int) (10 * Math.random());
                    return Future.succeededFuture(new TestQueueTask(
                            UUID.randomUUID().toString(),
                            rest
                    ));
                });
    }
}
