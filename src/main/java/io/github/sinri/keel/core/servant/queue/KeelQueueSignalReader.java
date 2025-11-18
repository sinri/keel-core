package io.github.sinri.keel.core.servant.queue;

import io.github.sinri.keel.logger.api.logger.SpecificLogger;
import io.vertx.core.Future;

/**
 * 队列状态信号读取器
 *
 * @since 5.0.0
 */
public interface KeelQueueSignalReader {
    SpecificLogger<QueueManageSpecificLog> getQueueManageLogger();

    Future<KeelQueueSignal> readSignal();
}
