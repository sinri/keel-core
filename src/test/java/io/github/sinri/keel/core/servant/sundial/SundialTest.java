package io.github.sinri.keel.core.servant.sundial;

import io.github.sinri.keel.tesuto.KeelJUnit5Test;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxTestContext;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

public class SundialTest extends KeelJUnit5Test {

    /**
     * 构造方法。
     * <p>本方法在 {@code @BeforeAll} 注解的静态方法运行后运行。
     * <p>注意，本构造方法会注册 {@code JsonifiableSerializer} 所载 JSON 序列化能力。
     *
     * @param vertx 由 VertxExtension 提供的 Vertx 实例。
     */
    public SundialTest(@NotNull Vertx vertx) {
        super(vertx);
    }

    @Test
    void test(VertxTestContext testContext) {
        testContext.completeNow();
    }

}