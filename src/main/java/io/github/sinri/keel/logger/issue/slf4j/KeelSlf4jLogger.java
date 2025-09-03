package io.github.sinri.keel.logger.issue.slf4j;

import io.github.sinri.keel.core.TechnicalPreview;
import io.github.sinri.keel.logger.KeelLogLevel;
import io.github.sinri.keel.logger.event.KeelEventLog;
import io.github.sinri.keel.logger.issue.recorder.adapter.KeelIssueRecorderAdapter;
import io.vertx.core.Handler;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.helpers.MessageFormatter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * {@link Marker} is not supported yet, and would be ignored.
 *
 * @since 4.1.1
 */
@TechnicalPreview(since = "4.1.1")
public final class KeelSlf4jLogger implements Logger {
    /**
     * Supplier for obtaining the issue recorder adapter used to handle log events.
     * This allows for lazy initialization and dynamic adapter switching.
     */
    @Nonnull
    private final Supplier<KeelIssueRecorderAdapter> adapterSupplier;

    /**
     * The topic/name of this logger instance, typically representing the class or component being logged.
     */
    @Nonnull
    private final String topic;

    /**
     * The minimum log level that will be processed by this logger.
     * Log events below this level will be filtered out.
     */
    @Nonnull
    private final KeelLogLevel visibleBaseLevel;
    @Nullable
    private final Handler<KeelEventLog> issueRecordInitializer;

    /**
     * Constructs a new KeelSlf4jLogger instance.
     *
     * @param adapterSupplier  supplier for obtaining the issue recorder adapter
     * @param visibleBaseLevel the minimum log level that will be processed
     * @param topic            the name/topic of this logger instance
     */
    KeelSlf4jLogger(
            @Nonnull Supplier<KeelIssueRecorderAdapter> adapterSupplier,
            @Nonnull KeelLogLevel visibleBaseLevel,
            @Nonnull String topic,
            @Nullable Handler<KeelEventLog> issueRecordInitializer
    ) {
        this.adapterSupplier = adapterSupplier;
        this.topic = topic;
        this.visibleBaseLevel = visibleBaseLevel;
        this.issueRecordInitializer = issueRecordInitializer;
    }

    /**
     * Returns the name of this logger instance.
     *
     * @return the logger name/topic
     */
    @Override
    public String getName() {
        return topic;
    }

    /**
     * Gets the minimum log level that will be processed by this logger.
     *
     * @return the visible base log level
     */
    @Nonnull
    private KeelLogLevel getVisibleBaseLevel() {
        return visibleBaseLevel;
    }

    private KeelEventLog createIssueRecordTemplate() {
        var x = new KeelEventLog();
        if (this.issueRecordInitializer != null) {
            this.issueRecordInitializer.handle(x);
        }
        return x;
    }

    /**
     * Record an issue (created with `issueRecordBuilder` and modified with `issueHandler`).
     * It may be handled later async, actually.
     *
     * @param issueHandler the handler to modify the base issue.
     */
    private void record(@Nonnull Handler<KeelEventLog> issueHandler) {
        KeelEventLog issue = createIssueRecordTemplate();
        issueHandler.handle(issue);

        if (issue.level().isEnoughSeriousAs(getVisibleBaseLevel())) {
            KeelIssueRecorderAdapter adapter = adapterSupplier.get();
            if (adapter != null) {
                adapter.record(getName(), issue);
            }
        }
    }

    /**
     * Checks if TRACE level logging is enabled.
     * TRACE level is not supported in the Keel logger system.
     *
     * @return always false, as TRACE level is not supported
     */
    @Override
    public boolean isTraceEnabled() {
        // TRACE is not supported in Keel logger system
        return false;
    }

    /**
     * Logs a message at TRACE level.
     * TRACE level is not supported in the Keel logger system, so this method does nothing.
     *
     * @param msg the message to log (ignored)
     */
    @Override
    public void trace(String msg) {
        // TRACE is not supported in Keel logger system
    }

    /**
     * Logs a formatted message at TRACE level.
     * TRACE level is not supported in the Keel logger system, so this method does nothing.
     *
     * @param format the format string (ignored)
     * @param arg    the argument (ignored)
     */
    @Override
    public void trace(String format, Object arg) {
        // TRACE is not supported in Keel logger system
    }

    /**
     * Logs a formatted message at TRACE level.
     * TRACE level is not supported in the Keel logger system, so this method does nothing.
     *
     * @param format the format string (ignored)
     * @param arg1   the first argument (ignored)
     * @param arg2   the second argument (ignored)
     */
    @Override
    public void trace(String format, Object arg1, Object arg2) {
        // TRACE is not supported in Keel logger system
    }

    /**
     * Logs a formatted message at TRACE level.
     * TRACE level is not supported in the Keel logger system, so this method does nothing.
     *
     * @param format    the format string (ignored)
     * @param arguments the arguments (ignored)
     */
    @Override
    public void trace(String format, Object... arguments) {
        // TRACE is not supported in Keel logger system
    }

    /**
     * Logs a message with an exception at TRACE level.
     * TRACE level is not supported in the Keel logger system, so this method does nothing.
     *
     * @param msg the message to log (ignored)
     * @param t   the exception (ignored)
     */
    @Override
    public void trace(String msg, Throwable t) {
        // TRACE is not supported in Keel logger system
    }

    /**
     * Checks if TRACE level logging is enabled for the given marker.
     * TRACE level is not supported in the Keel logger system.
     *
     * @param marker the marker (ignored)
     * @return always false, as TRACE level is not supported
     */
    @Override
    public boolean isTraceEnabled(Marker marker) {
        return isTraceEnabled();
    }

    /**
     * Logs a message with marker at TRACE level.
     * TRACE level is not supported in the Keel logger system, so this method does nothing.
     *
     * @param marker the marker (ignored)
     * @param msg    the message to log (ignored)
     */
    @Override
    public void trace(Marker marker, String msg) {
        // TRACE is not supported in Keel logger system
    }

    /**
     * Logs a formatted message with marker at TRACE level.
     * TRACE level is not supported in the Keel logger system, so this method does nothing.
     *
     * @param marker the marker (ignored)
     * @param format the format string (ignored)
     * @param arg    the argument (ignored)
     */
    @Override
    public void trace(Marker marker, String format, Object arg) {
        // TRACE is not supported in Keel logger system
    }

    /**
     * Logs a formatted message with marker at TRACE level.
     * TRACE level is not supported in the Keel logger system, so this method does nothing.
     *
     * @param marker the marker (ignored)
     * @param format the format string (ignored)
     * @param arg1   the first argument (ignored)
     * @param arg2   the second argument (ignored)
     */
    @Override
    public void trace(Marker marker, String format, Object arg1, Object arg2) {
        // TRACE is not supported in Keel logger system
    }

    /**
     * Logs a formatted message with marker at TRACE level.
     * TRACE level is not supported in the Keel logger system, so this method does nothing.
     *
     * @param marker   the marker (ignored)
     * @param format   the format string (ignored)
     * @param argArray the arguments (ignored)
     */
    @Override
    public void trace(Marker marker, String format, Object... argArray) {
        // TRACE is not supported in Keel logger system
    }

    /**
     * Logs a message with marker and exception at TRACE level.
     * TRACE level is not supported in the Keel logger system, so this method does nothing.
     *
     * @param marker the marker (ignored)
     * @param msg    the message to log (ignored)
     * @param t      the exception (ignored)
     */
    @Override
    public void trace(Marker marker, String msg, Throwable t) {
        // TRACE is not supported in Keel logger system
    }

    /**
     * Checks if DEBUG level logging is enabled.
     *
     * @return true if DEBUG level is enabled based on the visible base level
     */
    @Override
    public boolean isDebugEnabled() {
        return KeelLogLevel.DEBUG.isEnoughSeriousAs(getVisibleBaseLevel());
    }

    /**
     * Logs a message at DEBUG level.
     *
     * @param msg the message to log
     */
    @Override
    public void debug(String msg) {
        record(log -> {
            log.level(KeelLogLevel.DEBUG);
            log.message(msg);
        });
    }

    /**
     * Logs a formatted message at DEBUG level.
     *
     * @param format the format string
     * @param arg    the argument to be formatted
     */
    @Override
    public void debug(String format, Object arg) {
        record(log -> {
            log.level(KeelLogLevel.DEBUG);
            log.message(MessageFormatter.arrayFormat(format, new Object[]{arg}).getMessage());
        });
    }

    /**
     * Logs a formatted message at DEBUG level.
     *
     * @param format the format string
     * @param arg1   the first argument to be formatted
     * @param arg2   the second argument to be formatted
     */
    @Override
    public void debug(String format, Object arg1, Object arg2) {
        record(log -> {
            log.level(KeelLogLevel.DEBUG);
            log.message(MessageFormatter.arrayFormat(format, new Object[]{arg1, arg2}).getMessage());
        });
    }

    /**
     * Logs a formatted message at DEBUG level.
     *
     * @param format    the format string
     * @param arguments the arguments to be formatted
     */
    @Override
    public void debug(String format, Object... arguments) {
        record(log -> {
            log.level(KeelLogLevel.DEBUG);
            log.message(MessageFormatter.arrayFormat(format, arguments).getMessage());
        });
    }

    /**
     * Logs a message with an exception at DEBUG level.
     *
     * @param msg the message to log
     * @param t   the exception to log
     */
    @Override
    public void debug(String msg, Throwable t) {
        record(log -> {
            log.level(KeelLogLevel.DEBUG);
            log.message(msg);
            log.exception(t);
        });
    }

    /**
     * Checks if DEBUG level logging is enabled for the given marker.
     *
     * @param marker the marker (currently ignored in level determination)
     * @return true if DEBUG level is enabled based on the visible base level
     */
    @Override
    public boolean isDebugEnabled(Marker marker) {
        return isDebugEnabled();
    }

    /**
     * Logs a message with marker at DEBUG level.
     *
     * @param marker the marker for classification
     * @param msg    the message to log
     */
    @Override
    public void debug(Marker marker, String msg) {
        record(log -> {
            log.level(KeelLogLevel.DEBUG);
            log.classification(transformMarkerToClassification(marker));
            log.message(msg);
        });
    }

    /**
     * Logs a formatted message with marker at DEBUG level.
     *
     * @param marker the marker for classification
     * @param format the format string
     * @param arg    the argument to be formatted
     */
    @Override
    public void debug(Marker marker, String format, Object arg) {
        record(log -> {
            log.level(KeelLogLevel.DEBUG);
            log.classification(transformMarkerToClassification(marker));
            log.message(MessageFormatter.arrayFormat(format, new Object[]{arg}).getMessage());
        });
    }

    /**
     * Logs a formatted message with marker at DEBUG level.
     *
     * @param marker the marker for classification
     * @param format the format string
     * @param arg1   the first argument to be formatted
     * @param arg2   the second argument to be formatted
     */
    @Override
    public void debug(Marker marker, String format, Object arg1, Object arg2) {
        record(log -> {
            log.level(KeelLogLevel.DEBUG);
            log.classification(transformMarkerToClassification(marker));
            log.message(MessageFormatter.arrayFormat(format, new Object[]{arg1, arg2}).getMessage());
        });
    }

    /**
     * Logs a formatted message with marker at DEBUG level.
     *
     * @param marker    the marker for classification
     * @param format    the format string
     * @param arguments the arguments to be formatted
     */
    @Override
    public void debug(Marker marker, String format, Object... arguments) {
        record(log -> {
            log.level(KeelLogLevel.DEBUG);
            log.classification(transformMarkerToClassification(marker));
            log.message(MessageFormatter.arrayFormat(format, arguments).getMessage());
        });
    }

    /**
     * Logs a message with marker and exception at DEBUG level.
     *
     * @param marker the marker for classification
     * @param msg    the message to log
     * @param t      the exception to log
     */
    @Override
    public void debug(Marker marker, String msg, Throwable t) {
        record(log -> {
            log.level(KeelLogLevel.DEBUG);
            log.classification(transformMarkerToClassification(marker));
            log.message(msg);
            log.exception(t);
        });
    }

    /**
     * Transforms an SLF4J Marker into a list of classification strings.
     * The marker name and all referenced marker names are included in the classification.
     *
     * @param marker the SLF4J marker to transform, may be null
     * @return a list of classification strings, empty if marker is null
     */
    private List<String> transformMarkerToClassification(Marker marker) {
        List<String> classification = new ArrayList<>();
        if (marker != null) {
            classification.add(marker.getName());
            if (marker.hasReferences()) {
                marker.iterator().forEachRemaining(x -> {
                    classification.add(x.getName());
                });
            }
        }
        return classification;
    }

    /**
     * Checks if INFO level logging is enabled.
     *
     * @return true if INFO level is enabled based on the visible base level
     */
    @Override
    public boolean isInfoEnabled() {
        return KeelLogLevel.INFO.isEnoughSeriousAs(getVisibleBaseLevel());
    }

    /**
     * Logs a message at INFO level.
     *
     * @param msg the message to log
     */
    @Override
    public void info(String msg) {
        record(log -> {
            log.level(KeelLogLevel.INFO);
            log.message(msg);
        });
    }

    /**
     * Logs a formatted message at INFO level.
     *
     * @param format the format string
     * @param arg    the argument to be formatted
     */
    @Override
    public void info(String format, Object arg) {
        record(log -> {
            log.level(KeelLogLevel.INFO);
            log.message(MessageFormatter.arrayFormat(format, new Object[]{arg}).getMessage());
        });
    }

    /**
     * Logs a formatted message at INFO level.
     *
     * @param format the format string
     * @param arg1   the first argument to be formatted
     * @param arg2   the second argument to be formatted
     */
    @Override
    public void info(String format, Object arg1, Object arg2) {
        record(log -> {
            log.level(KeelLogLevel.INFO);
            log.message(MessageFormatter.arrayFormat(format, new Object[]{arg1, arg2}).getMessage());
        });
    }

    /**
     * Logs a formatted message at INFO level.
     *
     * @param format    the format string
     * @param arguments the arguments to be formatted
     */
    @Override
    public void info(String format, Object... arguments) {
        record(log -> {
            log.level(KeelLogLevel.INFO);
            log.message(MessageFormatter.arrayFormat(format, arguments).getMessage());
        });
    }

    /**
     * Logs a message with an exception at INFO level.
     *
     * @param msg the message to log
     * @param t   the exception to log
     */
    @Override
    public void info(String msg, Throwable t) {
        record(log -> {
            log.level(KeelLogLevel.INFO);
            log.message(msg);
            log.exception(t);
        });
    }

    /**
     * Checks if INFO level logging is enabled for the given marker.
     *
     * @param marker the marker (currently ignored in level determination)
     * @return true if INFO level is enabled based on the visible base level
     */
    @Override
    public boolean isInfoEnabled(Marker marker) {
        return isInfoEnabled();
    }

    /**
     * Logs a message with marker at INFO level.
     *
     * @param marker the marker for classification
     * @param msg    the message to log
     */
    @Override
    public void info(Marker marker, String msg) {
        record(log -> {
            log.level(KeelLogLevel.INFO);
            log.classification(transformMarkerToClassification(marker));
            log.message(msg);
        });
    }

    /**
     * Logs a formatted message with marker at INFO level.
     *
     * @param marker the marker for classification
     * @param format the format string
     * @param arg    the argument to be formatted
     */
    @Override
    public void info(Marker marker, String format, Object arg) {
        record(log -> {
            log.level(KeelLogLevel.INFO);
            log.classification(transformMarkerToClassification(marker));
            log.message(MessageFormatter.arrayFormat(format, new Object[]{arg}).getMessage());
        });
    }

    /**
     * Logs a formatted message with marker at INFO level.
     *
     * @param marker the marker for classification
     * @param format the format string
     * @param arg1   the first argument to be formatted
     * @param arg2   the second argument to be formatted
     */
    @Override
    public void info(Marker marker, String format, Object arg1, Object arg2) {
        record(log -> {
            log.level(KeelLogLevel.INFO);
            log.classification(transformMarkerToClassification(marker));
            log.message(MessageFormatter.arrayFormat(format, new Object[]{arg1, arg2}).getMessage());
        });
    }

    /**
     * Logs a formatted message with marker at INFO level.
     *
     * @param marker    the marker for classification
     * @param format    the format string
     * @param arguments the arguments to be formatted
     */
    @Override
    public void info(Marker marker, String format, Object... arguments) {
        record(log -> {
            log.level(KeelLogLevel.INFO);
            log.classification(transformMarkerToClassification(marker));
            log.message(MessageFormatter.arrayFormat(format, arguments).getMessage());
        });
    }

    /**
     * Logs a message with marker and exception at INFO level.
     *
     * @param marker the marker for classification
     * @param msg    the message to log
     * @param t      the exception to log
     */
    @Override
    public void info(Marker marker, String msg, Throwable t) {
        record(log -> {
            log.level(KeelLogLevel.INFO);
            log.classification(transformMarkerToClassification(marker));
            log.message(msg);
            log.exception(t);
        });
    }

    /**
     * Checks if WARN level logging is enabled.
     *
     * @return true if WARN level is enabled based on the visible base level
     */
    @Override
    public boolean isWarnEnabled() {
        return KeelLogLevel.WARNING.isEnoughSeriousAs(getVisibleBaseLevel());
    }

    /**
     * Logs a message at WARN level.
     *
     * @param msg the message to log
     */
    @Override
    public void warn(String msg) {
        record(log -> {
            log.level(KeelLogLevel.WARNING);
            log.message(msg);
        });
    }

    /**
     * Logs a formatted message at WARN level.
     *
     * @param format the format string
     * @param arg    the argument to be formatted
     */
    @Override
    public void warn(String format, Object arg) {
        record(log -> {
            log.level(KeelLogLevel.WARNING);
            log.message(MessageFormatter.arrayFormat(format, new Object[]{arg}).getMessage());
        });
    }

    /**
     * Logs a formatted message at WARN level.
     *
     * @param format    the format string
     * @param arguments the arguments to be formatted
     */
    @Override
    public void warn(String format, Object... arguments) {
        record(log -> {
            log.level(KeelLogLevel.WARNING);
            log.message(MessageFormatter.arrayFormat(format, arguments).getMessage());
        });
    }

    /**
     * Logs a formatted message at WARN level.
     *
     * @param format the format string
     * @param arg1   the first argument to be formatted
     * @param arg2   the second argument to be formatted
     */
    @Override
    public void warn(String format, Object arg1, Object arg2) {
        record(log -> {
            log.level(KeelLogLevel.WARNING);
            log.message(MessageFormatter.arrayFormat(format, new Object[]{arg1, arg2}).getMessage());
        });
    }

    /**
     * Logs a message with an exception at WARN level.
     *
     * @param msg the message to log
     * @param t   the exception to log
     */
    @Override
    public void warn(String msg, Throwable t) {
        record(log -> {
            log.level(KeelLogLevel.WARNING);
            log.message(msg);
            log.exception(t);
        });
    }

    /**
     * Checks if WARN level logging is enabled for the given marker.
     *
     * @param marker the marker (currently ignored in level determination)
     * @return true if WARN level is enabled based on the visible base level
     */
    @Override
    public boolean isWarnEnabled(Marker marker) {
        return isWarnEnabled();
    }

    /**
     * Logs a message with marker at WARN level.
     *
     * @param marker the marker for classification
     * @param msg    the message to log
     */
    @Override
    public void warn(Marker marker, String msg) {
        record(log -> {
            log.level(KeelLogLevel.WARNING);
            log.classification(transformMarkerToClassification(marker));
            log.message(msg);
        });
    }

    /**
     * Logs a formatted message with marker at WARN level.
     *
     * @param marker the marker for classification
     * @param format the format string
     * @param arg    the argument to be formatted
     */
    @Override
    public void warn(Marker marker, String format, Object arg) {
        record(log -> {
            log.level(KeelLogLevel.WARNING);
            log.classification(transformMarkerToClassification(marker));
            log.message(MessageFormatter.arrayFormat(format, new Object[]{arg}).getMessage());
        });
    }

    /**
     * Logs a formatted message with marker at WARN level.
     *
     * @param marker the marker for classification
     * @param format the format string
     * @param arg1   the first argument to be formatted
     * @param arg2   the second argument to be formatted
     */
    @Override
    public void warn(Marker marker, String format, Object arg1, Object arg2) {
        record(log -> {
            log.level(KeelLogLevel.WARNING);
            log.classification(transformMarkerToClassification(marker));
            log.message(MessageFormatter.arrayFormat(format, new Object[]{arg1, arg2}).getMessage());
        });
    }

    /**
     * Logs a formatted message with marker at WARN level.
     *
     * @param marker    the marker for classification
     * @param format    the format string
     * @param arguments the arguments to be formatted
     */
    @Override
    public void warn(Marker marker, String format, Object... arguments) {
        record(log -> {
            log.level(KeelLogLevel.WARNING);
            log.classification(transformMarkerToClassification(marker));
            log.message(MessageFormatter.arrayFormat(format, arguments).getMessage());
        });
    }

    /**
     * Logs a message with marker and exception at WARN level.
     *
     * @param marker the marker for classification
     * @param msg    the message to log
     * @param t      the exception to log
     */
    @Override
    public void warn(Marker marker, String msg, Throwable t) {
        record(log -> {
            log.level(KeelLogLevel.WARNING);
            log.classification(transformMarkerToClassification(marker));
            log.message(msg);
            log.exception(t);
        });
    }

    /**
     * Checks if ERROR level logging is enabled.
     *
     * @return true if ERROR level is enabled based on the visible base level
     */
    @Override
    public boolean isErrorEnabled() {
        return KeelLogLevel.ERROR.isEnoughSeriousAs(this.getVisibleBaseLevel());
    }

    /**
     * Logs a message at ERROR level.
     *
     * @param msg the message to log
     */
    @Override
    public void error(String msg) {
        record(log -> {
            log.level(KeelLogLevel.ERROR);
            log.message(msg);
        });
    }

    /**
     * Logs a formatted message at ERROR level.
     *
     * @param format the format string
     * @param arg    the argument to be formatted
     */
    @Override
    public void error(String format, Object arg) {
        record(log -> {
            log.level(KeelLogLevel.ERROR);
            log.message(MessageFormatter.arrayFormat(format, new Object[]{arg}).getMessage());
        });
    }

    /**
     * Logs a formatted message at ERROR level.
     *
     * @param format the format string
     * @param arg1   the first argument to be formatted
     * @param arg2   the second argument to be formatted
     */
    @Override
    public void error(String format, Object arg1, Object arg2) {
        record(log -> {
            log.level(KeelLogLevel.ERROR);
            log.message(MessageFormatter.arrayFormat(format, new Object[]{arg1, arg2}).getMessage());
        });
    }

    /**
     * Logs a formatted message at ERROR level.
     *
     * @param format    the format string
     * @param arguments the arguments to be formatted
     */
    @Override
    public void error(String format, Object... arguments) {
        record(log -> {
            log.level(KeelLogLevel.ERROR);
            log.message(MessageFormatter.arrayFormat(format, arguments).getMessage());
        });
    }

    /**
     * Logs a message with an exception at ERROR level.
     *
     * @param msg the message to log
     * @param t   the exception to log
     */
    @Override
    public void error(String msg, Throwable t) {
        record(log -> {
            log.level(KeelLogLevel.ERROR);
            log.message(msg);
            log.exception(t);
        });
    }

    /**
     * Checks if ERROR level logging is enabled for the given marker.
     *
     * @param marker the marker (currently ignored in level determination)
     * @return true if ERROR level is enabled based on the visible base level
     */
    @Override
    public boolean isErrorEnabled(Marker marker) {
        return isErrorEnabled();
    }

    /**
     * Logs a message with marker at ERROR level.
     *
     * @param marker the marker for classification
     * @param msg    the message to log
     */
    @Override
    public void error(Marker marker, String msg) {
        record(log -> {
            log.level(KeelLogLevel.ERROR);
            log.classification(transformMarkerToClassification(marker));
            log.message(msg);
        });
    }

    /**
     * Logs a formatted message with marker at ERROR level.
     *
     * @param marker the marker for classification
     * @param format the format string
     * @param arg    the argument to be formatted
     */
    @Override
    public void error(Marker marker, String format, Object arg) {
        record(log -> {
            log.level(KeelLogLevel.ERROR);
            log.classification(transformMarkerToClassification(marker));
            log.message(MessageFormatter.arrayFormat(format, new Object[]{arg}).getMessage());
        });
    }

    /**
     * Logs a formatted message with marker at ERROR level.
     *
     * @param marker the marker for classification
     * @param format the format string
     * @param arg1   the first argument to be formatted
     * @param arg2   the second argument to be formatted
     */
    @Override
    public void error(Marker marker, String format, Object arg1, Object arg2) {
        record(log -> {
            log.level(KeelLogLevel.ERROR);
            log.classification(transformMarkerToClassification(marker));
            log.message(MessageFormatter.arrayFormat(format, new Object[]{arg1, arg2}).getMessage());
        });
    }

    /**
     * Logs a formatted message with marker at ERROR level.
     *
     * @param marker    the marker for classification
     * @param format    the format string
     * @param arguments the arguments to be formatted
     */
    @Override
    public void error(Marker marker, String format, Object... arguments) {
        record(log -> {
            log.level(KeelLogLevel.ERROR);
            log.classification(transformMarkerToClassification(marker));
            log.message(MessageFormatter.arrayFormat(format, arguments).getMessage());
        });
    }

    /**
     * Logs a message with marker and exception at ERROR level.
     *
     * @param marker the marker for classification
     * @param msg    the message to log
     * @param t      the exception to log
     */
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