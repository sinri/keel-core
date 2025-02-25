package io.github.sinri.keel.web.http.receptionist.responder;

import io.github.sinri.keel.core.ValueBox;
import io.github.sinri.keel.logger.issue.recorder.KeelIssueRecorder;
import io.github.sinri.keel.web.http.receptionist.ReceptionistIssueRecord;
import io.vertx.ext.web.RoutingContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @since 4.0.4
 */
public interface KeelWebResponder {
    static KeelWebResponder createCommonInstance(@Nonnull RoutingContext routingContext, @Nonnull KeelIssueRecorder<ReceptionistIssueRecord> issueRecorder) {
        return new KeelWebResponderCommonApiImpl(routingContext, issueRecorder);
    }

    void respondOnSuccess(@Nullable Object data);

    void respondOnFailure(@Nonnull Throwable throwable, @Nonnull ValueBox<?> dataValueBox);

    default void respondOnFailure(@Nonnull Throwable throwable) {
        respondOnFailure(throwable, new ValueBox<>());
    }

    boolean isVerboseLogging();
}
