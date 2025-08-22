package io.github.sinri.keel.logger.issue.slf4j;

import io.github.sinri.keel.core.TechnicalPreview;
import io.github.sinri.keel.logger.KeelLogLevel;
import io.github.sinri.keel.logger.event.KeelEventLog;
import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenter;
import io.vertx.core.Handler;
import org.slf4j.Logger;
import org.slf4j.Marker;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * {@link Marker} is not supported yet, and would be ignored.
 *
 * @since 4.1.1
 */
@TechnicalPreview(since = "4.1.1")
public class KeelSlf4jLogger implements Logger {
    @Nonnull
    private final KeelIssueRecordCenter issueRecordCenter;
    @Nonnull
    private final String topic;
    @Nonnull
    private final KeelLogLevel visibleBaseLevel;

    KeelSlf4jLogger(
            @Nonnull KeelIssueRecordCenter issueRecordCenter,
            @Nonnull KeelLogLevel visibleBaseLevel,
            @Nonnull String topic) {
        this.issueRecordCenter = issueRecordCenter;
        this.topic = topic;
        this.visibleBaseLevel = visibleBaseLevel;
    }

    @Nonnull
    protected KeelIssueRecordCenter getIssueRecordCenter() {
        return issueRecordCenter;
    }

    @Override
    public String getName() {
        return topic;
    }

    @Nonnull
    protected KeelLogLevel getVisibleBaseLevel() {
        return visibleBaseLevel;
    }

    /**
     * Record an issue (created with `issueRecordBuilder` and modified with `issueHandler`).
     * It may be handled later async, actually.
     *
     * @param issueHandler the handler to modify the base issue.
     */
    protected void record(@Nonnull Handler<KeelEventLog> issueHandler) {
        KeelEventLog issue = new KeelEventLog();
        issueHandler.handle(issue);
        getIssueRecordCenter().getAdapter().record(getName(), issue);
    }

    @Override
    public boolean isTraceEnabled() {
        return false;
    }

    @Override
    public void trace(String msg) {

    }

    @Override
    public void trace(String format, Object arg) {

    }

    @Override
    public void trace(String format, Object arg1, Object arg2) {

    }

    @Override
    public void trace(String format, Object... arguments) {

    }

    @Override
    public void trace(String msg, Throwable t) {

    }

    @Override
    public boolean isTraceEnabled(Marker marker) {
        return false;
    }

    @Override
    public void trace(Marker marker, String msg) {

    }

    @Override
    public void trace(Marker marker, String format, Object arg) {

    }

    @Override
    public void trace(Marker marker, String format, Object arg1, Object arg2) {

    }

    @Override
    public void trace(Marker marker, String format, Object... argArray) {

    }

    @Override
    public void trace(Marker marker, String msg, Throwable t) {

    }

    @Override
    public boolean isDebugEnabled() {
        return getVisibleBaseLevel().isEnoughSeriousAs(KeelLogLevel.DEBUG);
    }

    @Override
    public void debug(String msg) {
        record(log -> {
            log.level(KeelLogLevel.DEBUG);
            log.message(msg);
        });
    }

    @Override
    public void debug(String format, Object arg) {
        record(log -> {
            log.level(KeelLogLevel.DEBUG);
            log.message(String.format(format, arg));
        });
    }

    @Override
    public void debug(String format, Object arg1, Object arg2) {
        record(log -> {
            log.level(KeelLogLevel.DEBUG);
            log.message(String.format(format, arg1, arg2));
        });
    }

    @Override
    public void debug(String format, Object... arguments) {
        record(log -> {
            log.level(KeelLogLevel.DEBUG);
            log.message(String.format(format, arguments));
        });
    }

    @Override
    public void debug(String msg, Throwable t) {
        record(log -> {
            log.level(KeelLogLevel.DEBUG);
            log.message(msg);
            log.exception(t);
        });
    }

    @Override
    public boolean isDebugEnabled(Marker marker) {
        return true;
    }

    @Override
    public void debug(Marker marker, String msg) {
        record(log -> {
            log.level(KeelLogLevel.DEBUG);
            log.classification(transformMarkerToClassification(marker));
            log.message(msg);
        });
    }

    @Override
    public void debug(Marker marker, String format, Object arg) {
        record(log -> {
            log.level(KeelLogLevel.DEBUG);
            log.classification(transformMarkerToClassification(marker));
            log.message(String.format(format, arg));
        });
    }

    @Override
    public void debug(Marker marker, String format, Object arg1, Object arg2) {
        record(log -> {
            log.level(KeelLogLevel.DEBUG);
            log.classification(transformMarkerToClassification(marker));
            log.message(String.format(format, arg1, arg2));
        });
    }

    @Override
    public void debug(Marker marker, String format, Object... arguments) {
        record(log -> {
            log.level(KeelLogLevel.DEBUG);
            log.classification(transformMarkerToClassification(marker));
            log.message(String.format(format, arguments));
        });
    }

    @Override
    public void debug(Marker marker, String msg, Throwable t) {
        record(log -> {
            log.level(KeelLogLevel.DEBUG);
            log.classification(transformMarkerToClassification(marker));
            log.message(msg);
            log.exception(t);
        });
    }

    private List<String> transformMarkerToClassification(Marker marker) {
        List<String> classification = new ArrayList<>();
        classification.add(marker.getName());
        if (marker.hasReferences()) {
            marker.iterator().forEachRemaining(x -> {
                classification.add(x.getName());
            });
        }
        return classification;
    }

    @Override
    public boolean isInfoEnabled() {
        return false;
    }

    @Override
    public void info(String msg) {
        record(log -> {
            log.level(KeelLogLevel.INFO);
            log.message(msg);
        });
    }

    @Override
    public void info(String format, Object arg) {
        record(log -> {
            log.level(KeelLogLevel.INFO);
            log.message(String.format(format, arg));
        });
    }

    @Override
    public void info(String format, Object arg1, Object arg2) {
        record(log -> {
            log.level(KeelLogLevel.INFO);
            log.message(String.format(format, arg1, arg2));
        });
    }

    @Override
    public void info(String format, Object... arguments) {
        record(log -> {
            log.level(KeelLogLevel.INFO);
            log.message(String.format(format, arguments));
        });
    }

    @Override
    public void info(String msg, Throwable t) {
        record(log -> {
            log.level(KeelLogLevel.INFO);
            log.message(msg);
            log.exception(t);
        });
    }

    @Override
    public boolean isInfoEnabled(Marker marker) {
        return false;
    }

    @Override
    public void info(Marker marker, String msg) {
        record(log -> {
            log.level(KeelLogLevel.INFO);
            log.classification(transformMarkerToClassification(marker));
            log.message(msg);
        });
    }

    @Override
    public void info(Marker marker, String format, Object arg) {
        record(log -> {
            log.level(KeelLogLevel.INFO);
            log.classification(transformMarkerToClassification(marker));
            log.message(String.format(format, arg));
        });
    }

    @Override
    public void info(Marker marker, String format, Object arg1, Object arg2) {
        record(log -> {
            log.level(KeelLogLevel.INFO);
            log.classification(transformMarkerToClassification(marker));
            log.message(String.format(format, arg1, arg2));
        });
    }

    @Override
    public void info(Marker marker, String format, Object... arguments) {
        record(log -> {
            log.level(KeelLogLevel.INFO);
            log.classification(transformMarkerToClassification(marker));
            log.message(String.format(format, arguments));
        });
    }

    @Override
    public void info(Marker marker, String msg, Throwable t) {
        record(log -> {
            log.level(KeelLogLevel.INFO);
            log.classification(transformMarkerToClassification(marker));
            log.message(msg);
            log.exception(t);
        });
    }

    @Override
    public boolean isWarnEnabled() {
        return false;
    }

    @Override
    public void warn(String msg) {
        record(log -> {
            log.level(KeelLogLevel.WARNING);
            log.message(msg);
        });
    }

    @Override
    public void warn(String format, Object arg) {
        record(log -> {
            log.level(KeelLogLevel.WARNING);
            log.message(String.format(format, arg));
        });
    }

    @Override
    public void warn(String format, Object... arguments) {
        record(log -> {
            log.level(KeelLogLevel.WARNING);
            log.message(String.format(format, arguments));
        });
    }

    @Override
    public void warn(String format, Object arg1, Object arg2) {
        record(log -> {
            log.level(KeelLogLevel.WARNING);
            log.message(String.format(format, arg1, arg2));
        });
    }

    @Override
    public void warn(String msg, Throwable t) {
        record(log -> {
            log.level(KeelLogLevel.WARNING);
            log.message(msg);
            log.exception(t);
        });
    }

    @Override
    public boolean isWarnEnabled(Marker marker) {
        return false;
    }

    @Override
    public void warn(Marker marker, String msg) {
        record(log -> {
            log.level(KeelLogLevel.WARNING);
            log.classification(transformMarkerToClassification(marker));
            log.message(msg);
        });
    }

    @Override
    public void warn(Marker marker, String format, Object arg) {
        record(log -> {
            log.level(KeelLogLevel.WARNING);
            log.classification(transformMarkerToClassification(marker));
            log.message(String.format(format, arg));
        });
    }

    @Override
    public void warn(Marker marker, String format, Object arg1, Object arg2) {
        record(log -> {
            log.level(KeelLogLevel.WARNING);
            log.classification(transformMarkerToClassification(marker));
            log.message(String.format(format, arg1, arg2));
        });
    }

    @Override
    public void warn(Marker marker, String format, Object... arguments) {
        record(log -> {
            log.level(KeelLogLevel.WARNING);
            log.classification(transformMarkerToClassification(marker));
            log.message(String.format(format, arguments));
        });
    }

    @Override
    public void warn(Marker marker, String msg, Throwable t) {
        record(log -> {
            log.level(KeelLogLevel.WARNING);
            log.classification(transformMarkerToClassification(marker));
            log.message(msg);
            log.exception(t);
        });
    }

    @Override
    public boolean isErrorEnabled() {
        return false;
    }

    @Override
    public void error(String msg) {
        record(log -> {
            log.level(KeelLogLevel.ERROR);
            log.message(msg);
        });
    }

    @Override
    public void error(String format, Object arg) {
        record(log -> {
            log.level(KeelLogLevel.ERROR);
            log.message(String.format(format, arg));
        });
    }

    @Override
    public void error(String format, Object arg1, Object arg2) {
        record(log -> {
            log.level(KeelLogLevel.ERROR);
            log.message(String.format(format, arg1, arg2));
        });
    }

    @Override
    public void error(String format, Object... arguments) {
        record(log -> {
            log.level(KeelLogLevel.ERROR);
            log.message(String.format(format, arguments));
        });
    }

    @Override
    public void error(String msg, Throwable t) {
        record(log -> {
            log.level(KeelLogLevel.ERROR);
            log.message(msg);
            log.exception(t);
        });
    }

    @Override
    public boolean isErrorEnabled(Marker marker) {
        return false;
    }

    @Override
    public void error(Marker marker, String msg) {
        record(log -> {
            log.level(KeelLogLevel.ERROR);
            log.classification(transformMarkerToClassification(marker));
            log.message(msg);
        });
    }

    @Override
    public void error(Marker marker, String format, Object arg) {
        record(log -> {
            log.level(KeelLogLevel.ERROR);
            log.classification(transformMarkerToClassification(marker));
            log.message(String.format(format, arg));
        });
    }

    @Override
    public void error(Marker marker, String format, Object arg1, Object arg2) {
        record(log -> {
            log.level(KeelLogLevel.ERROR);
            log.classification(transformMarkerToClassification(marker));
            log.message(String.format(format, arg1, arg2));
        });
    }

    @Override
    public void error(Marker marker, String format, Object... arguments) {
        record(log -> {
            log.level(KeelLogLevel.ERROR);
            log.classification(transformMarkerToClassification(marker));
            log.message(String.format(format, arguments));
        });
    }

    @Override
    public void error(Marker marker, String msg, Throwable t) {
        record(log -> {
            log.level(KeelLogLevel.ERROR);
            log.classification(transformMarkerToClassification(marker));
            log.message(msg);
            log.exception(t);
        });
    }
}