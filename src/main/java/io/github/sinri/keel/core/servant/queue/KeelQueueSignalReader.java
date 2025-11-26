package io.github.sinri.keel.core.servant.queue;

import io.github.sinri.keel.logger.api.logger.SpecificLogger;
import io.vertx.core.Future;
import org.jetbrains.annotations.NotNull;

/**
 * 队列状态信号读取器
 *
 * @since 5.0.0
 */
public interface KeelQueueSignalReader {
    @NotNull
    SpecificLogger<QueueManageSpecificLog> getQueueManageLogger();

    @NotNull
    Future<KeelQueueSignal> readSignal();
}
