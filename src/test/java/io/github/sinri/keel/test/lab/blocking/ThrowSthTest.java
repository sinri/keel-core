package io.github.sinri.keel.test.lab.blocking;

import io.github.sinri.keel.facade.tesuto.unit.KeelUnitTest;
import io.vertx.core.Future;
import org.junit.jupiter.api.Test;

public class ThrowSthTest extends KeelUnitTest {
    @Test
    void test1() {
        try {
            Future<Void> voidFuture = oneAsyncFunc()
                    .compose(v -> {
                        getUnitTestLogger().info("oneAsyncFunc done");
                        return Future.succeededFuture();
                    }, throwable -> {
                        getUnitTestLogger().exception(throwable, "oneAsyncFunc error");
                        return Future.succeededFuture();
                    });
            async(voidFuture);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    Future<Void> oneAsyncFunc() throws Exception {
        throw new Exception("X");
    }
}
