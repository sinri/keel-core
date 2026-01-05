package io.github.sinri.keel.core.servant.queue;

import io.github.sinri.keel.logger.api.logger.SpecificLogger;
import io.vertx.core.Future;
import org.jspecify.annotations.NullMarked;

/**
 * 队列状态信号读取器
 *
 * @since 5.0.0
 */
@NullMarked
public interface QueueSignalReader {
    SpecificLogger<QueueManageSpecificLog> getQueueManageLogger();

    Future<QueueSignal> readSignal();
}
