package io.github.sinri.keel.facade.tesuto.unit;

import io.github.sinri.keel.core.json.JsonifiableSerializer;
import io.github.sinri.keel.logger.event.KeelEventLog;
import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenter;
import io.github.sinri.keel.logger.issue.recorder.KeelIssueRecorder;
import io.vertx.core.VertxOptions;

import javax.annotation.Nullable;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

/**
 * A base class for {@code mvn test}where each method named like {@code void test*()} would be executed. This class
 * initializes
 * the testing environment, including Vertx (if configured), and loads configuration properties. It also provides a
 * logger to write to STDOUT, which can be customized.
 *
 * <p>Subclasses should override methods such as {@link #setUp()} and {@link #tearDown()} to perform setup and cleanup
 * tasks before and
 * after each test method, respectively. Additionally, subclasses can override {@link #buildVertxOptions()} to configure
 * Vertx
 * and {@link #buildUnitTestLogger()} to provide a custom logger.</p>
 *
 * @since 3.2.19
 */
public class KeelUnitTest implements KeelUnitTestCore {
    private KeelIssueRecorder<KeelEventLog> unitTestLogger;

    public KeelUnitTest() {
        VertxOptions vertxOptions = buildVertxOptions();
        if (vertxOptions != null) {
            Keel.initializeVertxStandalone(vertxOptions);
        }

        JsonifiableSerializer.register();

        Keel.getConfiguration().loadPropertiesFile("config.properties");
        this.unitTestLogger = KeelIssueRecordCenter.outputCenter()
                                                   .generateIssueRecorder("KeelUnitTest", KeelEventLog::new);
        prepareEnvironment();
        var builtLogger = this.buildUnitTestLogger();
        if (builtLogger != null) {
            resetUnitTestLogger(builtLogger);
        }
    }

    /**
     * @return An instance of VertxOptions to initialize Vertx; or NULL to disable Vertx Event Loop.
     */
    protected @Nullable VertxOptions buildVertxOptions() {
        return new VertxOptions();
    }

    /**
     * Prepare the test environment.
     */
    protected void prepareEnvironment() {
        getUnitTestLogger().debug("KeelUnitTest.prepareEnvironment is not detailed required.");
    }

    /**
     * By default, KeelUnitTest provides a logger to write to STDOUT; Override this method to return a Non-Null instance
     * of KeelEventLogger to initialize the logger if you have a special logging requirement.
     */
    protected @Nullable KeelIssueRecorder<KeelEventLog> buildUnitTestLogger() {
        return buildUnitTestLoggerForEachTest(null);
    }

    /**
     * 本方法默认情况下仅在单元测试类初始化时调用以生成和该测试类相关的日志记录器。
     * 如果需要生成和特定测试方法相关的日志记录器，需要在{@code @BeforeEach}标记的setUp方法或测试方法中调用并生成局部变量用于记录日志。
     *
     * @param testMeta 如果这个参数为null，则生成和测试类相关的日志记录器；
     *                 否则生成和测试方法相关的测试记录器，一般应使用 {@code org.junit.jupiter.api.TestInfo} 等单元测试框架提供的类。
     * @since 4.1.0
     */
    protected @Nullable KeelIssueRecorder<KeelEventLog> buildUnitTestLoggerForEachTest(@Nullable Object testMeta) {
        return null;
    }

    /**
     * @param logger if a non-null logger instance is provided, {@link KeelUnitTest#unitTestLogger} would be replaced.
     * @since 4.1.0
     */
    protected void resetUnitTestLogger(@Nullable KeelIssueRecorder<KeelEventLog> logger) {
        if (logger != null) {
            this.unitTestLogger = logger;
        }
    }

    /**
     * @since 4.0.2
     */
    @Override
    public KeelIssueRecorder<KeelEventLog> getUnitTestLogger() {
        return unitTestLogger;
    }
}
