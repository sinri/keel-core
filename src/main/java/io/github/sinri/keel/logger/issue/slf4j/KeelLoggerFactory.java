package io.github.sinri.keel.logger.issue.slf4j;

import io.github.sinri.keel.core.TechnicalPreview;
import io.github.sinri.keel.core.cache.KeelEverlastingCacheInterface;
import io.github.sinri.keel.core.cache.NotCached;
import io.github.sinri.keel.logger.KeelLogLevel;
import io.github.sinri.keel.logger.event.KeelEventLog;
import io.github.sinri.keel.logger.issue.recorder.adapter.KeelIssueRecorderAdapter;
import io.vertx.core.Handler;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Supplier;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

/**
 * A factory implementation for creating SLF4J Logger instances that integrate with the Keel logging framework.
 * <p>
 * This factory implements the SLF4J {@link ILoggerFactory} interface and creates {@link KeelSlf4jLogger} instances
 * that bridge SLF4J logging calls to the Keel issue recording system. The factory uses a caching mechanism to
 * ensure that multiple requests for the same logger name return the same logger instance.
 * <p>
 * The factory is configured with a {@link KeelIssueRecorderAdapter} supplier that provides the underlying
 * logging infrastructure. This allows for flexible configuration and lazy initialization of the logging backend.
 * <p>
 * <strong>Thread Safety:</strong> This factory is thread-safe. Logger creation is synchronized to prevent
 * race conditions when multiple threads request the same logger simultaneously.
 * <p>
 * <strong>Usage Example:</strong>
 * <pre>{@code
 * // Create a factory with a stdout adapter
 * KeelLoggerFactory factory = new KeelLoggerFactory(() -> SyncStdoutAdapter.getInstance());
 *
 * // Get a logger instance
 * Logger logger = factory.getLogger("com.example.MyClass");
 * }</pre>
 *
 * @see KeelSlf4jLogger
 * @see KeelIssueRecorderAdapter
 * @see ILoggerFactory
 * @since 4.1.1
 */
@TechnicalPreview(since = "4.1.1")
public final class KeelLoggerFactory implements ILoggerFactory {

    /**
     * Supplier for obtaining the {@link KeelIssueRecorderAdapter} instance used by created loggers.
     * <p>
     * This supplier allows for lazy initialization and dynamic configuration of the logging backend.
     * The same supplier instance is shared among all loggers created by this factory, enabling
     * consistent logging behavior across the application.
     */
    @Nonnull
    private final Supplier<KeelIssueRecorderAdapter> adapterSupplier;
    @Nullable
    private final Handler<KeelEventLog> issueRecordInitializer;

    /**
     * Cache for storing created logger instances to ensure singleton behavior per logger name.
     * <p>
     * This cache prevents the creation of multiple logger instances for the same name,
     * which is a requirement of the SLF4J specification. The cache uses an everlasting
     * strategy, meaning logger instances are retained for the lifetime of the factory.
     */
    private final KeelEverlastingCacheInterface<String, Logger> loggerCache = KeelEverlastingCacheInterface.createDefaultInstance();

    /**
     * Constructs a new KeelLoggerFactory with the specified adapter supplier.
     * <p>
     * The adapter supplier will be used to obtain {@link KeelIssueRecorderAdapter} instances
     * for all loggers created by this factory. The supplier should return a consistent
     * adapter instance or instances with compatible configuration.
     *
     * @param adapterSupplier the supplier for obtaining issue recorder adapter instances;
     *                        must not be null and should return non-null adapters
     * @throws NullPointerException if adapterSupplier is null
     */
    public KeelLoggerFactory(
            @Nonnull Supplier<KeelIssueRecorderAdapter> adapterSupplier,
            @Nullable Handler<KeelEventLog> issueRecordInitializer) {
        this.adapterSupplier = adapterSupplier;
        this.issueRecordInitializer = issueRecordInitializer;
    }

    /**
     * Returns a logger instance for the specified name.
     * <p>
     * This method implements the SLF4J contract by returning the same logger instance
     * for multiple calls with the same name. If a logger with the given name doesn't
     * exist in the cache, a new {@link KeelSlf4jLogger} instance is created with:
     * <ul>
     *   <li>The configured adapter supplier</li>
     *   <li>A default log level of {@link KeelLogLevel#INFO}</li>
     *   <li>The provided name as the logger topic</li>
     * </ul>
     * <p>
     * <strong>Thread Safety:</strong> Logger creation is synchronized on the adapter supplier
     * to prevent race conditions when multiple threads request the same logger name simultaneously.
     *
     * @param name the name of the logger to retrieve; typically a class name or component identifier
     * @return a logger instance for the specified name; never null
     * @throws RuntimeException if logger creation fails due to adapter supplier issues
     */
    @Override
    public Logger getLogger(String name) {
        try {
            return loggerCache.read(name);
        } catch (NotCached e) {
            synchronized (adapterSupplier) {
                var logger = new KeelSlf4jLogger(adapterSupplier, KeelLogLevel.INFO, name, issueRecordInitializer);
                Keel.getLogger().notice("Keel Logging for slf4j built logger for [" + name + "]");
                loggerCache.save(name, logger);
                return logger;
            }
        }
    }
}
