package io.github.sinri.keel.core.verticles;

import io.github.sinri.keel.core.TechnicalPreview;
import io.github.sinri.keel.core.helper.io.OutputToReadStream;
import io.vertx.core.*;

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
    // private static final long IO_AWAIT_MS = 10L;
    @Nonnull
    private final ProcessBuilder processBuilder;
    @Nullable
    private final Handler<OutputToReadStream> stdoutStreamHandler;
    @Nullable
    private final Handler<OutputToReadStream> stderrStreamHandler;
    @Nullable
    private final Function<Process, Future<Void>> processExitProcessor;
    @Nullable
    private volatile Process process;
    private OutputToReadStream stdoutStream;
    private OutputToReadStream stderrStream;

    public ProcessAsVerticle(
            @Nonnull List<String> command,
            @Nullable File workingDirectory,
            @Nullable Handler<Map<String, String>> environmentHandler,
            @Nullable Handler<OutputToReadStream> stdoutStreamHandler,
            @Nullable Handler<OutputToReadStream> stderrStreamHandler,
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
        this.stdoutStreamHandler = stdoutStreamHandler;
        this.stderrStreamHandler = stderrStreamHandler;
        this.processExitProcessor = processExitProcessor;
    }

    private static Future<OutputToReadStream> processProcessOutputStream(
            InputStream inputStream,
            //Function<Buffer, Future<Void>> outputProcessor,
            Handler<OutputToReadStream> streamHandler
    ) {
        if (streamHandler != null) {
            return Keel.getVertx().executeBlocking(() -> {
                try {
                    OutputToReadStream outputToReadStream = new OutputToReadStream();
                    outputToReadStream.pause();
                    streamHandler.handle(outputToReadStream);
                    //                    outputToReadStream.handler(outputProcessor::apply)
                    //                                      .endHandler(v -> {
                    //                                          //Keel.getLogger().fatal("processProcessOutputStream endHandler");
                    //                                          try {
                    //                                              outputToReadStream.close();
                    //                                          } catch (IOException e) {
                    //                                              throw new RuntimeException(e);
                    //                                          }
                    //                                      })
                    //                                      .exceptionHandler(throwable -> {
                    //                                          Keel.getLogger()
                    //                                              .exception(throwable, "processProcessOutputStream exceptionHandler");
                    //                                          try {
                    //                                              outputToReadStream.close();
                    //                                          } catch (IOException e) {
                    //                                              throw new RuntimeException(e);
                    //                                          }
                    //                                      });
                    outputToReadStream.resume();
                    inputStream.transferTo(outputToReadStream);
                    return outputToReadStream;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        // stream is ignored
        return Future.succeededFuture(null);
    }

    @Override
    protected Future<Void> startVerticle() {
        try {
            var process = processBuilder.start();
            processProcessOutputStream(process.getInputStream(), stdoutStreamHandler)
                    .onSuccess(stream -> {
                        this.stdoutStream = stream;
                    });
            processProcessOutputStream(process.getErrorStream(), stderrStreamHandler)
                    .onSuccess(stream -> {
                        this.stderrStream = stream;
                    });

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
                    try {
                        p.destroyForcibly();
                    } catch (Throwable ignored) {
                    }
                }
            }
            if (this.stdoutStream != null) {
                try {
                    this.stdoutStream.close();
                } catch (Throwable ignored) {
                }
            }
            if (this.stderrStream != null) {
                try {
                    this.stderrStream.close();
                } catch (Throwable ignored) {
                }
            }
        } catch (Exception e) {
            Keel.getLogger().exception(e, "ProcessAsVerticle: stop error");
        } finally {
            stopPromise.complete();
        }
    }
}
