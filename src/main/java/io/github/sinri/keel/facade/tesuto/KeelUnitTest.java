package io.github.sinri.keel.facade.tesuto;

import io.github.sinri.keel.core.async.KeelAsyncKit;
import io.github.sinri.keel.logger.event.KeelEventLogger;
import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenter;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;

import javax.annotation.Nullable;
import java.util.function.Supplier;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

/**
 * @since 3.2.19
 * A base class for `mvn test`, each method named like `void test*()` would be executed.
 */
public class KeelUnitTest {
    private KeelEventLogger logger;

    public KeelUnitTest() {
        VertxOptions vertxOptions = buildVertxOptions();
        if (vertxOptions != null) {
            Keel.initializeVertxStandalone(vertxOptions);
        }
        Keel.getConfiguration().loadPropertiesFile("config.properties");
        this.logger = KeelIssueRecordCenter.outputCenter().generateEventLogger("KeelUnitTest");
        prepareEnvironment();
        KeelEventLogger builtLogger = this.buildLogger();
        if (builtLogger != null) {
            this.logger = builtLogger;
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
     * By default, KeelUnitTest provides a logger to write to STDOUT;
     * Override this method to return a Non-Null instance of KeelEventLogger
     * to initialize the logger if you have a special logging requirement.
     */
    protected @Nullable KeelEventLogger buildLogger() {
        return null;
    }

    public final KeelEventLogger getLogger() {
        return logger;
    }

    /**
     * Override this method to be executed before each `test*` method.
     */
    public void setUp() {
        JsonObject env = new JsonObject();
        System.getenv().forEach(env::put);
        getLogger().info("env", env);
        for (var e : Thread.currentThread().getStackTrace()) {
            getLogger().info("stack: " + e.getClassName() + "::" + e.getMethodName());
        }

        System.getProperties().forEach((k, v) -> {
            getLogger().info("property: " + k + "=" + v);
        });
    }

    /**
     * Override this method to be executed after each `test*` method.
     */
    public void tearDown() {
    }

    protected void async(Handler<Promise<Void>> testHandler) {
        KeelAsyncKit.pseudoAwait(testHandler);
    }

    protected void async(Supplier<Future<Void>> testSupplier) {
        KeelAsyncKit.pseudoAwait(p -> {
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
