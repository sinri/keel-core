package io.github.sinri.keel.core.servant.queue;

import io.github.sinri.keel.logger.api.logger.SpecificLogger;
import io.vertx.core.Future;

/**
 * @since 4.0.0
 */
public interface KeelQueueSignalReader {
    /**
     * @since 4.0.0
     */
    SpecificLogger<QueueManageIssueRecord> getQueueManageIssueRecorder();

    Future<KeelQueueSignal> readSignal();
}
