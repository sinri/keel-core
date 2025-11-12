package io.github.sinri.keel.core.servant.queue;

import io.github.sinri.keel.logger.api.issue.IssueRecorder;
import io.vertx.core.Future;

/**
 * @since 4.0.0
 */
public interface KeelQueueSignalReader {
    /**
     * @since 4.0.0
     */
    IssueRecorder<QueueManageIssueRecord> getQueueManageIssueRecorder();

    Future<KeelQueueSignal> readSignal();
}
