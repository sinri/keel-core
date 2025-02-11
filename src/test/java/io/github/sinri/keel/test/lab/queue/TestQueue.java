package io.github.sinri.keel.test.lab.queue;

import io.github.sinri.keel.core.servant.queue.*;
import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenter;
import io.github.sinri.keel.logger.issue.recorder.KeelIssueRecorder;
import io.vertx.core.Future;

import javax.annotation.Nonnull;

public class TestQueue extends KeelQueue {
    @Nonnull
    @Override
    protected KeelQueueNextTaskSeeker getNextTaskSeeker() {
        KeelIssueRecorder<QueueManageIssueRecord> issueRecorder = getQueueManageIssueRecorder();
        return new TestQueueTaskSeeker(issueRecorder);
    }

    @Nonnull
    @Override
    protected KeelQueueSignalReader getSignalReader() {
        KeelIssueRecorder<QueueManageIssueRecord> issueRecorder = this.getQueueManageIssueRecorder();
        return new KeelQueueSignalReader() {
            @Override
            public KeelIssueRecorder<QueueManageIssueRecord> getIssueRecorder() {
                return issueRecorder;
            }

            @Override
            public Future<KeelQueueSignal> readSignal() {
                return Future.succeededFuture(KeelQueueSignal.RUN);
            }
        };
    }

    @Nonnull
    @Override
    protected QueueWorkerPoolManager getQueueWorkerPoolManager() {
        return new QueueWorkerPoolManager(3);
    }


    @Override
    protected KeelIssueRecordCenter getIssueRecordCenter() {
        return KeelIssueRecordCenter.outputCenter();
    }
}
