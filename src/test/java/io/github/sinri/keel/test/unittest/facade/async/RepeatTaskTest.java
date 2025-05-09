package io.github.sinri.keel.test.unittest.facade.async;

import io.github.sinri.keel.facade.tesuto.unit.KeelUnitTest;
import io.vertx.core.Future;
import org.junit.jupiter.api.Test;

import java.lang.management.MemoryUsage;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

public class RepeatTaskTest extends KeelUnitTest {
    @Test
    public void testRepeatCallMany() {
        async(() -> {
            return Keel.asyncCallRepeatedly(repeatedlyCallTask -> {
                return Keel.getVertx().sharedData().getCounter(getClass().getName())
                           .compose(counter -> {
                               return counter.incrementAndGet();
                           })
                           .compose(count -> {
                               MemoryUsage heapMemoryUsage = Keel.runtimeHelper().getHeapMemoryUsage();
                               double usage = 1.0 * heapMemoryUsage.getUsed() / heapMemoryUsage.getMax();
                               getUnitTestLogger().info("<" + count + "> Heap Usage: " + usage);

                               if (count > 10000) {
                                   repeatedlyCallTask.stop();
                               }

                               return Future.succeededFuture();
                           });
            });
        });
    }
}
