package io.github.sinri.keel.logger.issue.slf4j;

import io.github.sinri.keel.facade.tesuto.unit.KeelJUnit5Test;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.helpers.MessageFormatter;

@ExtendWith(VertxExtension.class)
class KeelSlf4jLoggerTest extends KeelJUnit5Test {

    public KeelSlf4jLoggerTest(Vertx vertx) {
        super(vertx);
    }

    @Test
    void test1(){
        String s = MessageFormatter.arrayFormat("Hello {}", new Object[]{"World"}).getMessage();
        getUnitTestLogger().info(s, ctx -> {
            ctx.put("a", "b");
        });
    }
}