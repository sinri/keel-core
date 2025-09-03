package io.github.sinri.keel.logger.issue.log4j2;

import io.github.sinri.keel.core.TechnicalPreview;
import io.github.sinri.keel.logger.KeelLogLevel;
import io.github.sinri.keel.logger.event.KeelEventLog;
import io.github.sinri.keel.logger.issue.recorder.adapter.KeelIssueRecorderAdapter;
import io.vertx.core.Handler;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.spi.AbstractLogger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Supplier;

@TechnicalPreview(since = "4.1.3")
public final class KeelLog4j2Logger extends AbstractLogger {
    private final Supplier<KeelIssueRecorderAdapter> adapterSupplier;
    private final String topic;
    private final KeelLogLevel visibleBaseLevel;
    @Nullable
    private final Handler<KeelEventLog> issueRecordInitializer;

    public KeelLog4j2Logger(
            @Nonnull Supplier<KeelIssueRecorderAdapter> adapterSupplier,
            @Nonnull KeelLogLevel visibleBaseLevel,
            @Nonnull String topic,
            @Nullable Handler<KeelEventLog> issueRecordInitializer) {
        super(topic, null, null);
        this.adapterSupplier = adapterSupplier;
        this.topic = topic;
        this.visibleBaseLevel = visibleBaseLevel;
        this.issueRecordInitializer = issueRecordInitializer;
    }

    private static KeelLogLevel transLevel(Level level) {
        if (level == Level.DEBUG || level == Level.ALL) {
            return KeelLogLevel.DEBUG;
        } else if (level == Level.INFO) {
            return KeelLogLevel.INFO;
        } else if (level == Level.WARN) {
            return KeelLogLevel.WARNING;
        } else if (level == Level.ERROR) {
            return KeelLogLevel.ERROR;
        } else if (level == Level.FATAL) {
            return KeelLogLevel.FATAL;
        } else if (level == Level.TRACE || level == Level.OFF) {
            return KeelLogLevel.SILENT;
        } else {
            return KeelLogLevel.SILENT;
        }
    }

    private static Level transLevel(KeelLogLevel level) {
        switch (level) {
            case DEBUG:
                return Level.DEBUG;
            case INFO:
            case NOTICE:
                return Level.INFO;
            case WARNING:
                return Level.WARN;
            case ERROR:
                return Level.ERROR;
            case FATAL:
                return Level.FATAL;
            case SILENT:
            default:
                return Level.OFF;
        }
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, Message message, Throwable t) {
        return isEnabled(level, marker, (Object) message, t);
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, CharSequence message, Throwable t) {
        return isEnabled(level, marker, (Object) message, t);
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, Object message, Throwable t) {
        KeelLogLevel keelLogLevel = transLevel(level);
        return keelLogLevel.isEnoughSeriousAs(visibleBaseLevel);
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Throwable t) {
        return isEnabled(level, marker, (Object) message, t);
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message) {
        return isEnabled(level, marker, message, (Throwable) null);
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Object... params) {
        KeelLogLevel keelLogLevel = transLevel(level);
        return keelLogLevel.isEnoughSeriousAs(visibleBaseLevel);
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Object p0) {
        return isEnabled(level, marker, message, new Object[]{p0});
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Object p0, Object p1) {
        return isEnabled(level, marker, message, new Object[]{p0, p1});
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Object p0, Object p1, Object p2) {
        return isEnabled(level, marker, message, new Object[]{p0, p1, p2});
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3) {
        return isEnabled(level, marker, message, new Object[]{p0, p1, p2, p3});
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4) {
        return isEnabled(level, marker, message, new Object[]{p0, p1, p2, p3, p4});
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5) {
        return isEnabled(level, marker, message, new Object[]{p0, p1, p2, p3, p4, p5});
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6) {
        return isEnabled(level, marker, message, new Object[]{p0, p1, p2, p3, p4, p5, p6});
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7) {
        return isEnabled(level, marker, message, new Object[]{p0, p1, p2, p3, p4, p5, p6, p7});
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8) {
        return isEnabled(level, marker, message, new Object[]{p0, p1, p2, p3, p4, p5, p6, p7, p8});
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8, Object p9) {
        return isEnabled(level, marker, message, new Object[]{p0, p1, p2, p3, p4, p5, p6, p7, p8, p9});
    }

    @Override
    public void logMessage(String fqcn, Level level, Marker marker, Message message, Throwable t) {
        KeelIssueRecorderAdapter adapter = this.adapterSupplier.get();
        if (adapter == null) return;

        KeelEventLog keelEventLog = new KeelEventLog();
        if (issueRecordInitializer != null) {
            issueRecordInitializer.handle(keelEventLog);
        }
        keelEventLog.classification(fqcn);
        keelEventLog.level(transLevel(level));
        if (t != null) {
            keelEventLog.exception(t);
        }
        keelEventLog.message(message.getFormattedMessage());

        // 添加线程上下文信息到日志事件
        addThreadContextToLog(keelEventLog);

        adapter.record(topic, keelEventLog);
    }

    /**
     * Adds thread context information to the log event.
     * <p>
     * This method retrieves the current thread's context map from Log4j's
     * ThreadContext and adds it to the Keel log event as context information.
     *
     * @param keelEventLog the log event to add context to
     */
    private void addThreadContextToLog(KeelEventLog keelEventLog) {
        try {
            var contextMap = ThreadContext.getContext();
            if (contextMap != null && !contextMap.isEmpty()) {
                contextMap.forEach(keelEventLog::context);
            }
        } catch (Exception e) {
            // 忽略上下文获取错误，不影响日志记录
            // 可以考虑记录一个调试级别的错误，但避免递归日志记录
        }
    }

    @Override
    public Level getLevel() {
        return transLevel(visibleBaseLevel);
    }
}
