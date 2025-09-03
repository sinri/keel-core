package io.github.sinri.keel.logger.issue.log4j;

import io.github.sinri.keel.core.TechnicalPreview;
import io.github.sinri.keel.logger.KeelLogLevel;
import io.github.sinri.keel.logger.issue.recorder.adapter.KeelIssueRecorderAdapter;
import org.apache.logging.log4j.message.MessageFactory;
import org.apache.logging.log4j.spi.ExtendedLogger;
import org.apache.logging.log4j.spi.LoggerContext;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

@TechnicalPreview(since = "4.1.3")
public final class KeelLog4jLoggerContext implements LoggerContext {
    private final Map<String, KeelLog4jLogger> loggerMap;
    @Nonnull
    private final Supplier<KeelIssueRecorderAdapter> adapterSupplier;
    @Nonnull
    private final KeelLogLevel visibleBaseLevel;

    public KeelLog4jLoggerContext(
            @Nonnull Supplier<KeelIssueRecorderAdapter> adapterSupplier,
            @Nonnull KeelLogLevel visibleBaseLevel
    ) {
        this.loggerMap = new ConcurrentHashMap<>();
        this.adapterSupplier = adapterSupplier;
        this.visibleBaseLevel = visibleBaseLevel;
    }

    @Override
    public Object getExternalContext() {
        return null;
    }

    @Override
    public ExtendedLogger getLogger(String name) {
        if (loggerMap.containsKey(name)) {
            return loggerMap.get(name);
        } else {
            synchronized (loggerMap) {
                KeelLog4jLogger existed = loggerMap.get(name);
                if (existed == null) {
                    var logger = new KeelLog4jLogger(this.adapterSupplier, visibleBaseLevel, name);
                    loggerMap.put(name, logger);
                    Keel.getLogger().notice("Keel Logging for log4j built logger for [" + name + "]");
                    return logger;
                } else {
                    return existed;
                }
            }
        }
    }

    @Override
    public ExtendedLogger getLogger(String name, MessageFactory messageFactory) {
        return getLogger(name);
    }

    @Override
    public boolean hasLogger(String name) {
        return this.loggerMap.containsKey(name);
    }

    @Override
    public boolean hasLogger(String name, Class<? extends MessageFactory> messageFactoryClass) {
        return hasLogger(name);
    }

    @Override
    public boolean hasLogger(String name, MessageFactory messageFactory) {
        return hasLogger(name);
    }
}
