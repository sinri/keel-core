package io.github.sinri.keel.logger.issue.slf4j;

import io.github.sinri.keel.facade.tesuto.KeelJUnit5Test;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;
import org.slf4j.helpers.MessageFormatter;

class KeelSlf4jLoggerTest extends KeelJUnit5Test {

    public KeelSlf4jLoggerTest(Vertx vertx) {
        super(vertx);
    }

    @Test
    @Override
    protected void test(VertxTestContext testContext) {
        String s = MessageFormatter.arrayFormat("Hello {}", new Object[]{"World"}).getMessage();
        getUnitTestLogger().info(s, ctx -> {
            ctx.put("a", "b");
        });
        testContext.completeNow();
    }
}