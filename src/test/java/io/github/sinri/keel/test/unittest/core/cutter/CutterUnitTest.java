package io.github.sinri.keel.test.unittest.core.cutter;

import io.github.sinri.keel.core.cutter.CutterOnString;
import io.github.sinri.keel.facade.tesuto.unit.KeelUnitTest;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

public class CutterUnitTest extends KeelUnitTest {
    @Test
    public void testCutterOnString() {
        async(() -> {
            CutterOnString cutterOnString = new CutterOnString();
            List<String> results = new ArrayList<>();
            cutterOnString.setComponentHandler(s -> {
                getUnitTestLogger().info("component: " + s);
                results.add(s);
            });

            String text = "123456\n|\n789012|345678\n\n9012|34567890";
            String[] split = text.split("\\|");
            return Keel.asyncCallStepwise(4, i -> {
                        String substring = split[Math.toIntExact(i)];
                        //getLogger().info("substring: " + substring);
                        cutterOnString.handle(Buffer.buffer(substring));
                        return Keel.asyncSleep(1_000L);
                    })
                    .compose(v -> {
                        return cutterOnString.end();
                    })
                    .compose(v -> {
                        return Keel.asyncSleep(1_000L);
                    })
                    .compose(v -> {
                        String s = results.get(0) + results.get(1) + results.get(2);
                        Assertions.assertEquals(s, text.replaceAll("[|\n]", ""));
                        return Future.succeededFuture();
                    });
        });
    }
}
