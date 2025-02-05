package io.github.sinri.keel.core.servant.queue;

import io.github.sinri.keel.logger.issue.recorder.KeelIssueRecorder;
import io.vertx.core.Future;

/**
 * @since 4.0.0
 */
public interface KeelQueueSignalReader {
    /**
     * @since 4.0.0
     */
    KeelIssueRecorder<QueueManageIssueRecord> getIssueRecorder();

    Future<KeelQueueSignal> readSignal();
}
