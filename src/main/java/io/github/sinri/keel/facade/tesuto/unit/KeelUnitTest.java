package io.github.sinri.keel.facade.tesuto.unit;

import io.github.sinri.keel.logger.event.KeelEventLog;
import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenter;
import io.github.sinri.keel.logger.issue.recorder.KeelIssueRecorder;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;

import javax.annotation.Nullable;
import java.util.function.Supplier;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

/**
 * @since 3.2.19 A base class for `mvn test`, each method named like `void test*()` would be executed.
 */
public class KeelUnitTest {
    private KeelIssueRecorder<KeelEventLog> unitTestLogger;

    public KeelUnitTest() {
        VertxOptions vertxOptions = buildVertxOptions();
        if (vertxOptions != null) {
            Keel.initializeVertxStandalone(vertxOptions);
        }
        Keel.getConfiguration().loadPropertiesFile("config.properties");
        this.unitTestLogger = KeelIssueRecordCenter.outputCenter()
                                                   .generateIssueRecorder("KeelUnitTest", KeelEventLog::new);
        prepareEnvironment();
        var builtLogger = this.buildUnitTestLogger();
        if (builtLogger != null) {
            this.unitTestLogger = builtLogger;
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

    }

    /**
     * By default, KeelUnitTest provides a logger to write to STDOUT; Override this method to return a Non-Null instance
     * of KeelEventLogger to initialize the logger if you have a special logging requirement.
     */
    protected @Nullable KeelIssueRecorder<KeelEventLog> buildUnitTestLogger() {
        return null;
    }

    /**
     * @since 4.0.2
     */
    public KeelIssueRecorder<KeelEventLog> getUnitTestLogger() {
        return unitTestLogger;
    }

    /**
     * Override this method to be executed before each `test*` method.
     */
    public void setUp() {
        JsonObject env = new JsonObject();
        System.getenv().forEach(env::put);
        getUnitTestLogger().info(x -> x.message("env").context(env));
        for (var e : Thread.currentThread().getStackTrace()) {
            getUnitTestLogger().info("stack: " + e.getClassName() + "::" + e.getMethodName());
        }

        System.getProperties().forEach((k, v) -> {
            getUnitTestLogger().info("property: " + k + "=" + v);
        });
    }

    /**
     * Override this method to be executed after each `test*` method.
     */
    public void tearDown() {
    }

    protected void async(Handler<Promise<Void>> testHandler) {
        Keel.pseudoAwait(testHandler);
    }

    protected void async(Supplier<Future<Void>> testSupplier) {
        Keel.pseudoAwait(p -> {
            testSupplier.get().andThen(ar -> {
                if (ar.succeeded()) {
                    p.complete();
                } else {
                    p.fail(ar.cause());
                }
            });
        });
    }
}
