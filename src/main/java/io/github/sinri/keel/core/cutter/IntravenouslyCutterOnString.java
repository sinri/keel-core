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
    /**
     * A reference to preserve the cause to stop, if exists.
     *
     * @since 4.0.12
     */
    private final AtomicReference<Throwable> stopCause = new AtomicReference<>();

    /**
     * Constructor to initialize the IntravenouslyCutterOnString instance.
     *
     * @param stringSingleDropProcessor The processor for handling individual strings.
     */
    public IntravenouslyCutterOnString(KeelIntravenous.SingleDropProcessor<String> stringSingleDropProcessor) {
        this.stringSingleDropProcessor = stringSingleDropProcessor;
        intravenous = KeelIntravenous.instant(getSingleDropProcessor());
        intravenous.deployMe(new DeploymentOptions());
    }

    /**
     * Retrieves the single drop processor for strings.
     *
     * @return The single drop processor for strings.
     */
    @Override
    public KeelIntravenous.SingleDropProcessor<String> getSingleDropProcessor() {
        return stringSingleDropProcessor;
    }

    /**
     * Accepts data from the stream and processes it.
     *
     * @param s The Buffer data read from the stream.
     */
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
     * Cuts the data in the buffer using a delimiter.
     *
     * @return The cut-off head string, or null if the buffer cannot be cut.
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

    /**
     * Stops reading from the stream and processes any remaining data in the buffer.
     */
    @Override
    public void stopHere(Throwable throwable) {
        if (!readStopRef.get()) {
            synchronized (buffer) {
                if (buffer.get().length() > 0) {
                    String rest = buffer.get().toString(StandardCharsets.UTF_8);
                    String[] split = rest.split("\n\n");
                    for (String s : split) {
                        if (s != null) {
                            intravenous.add(s);
                        }
                    }
                    // since 4.0.12
                    buffer.set(Buffer.buffer());
                }
            }
            stopCause.set(throwable);
            readStopRef.set(true);
        }
    }

    /**
     * Waits for all data processing to complete.
     *
     * @return A Future indicating that all data processing is complete; if stop cause declared, it would be put into
     *         the failure future to return.
     */
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
                   })
                   .compose(v -> {
                       Throwable throwable = stopCause.get();
                       if (throwable != null) {
                           return Future.failedFuture(throwable);
                       }
                       return Future.succeededFuture();
                   });
    }
}
