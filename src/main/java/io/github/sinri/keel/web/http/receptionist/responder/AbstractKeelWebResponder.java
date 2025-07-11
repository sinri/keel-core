package io.github.sinri.keel.web.http.receptionist.responder;

import io.github.sinri.keel.core.ValueBox;
import io.github.sinri.keel.logger.KeelLogLevel;
import io.github.sinri.keel.logger.issue.recorder.KeelIssueRecorder;
import io.github.sinri.keel.web.http.prehandler.KeelPlatformHandler;
import io.github.sinri.keel.web.http.receptionist.ReceptionistIssueRecord;
import io.vertx.ext.web.RoutingContext;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

/**
 * @since 4.0.6
 */
public abstract class AbstractKeelWebResponder implements KeelWebResponder {
    private final @Nonnull RoutingContext routingContext;
    private final @Nonnull KeelIssueRecorder<ReceptionistIssueRecord> issueRecorder;

    public AbstractKeelWebResponder(@Nonnull RoutingContext routingContext, @Nonnull KeelIssueRecorder<ReceptionistIssueRecord> issueRecorder) {
        this.routingContext = routingContext;
        this.issueRecorder = issueRecorder;
    }

    @Nonnull
    public RoutingContext getRoutingContext() {
        return routingContext;
    }

    @Nonnull
    public KeelIssueRecorder<ReceptionistIssueRecord> getIssueRecorder() {
        return issueRecorder;
    }

    @Override
    public boolean isVerboseLogging() {
        KeelLogLevel visibleLevel = getIssueRecorder().getVisibleLevel();
        return KeelLogLevel.DEBUG.isEnoughSeriousAs(visibleLevel);
    }

    protected void recordResponseVerbosely(Object response) {
        if (isVerboseLogging()) {
            getIssueRecorder().debug(r -> r.setResponse(response));
        }
    }

    /**
     * @since 3.0.8 mark it nullable as it might be null.
     */
    public @Nonnull String readRequestID() {
        return Objects.requireNonNull(routingContext.get(KeelPlatformHandler.KEEL_REQUEST_ID));
    }

    /**
     * @since 3.0.8 mark it nullable as it might be null.
     */
    public @Nonnull Long readRequestStartTime() {
        return Objects.requireNonNull(routingContext.get(KeelPlatformHandler.KEEL_REQUEST_START_TIME));
    }

    public @Nonnull List<String> readRequestIPChain() {
        return Keel.netHelper().parseWebClientIPChain(routingContext);
    }

    /**
     * @deprecated let this deprecated method be final.
     */
    @Deprecated(since = "4.1.0")
    public final void respondOnFailure(@Nonnull Throwable throwable, @Nonnull ValueBox<?> dataValueBox) {
        KeelWebResponder.super.respondOnFailure(throwable, dataValueBox);
    }

    /**
     * @deprecated let this deprecated method be final.
     */
    @Deprecated(since = "4.1.0")
    public final void respondOnFailure(@Nonnull Throwable throwable) {
        respondOnFailure(throwable, new ValueBox<>());
    }
}
