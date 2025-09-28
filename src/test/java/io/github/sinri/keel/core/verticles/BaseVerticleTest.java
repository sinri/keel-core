package io.github.sinri.keel.core.verticles;

import io.github.sinri.keel.facade.tesuto.unit.KeelJUnit5Test;
import io.vertx.core.Future;
import io.vertx.core.VerticleBase;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

@ExtendWith(VertxExtension.class)
public class BaseVerticleTest extends KeelJUnit5Test {
    /**
     * The constructor would run after {@code @BeforeAll} annotated method.
     */
    public BaseVerticleTest(Vertx vertx) {
        super(vertx);
    }

    @Test
    public void test1(VertxTestContext testContext) {
        Keel.getVertx().deployVerticle(new V())
            .andThen(ar -> {
                getUnitTestLogger().info("deployed: " + ar.result());
            });
        Keel.getVertx().setTimer(5000L, t -> {
            testContext.completeNow();
        });
    }

    public static class V extends VerticleBase {
        @Override
        public Future<?> start() throws Exception {
            return Keel.asyncSleep(1000L)
                       .compose(slept -> {
                           Keel.asyncCallStepwise(3, i -> {
                                   Keel.getLogger().fatal("i=" + i);
                                   return Future.succeededFuture();
                               })
                               .onComplete(ar1 -> {
                                   Keel.getVertx().undeploy(deploymentID())
                                       .onComplete(ar2 -> {
                                           Keel.getLogger().fatal("undeploy");
                                       });
                               });
                           return Future.succeededFuture();
                       });
        }
    }
}
