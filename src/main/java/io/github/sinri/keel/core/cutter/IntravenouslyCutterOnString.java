package io.github.sinri.keel.core.cutter;

import io.github.sinri.keel.core.servant.intravenous.KeelIntravenous;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

/**
 * @since 4.0.11
 */
public class IntravenouslyCutterOnString implements IntravenouslyCutter<String> {
    private final AtomicReference<Buffer> buffer = new AtomicReference<>(Buffer.buffer());
    private final KeelIntravenous<String> intravenous;
    private final AtomicBoolean readStopRef = new AtomicBoolean(false);
    private final KeelIntravenous.SingleDropProcessor<String> stringSingleDropProcessor;

    public IntravenouslyCutterOnString(KeelIntravenous.SingleDropProcessor<String> stringSingleDropProcessor) {
        this.stringSingleDropProcessor = stringSingleDropProcessor;
        intravenous = KeelIntravenous.instant(getSingleDropProcessor());
        intravenous.deployMe(new DeploymentOptions());
    }

    @Override
    public KeelIntravenous.SingleDropProcessor<String> getSingleDropProcessor() {
        return stringSingleDropProcessor;
    }

    @Override
    public void acceptFromStream(Buffer s) {
        synchronized (buffer) {
            buffer.get().appendBuffer(s);

            // try to cut!
            while (true) {
                var head = cutWithDelimiter();
                if (head == null) break;
                intravenous.add(head);
            }
        }
    }

    /**
     * @return the cut-off head, null if the buffer is not able to cut.
     */
    private String cutWithDelimiter() {
        String s0 = buffer.get().toString(StandardCharsets.UTF_8);
        int index = s0.indexOf("\n\n");
        if (index < 0) return null;

        var s1 = s0.substring(0, index);
        var s2 = s0.substring(index + 2);
        buffer.set(Buffer.buffer(s2.getBytes(StandardCharsets.UTF_8)));
        return s1;
    }

    @Override
    public void stopHere() {
        synchronized (buffer) {
            if (buffer.get().length() > 0) {
                String rest = buffer.get().toString(StandardCharsets.UTF_8);
                String[] split = rest.split("\n\n");
                for (String s : split) {
                    if (s != null) {
                        intravenous.add(s);
                    }
                }
            }
        }
        readStopRef.set(true);
    }

    @Override
    public Future<Void> waitForAllHandled() {
        return Keel.asyncCallRepeatedly(repeatedlyCallTask -> {
            if (!this.readStopRef.get()) {
                return Keel.asyncSleep(200L);
            }
            if (!intravenous.isNoDropsLeft()) {
                return Keel.asyncSleep(100L);
            }
            intravenous.shutdown();
            if (!intravenous.isUndeployed()) {
                return Keel.asyncSleep(100L);
            }
            repeatedlyCallTask.stop();
            return Future.succeededFuture();
        });
    }
}
