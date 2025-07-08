package io.github.sinri.keel.test.lab.blocking;

import io.github.sinri.keel.logger.KeelLogLevel;
import io.vertx.core.Future;
import io.vertx.core.VertxOptions;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

public class AsyncBlockTest {
    public static void main(String[] args) throws InterruptedException {
        Keel.initializeVertxStandalone(new VertxOptions());
        Keel.getLogger().setVisibleLevel(KeelLogLevel.DEBUG);

        Future<Void> future = Keel.asyncSleep(1000L);
        try {
            Keel.getLogger().notice("start");
            Keel.blockAwait(future);
            Keel.getLogger().notice("end");
        } catch (Throwable e) {
            Keel.getLogger().exception(e);
        }
        Keel.close();
    }
}
