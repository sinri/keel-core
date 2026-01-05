package io.github.sinri.keel.core.servant.queue;


import io.github.sinri.keel.logger.api.log.SpecificLog;
import org.jspecify.annotations.NullMarked;

import java.util.List;

/**
 * 面向队列调度管理器的特定日志。
 *
 * @since 5.0.0
 */
@NullMarked
public final class QueueManageSpecificLog extends SpecificLog<QueueManageSpecificLog> {
    public static final String TopicQueue = "Queue";

    public QueueManageSpecificLog() {
        super();
        this.classification(List.of("manage"));
    }
}
