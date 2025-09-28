package io.github.sinri.keel.core.verticles;

import io.github.sinri.keel.facade.tesuto.unit.KeelJUnit5Test;
import io.github.sinri.keel.logger.KeelLogLevel;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.CountDownLatch;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

@ExtendWith(VertxExtension.class)
public class MultiVerticleTest extends KeelJUnit5Test {
    static CountDownLatch latch;

    /**
     * The constructor would run after {@code @BeforeAll} annotated method.
     */
    public MultiVerticleTest(Vertx vertx) {
        super(vertx);
    }

    @Test
    public void test1(VertxTestContext testContext) throws Exception {
        Keel.getLogger().setVisibleLevel(KeelLogLevel.DEBUG);
        latch = new CountDownLatch(2);
        Keel.getVertx().deployVerticle(V.class, new DeploymentOptions()
                    .setInstances(2)
            )
            .compose(deploymentId -> {
                getUnitTestLogger().info("deploymentId: " + deploymentId);
                return Keel.getVertx().executeBlocking(() -> {
                    latch.await();
                    getUnitTestLogger().info("latch awaited");
                    return null;
                }).compose(v -> {
                    return Keel.getVertx().undeploy(deploymentId);
                });
            })
            .onComplete(testContext.succeedingThenComplete());
    }

    public static class V extends KeelVerticleImpl {

        @Override
        protected Future<Void> startVerticle() {
            int sleep = Keel.randomHelper().generateRandomInt(1000, 2000);
            Keel.asyncCallStepwise(3, i -> {
                    return Keel.asyncSleep(sleep)
                               .compose(v -> {
                                   Keel.getLogger().info(this.verticleIdentity() + " sleep=" + sleep + " i=" + i);
                                   return Future.succeededFuture();
                               });
                })
                .onComplete(ar1 -> {
                    latch.countDown();
                    Keel.getLogger().info(this.verticleIdentity() + " one instance over");
                });
            return Future.succeededFuture();
        }
    }
}
