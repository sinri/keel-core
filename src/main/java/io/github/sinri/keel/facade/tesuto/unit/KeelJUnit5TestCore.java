package io.github.sinri.keel.facade.tesuto.unit;

import io.github.sinri.keel.logger.event.KeelEventLog;
import io.github.sinri.keel.logger.issue.recorder.KeelIssueRecorder;

/**
 * @since 4.1.1
 */
public interface KeelJUnit5TestCore {
    KeelIssueRecorder<KeelEventLog> getUnitTestLogger();
}
