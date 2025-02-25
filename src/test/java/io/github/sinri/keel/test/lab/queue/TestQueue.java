package io.github.sinri.keel.test.lab.queue;

import io.github.sinri.keel.core.servant.queue.KeelQueue;
import io.github.sinri.keel.core.servant.queue.KeelQueueSignal;
import io.github.sinri.keel.core.servant.queue.KeelQueueTask;
import io.github.sinri.keel.core.servant.queue.QueueWorkerPoolManager;
import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenter;
import io.vertx.core.Future;

import javax.annotation.Nonnull;
import java.util.UUID;

public class TestQueue extends KeelQueue {
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

    @Override
    public Future<KeelQueueSignal> readSignal() {
        return Future.succeededFuture(KeelQueueSignal.RUN);
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
