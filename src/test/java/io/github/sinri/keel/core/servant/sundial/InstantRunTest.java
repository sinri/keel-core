package io.github.sinri.keel.core.servant.sundial;

import io.github.sinri.keel.tesuto.KeelJUnit5Test;
import io.vertx.core.Future;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;

public class InstantRunTest extends KeelJUnit5Test {
    public InstantRunTest() {
        super();
    }

    @Test
    void test(VertxTestContext testContext) {
        TestWorkerSundialPlan plan = new TestWorkerSundialPlan();
        SundialPlan.executeAndAwait(getVertx(), plan)
                   .compose(over -> {
                       getUnitTestLogger().info("WorkerSundialPlan over");
                       return Future.succeededFuture();
                   })
                   .andThen(testContext.succeedingThenComplete());
    }
}
