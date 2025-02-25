package io.github.sinri.keel.web.http.receptionist.responder;

import io.github.sinri.keel.core.ValueBox;
import io.github.sinri.keel.logger.KeelLogLevel;
import io.github.sinri.keel.logger.issue.recorder.KeelIssueRecorder;
import io.github.sinri.keel.web.http.prehandler.KeelPlatformHandler;
import io.github.sinri.keel.web.http.receptionist.ReceptionistIssueRecord;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

/**
 * @since 4.0.4
 */
public class KeelWebResponderCommonApiImpl implements KeelWebResponder {
    private final @Nonnull RoutingContext routingContext;
    private final @Nonnull KeelIssueRecorder<ReceptionistIssueRecord> issueRecorder;

    public KeelWebResponderCommonApiImpl(@Nonnull RoutingContext routingContext, @Nonnull KeelIssueRecorder<ReceptionistIssueRecord> issueRecorder) {
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

    @Override
    public void respondOnSuccess(@Nullable Object data) {
        JsonObject resp = buildResponseBody(Code.OK, data);
        if (isVerboseLogging()) {
            getIssueRecorder().debug(r -> r.setResponse(resp));
        }
        routingContext.json(resp);
    }

    @Override
    public void respondOnFailure(@Nonnull Throwable throwable, @Nonnull ValueBox<?> dataValueBox) {
        JsonObject resp;
        if (dataValueBox.isValueAlreadySet()) {
            resp = buildResponseBody(Code.FAILED, dataValueBox.getValue());
        } else {
            resp = buildResponseBody(Code.FAILED, throwable.getMessage());
        }
        resp.put("throwable", Keel.stringHelper().renderThrowableChain(throwable));
        if (isVerboseLogging()) {
            getIssueRecorder().debug(r -> r.setResponse(resp));
        }
        routingContext.json(resp);
    }

    protected final JsonObject buildResponseBody(Code code, Object data) {
        return new JsonObject()
                .put("request_id", routingContext.get(KeelPlatformHandler.KEEL_REQUEST_ID))
                .put("code", code.name())
                .put("data", data);
    }

    public enum Code {
        OK, FAILED
    }
}
