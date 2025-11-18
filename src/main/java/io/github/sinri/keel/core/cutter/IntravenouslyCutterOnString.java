package io.github.sinri.keel.core.cutter;

import io.github.sinri.keel.core.servant.intravenous.KeelIntravenous;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static io.github.sinri.keel.base.KeelInstance.Keel;


/**
 * 数据流切分处理器的字符串切片实现。
 * <p>
 * 本类主要面向 Server Sent Events 协议下的数据流切分，数据流为字符流，可通过两个换行符({@code \n\n})切分为字符串片段分别处理。
 *
 * @since 5.0.0
 */
public class IntravenouslyCutterOnString implements IntravenouslyCutter<String> {
    private final AtomicReference<Buffer> buffer = new AtomicReference<>(Buffer.buffer());
    private final KeelIntravenous<String> intravenous;
    private final AtomicBoolean readStopRef = new AtomicBoolean(false);
    private final KeelIntravenous.SingleDropProcessor<String> stringSingleDropProcessor;
    private final AtomicReference<Throwable> stopCause = new AtomicReference<>();
    private Long timeoutTimer;

    /**
     * 数据流切分处理器的字符串切片实现构造函数，带超时机制。
     *
     * @param stringSingleDropProcessor 切分出的字符串文本片段处理器，由内置的{@link KeelIntravenous}实例调用
     * @param deploymentOptions         部署配置，用于部署内置的{@link KeelIntravenous}实例
     * @param timeout                   以毫秒计的数据流接收处理总时间限制
     */
    public IntravenouslyCutterOnString(
            @NotNull KeelIntravenous.SingleDropProcessor<String> stringSingleDropProcessor,
            @NotNull DeploymentOptions deploymentOptions,
            long timeout
    ) {
        this.stringSingleDropProcessor = stringSingleDropProcessor;
        intravenous = KeelIntravenous.instant(getSingleDropProcessor());
        intravenous.deployMe(deploymentOptions);
        if (timeout > 0) {
            timeoutTimer = Keel.getVertx().setTimer(timeout, timer -> {
                this.timeoutTimer = timer;
                this.stopHere(new Timeout());
            });
        }
    }

    /**
     * 数据流切分处理器的字符串切片实现构造函数，不带超时机制。
     *
     * @param stringSingleDropProcessor 切分出的字符串文本片段处理器，由内置的{@link KeelIntravenous}实例调用
     * @param deploymentOptions         部署配置，用于部署内置的{@link KeelIntravenous}实例
     */
    public IntravenouslyCutterOnString(
            KeelIntravenous.SingleDropProcessor<String> stringSingleDropProcessor,
            DeploymentOptions deploymentOptions
    ) {
        this(stringSingleDropProcessor, deploymentOptions, 0);
    }


    @Override
    public KeelIntravenous.SingleDropProcessor<String> getSingleDropProcessor() {
        return stringSingleDropProcessor;
    }


    @Override
    public void acceptFromStream(@NotNull Buffer buffer) {
        synchronized (this.buffer) {
            this.buffer.get().appendBuffer(buffer);

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
                if (timeoutTimer != null) {
                    Keel.getVertx().cancelTimer(timeoutTimer);
                    timeoutTimer = null;
                }
                stopCause.set(throwable);
                readStopRef.set(true);
            }
        }
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
