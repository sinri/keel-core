package io.github.sinri.keel.core.verticles;

import io.github.sinri.keel.core.TechnicalPreview;
import io.github.sinri.keel.core.helper.io.OutputToReadStream;
import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

@TechnicalPreview(since = "4.1.5")
public class ProcessAsVerticle extends KeelVerticleImpl {
    private static final long IO_AWAIT_MS = 10L;
    @Nonnull
    private final ProcessBuilder processBuilder;
    @Nullable
    private final Function<Buffer, Future<Void>> stdoutProcessor;
    @Nullable
    private final Function<Buffer, Future<Void>> stderrProcessor;
    @Nullable
    private final Function<Process, Future<Void>> processExitProcessor;
    @Nullable
    private volatile Process process;

    public ProcessAsVerticle(
            @Nonnull List<String> command,
            @Nullable File workingDirectory,
            @Nullable Handler<Map<String, String>> environmentHandler,
            @Nullable Function<Buffer, Future<Void>> stdoutProcessor,
            @Nullable Function<Buffer, Future<Void>> stderrProcessor,
            @Nullable Function<Process, Future<Void>> processExitProcessor) {
        if (command.isEmpty()) {
            throw new IllegalArgumentException("Command is empty!");
        }
        processBuilder = new ProcessBuilder(command);
        if (workingDirectory != null) {
            if (workingDirectory.exists() && workingDirectory.isDirectory()) {
                processBuilder.directory(workingDirectory);
            } else {
                throw new IllegalArgumentException("Working directory is not exists or not a directory!");
            }
        }
        if (environmentHandler != null) {
            var env = processBuilder.environment();
            environmentHandler.handle(env);
        }
        this.stdoutProcessor = stdoutProcessor;
        this.stderrProcessor = stderrProcessor;
        this.processExitProcessor = processExitProcessor;
    }

    private static void processProcessOutputStream(
            Process process,
            InputStream inputStream,
            Function<Buffer, Future<Void>> outputProcessor
    ) {
        if (outputProcessor != null) {
            OutputToReadStream outputToReadStream = new OutputToReadStream(Keel.getVertx());
            outputToReadStream.handler(buffer -> {
                                  outputProcessor.apply(buffer);
                              })
                              .endHandler(v -> {
                                  Keel.getLogger().fatal("processProcessOutputStream endHandler");
                              })
                              .exceptionHandler(throwable -> {
                                  Keel.getLogger().exception(throwable, "processProcessOutputStream exceptionHandler");
                              });

            Keel.getVertx().executeBlocking(() -> {
                try {
                    var x = inputStream.transferTo(outputToReadStream);
                    // Keel.getLogger().fatal("processProcessOutputStream transferTo end: " + x);
                    return null;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    @Override
    protected Future<Void> startVerticle() {
        try {
            var process = processBuilder.start();
            processProcessOutputStream(process, process.getInputStream(), stdoutProcessor);
            processProcessOutputStream(process, process.getErrorStream(), stderrProcessor);

            CompletableFuture<Process> processCompletableFuture = process.onExit();
            Keel.asyncTransformCompletableFuture(processCompletableFuture)
                .compose(exitedProcess -> {
                    if (this.processExitProcessor != null) {
                        return this.processExitProcessor.apply(exitedProcess);
                    } else {
                        return Future.succeededFuture();
                    }
                })
                .onFailure(throwable -> {
                    Keel.getLogger()
                        .exception(throwable, "[" + getClass().getName() + "] monitoring process exit, failed");
                })
                .eventually(this::undeployMe)
                .onSuccess(v -> {
                    Keel.getLogger().fatal("[" + getClass().getName() + "] monitoring process exit, undeployed");
                });

            this.process = process;

            return Future.succeededFuture();
        } catch (IOException e) {
            return Future.failedFuture(e);
        }
    }

    @Nonnull
    protected Process getProcess() {
        if (process != null) {
            return Objects.requireNonNull(process);
        }
        throw new IllegalStateException("Process is not started yet!");
    }

    public Future<String> deployMe() {
        return super.deployMe(new DeploymentOptions().setThreadingModel(ThreadingModel.WORKER));
    }

    @Override
    public void stop(Promise<Void> stopPromise) throws Exception {
        try {
            if (process != null) {
                var p = Objects.requireNonNull(process);
                if (p.isAlive()) {
                    p.destroyForcibly();
                    p.getOutputStream().close();
                    p.getInputStream().close();
                    p.getErrorStream().close();
                }
            }
        } catch (Exception e) {
            Keel.getLogger().exception(e, "ProcessAsVerticle: stop error");
        } finally {
            stopPromise.complete();
        }
    }
}
