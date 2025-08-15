package io.github.sinri.keel.facade.tesuto.unit;

import io.github.sinri.keel.core.json.JsonifiableSerializer;
import io.github.sinri.keel.logger.event.KeelEventLog;
import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenter;
import io.github.sinri.keel.logger.issue.recorder.KeelIssueRecorder;
import io.vertx.core.Vertx;

import javax.annotation.Nonnull;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

/**
 * The base class for JUnit5 test cases.
 * <p>
 * Any implementation of this class should be annotated with {@code @ExtendWith(VertxExtension.class)}.
 * <p>
 * In any implementation of this class, you should define a method annotated {@code @BeforeAll}
 * with parameters {@code Vertx vertx} and {@code VertxTestContext testContext},
 * in which {@link KeelJUnit5Test#beforeAllShared(Vertx)} should be called to enable Keel functionality.
 *
 * @since 4.1.1
 */
public abstract class KeelJUnit5Test implements KeelJUnit5TestCore {
    private final KeelIssueRecorder<KeelEventLog> unitTestLogger;

    /**
     * The constructor would run after {@code @BeforeAll} annotated method.
     */
    public KeelJUnit5Test() {
        this.unitTestLogger = buildUnitTestLogger();
    }

    /**
     * This method would run before all test cases by called in {@code @BeforeAll} annotated method.
     *
     * @param vertx the vertx instance to be used in Keel, provided by the JUnit5 framework.
     */
    protected static void beforeAllShared(Vertx vertx) {
        Keel.initializeVertx(vertx);
        JsonifiableSerializer.register();

        Keel.getConfiguration().loadPropertiesFile("config.properties");
    }

    @Nonnull
    protected KeelIssueRecorder<KeelEventLog> buildUnitTestLogger() {
        return KeelIssueRecordCenter.outputCenter().generateIssueRecorder("KeelUnitTest", KeelEventLog::new);
    }

    @Override
    public final KeelIssueRecorder<KeelEventLog> getUnitTestLogger() {
        return unitTestLogger;
    }
}
